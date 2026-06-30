package dev.alicon.mushroomyorkie;

import dev.alicon.mushroomyorkie.entity.ModEntities;
import dev.alicon.mushroomyorkie.entity.MushroomYorkieEntity;
import dev.alicon.mushroomyorkie.spawn.YorkieSpawnPolicy;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;

final class MushroomWakeUpSpawner {
	private static final String RECEIVED_TAG = "mushroom_yorkie.received";
	private static final int SUCCESSFUL_SLEEP_END = 12_542;
	private static final int SEARCH_RADIUS = 512;

	private final MushroomYorkieConfig config;

	private MushroomWakeUpSpawner(MushroomYorkieConfig config) {
		this.config = config;
	}

	static void register(MushroomYorkieConfig config) {
		MushroomWakeUpSpawner spawner = new MushroomWakeUpSpawner(config);
		EntitySleepEvents.STOP_SLEEPING.register(spawner::onStopSleeping);
	}

	private void onStopSleeping(LivingEntity entity, BlockPos bedPos) {
		if (!(entity instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel level)) {
			return;
		}
		if (!this.config.spawnAfterSuccessfulSleep()) {
			return;
		}

		boolean successfulSleep = isSuccessfulSleepWake(level);
		boolean hasLoadedYorkie = hasLoadedYorkie(level, player);
		boolean hasReceivedYorkie = player.getTags().contains(RECEIVED_TAG);
		if (!YorkieSpawnPolicy.shouldSpawn(this.config.spawnMode(), successfulSleep, hasLoadedYorkie, hasReceivedYorkie)) {
			return;
		}

		spawnYorkie(level, player, bedPos);
		player.addTag(RECEIVED_TAG);
	}

	private static boolean isSuccessfulSleepWake(ServerLevel level) {
		long dayTime = level.getDayTime() % 24_000L;
		return dayTime >= 0 && dayTime < SUCCESSFUL_SLEEP_END;
	}

	private static boolean hasLoadedYorkie(ServerLevel level, ServerPlayer player) {
		AABB searchBox = player.getBoundingBox().inflate(SEARCH_RADIUS);
		return !level.getEntities(
				EntityTypeTest.forClass(MushroomYorkieEntity.class),
				searchBox,
				yorkie -> yorkie.isAlive() && yorkie.belongsTo(player)
		).isEmpty();
	}

	private static void spawnYorkie(ServerLevel level, ServerPlayer player, BlockPos bedPos) {
		MushroomYorkieEntity yorkie = ModEntities.MUSHROOM_YORKIE.create(level, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
		if (yorkie == null) {
			return;
		}

		BlockPos spawnPos = spawnPos(level, bedPos);
		yorkie.snapTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, player.getYRot(), 0.0F);
		yorkie.claimFor(player);
		yorkie.setCustomName(Component.literal("Mushroom"));
		yorkie.setPersistenceRequired();

		if (level.noCollision(yorkie) && level.addFreshEntity(yorkie)) {
			player.displayClientMessage(Component.translatable("message.mushroom_yorkie.woke_up"), true);
		}
	}

	private static BlockPos spawnPos(ServerLevel level, BlockPos bedPos) {
		Direction direction = BedBlock.getBedOrientation(level, bedPos);
		BlockPos foot = direction == null ? bedPos : bedPos.relative(direction.getOpposite());
		BlockPos[] candidates = {
				foot,
				foot.above(),
				bedPos,
				bedPos.above()
		};

		for (BlockPos candidate : candidates) {
			if (level.getBlockState(candidate).isAir() && level.getBlockState(candidate.above()).isAir()) {
				return candidate;
			}
		}

		return bedPos.above();
	}
}
