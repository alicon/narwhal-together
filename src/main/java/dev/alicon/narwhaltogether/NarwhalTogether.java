package dev.alicon.narwhaltogether;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NarwhalTogether implements ModInitializer {
	public static final String MOD_ID = "narwhal_together";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.playC2S().register(
				TeleportToNextPlayerPayload.TYPE,
				TeleportToNextPlayerPayload.CODEC
		);
		ServerPlayNetworking.registerGlobalReceiver(
				TeleportToNextPlayerPayload.TYPE,
				(payload, context) -> PlayerTeleporter.teleportToNextPlayer(context.player())
		);

		LOGGER.info("NARwhal Together initialized");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
