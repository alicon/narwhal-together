package dev.alicon.copsrobbers.client;

import dev.alicon.copsrobbers.CopsAndRobbers;
import dev.alicon.copsrobbers.ToggleCruiserFlightPayload;
import dev.alicon.copsrobbers.ToggleCruiserLightsPayload;
import dev.alicon.copsrobbers.ToggleCruiserSirenPayload;
import dev.alicon.copsrobbers.TriggerCruiserBarrelRollPayload;
import dev.alicon.copsrobbers.TriggerCruiserLoopPayload;
import dev.alicon.copsrobbers.UpdateCruiserFlightInputPayload;
import dev.alicon.copsrobbers.client.render.BankRobberModel;
import dev.alicon.copsrobbers.client.render.BankRobberRenderer;
import dev.alicon.copsrobbers.client.render.PoliceCruiserModel;
import dev.alicon.copsrobbers.client.render.PoliceCruiserRenderer;
import dev.alicon.copsrobbers.client.render.FireTruckRenderer;
import dev.alicon.copsrobbers.client.render.PoliceNpcRenderer;
import dev.alicon.copsrobbers.entity.PoliceCruiserEntity;
import dev.alicon.copsrobbers.entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/** Client entrypoint for Cops and Robbers renderers and model layers. */
public final class CopsAndRobbersClient implements ClientModInitializer {
	private static final int FLIGHT_DOUBLE_TAP_TICKS = 7;
	private static final KeyMapping.Category CONTROLS_CATEGORY =
			KeyMapping.Category.register(CopsAndRobbers.id("controls"));
	private static int lastJumpTapTick = -FLIGHT_DOUBLE_TAP_TICKS;
	private static boolean lastJumpPressed;
	private static final KeyMapping TOGGLE_LIGHTS = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.cops_robbers.toggle_lights",
			GLFW.GLFW_KEY_Z,
			CONTROLS_CATEGORY
	));
	private static final KeyMapping TOGGLE_SIREN = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.cops_robbers.toggle_siren",
			GLFW.GLFW_KEY_X,
			CONTROLS_CATEGORY
	));
	private static final KeyMapping BARREL_ROLL = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.cops_robbers.barrel_roll",
			GLFW.GLFW_KEY_C,
			CONTROLS_CATEGORY
	));
	private static final KeyMapping LOOP = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.cops_robbers.loop",
			GLFW.GLFW_KEY_V,
			CONTROLS_CATEGORY
	));

	@Override
	public void onInitializeClient() {
		EntityModelLayerRegistry.registerModelLayer(PoliceCruiserModel.LAYER_LOCATION, PoliceCruiserModel::createBodyLayer);
		EntityModelLayerRegistry.registerModelLayer(BankRobberModel.LAYER_LOCATION, BankRobberModel::createBodyLayer);
		EntityRendererRegistry.register(ModEntities.POLICE_CRUISER, PoliceCruiserRenderer::new);
		EntityRendererRegistry.register(ModEntities.FIRE_TRUCK, FireTruckRenderer::new);
		EntityRendererRegistry.register(ModEntities.BANK_ROBBER, BankRobberRenderer::new);
		EntityRendererRegistry.register(ModEntities.TELLER, context ->
				new PoliceNpcRenderer<>(context, CopsAndRobbers.id("textures/entity/teller.png")));
		EntityRendererRegistry.register(ModEntities.COP, context ->
				new PoliceNpcRenderer<>(context, CopsAndRobbers.id("textures/entity/cop.png")));
		EntityRendererRegistry.register(ModEntities.FIREMAN, context ->
				new PoliceNpcRenderer<>(context, CopsAndRobbers.id("textures/entity/fireman.png")));
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (TOGGLE_LIGHTS.consumeClick()) {
				if (isDrivingCruiser(client) && ClientPlayNetworking.canSend(ToggleCruiserLightsPayload.TYPE)) {
					ClientPlayNetworking.send(ToggleCruiserLightsPayload.INSTANCE);
				}
			}
			while (TOGGLE_SIREN.consumeClick()) {
				if (isDrivingCruiser(client) && ClientPlayNetworking.canSend(ToggleCruiserSirenPayload.TYPE)) {
					ClientPlayNetworking.send(ToggleCruiserSirenPayload.INSTANCE);
				}
			}
			while (BARREL_ROLL.consumeClick()) {
				if (isDrivingCruiser(client) && ClientPlayNetworking.canSend(TriggerCruiserBarrelRollPayload.TYPE)) {
					ClientPlayNetworking.send(TriggerCruiserBarrelRollPayload.INSTANCE);
				}
			}
			while (LOOP.consumeClick()) {
				if (isDrivingCruiser(client) && ClientPlayNetworking.canSend(TriggerCruiserLoopPayload.TYPE)) {
					ClientPlayNetworking.send(TriggerCruiserLoopPayload.INSTANCE);
				}
			}
			sendVanillaDrivingShortcuts(client);
			tickCreativeFlightToggle(client);
			sendCreativeFlightInput(client);
			CopsAndRobbersControllerShortcuts.tick(client);
		});
	}

	private static void sendVanillaDrivingShortcuts(Minecraft client) {
		if (!isDrivingCruiser(client)) {
			return;
		}
		while (client.options.keyInventory.consumeClick()) {
			if (ClientPlayNetworking.canSend(ToggleCruiserLightsPayload.TYPE)) {
				ClientPlayNetworking.send(ToggleCruiserLightsPayload.INSTANCE);
			}
		}
		while (client.options.keyDrop.consumeClick()) {
			if (ClientPlayNetworking.canSend(ToggleCruiserSirenPayload.TYPE)) {
				ClientPlayNetworking.send(ToggleCruiserSirenPayload.INSTANCE);
			}
		}
	}

	private static void tickCreativeFlightToggle(Minecraft client) {
		if (!isDrivingCruiser(client) || client.player == null || !client.player.isCreative()) {
			lastJumpPressed = false;
			lastJumpTapTick = -FLIGHT_DOUBLE_TAP_TICKS;
			return;
		}

		boolean jumpPressed = client.options.keyJump.isDown();
		if (jumpPressed && !lastJumpPressed) {
			if (client.player.tickCount - lastJumpTapTick <= FLIGHT_DOUBLE_TAP_TICKS) {
				if (ClientPlayNetworking.canSend(ToggleCruiserFlightPayload.TYPE)) {
					ClientPlayNetworking.send(ToggleCruiserFlightPayload.INSTANCE);
				}
				lastJumpTapTick = -FLIGHT_DOUBLE_TAP_TICKS;
			} else {
				lastJumpTapTick = client.player.tickCount;
			}
		}
		lastJumpPressed = jumpPressed;
	}

	private static void sendCreativeFlightInput(Minecraft client) {
		if (!isDrivingCruiser(client) || client.player == null || !client.player.isCreative()
				|| !ClientPlayNetworking.canSend(UpdateCruiserFlightInputPayload.TYPE)) {
			return;
		}

		float lift = 0.0F;
		if (client.options.keyJump.isDown()) {
			lift += 1.0F;
		}
		if (client.options.keyShift.isDown()) {
			lift -= 1.0F;
		}
		ClientPlayNetworking.send(new UpdateCruiserFlightInputPayload(lift));
	}

	static boolean isDrivingCruiser(Minecraft client) {
		return client.screen == null
				&& client.player != null
				&& client.player.getVehicle() instanceof PoliceCruiserEntity;
	}
}
