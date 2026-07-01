package dev.alicon.copsrobbers;

import dev.alicon.copsrobbers.entity.PoliceCruiserEntity;
import dev.alicon.copsrobbers.entity.BankRobberEntity;
import dev.alicon.copsrobbers.entity.CopEntity;
import dev.alicon.copsrobbers.entity.ModEntities;
import dev.alicon.copsrobbers.entity.TellerEntity;
import dev.alicon.copsrobbers.entity.FiremanEntity;
import dev.alicon.copsrobbers.entity.FireTruckEntity;
import dev.alicon.copsrobbers.item.ModCreativeTabs;
import dev.alicon.copsrobbers.item.ModItems;
import dev.alicon.copsrobbers.spawn.BankRobberSpawner;
import dev.alicon.copsrobbers.world.LongmontPatrolNeighborhood;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Entrypoint and shared identifiers for the Cops and Robbers mod. */
public final class CopsAndRobbers implements ModInitializer {
	/** Fabric mod id used for registries, assets, and translations. */
	public static final String MOD_ID = "cops_robbers";
	/** Logger scoped to the Cops and Robbers mod id. */
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.playC2S().register(ToggleCruiserLightsPayload.TYPE, ToggleCruiserLightsPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(ToggleCruiserSirenPayload.TYPE, ToggleCruiserSirenPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(ToggleCruiserFlightPayload.TYPE, ToggleCruiserFlightPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(UpdateCruiserFlightInputPayload.TYPE, UpdateCruiserFlightInputPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(TriggerCruiserBarrelRollPayload.TYPE, TriggerCruiserBarrelRollPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(TriggerCruiserLoopPayload.TYPE, TriggerCruiserLoopPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(ToggleCruiserLightsPayload.TYPE, (payload, context) ->
				PoliceCruiserEntity.toggleLightsForDriver(context.player()));
		ServerPlayNetworking.registerGlobalReceiver(ToggleCruiserSirenPayload.TYPE, (payload, context) ->
				PoliceCruiserEntity.toggleSirenForDriver(context.player()));
		ServerPlayNetworking.registerGlobalReceiver(ToggleCruiserFlightPayload.TYPE, (payload, context) ->
				PoliceCruiserEntity.toggleFlightForDriver(context.player()));
		ServerPlayNetworking.registerGlobalReceiver(UpdateCruiserFlightInputPayload.TYPE, (payload, context) ->
				PoliceCruiserEntity.updateFlightInputForDriver(context.player(), payload.lift()));
		ServerPlayNetworking.registerGlobalReceiver(TriggerCruiserBarrelRollPayload.TYPE, (payload, context) ->
				PoliceCruiserEntity.triggerBarrelRollForDriver(context.player()));
		ServerPlayNetworking.registerGlobalReceiver(TriggerCruiserLoopPayload.TYPE, (payload, context) ->
				PoliceCruiserEntity.triggerLoopForDriver(context.player()));

		ModEntities.initialize();
		ModItems.initialize();
		ModCreativeTabs.initialize();
		FabricDefaultAttributeRegistry.register(ModEntities.POLICE_CRUISER, PoliceCruiserEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(ModEntities.FIRE_TRUCK, FireTruckEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(ModEntities.BANK_ROBBER, BankRobberEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(ModEntities.TELLER, TellerEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(ModEntities.COP, CopEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(ModEntities.FIREMAN, FiremanEntity.createAttributes());
		BankRobberSpawner.register();
		LongmontPatrolNeighborhood.register();

		LOGGER.info("Cops and Robbers initialized");
	}

	/**
	 * Creates an identifier in this mod's namespace.
	 *
	 * @param path resource path inside the `cops_robbers` namespace
	 * @return namespaced Minecraft identifier
	 */
	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
