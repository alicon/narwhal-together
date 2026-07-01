package dev.alicon.copsrobbers.client;

import dev.alicon.copsrobbers.ToggleCruiserLightsPayload;
import dev.alicon.copsrobbers.ToggleCruiserSirenPayload;
import dev.alicon.copsrobbers.TriggerCruiserBarrelRollPayload;
import dev.alicon.copsrobbers.TriggerCruiserLoopPayload;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

final class CopsAndRobbersControllerShortcuts {
	private static final ControlifyActions ACTIONS = new ControlifyActions();

	private CopsAndRobbersControllerShortcuts() {
	}

	static void tick(Minecraft client) {
		if (!CopsAndRobbersClient.isDrivingCruiser(client)) {
			return;
		}

		ACTIONS.sendIfPressed("INVENTORY", ToggleCruiserLightsPayload.TYPE, ToggleCruiserLightsPayload.INSTANCE);
		ACTIONS.sendIfPressed("DROP", ToggleCruiserSirenPayload.TYPE, ToggleCruiserSirenPayload.INSTANCE);
		ACTIONS.sendIfPressed("SWAP_HANDS", TriggerCruiserBarrelRollPayload.TYPE, TriggerCruiserBarrelRollPayload.INSTANCE);
		ACTIONS.sendIfPressed("PICK_BLOCK", TriggerCruiserLoopPayload.TYPE, TriggerCruiserLoopPayload.INSTANCE);
	}

	private static final class ControlifyActions {
		private boolean unavailable;
		private Object controlifyApi;
		private Class<?> bindingsClass;
		private Method getCurrentController;
		private Method bindingOn;
		private Method justPressed;

		private <T extends CustomPacketPayload> void sendIfPressed(String bindingName, CustomPacketPayload.Type<T> type, T payload) {
			if (!ClientPlayNetworking.canSend(type) || unavailable || !initialize()) {
				return;
			}

			try {
				Optional<?> controller = (Optional<?>) getCurrentController.invoke(controlifyApi);
				if (controller.isEmpty()) {
					return;
				}

				Object binding = staticField(bindingsClass, bindingName);
				Object controllerBinding = bindingOn.invoke(binding, controller.get());
				if (Boolean.TRUE.equals(justPressed.invoke(controllerBinding))) {
					ClientPlayNetworking.send(payload);
				}
			} catch (ReflectiveOperationException | ClassCastException exception) {
				unavailable = true;
			}
		}

		private boolean initialize() {
			if (controlifyApi != null) {
				return true;
			}

			try {
				Class<?> apiClass = Class.forName("dev.isxander.controlify.api.ControlifyApi");
				bindingsClass = Class.forName("dev.isxander.controlify.bindings.ControlifyBindings");
				controlifyApi = apiClass.getMethod("get").invoke(null);
				getCurrentController = apiClass.getMethod("getCurrentController");
				Object sampleBinding = staticField(bindingsClass, "INVENTORY");
				bindingOn = methodNamed(sampleBinding.getClass(), "on", 1);
				Class<?> inputBindingClass = bindingOn.getReturnType();
				justPressed = methodNamed(inputBindingClass, "justPressed", 0);
				return true;
			} catch (ReflectiveOperationException | LinkageError exception) {
				unavailable = true;
				return false;
			}
		}

		private static Object staticField(Class<?> owner, String name) throws ReflectiveOperationException {
			Field field = owner.getField(name);
			return field.get(null);
		}

		private static Method methodNamed(Class<?> owner, String name, int parameterCount) throws NoSuchMethodException {
			for (Method method : owner.getMethods()) {
				if (method.getName().equals(name) && method.getParameterCount() == parameterCount) {
					return method;
				}
			}
			throw new NoSuchMethodException(owner.getName() + "." + name);
		}
	}
}
