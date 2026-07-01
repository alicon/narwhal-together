package dev.alicon.copsrobbers.spawn;

import dev.alicon.copsrobbers.entity.BankRobberEntity;
import dev.alicon.copsrobbers.entity.ModEntities;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.phys.AABB;

/** Spawns small robber groups around villages in survival worlds. */
public final class BankRobberSpawner {
	private BankRobberSpawner() {
	}

	/** Registers the village robber spawner. */
	public static void register() {
		ServerTickEvents.END_WORLD_TICK.register(BankRobberSpawner::tick);
	}

	private static void tick(ServerLevel level) {
		if (level.getDifficulty() == Difficulty.PEACEFUL || level.getGameTime() % 240 != 0) {
			return;
		}

		for (ServerPlayer player : level.players()) {
			if (player.isCreative() || !level.isCloseToVillage(player.blockPosition(), 4) || level.random.nextInt(5) != 0) {
				continue;
			}

			AABB nearby = player.getBoundingBox().inflate(48.0D);
			if (level.getEntities(ModEntities.BANK_ROBBER, nearby, BankRobberEntity::isAlive).size() >= 4) {
				continue;
			}

			BlockPos spawnPos = player.blockPosition().offset(level.random.nextInt(33) - 16, 0, level.random.nextInt(33) - 16);
			spawnPos = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawnPos);
			BankRobberEntity robber = ModEntities.BANK_ROBBER.create(level, EntitySpawnReason.NATURAL);
			if (robber != null) {
				robber.snapTo(spawnPos, level.random.nextFloat() * 360.0F, 0.0F);
				level.addFreshEntity(robber);
			}
		}
	}
}
