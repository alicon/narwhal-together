package dev.alicon.mushroomyorkie.pet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PetNeedsTest {
	@Test
	void defaultNeedsStartInHealthyRange() {
		PetNeeds needs = new PetNeeds();

		assertEquals(0, needs.hunger());
		assertEquals(0, needs.potty());
		assertEquals(80, needs.mood());
		assertEquals(80, needs.energy());
		assertFalse(needs.shouldWarnPotty());
	}

	@Test
	void constructorClampsLoadedValues() {
		PetNeeds needs = new PetNeeds(-20, 120, 250, -1);

		assertEquals(0, needs.hunger());
		assertEquals(100, needs.potty());
		assertEquals(100, needs.mood());
		assertEquals(0, needs.energy());
	}

	@Test
	void treatFeedsAndImprovesMoodButRaisesPottyNeed() {
		PetNeeds needs = new PetNeeds(70, 15, 60, 40);

		needs.feedTreat();

		assertEquals(35, needs.hunger());
		assertEquals(35, needs.potty());
		assertEquals(78, needs.mood());
		assertEquals(48, needs.energy());
	}

	@Test
	void treatEffectsClampAtBounds() {
		PetNeeds needs = new PetNeeds(20, 90, 95, 98);

		needs.feedTreat();

		assertEquals(0, needs.hunger());
		assertEquals(100, needs.potty());
		assertEquals(100, needs.mood());
		assertEquals(100, needs.energy());
	}

	@Test
	void normalInsideTickIncreasesHungerPottyAndSpendsEnergy() {
		PetNeeds needs = new PetNeeds(20, 10, 80, 50);

		needs.tickNeeds(false, false);

		assertEquals(21, needs.hunger());
		assertEquals(11, needs.potty());
		assertEquals(80, needs.mood());
		assertEquals(49, needs.energy());
	}

	@Test
	void hungryDogBuildsPottyNeedFaster() {
		PetNeeds needs = new PetNeeds(65, 10, 80, 50);

		needs.tickNeeds(false, false);

		assertEquals(66, needs.hunger());
		assertEquals(12, needs.potty());
	}

	@Test
	void sittingPreservesEnergy() {
		PetNeeds needs = new PetNeeds(20, 10, 80, 50);

		needs.tickNeeds(false, true);

		assertEquals(50, needs.energy());
	}

	@Test
	void outsideTickDrainsPottyAndImprovesMood() {
		PetNeeds needs = new PetNeeds(20, 30, 70, 50);

		needs.tickNeeds(true, false);

		assertEquals(21, needs.hunger());
		assertEquals(23, needs.potty());
		assertEquals(71, needs.mood());
		assertEquals(49, needs.energy());
	}

	@Test
	void indoorPottyWarningDropsMood() {
		PetNeeds needs = new PetNeeds(20, 81, 70, 50);

		needs.tickNeeds(false, false);

		assertEquals(82, needs.potty());
		assertEquals(68, needs.mood());
		assertTrue(needs.shouldWarnPotty());
	}
}
