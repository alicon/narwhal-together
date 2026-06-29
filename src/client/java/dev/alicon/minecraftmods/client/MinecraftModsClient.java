package dev.alicon.minecraftmods.client;

import dev.alicon.minecraftmods.MinecraftMods;
import dev.alicon.minecraftmods.TeleportToNextPlayerPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class MinecraftModsClient implements ClientModInitializer {
	private static final KeyMapping.Category CONTROLS_CATEGORY =
			KeyMapping.Category.register(MinecraftMods.id("controls"));
	private static final KeyMapping TELEPORT_TO_PLAYER = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.minecraft_mods.teleport_to_player",
			GLFW.GLFW_KEY_G,
			CONTROLS_CATEGORY
	));

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (TELEPORT_TO_PLAYER.consumeClick()) {
				requestTeleport(client);
			}
		});
	}

	private static void requestTeleport(Minecraft client) {
		if (client.player == null) {
			return;
		}

		if (!ClientPlayNetworking.canSend(TeleportToNextPlayerPayload.TYPE)) {
			client.player.displayClientMessage(
					Component.translatable("message.minecraft_mods.server_required"),
					true
			);
			return;
		}

		ClientPlayNetworking.send(TeleportToNextPlayerPayload.INSTANCE);
	}
}
