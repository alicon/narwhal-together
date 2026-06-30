package dev.alicon.narwhaltogether.client;

import dev.alicon.narwhaltogether.client.mixin.AbstractContainerScreenAccessor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

final class SwitchStyleControllerCrafting {
	private static final ControlifyInputs CONTROLIFY_INPUTS = new ControlifyInputs();
	private static PendingCreativeAdd pendingCreativeAdd;
	private static int pendingCraftOneTicks;
	private static int pendingCraftMaxTicks;
	private static int pendingPlaceCarriedTicks;

	private SwitchStyleControllerCrafting() {
	}

	static void tick(Minecraft client) {
		if (client.player == null || client.gameMode == null || !(client.screen instanceof AbstractContainerScreen<?> screen)) {
			clearPending();
			return;
		}

		ControllerPresses presses = CONTROLIFY_INPUTS.poll();
		AbstractContainerMenu menu = screen.getMenu();
		Slot hoveredSlot = ((AbstractContainerScreenAccessor) screen).narwhalTogether$hoveredSlot();
		if (screen instanceof CreativeModeInventoryScreen) {
			handleCreativeScreen(client, menu, hoveredSlot, presses);
			return;
		}

		handleCraftingScreen(client, menu, hoveredSlot, presses);
	}

	private static void handleCreativeScreen(
			Minecraft client,
			AbstractContainerMenu menu,
			Slot hoveredSlot,
			ControllerPresses presses
	) {
		pendingCraftOneTicks = 0;
		pendingCraftMaxTicks = 0;
		pendingPlaceCarriedTicks = 0;

		if (isCreativePaletteSlot(client, hoveredSlot) && (presses.select() || presses.quickMove())) {
			ItemStack stack = hoveredSlot.getItem().copy();
			stack.setCount(presses.quickMove() ? stack.getMaxStackSize() : 1);
			pendingCreativeAdd = new PendingCreativeAdd(stack, 2);
			return;
		}

		if (pendingCreativeAdd == null) {
			return;
		}

		pendingCreativeAdd = pendingCreativeAdd.nextTick();
		if (pendingCreativeAdd.ticksRemaining() > 0) {
			return;
		}

		ItemStack stack = pendingCreativeAdd.stack();
		pendingCreativeAdd = null;
		int freeSlot = client.player.getInventory().getFreeSlot();
		if (freeSlot < 0) {
			menu.setCarried(stack.copy());
			return;
		}

		ItemStack stackToAdd = stack.copy();
		client.player.getInventory().setItem(freeSlot, stackToAdd.copy());
		client.player.inventoryMenu.broadcastChanges();
		client.gameMode.handleCreativeModeItemAdd(stackToAdd, creativeProtocolSlot(freeSlot));
		menu.setCarried(ItemStack.EMPTY);
	}

	private static boolean isCreativePaletteSlot(Minecraft client, Slot slot) {
		return slot != null
				&& slot.hasItem()
				&& !slot.isFake()
				&& client.player != null
				&& slot.container != client.player.getInventory();
	}

	private static int creativeProtocolSlot(int inventorySlot) {
		if (inventorySlot >= 0 && inventorySlot < Inventory.getSelectionSize()) {
			return 36 + inventorySlot;
		}
		return inventorySlot;
	}

	private static void handleCraftingScreen(
			Minecraft client,
			AbstractContainerMenu menu,
			Slot hoveredSlot,
			ControllerPresses presses
	) {
		pendingCreativeAdd = null;
		Slot resultSlot = resultSlot(menu);
		if (resultSlot == null) {
			pendingCraftOneTicks = 0;
			pendingCraftMaxTicks = 0;
			pendingPlaceCarriedTicks = 0;
			return;
		}

		if (presses.select()) {
			if (hoveredSlot != resultSlot && menu.getCarried().isEmpty()) {
				pendingCraftOneTicks = 3;
			} else {
				pendingPlaceCarriedTicks = 3;
			}
		}
		if (presses.quickMove() && hoveredSlot != resultSlot) {
			pendingCraftMaxTicks = 3;
		}

		if (pendingCraftMaxTicks > 0) {
			pendingCraftMaxTicks--;
			if (resultSlot.hasItem()) {
				client.gameMode.handleInventoryMouseClick(
						menu.containerId,
						resultSlot.index,
						0,
						ClickType.QUICK_MOVE,
						client.player
				);
				pendingCraftMaxTicks = 0;
			}
		}

		if (pendingCraftOneTicks > 0) {
			pendingCraftOneTicks--;
			if (resultSlot.hasItem()) {
				client.gameMode.handleInventoryMouseClick(
						menu.containerId,
						resultSlot.index,
						0,
						ClickType.PICKUP,
						client.player
				);
				pendingCraftOneTicks = 0;
				pendingPlaceCarriedTicks = 3;
			}
		}

		if (pendingPlaceCarriedTicks > 0) {
			pendingPlaceCarriedTicks--;
			placeCarriedStack(client, menu);
		}
	}

	private static Slot resultSlot(AbstractContainerMenu menu) {
		if (menu instanceof CraftingMenu craftingMenu) {
			return craftingMenu.getResultSlot();
		}
		if (menu instanceof InventoryMenu inventoryMenu) {
			return inventoryMenu.getResultSlot();
		}

		for (Slot slot : menu.slots) {
			if (slot instanceof ResultSlot) {
				return slot;
			}
		}
		return null;
	}

	private static void placeCarriedStack(Minecraft client, AbstractContainerMenu menu) {
		ItemStack carried = menu.getCarried();
		if (carried.isEmpty()) {
			pendingPlaceCarriedTicks = 0;
			return;
		}

		Slot destination = carriedMergeSlot(client, menu, carried);
		if (destination == null) {
			destination = emptyInventorySlot(client, menu, carried);
		}
		if (destination == null) {
			return;
		}

		client.gameMode.handleInventoryMouseClick(
				menu.containerId,
				destination.index,
				0,
				ClickType.PICKUP,
				client.player
		);
		pendingPlaceCarriedTicks = 0;
	}

	private static Slot carriedMergeSlot(Minecraft client, AbstractContainerMenu menu, ItemStack carried) {
		for (Slot slot : menu.slots) {
			if (isPlayerInventoryDestination(client, slot, carried)
					&& slot.hasItem()
					&& ItemStack.isSameItemSameComponents(slot.getItem(), carried)
					&& slot.getItem().getCount() < slot.getItem().getMaxStackSize()) {
				return slot;
			}
		}
		return null;
	}

	private static Slot emptyInventorySlot(Minecraft client, AbstractContainerMenu menu, ItemStack carried) {
		for (Slot slot : menu.slots) {
			if (isPlayerInventoryDestination(client, slot, carried) && !slot.hasItem()) {
				return slot;
			}
		}
		return null;
	}

	private static boolean isPlayerInventoryDestination(Minecraft client, Slot slot, ItemStack stack) {
		return client.player != null
				&& slot.container == client.player.getInventory()
				&& slot.mayPlace(stack)
				&& !slot.isFake();
	}

	private static void clearPending() {
		pendingCreativeAdd = null;
		pendingCraftOneTicks = 0;
		pendingCraftMaxTicks = 0;
		pendingPlaceCarriedTicks = 0;
	}

	private record PendingCreativeAdd(ItemStack stack, int ticksRemaining) {
		private PendingCreativeAdd nextTick() {
			return new PendingCreativeAdd(stack, ticksRemaining - 1);
		}
	}

	private record ControllerPresses(boolean select, boolean quickMove) {
		private static final ControllerPresses NONE = new ControllerPresses(false, false);
	}

	private static final class ControlifyInputs {
		private boolean unavailable;
		private Object controlifyApi;
		private Object selectBinding;
		private Object quickMoveBinding;
		private Method getCurrentController;
		private Method bindingOn;
		private Method justPressed;

		private ControllerPresses poll() {
			if (unavailable || !initialize()) {
				return ControllerPresses.NONE;
			}

			try {
				Optional<?> controller = (Optional<?>) getCurrentController.invoke(controlifyApi);
				if (controller.isEmpty()) {
					return ControllerPresses.NONE;
				}

				return new ControllerPresses(
						isJustPressed(selectBinding, controller.get()),
						isJustPressed(quickMoveBinding, controller.get())
				);
			} catch (ReflectiveOperationException | ClassCastException exception) {
				unavailable = true;
				return ControllerPresses.NONE;
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
				selectBinding = staticField(bindingsClass, "INV_SELECT");
				quickMoveBinding = staticField(bindingsClass, "INV_QUICK_MOVE");
				bindingOn = methodNamed(selectBinding.getClass(), "on", 1);
				justPressed = methodNamed(bindingOn.getReturnType(), "justPressed", 0);
				return true;
			} catch (ReflectiveOperationException | LinkageError exception) {
				unavailable = true;
				return false;
			}
		}

		private boolean isJustPressed(Object binding, Object controller) throws ReflectiveOperationException {
			Object controllerBinding = bindingOn.invoke(binding, controller);
			return Boolean.TRUE.equals(justPressed.invoke(controllerBinding));
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
