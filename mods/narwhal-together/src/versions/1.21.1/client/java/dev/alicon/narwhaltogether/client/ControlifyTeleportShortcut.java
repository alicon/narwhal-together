package dev.alicon.narwhaltogether.client;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

final class ControlifyTeleportShortcut {
	private static final ControlifyChord CHORD = new ControlifyChord();

	private ControlifyTeleportShortcut() {
	}

	static boolean consumeClick(Minecraft client) {
		return client.screen == null && CHORD.consumeClick();
	}

	private static final class ControlifyChord {
		private static final ResourceLocation PADDLE_1 = ResourceLocation.fromNamespaceAndPath("controlify", "button/paddle1");
		private static final ResourceLocation RIGHT_STICK = ResourceLocation.fromNamespaceAndPath("controlify", "button/right_stick");
		private boolean unavailable;
		private Object controlifyApi;
		private Object fallbackHoldBinding;
		private Object fallbackTapBinding;
		private Object paddleTapBinding;
		private Method getCurrentController;
		private Method controllerInput;
		private Method inputStateNow;
		private Method inputStateThen;
		private Method isButtonDown;
		private Method bindingOn;
		private Method digitalNow;
		private Method justPressed;

		private boolean consumeClick() {
			if (unavailable || !initialize()) {
				return false;
			}

			try {
				Optional<?> controller = (Optional<?>) getCurrentController.invoke(controlifyApi);
				if (controller.isEmpty()) {
					return false;
				}

				return rightStickClicked(controller.get())
						|| paddleChordPressed(controller.get())
						|| fallbackChordPressed(controller.get());
			} catch (ReflectiveOperationException | ClassCastException exception) {
				unavailable = true;
				return false;
			}
		}

		private boolean initialize() {
			if (controlifyApi != null) {
				return true;
			}

			try {
				Class<?> apiClass = Class.forName("dev.isxander.controlify.api.ControlifyApi");
				Class<?> bindingsClass = Class.forName("dev.isxander.controlify.bindings.ControlifyBindings");
				controlifyApi = apiClass.getMethod("get").invoke(null);
				getCurrentController = apiClass.getMethod("getCurrentController");
				controllerInput = methodNamed(Class.forName("dev.isxander.controlify.controller.ControllerEntity"), "input", 0);
				inputStateNow = methodNamed(Class.forName("dev.isxander.controlify.controller.input.InputComponent"), "stateNow", 0);
				inputStateThen = methodNamed(Class.forName("dev.isxander.controlify.controller.input.InputComponent"), "stateThen", 0);
				isButtonDown = methodNamed(
						Class.forName("dev.isxander.controlify.controller.input.ControllerStateView"),
						"isButtonDown",
						1
				);
				fallbackHoldBinding = staticField(bindingsClass, "CHANGE_PERSPECTIVE");
				fallbackTapBinding = staticField(bindingsClass, "OPEN_CHAT");
				paddleTapBinding = staticField(bindingsClass, "PICK_BLOCK");
				bindingOn = methodNamed(fallbackHoldBinding.getClass(), "on", 1);
				Class<?> inputBindingClass = bindingOn.getReturnType();
				digitalNow = methodNamed(inputBindingClass, "digitalNow", 0);
				justPressed = methodNamed(inputBindingClass, "justPressed", 0);
				return true;
			} catch (ReflectiveOperationException | LinkageError exception) {
				unavailable = true;
				return false;
			}
		}

		private boolean rightStickClicked(Object controller) throws ReflectiveOperationException {
			Optional<?> input = (Optional<?>) controllerInput.invoke(controller);
			if (input.isEmpty()) {
				return false;
			}

			Object stateNow = inputStateNow.invoke(input.get());
			Object stateThen = inputStateThen.invoke(input.get());
			return Boolean.TRUE.equals(isButtonDown.invoke(stateNow, RIGHT_STICK))
					&& !Boolean.TRUE.equals(isButtonDown.invoke(stateThen, RIGHT_STICK));
		}

		private boolean paddleChordPressed(Object controller) throws ReflectiveOperationException {
			Optional<?> input = (Optional<?>) controllerInput.invoke(controller);
			if (input.isEmpty()) {
				return false;
			}

			Object stateNow = inputStateNow.invoke(input.get());
			Object tap = bindingOn.invoke(paddleTapBinding, controller);
			return Boolean.TRUE.equals(isButtonDown.invoke(stateNow, PADDLE_1))
					&& Boolean.TRUE.equals(justPressed.invoke(tap));
		}

		private boolean fallbackChordPressed(Object controller) throws ReflectiveOperationException {
			Object hold = bindingOn.invoke(fallbackHoldBinding, controller);
			Object tap = bindingOn.invoke(fallbackTapBinding, controller);
			return Boolean.TRUE.equals(digitalNow.invoke(hold))
					&& Boolean.TRUE.equals(justPressed.invoke(tap));
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
