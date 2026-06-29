package dev.alicon.mushroomyorkie.spawn;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class YorkieSpawnPolicyTest {
	@Test
	void respawnModeSpawnsAfterSuccessfulSleepWhenNoYorkieIsLoaded() {
		assertTrue(YorkieSpawnPolicy.shouldSpawn(YorkieSpawnMode.RESPAWN, true, false, true));
	}

	@Test
	void respawnModeDoesNotSpawnDuplicateLoadedYorkies() {
		assertFalse(YorkieSpawnPolicy.shouldSpawn(YorkieSpawnMode.RESPAWN, true, true, true));
	}

	@Test
	void noModeSpawnsBeforeSuccessfulSleep() {
		assertFalse(YorkieSpawnPolicy.shouldSpawn(YorkieSpawnMode.RESPAWN, false, false, false));
		assertFalse(YorkieSpawnPolicy.shouldSpawn(YorkieSpawnMode.EXTREME, false, false, false));
	}

	@Test
	void extremeModeOnlySpawnsBeforePlayerHasReceivedYorkie() {
		assertTrue(YorkieSpawnPolicy.shouldSpawn(YorkieSpawnMode.EXTREME, true, false, false));
		assertFalse(YorkieSpawnPolicy.shouldSpawn(YorkieSpawnMode.EXTREME, true, false, true));
	}

	@Test
	void unknownConfigValuesDefaultToRespawnMode() {
		assertSame(YorkieSpawnMode.RESPAWN, YorkieSpawnMode.fromConfigValue("nonsense"));
		assertSame(YorkieSpawnMode.RESPAWN, YorkieSpawnMode.fromConfigValue(null));
		assertSame(YorkieSpawnMode.EXTREME, YorkieSpawnMode.fromConfigValue("extreme"));
	}
}
