package dev.alicon.mushroomyorkie.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

final class MushroomDoorLocator {
	private static final int DOOR_SEARCH_RADIUS = 14;

	private MushroomDoorLocator() {
	}

	static BlockPos findNearestDoor(ServerLevel level, BlockPos origin) {
		BlockPos closest = null;
		double closestDistance = Double.MAX_VALUE;
		for (BlockPos pos : BlockPos.withinManhattan(origin, DOOR_SEARCH_RADIUS, 3, DOOR_SEARCH_RADIUS)) {
			if (!isDoorBottom(level.getBlockState(pos))) {
				continue;
			}

			double distance = pos.distSqr(origin);
			if (distance < closestDistance) {
				closest = pos.immutable();
				closestDistance = distance;
			}
		}

		return closest;
	}

	private static boolean isDoorBottom(BlockState state) {
		return DoorBlock.isWoodenDoor(state) && state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER;
	}
}
