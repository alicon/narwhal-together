package dev.alicon.copsrobbers.world;

import dev.alicon.copsrobbers.entity.BankRobberEntity;
import dev.alicon.copsrobbers.entity.CopEntity;
import dev.alicon.copsrobbers.entity.PoliceCruiserEntity;
import dev.alicon.copsrobbers.entity.FireTruckEntity;
import dev.alicon.copsrobbers.entity.FiremanEntity;
import dev.alicon.copsrobbers.entity.ModEntities;
import dev.alicon.copsrobbers.item.BankKitItem;
import dev.alicon.copsrobbers.item.PoliceStationKitItem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;

/** Builds a small kid-friendly police-and-robbers play area near fresh world spawn. */
public final class LongmontPatrolNeighborhood {
	private static final int AUTO_GENERATE_MAX_TICK = 600;
	private static final int CRUISER_CHECK_INTERVAL = 100;
	private static final Direction FRONT = Direction.EAST;
	private static final BlockPos[] STATION_OFFSETS = {
			new BlockPos(-28, 0, -12),
			new BlockPos(52, 0, 30)
	};
	private static final BlockPos[] BANK_OFFSETS = {
			new BlockPos(22, 0, -18),
			new BlockPos(-58, 0, 34),
			new BlockPos(84, 0, -26)
	};
	private static final BlockPos[] HIDEOUT_OFFSETS = {
			new BlockPos(-92, 0, -46),
			new BlockPos(108, 0, 44)
	};
	private static final BlockPos FIRE_STATION_OFFSET = new BlockPos(-4, 0, 58);

	private LongmontPatrolNeighborhood() {
	}

	/** Registers world tick behavior for starter-area generation and cruiser replacement. */
	public static void register() {
		ServerTickEvents.END_WORLD_TICK.register(LongmontPatrolNeighborhood::tick);
	}

	private static void tick(ServerLevel level) {
		if (level.dimension() != Level.OVERWORLD) {
			return;
		}

		PatrolNeighborhoodData data = level.getDataStorage().computeIfAbsent(PatrolNeighborhoodData.TYPE);
		if (!data.generated() && level.getGameTime() < AUTO_GENERATE_MAX_TICK) {
			generate(level, data);
		}
		if (data.generated() && level.getGameTime() % CRUISER_CHECK_INTERVAL == 0) {
			maintainCruiserNearSpawn(level, data.stationSpawn());
			maintainTellers(level, data.stationSpawn());
		}
	}

	private static void generate(ServerLevel level, PatrolNeighborhoodData data) {
		BlockPos anchor = surface(level, level.getRespawnData().pos());
		BlockPos firstStation = null;
		for (BlockPos offset : STATION_OFFSETS) {
			BlockPos station = clearPad(level, anchor.offset(offset), 16, 18);
			PoliceStationKitItem.placeStation(level, station, FRONT);
			spawnCops(level, station);
			spawnCruiser(level, cruiserParkingSpot(level, station));
			if (firstStation == null) {
				firstStation = station;
			}
		}

		for (BlockPos offset : BANK_OFFSETS) {
			BlockPos bank = clearPad(level, anchor.offset(offset), 15, 17);
			BankKitItem.placeBankWithTellers(level, bank, FRONT.getOpposite());
		}
		for (BlockPos offset : HIDEOUT_OFFSETS) {
			BlockPos hideout = clearPad(level, anchor.offset(offset), 11, 11);
			buildHideout(level, hideout);
		}
		BlockPos fireStation = clearPad(level, anchor.offset(FIRE_STATION_OFFSET), 16, 16);
		buildFireStation(level, fireStation);
		spawnFireCrew(level, fireStation);

		buildSimplePaths(level, anchor);
		spawnRobbers(level, anchor);
		if (firstStation != null) {
			BlockPos spawn = firstStation.relative(FRONT.getOpposite(), 1).above(2);
			level.setRespawnData(LevelData.RespawnData.of(Level.OVERWORLD, spawn, FRONT.toYRot(), 0.0F));
			data.markGenerated(spawn);
			for (ServerPlayer player : level.players()) {
				if (player.distanceToSqr(anchor.getCenter()) < 96.0D * 96.0D) {
					player.teleportTo(spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D);
					level.playSound(null, spawn, SoundEvents.IRON_DOOR_OPEN, SoundSource.PLAYERS, 0.7F, 1.15F);
				}
			}
		}
	}

	private static void buildSimplePaths(ServerLevel level, BlockPos anchor) {
		pathLineX(level, anchor, 0, -108, 116, 1);
		pathLineZ(level, anchor, 0, -56, 66, 1);
		pathLineZ(level, anchor, 58, -38, 44, 1);
		for (BlockPos offset : STATION_OFFSETS) {
			connectPath(level, anchor, offset);
		}
		for (BlockPos offset : BANK_OFFSETS) {
			connectPath(level, anchor, offset);
		}
		for (BlockPos offset : HIDEOUT_OFFSETS) {
			connectPath(level, anchor, offset);
		}
		connectPath(level, anchor, FIRE_STATION_OFFSET);
		for (int x = -96; x <= 108; x += 24) {
			streetLight(level, anchor.offset(x, 0, 4));
		}
		for (int z = -48; z <= 60; z += 24) {
			streetLight(level, anchor.offset(4, 0, z));
		}
		for (int z = -36; z <= 36; z += 24) {
			streetLight(level, anchor.offset(62, 0, z));
		}
	}

	private static void connectPath(ServerLevel level, BlockPos anchor, BlockPos targetOffset) {
		int x = targetOffset.getX();
		int z = targetOffset.getZ();
		pathLineX(level, anchor, z, Math.min(0, x), Math.max(0, x), 1);
		pathLineZ(level, anchor, x, Math.min(0, z), Math.max(0, z), 1);
		pathSquare(level, anchor.offset(x, 0, z), 2);
	}

	private static void pathLineX(ServerLevel level, BlockPos anchor, int z, int minX, int maxX, int radius) {
		for (int x = minX; x <= maxX; x++) {
			pathSquare(level, anchor.offset(x, 0, z), radius);
		}
	}

	private static void pathLineZ(ServerLevel level, BlockPos anchor, int x, int minZ, int maxZ, int radius) {
		for (int z = minZ; z <= maxZ; z++) {
			pathSquare(level, anchor.offset(x, 0, z), radius);
		}
	}

	private static void pathSquare(ServerLevel level, BlockPos rough, int radius) {
		for (int x = -radius; x <= radius; x++) {
			for (int z = -radius; z <= radius; z++) {
				pathBlock(level, rough.offset(x, 0, z));
			}
		}
	}

	private static void pathBlock(ServerLevel level, BlockPos rough) {
		BlockPos pos = surface(level, rough).below();
		if (!isPathGround(level.getBlockState(pos))) {
			return;
		}
		clearColumn(level, pos, 2);
		level.setBlock(pos, Blocks.GRAVEL.defaultBlockState(), 3);
	}

	private static void streetLight(ServerLevel level, BlockPos rough) {
		BlockPos base = surface(level, rough).below();
		if (!isPathGround(level.getBlockState(base))) {
			return;
		}
		for (int y = 1; y <= 4; y++) {
			level.setBlock(base.above(y), Blocks.IRON_BARS.defaultBlockState(), 3);
		}
		level.setBlock(base.above(5), Blocks.GLOWSTONE.defaultBlockState(), 3);
	}

	private static boolean isPathGround(BlockState state) {
		Block block = state.getBlock();
		return block == Blocks.GRASS_BLOCK
				|| block == Blocks.DIRT
				|| block == Blocks.COARSE_DIRT
				|| block == Blocks.PODZOL
				|| block == Blocks.GRAVEL
				|| block == Blocks.SAND
				|| block == Blocks.RED_SAND;
	}

	private static void buildHideout(ServerLevel level, BlockPos center) {
		fill(level, center, -5, 1, -4, 5, 1, 4, Blocks.MOSSY_COBBLESTONE);
		fill(level, center, -5, 2, -4, 5, 3, -4, Blocks.OAK_PLANKS);
		fill(level, center, -5, 2, 4, 5, 3, 4, Blocks.OAK_PLANKS);
		fill(level, center, -5, 2, -3, -5, 3, 4, Blocks.MOSSY_COBBLESTONE);
		fill(level, center, 5, 2, -4, 5, 3, 4, Blocks.OAK_PLANKS);
		fill(level, center, -4, 4, -3, 4, 4, 3, Blocks.OAK_SLAB);
		fill(level, center, -4, 2, -3, 4, 3, 3, Blocks.AIR);
		set(level, center, 0, 1, 5, Blocks.OAK_PLANKS);
		placeOakDoor(level, center, 0, 2, 4, Direction.SOUTH);
		set(level, center, 0, 2, 5, Blocks.OAK_PRESSURE_PLATE);
		set(level, center, 0, 2, 3, Blocks.OAK_PRESSURE_PLATE);
		fill(level, center, -3, 3, -4, -2, 3, -4, Blocks.GLASS_PANE);
		fill(level, center, 2, 3, -4, 3, 3, -4, Blocks.GLASS_PANE);
		fill(level, center, 5, 3, -1, 5, 3, 1, Blocks.GLASS_PANE);
		placeSign(level, center, 0, 4, 5, Direction.SOUTH, "Robber", "Hideout");
		level.setBlock(center.offset(0, 2, 0), Blocks.CHEST.defaultBlockState(), 3);
		set(level, center, -3, 3, -2, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
		set(level, center, 3, 3, 2, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
		level.setBlock(center.offset(4, 2, -2), Blocks.COBWEB.defaultBlockState(), 3);
		level.setBlock(center.offset(2, 2, 3), Blocks.COBWEB.defaultBlockState(), 3);
		level.setBlock(center.offset(-1, 2, -2), Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 3);
	}

	private static void buildFireStation(ServerLevel level, BlockPos center) {
		fill(level, center, -8, 1, -5, 8, 1, 10, Blocks.SMOOTH_STONE);
		fill(level, center, -8, 2, -5, 8, 4, -5, Blocks.RED_CONCRETE);
		fill(level, center, -8, 2, 10, 8, 4, 10, Blocks.RED_CONCRETE);
		fill(level, center, -8, 2, -5, -8, 4, 10, Blocks.RED_CONCRETE);
		fill(level, center, 8, 2, -5, 8, 4, 10, Blocks.RED_CONCRETE);
		fill(level, center, -8, 5, -5, 8, 5, 10, Blocks.SMOOTH_QUARTZ);
		fill(level, center, -7, 2, -4, 7, 4, 9, Blocks.AIR);
		fill(level, center, -4, 2, -5, 4, 4, -5, Blocks.GLASS_PANE);
		fill(level, center, -1, 2, -5, 1, 4, -5, Blocks.WHITE_CONCRETE);
		set(level, center, 0, 1, -6, Blocks.SMOOTH_STONE);
		placeIronDoor(level, center, 0, 2, -5, Direction.NORTH);
		set(level, center, 0, 2, -6, Blocks.STONE_PRESSURE_PLATE);
		set(level, center, 0, 2, -4, Blocks.STONE_PRESSURE_PLATE);
		fill(level, center, -7, 3, -5, -5, 3, -5, Blocks.GLASS_PANE);
		fill(level, center, 5, 3, -5, 7, 3, -5, Blocks.GLASS_PANE);
		fill(level, center, -8, 3, 0, -8, 3, 3, Blocks.GLASS_PANE);
		fill(level, center, 8, 3, 0, 8, 3, 3, Blocks.GLASS_PANE);
		fill(level, center, -7, 2, 3, -5, 4, 8, Blocks.WHITE_WOOL);
		fill(level, center, -5, 4, 0, -1, 4, 0, Blocks.SEA_LANTERN);
		fill(level, center, 1, 4, 0, 5, 4, 0, Blocks.SEA_LANTERN);
		fill(level, center, -5, 4, 6, -1, 4, 6, Blocks.SEA_LANTERN);
		fill(level, center, 1, 4, 6, 5, 4, 6, Blocks.SEA_LANTERN);
		placeSign(level, center, 0, 6, -6, Direction.NORTH, "Fire", "Station");
		set(level, center, 0, 6, -5, Blocks.WHITE_CONCRETE);
		set(level, center, -1, 6, -5, Blocks.RED_CONCRETE);
		set(level, center, 1, 6, -5, Blocks.RED_CONCRETE);
	}

	private static void spawnRobbers(ServerLevel level, BlockPos anchor) {
		for (int i = 0; i < 14; i++) {
			int x = level.random.nextInt(121) - 60;
			int z = level.random.nextInt(81) - 40;
			BlockPos pos = surface(level, anchor.offset(x, 0, z)).above();
			BankRobberEntity robber = ModEntities.BANK_ROBBER.create(level, EntitySpawnReason.EVENT);
			if (robber != null) {
				robber.snapTo(pos, level.random.nextFloat() * 360.0F, 0.0F);
				level.addFreshEntity(robber);
			}
		}
	}

	private static void spawnCops(ServerLevel level, BlockPos station) {
		for (int i = 0; i < 3; i++) {
			CopEntity cop = ModEntities.COP.create(level, EntitySpawnReason.EVENT);
			if (cop != null) {
				BlockPos pos = station.relative(FRONT, 4 + i * 2).relative(FRONT.getClockWise(), i - 1).above();
				cop.snapTo(pos, FRONT.toYRot(), 0.0F);
				level.addFreshEntity(cop);
			}
		}
	}

	private static void spawnFireCrew(ServerLevel level, BlockPos station) {
		for (int i = 0; i < 3; i++) {
			FiremanEntity fireman = ModEntities.FIREMAN.create(level, EntitySpawnReason.EVENT);
			if (fireman != null) {
				BlockPos pos = station.offset(-4 + i * 4, 2, 2);
				fireman.snapTo(pos, FRONT.toYRot(), 0.0F);
				level.addFreshEntity(fireman);
			}
		}
		FireTruckEntity truck = ModEntities.FIRE_TRUCK.create(level, EntitySpawnReason.EVENT);
		if (truck != null) {
			truck.snapTo(station.offset(0, 2, -8), FRONT.toYRot(), 0.0F);
			level.addFreshEntity(truck);
		}
	}

	private static void maintainCruiserNearSpawn(ServerLevel level, BlockPos spawn) {
		for (ServerPlayer player : level.players()) {
			if (player.distanceToSqr(spawn.getCenter()) < 40.0D * 40.0D) {
				ensureCruiserNear(level, spawn);
				return;
			}
		}
	}

	private static void maintainTellers(ServerLevel level, BlockPos spawn) {
		BlockPos firstStation = spawn.below(2).relative(FRONT);
		BlockPos anchor = firstStation.offset(-STATION_OFFSETS[0].getX(), -STATION_OFFSETS[0].getY(), -STATION_OFFSETS[0].getZ());
		for (BlockPos offset : BANK_OFFSETS) {
			BankKitItem.ensureTellers(level, anchor.offset(offset), FRONT.getOpposite());
		}
	}

	private static void ensureCruiserNear(ServerLevel level, BlockPos spawn) {
		BlockPos firstStation = spawn.below(2).relative(FRONT);
		BlockPos parking = cruiserParkingSpot(level, firstStation);
		AABB nearby = AABB.ofSize(parking.getCenter(), 12.0D, 8.0D, 12.0D);
		if (level.getEntities(ModEntities.POLICE_CRUISER, nearby, PoliceCruiserEntity::isAlive).isEmpty()) {
			spawnCruiser(level, parking);
		}
	}

	private static BlockPos cruiserParkingSpot(ServerLevel level, BlockPos station) {
		return surface(level, station.relative(FRONT.getOpposite(), 11).relative(FRONT.getCounterClockWise(), 5)).above();
	}

	private static void spawnCruiser(ServerLevel level, BlockPos pos) {
		PoliceCruiserEntity cruiser = ModEntities.POLICE_CRUISER.create(level, EntitySpawnReason.EVENT);
		if (cruiser != null) {
			cruiser.snapTo(pos, FRONT.toYRot(), 0.0F);
			level.addFreshEntity(cruiser);
		}
	}

	private static BlockPos clearPad(ServerLevel level, BlockPos roughCenter, int halfX, int halfZ) {
		BlockPos center = surface(level, roughCenter);
		int y = center.getY();
		for (int x = -halfX; x <= halfX; x++) {
			for (int z = -halfZ; z <= halfZ; z++) {
				BlockPos pos = new BlockPos(center.getX() + x, y, center.getZ() + z);
				clearColumn(level, pos, 12);
				level.setBlock(pos, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
				level.setBlock(pos.below(), Blocks.DIRT.defaultBlockState(), 3);
			}
		}
		return center;
	}

	private static void clearColumn(ServerLevel level, BlockPos ground, int height) {
		for (int y = 1; y <= height; y++) {
			level.setBlock(ground.above(y), Blocks.AIR.defaultBlockState(), 3);
		}
	}

	private static void fill(ServerLevel level, BlockPos center, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Block block) {
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					level.setBlock(center.offset(x, y, z), block.defaultBlockState(), 3);
				}
			}
		}
	}

	private static void set(ServerLevel level, BlockPos center, int x, int y, int z, Block block) {
		level.setBlock(center.offset(x, y, z), block.defaultBlockState(), 3);
	}

	private static void set(ServerLevel level, BlockPos center, int x, int y, int z, BlockState state) {
		level.setBlock(center.offset(x, y, z), state, 3);
	}

	private static void placeIronDoor(ServerLevel level, BlockPos center, int x, int y, int z, Direction facing) {
		placeDoor(level, center, x, y, z, facing, Blocks.IRON_DOOR.defaultBlockState());
	}

	private static void placeOakDoor(ServerLevel level, BlockPos center, int x, int y, int z, Direction facing) {
		placeDoor(level, center, x, y, z, facing, Blocks.OAK_DOOR.defaultBlockState());
	}

	private static void placeDoor(ServerLevel level, BlockPos center, int x, int y, int z, Direction facing, BlockState base) {
		BlockState lower = base.setValue(DoorBlock.FACING, facing).setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
		BlockState upper = lower.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);
		set(level, center, x, y, z, lower);
		set(level, center, x, y + 1, z, upper);
	}

	private static void placeSign(ServerLevel level, BlockPos center, int x, int y, int z, Direction facing, String line0, String line1) {
		BlockPos pos = center.offset(x, y, z);
		level.setBlock(pos, Blocks.OAK_WALL_SIGN.defaultBlockState().setValue(WallSignBlock.FACING, facing), 3);
		if (level.getBlockEntity(pos) instanceof SignBlockEntity sign) {
			SignText text = sign.getFrontText()
					.setMessage(0, Component.literal(line0))
					.setMessage(1, Component.literal(line1));
			sign.setText(text, true);
		}
	}

	private static BlockPos surface(ServerLevel level, BlockPos pos) {
		return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos);
	}
}
