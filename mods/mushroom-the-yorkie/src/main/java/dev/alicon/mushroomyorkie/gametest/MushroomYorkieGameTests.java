package dev.alicon.mushroomyorkie.gametest;

import dev.alicon.mushroomyorkie.entity.ModEntities;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

/** Headless Minecraft GameTest functions for Mushroom behavior that needs a world. */
public final class MushroomYorkieGameTests {
	/** Verifies the custom Yorkie entity can spawn in a headless Minecraft test world. */
	@GameTest
	public void mushroomYorkieSpawns(GameTestHelper helper) {
		helper.spawn(ModEntities.MUSHROOM_YORKIE, 2, 2, 2);
		helper.assertEntityPresent(ModEntities.MUSHROOM_YORKIE);
		helper.succeed();
	}
}
