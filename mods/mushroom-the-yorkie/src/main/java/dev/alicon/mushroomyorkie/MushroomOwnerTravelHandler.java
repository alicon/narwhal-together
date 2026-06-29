package dev.alicon.mushroomyorkie;

import dev.alicon.mushroomyorkie.entity.MushroomYorkieEntity;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.entity.EntityTypeTest;

import java.util.List;
import java.util.Set;

final class MushroomOwnerTravelHandler {
	private MushroomOwnerTravelHandler() {
	}

	static void register() {
		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(MushroomOwnerTravelHandler::afterPlayerChangeWorld);
	}

	private static void afterPlayerChangeWorld(ServerPlayer player, ServerLevel origin, ServerLevel destination) {
		for (MushroomYorkieEntity yorkie : followingYorkies(player, origin)) {
			yorkie.teleportTo(
					destination,
					player.getX() + 0.5D,
					player.getY(),
					player.getZ() + 0.5D,
					Set.<Relative>of(),
					player.getYRot(),
					0.0F,
					false
			);
		}
	}

	private static List<? extends MushroomYorkieEntity> followingYorkies(ServerPlayer player, ServerLevel origin) {
		return origin.getEntities(
				EntityTypeTest.forClass(MushroomYorkieEntity.class),
				yorkie -> yorkie.isAlive() && yorkie.isOwnedBy(player) && !yorkie.isOrderedToSit()
		);
	}
}
