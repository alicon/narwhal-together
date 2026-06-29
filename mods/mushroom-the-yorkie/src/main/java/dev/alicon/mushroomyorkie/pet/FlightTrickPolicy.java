package dev.alicon.mushroomyorkie.pet;

/** Computes how likely Mushroom is to do playful flying tricks from his current pet state. */
public final class FlightTrickPolicy {
	private static final double HAPPY_FLIGHT_TRICK_CHANCE = 0.012D;

	private FlightTrickPolicy() {
	}

	/**
	 * Calculates the per-tick chance of starting a flying trick.
	 *
	 * @param needs current Mushroom needs state
	 * @param recentlyWalked temporary walking-care signal used until full walk tracking exists
	 * @return probability from 0.0 to 1.0 for a flight trick on the current tick
	 */
	public static double trickChance(PetNeeds needs, boolean recentlyWalked) {
		if (needs.hunger() > 65 || needs.shouldWarnPotty() || needs.energy() < 25 || !recentlyWalked) {
			return 0.0D;
		}

		double moodScale = Math.max(0.25D, needs.mood() / 100.0D);
		return HAPPY_FLIGHT_TRICK_CHANCE * moodScale;
	}
}
