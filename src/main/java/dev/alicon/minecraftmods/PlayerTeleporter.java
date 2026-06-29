package dev.alicon.minecraftmods;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

final class PlayerTeleporter {
	private static final long COOLDOWN_TICKS = 40;
	private static final Map<UUID, Long> LAST_TELEPORT_TICK = new HashMap<>();
	private static final Map<UUID, UUID> LAST_TARGET = new HashMap<>();

	private PlayerTeleporter() {
	}

	static void teleportToNextPlayer(ServerPlayer player) {
		long currentTick = player.level().getGameTime();
		long lastTick = LAST_TELEPORT_TICK.getOrDefault(player.getUUID(), Long.MIN_VALUE / 2);

		if (currentTick - lastTick < COOLDOWN_TICKS) {
			player.displayClientMessage(Component.translatable("message.minecraft_mods.teleport_cooldown"), true);
			return;
		}

		List<ServerPlayer> targets = player.level().getServer().getPlayerList().getPlayers().stream()
				.filter(candidate -> candidate != player)
				.filter(candidate -> !candidate.isSpectator())
				.sorted(Comparator.comparing(candidate -> candidate.getName().getString(), String.CASE_INSENSITIVE_ORDER))
				.toList();

		if (targets.isEmpty()) {
			player.displayClientMessage(Component.translatable("message.minecraft_mods.no_players"), true);
			return;
		}

		ServerPlayer target = nextTarget(player, targets);
		player.stopRiding();
		player.teleportTo(
				target.level(),
				target.getX(),
				target.getY(),
				target.getZ(),
				Set.of(),
				target.getYRot(),
				target.getXRot(),
				false
		);
		player.resetFallDistance();

		LAST_TELEPORT_TICK.put(player.getUUID(), currentTick);
		LAST_TARGET.put(player.getUUID(), target.getUUID());
		player.displayClientMessage(
				Component.translatable("message.minecraft_mods.teleported", target.getDisplayName()),
				true
		);
		target.displayClientMessage(
				Component.translatable("message.minecraft_mods.arrived", player.getDisplayName()),
				true
		);
	}

	private static ServerPlayer nextTarget(ServerPlayer player, List<ServerPlayer> targets) {
		UUID lastTarget = LAST_TARGET.get(player.getUUID());

		for (int index = 0; index < targets.size(); index++) {
			if (targets.get(index).getUUID().equals(lastTarget)) {
				return targets.get((index + 1) % targets.size());
			}
		}

		return targets.getFirst();
	}
}
