package dev.alicon.mushroomyorkie.pet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FlightTrickPolicyTest {
	@Test
	void healthyWalkedYorkieCanDoFlightTricks() {
		double chance = FlightTrickPolicy.trickChance(new PetNeeds(10, 20, 90, 80), true);

		assertTrue(chance > 0.0D);
	}

	@Test
	void hungryYorkieDoesNotDoFlightTricks() {
		double chance = FlightTrickPolicy.trickChance(new PetNeeds(80, 20, 90, 80), true);

		assertEquals(0.0D, chance);
	}

	@Test
	void yorkieThatNeedsOutsideDoesNotDoFlightTricks() {
		double chance = FlightTrickPolicy.trickChance(new PetNeeds(10, 90, 90, 80), true);

		assertEquals(0.0D, chance);
	}

	@Test
	void unwalkedYorkieDoesNotDoFlightTricksYet() {
		double chance = FlightTrickPolicy.trickChance(new PetNeeds(10, 20, 90, 80), false);

		assertEquals(0.0D, chance);
	}

	@Test
	void lowMoodStillAllowsRareTricksWhenNeedsAreMet() {
		double lowMoodChance = FlightTrickPolicy.trickChance(new PetNeeds(10, 20, 20, 80), true);
		double highMoodChance = FlightTrickPolicy.trickChance(new PetNeeds(10, 20, 90, 80), true);

		assertTrue(lowMoodChance > 0.0D);
		assertTrue(lowMoodChance < highMoodChance);
	}
}
