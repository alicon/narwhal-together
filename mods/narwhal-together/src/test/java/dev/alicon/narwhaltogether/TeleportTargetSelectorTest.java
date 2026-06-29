package dev.alicon.narwhaltogether;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class TeleportTargetSelectorTest {
	private static final Candidate AOIFE = new Candidate(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Aoife");
	private static final Candidate NOELLE = new Candidate(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Noelle");
	private static final Candidate RUFUS = new Candidate(UUID.fromString("00000000-0000-0000-0000-000000000003"), "Rufus");

	@Test
	void firstTargetIsSelectedWhenThereIsNoPreviousTarget() {
		Candidate selected = TeleportTargetSelector.nextTarget(null, List.of(AOIFE, NOELLE, RUFUS), Candidate::id);

		assertEquals(AOIFE, selected);
	}

	@Test
	void nextTargetAfterPreviousTargetIsSelected() {
		Candidate selected = TeleportTargetSelector.nextTarget(AOIFE.id(), List.of(AOIFE, NOELLE, RUFUS), Candidate::id);

		assertEquals(NOELLE, selected);
	}

	@Test
	void targetSelectionWrapsToFirstTarget() {
		Candidate selected = TeleportTargetSelector.nextTarget(RUFUS.id(), List.of(AOIFE, NOELLE, RUFUS), Candidate::id);

		assertEquals(AOIFE, selected);
	}

	@Test
	void firstTargetIsSelectedWhenPreviousTargetLeftTheGame() {
		UUID missingTarget = UUID.fromString("00000000-0000-0000-0000-000000000099");

		Candidate selected = TeleportTargetSelector.nextTarget(missingTarget, List.of(AOIFE, NOELLE, RUFUS), Candidate::id);

		assertEquals(AOIFE, selected);
	}

	@Test
	void emptyTargetListsAreRejected() {
		assertThrows(IllegalArgumentException.class, () -> TeleportTargetSelector.nextTarget(null, List.<Candidate>of(), Candidate::id));
	}

	private record Candidate(UUID id, String name) {
	}
}
