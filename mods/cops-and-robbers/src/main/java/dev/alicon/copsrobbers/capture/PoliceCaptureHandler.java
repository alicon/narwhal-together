package dev.alicon.copsrobbers.capture;

import dev.alicon.copsrobbers.entity.BankRobberEntity;
import dev.alicon.copsrobbers.entity.CopEntity;
import dev.alicon.copsrobbers.entity.PoliceCruiserEntity;
import dev.alicon.copsrobbers.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Server-side capture and jail dropoff behavior for the police cruiser. */
public final class PoliceCaptureHandler {
	private static final int JAIL_RADIUS = 9;
	private static final int MAX_JAILED_ROBBERS_PER_JAIL = 5;
	private static final int SPECIAL_JAILBREAKER_CHANCE = 10;
	private static final int DROPOFF_RADIUS = 4;

	private PoliceCaptureHandler() {
	}

	/** Captures robbers swept up by a moving, driven cruiser. */
	public static void captureRobbersNear(PoliceCruiserEntity cruiser) {
		if (!(cruiser.level() instanceof ServerLevel level) || !cruiser.isVehicle() || !isMovingForCapture(cruiser)) {
			return;
		}

		boolean captured = false;
		for (BankRobberEntity robber : level.getEntities(ModEntities.BANK_ROBBER, captureSweepBox(cruiser), robber -> robber.isAlive() && !robber.isJailed())) {
			captured |= captureRobber(cruiser, robber);
		}
		if (captured) {
			level.playSound(null, cruiser.blockPosition(), SoundEvents.MACE_SMASH_GROUND, SoundSource.PLAYERS, 0.65F, 1.15F);
		}
	}

	/** Captures a robber into the cruiser and removes the roaming entity. */
	public static boolean captureRobber(PoliceCruiserEntity cruiser, BankRobberEntity robber) {
		if (!(cruiser.level() instanceof ServerLevel level) || !robber.isAlive() || robber.isJailed()) {
			return false;
		}

		awardRecoveredGold(cruiser, robber);
		cruiser.addCapturedRobber();
		robber.discard();
		level.playSound(null, cruiser.blockPosition(), SoundEvents.IRON_DOOR_CLOSE, SoundSource.PLAYERS, 0.9F, 1.25F);
		showDriverMessage(cruiser, Component.literal("Captured robbers: " + cruiser.capturedRobbers()));
		return true;
	}

	/** Releases captured robbers into a nearby barred jail cell. */
	public static void releaseAtNearbyJail(PoliceCruiserEntity cruiser) {
		if (!(cruiser.level() instanceof ServerLevel level) || cruiser.capturedRobbers() <= 0 || cruiser.tickCount % 20 != 0) {
			return;
		}

		DropOff dropOff = findDropOff(level, cruiser.blockPosition());
		if (dropOff == null) {
			if (cruiser.tickCount % 80 == 0) {
				showDriverMessage(cruiser, Component.literal("Captured robbers: " + cruiser.capturedRobbers() + " - park on Prisoner Drop Off"));
			}
			return;
		}

		int released = 0;
		int jailedNearby = jailedRobbersNear(level, dropOff.cellCenter());
		int availableCells = MAX_JAILED_ROBBERS_PER_JAIL - jailedNearby;
		if (availableCells <= 0) {
			if (cruiser.tickCount % 40 == 0) {
				showDriverMessage(cruiser, Component.literal("Jail is full: " + jailedNearby + "/" + MAX_JAILED_ROBBERS_PER_JAIL));
			}
			return;
		}

		int toRelease = Math.min(cruiser.capturedRobbers(), availableCells);
		for (BlockPos cellPos : jailStandingSpots(level, dropOff.cellCenter())) {
			if (released >= toRelease) {
				break;
			}
			if (spawnJailedRobber(level, cellPos)) {
				released++;
			}
		}

		if (released > 0) {
			cruiser.removeCapturedRobbers(released);
			level.playSound(null, cruiser.blockPosition(), SoundEvents.IRON_DOOR_OPEN, SoundSource.PLAYERS, 0.85F, 0.95F);
			showDriverMessage(cruiser, Component.literal(released + " robber" + (released == 1 ? "" : "s") + " sent to jail"));
			if (cruiser.capturedRobbers() > 0 && jailedRobbersNear(level, dropOff.cellCenter()) >= MAX_JAILED_ROBBERS_PER_JAIL) {
				showDriverMessage(cruiser, Component.literal("Jail is full: " + MAX_JAILED_ROBBERS_PER_JAIL + "/" + MAX_JAILED_ROBBERS_PER_JAIL));
			}
		} else if (cruiser.tickCount % 60 == 0) {
			showDriverMessage(cruiser, Component.literal("No open jail spots behind the bars"));
		}
	}

	private static void showDriverMessage(PoliceCruiserEntity cruiser, Component message) {
		if (cruiser.getControllingPassenger() instanceof Player driver) {
			driver.displayClientMessage(message, true);
		}
	}

	private static void awardRecoveredGold(PoliceCruiserEntity cruiser, BankRobberEntity robber) {
		if (!robber.hasStolenGold() || !(cruiser.getControllingPassenger() instanceof Player driver)) {
			return;
		}
		robber.clearStolenGold();
		ItemStack recovered = new ItemStack(Items.GOLD_INGOT);
		if (!driver.getInventory().add(recovered)) {
			driver.drop(recovered, false);
		}
		driver.displayClientMessage(Component.literal("Recovered stolen gold!"), true);
	}

	private static boolean isMovingForCapture(PoliceCruiserEntity cruiser) {
		Vec3 movement = cruiser.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D);
		return movement.lengthSqr() > 0.0025D || cruiser.walkAnimation.speed() > 0.01F;
	}

	private static AABB captureSweepBox(PoliceCruiserEntity cruiser) {
		AABB box = cruiser.getBoundingBox().inflate(1.15D, 0.55D, 1.15D);
		Vec3 movement = cruiser.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D);
		if (movement.lengthSqr() > 1.0E-4D) {
			box = box.expandTowards(movement.normalize().scale(1.35D))
					.minmax(box.move(movement.reverse().scale(1.35D)));
		}
		return box;
	}

	/** Returns whether a jailed robber is still enclosed by a barred cell. */
	public static boolean isSecureJailSpot(ServerLevel level, BlockPos pos) {
		int bars = 0;
		for (BlockPos nearby : BlockPos.betweenClosed(pos.offset(-2, 0, -2), pos.offset(2, 2, 2))) {
			BlockState state = level.getBlockState(nearby);
			if (state.is(Blocks.IRON_BARS)) {
				bars++;
			}
			if (state.is(Blocks.IRON_DOOR) && state.getOptionalValue(net.minecraft.world.level.block.DoorBlock.OPEN).orElse(false)) {
				return false;
			}
		}
		return bars >= 4;
	}

	/** Lets a nearby cop release normal prisoners after a full Minecraft day. */
	public static boolean tryCopReleaseServedRobbers(CopEntity cop, ServerLevel level) {
		List<BankRobberEntity> prisoners = level.getEntities(
				ModEntities.BANK_ROBBER,
				AABB.ofSize(cop.position(), 18.0D, 6.0D, 18.0D),
				BankRobberEntity::isJailed
		);
		if (prisoners.isEmpty()) {
			return false;
		}

		boolean hasSpecial = false;
		boolean allServed = true;
		for (BankRobberEntity prisoner : prisoners) {
			hasSpecial |= prisoner.isSpecialJailbreaker();
			allServed &= prisoner.hasServedFullDay(level);
		}
		if (hasSpecial || !allServed) {
			return false;
		}

		BankRobberEntity nearest = prisoners.get(0);
		for (BankRobberEntity prisoner : prisoners) {
			if (prisoner.distanceToSqr(cop) < nearest.distanceToSqr(cop)) {
				nearest = prisoner;
			}
		}
		if (cop.distanceToSqr(nearest) > 4.0D * 4.0D) {
			cop.getNavigation().moveTo(nearest, 1.0D);
			return true;
		}

		for (BankRobberEntity prisoner : prisoners) {
			prisoner.releaseFromJail();
		}
		level.playSound(null, nearest.blockPosition(), SoundEvents.IRON_DOOR_OPEN, SoundSource.NEUTRAL, 0.9F, 1.0F);
		alertNearbyPlayers(level, nearest.blockPosition(), Component.literal("A cop released the robbers after their day in jail."));
		return true;
	}

	/** Starts a rare overnight breakout, damaging the cell and freeing jailed robbers nearby. */
	public static void triggerJailbreak(BankRobberEntity jailbreaker, ServerLevel level) {
		BlockPos center = jailbreaker.blockPosition();
		level.playSound(null, center, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 0.9F, 0.8F);
		level.playSound(null, center, SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.0F, 1.8F);
		alertNearbyPlayers(level, center, Component.literal("The prison alarm is going off! Jailbreak!"));
		for (BankRobberEntity robber : level.getEntities(ModEntities.BANK_ROBBER, AABB.ofSize(center.getCenter(), 14.0D, 6.0D, 14.0D), BankRobberEntity::isJailed)) {
			robber.releaseFromJail();
		}
		messUpJail(level, center);
	}

	private static void messUpJail(ServerLevel level, BlockPos center) {
		int broken = 0;
		for (BlockPos pos : BlockPos.betweenClosed(center.offset(-3, 0, -3), center.offset(3, 3, 3))) {
			if (broken >= 12) {
				break;
			}
			BlockState state = level.getBlockState(pos);
			if ((state.is(Blocks.IRON_BARS) || isPoliceStationWall(state)) && level.random.nextInt(3) == 0) {
				level.destroyBlock(pos, false);
				broken++;
			}
		}

		BlockPos outside = center.relative(Direction.Plane.HORIZONTAL.getRandomDirection(level.random), 4);
		BlockPos floor = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, outside);
		level.setBlock(floor, Blocks.OAK_PLANKS.defaultBlockState(), 3);
		if (level.isEmptyBlock(floor.above()) && Blocks.FIRE.defaultBlockState().canSurvive(level, floor.above())) {
			level.setBlock(floor.above(), Blocks.FIRE.defaultBlockState(), 3);
		}
	}

	private static boolean isPoliceStationWall(BlockState state) {
		Block block = state.getBlock();
		return block == Blocks.QUARTZ_BLOCK || block == Blocks.SMOOTH_QUARTZ || block == Blocks.LIGHT_GRAY_CONCRETE;
	}

	private static int jailedRobbersNear(ServerLevel level, BlockPos center) {
		return level.getEntities(
				ModEntities.BANK_ROBBER,
				AABB.ofSize(center.getCenter(), JAIL_RADIUS * 2.0D, 6.0D, JAIL_RADIUS * 2.0D),
				BankRobberEntity::isJailed
		).size();
	}

	private static void alertNearbyPlayers(ServerLevel level, BlockPos center, Component message) {
		for (Player player : level.players()) {
			if (player.distanceToSqr(center.getCenter()) <= 96.0D * 96.0D) {
				player.displayClientMessage(message, true);
			}
		}
	}

	private static boolean spawnJailedRobber(ServerLevel level, BlockPos pos) {
		if (!level.getEntities(ModEntities.BANK_ROBBER, AABB.ofSize(pos.getCenter(), 0.9D, 1.9D, 0.9D), BankRobberEntity::isAlive).isEmpty()) {
			return false;
		}

		BankRobberEntity robber = ModEntities.BANK_ROBBER.create(level, EntitySpawnReason.EVENT);
		if (robber == null) {
			return false;
		}

		robber.snapTo(pos.getX() + 0.5D, pos.getY() + 0.02D, pos.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
		robber.jail(level.random.nextInt(SPECIAL_JAILBREAKER_CHANCE) == 0);
		level.addFreshEntity(robber);
		return true;
	}

	private static List<BlockPos> jailStandingSpots(ServerLevel level, BlockPos center) {
		List<BlockPos> spots = new ArrayList<>();
		for (BlockPos pos : BlockPos.betweenClosed(center.offset(-JAIL_RADIUS, -2, -JAIL_RADIUS), center.offset(JAIL_RADIUS, 4, JAIL_RADIUS))) {
			if (isJailStandingSpot(level, pos)) {
				spots.add(pos.immutable());
			}
		}
		spots.sort(Comparator.comparingDouble(pos -> pos.distSqr(center)));
		return spots;
	}

	private static boolean isJailStandingSpot(ServerLevel level, BlockPos pos) {
		return level.getBlockState(pos).isAir()
				&& level.getBlockState(pos.above()).isAir()
				&& level.getBlockState(pos.below()).is(Blocks.SMOOTH_STONE)
				&& isBetweenOppositeBars(level, pos);
	}

	private static boolean isBetweenOppositeBars(ServerLevel level, BlockPos pos) {
		return hasBarsInBothDirections(level, pos, Direction.NORTH, Direction.SOUTH)
				|| hasBarsInBothDirections(level, pos, Direction.EAST, Direction.WEST);
	}

	private static boolean hasBarsInBothDirections(ServerLevel level, BlockPos pos, Direction first, Direction second) {
		return hasBarsAlong(level, pos, first) && hasBarsAlong(level, pos, second);
	}

	private static boolean hasBarsAlong(ServerLevel level, BlockPos pos, Direction direction) {
		for (int distance = 1; distance <= 6; distance++) {
			if (level.getBlockState(pos.relative(direction, distance)).is(Blocks.IRON_BARS)) {
				return true;
			}
		}
		return false;
	}

	private static DropOff findDropOff(ServerLevel level, BlockPos cruiserPos) {
		BlockPos platform = null;
		for (BlockPos pos : BlockPos.betweenClosed(cruiserPos.offset(-DROPOFF_RADIUS, -2, -DROPOFF_RADIUS), cruiserPos.offset(DROPOFF_RADIUS, 1, DROPOFF_RADIUS))) {
			if (level.getBlockState(pos).is(Blocks.YELLOW_CONCRETE)) {
				platform = pos.immutable();
				break;
			}
		}
		if (platform == null || !hasNearbyDropOffStation(level, platform)) {
			return null;
		}
		return new DropOff(platform, nearestBars(level, platform));
	}

	private static boolean hasNearbyDropOffStation(ServerLevel level, BlockPos platform) {
		boolean door = false;
		boolean bars = false;
		for (BlockPos pos : BlockPos.betweenClosed(platform.offset(-10, 0, -10), platform.offset(10, 5, 10))) {
			BlockState state = level.getBlockState(pos);
			door |= state.is(Blocks.IRON_DOOR);
			bars |= state.is(Blocks.IRON_BARS);
			if (door && bars) {
				return true;
			}
		}
		return false;
	}

	private static BlockPos nearestBars(ServerLevel level, BlockPos center) {
		BlockPos nearest = center;
		double nearestDistance = Double.MAX_VALUE;
		for (BlockPos pos : BlockPos.betweenClosed(center.offset(-14, -1, -14), center.offset(14, 5, 14))) {
			if (level.getBlockState(pos).is(Blocks.IRON_BARS)) {
				double distance = pos.distSqr(center);
				if (distance < nearestDistance) {
					nearest = pos.immutable();
					nearestDistance = distance;
				}
			}
		}
		return nearest;
	}

	private record DropOff(BlockPos platform, BlockPos cellCenter) {
	}
}
