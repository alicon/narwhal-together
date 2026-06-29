package dev.alicon.minecraftmods;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MinecraftMods implements ModInitializer {
	public static final String MOD_ID = "minecraft_mods";
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

		LOGGER.info("Minecraft Mods initialized");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
