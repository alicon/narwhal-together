package dev.alicon.copsrobbers.entity;

import dev.alicon.copsrobbers.CopsAndRobbers;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.phys.Vec3;

/** Registry holder for Cops and Robbers entity types. */
public final class ModEntities {
	/** Rideable low-poly police truck entity type. */
	public static final EntityType<PoliceCruiserEntity> POLICE_CRUISER = register(
			"police_cruiser",
			EntityType.Builder.of(PoliceCruiserEntity::new, MobCategory.MISC)
					.sized(2.05F, 1.65F)
					.passengerAttachments(new Vec3(0.0D, 0.72D, 0.55D))
					.eyeHeight(1.2F)
					.clientTrackingRange(10)
					.updateInterval(3)
	);
	/** Rideable fire truck with a water cannon. */
	public static final EntityType<FireTruckEntity> FIRE_TRUCK = register(
			"fire_truck",
			EntityType.Builder.of(FireTruckEntity::new, MobCategory.MISC)
					.sized(2.55F, 1.9F)
					.passengerAttachments(new Vec3(0.0D, 0.88D, 0.55D))
					.eyeHeight(1.35F)
					.clientTrackingRange(10)
					.updateInterval(3)
	);
	/** Village-looting robber mob entity type. */
	public static final EntityType<BankRobberEntity> BANK_ROBBER = register(
			"bank_robber",
			EntityType.Builder.of(BankRobberEntity::new, MobCategory.MONSTER)
					.sized(0.6F, 1.95F)
					.eyeHeight(1.74F)
					.clientTrackingRange(8)
					.updateInterval(3)
	);
	/** Passive bank teller NPC entity type. */
	public static final EntityType<TellerEntity> TELLER = register(
			"teller",
			EntityType.Builder.of(TellerEntity::new, MobCategory.CREATURE)
					.sized(0.6F, 1.95F)
					.eyeHeight(1.74F)
					.clientTrackingRange(8)
					.updateInterval(3)
	);
	/** Foot patrol cop NPC entity type. */
	public static final EntityType<CopEntity> COP = register(
			"cop",
			EntityType.Builder.of(CopEntity::new, MobCategory.CREATURE)
					.sized(0.6F, 1.95F)
					.eyeHeight(1.74F)
					.clientTrackingRange(8)
					.updateInterval(3)
	);
	/** Friendly firefighter NPC entity type. */
	public static final EntityType<FiremanEntity> FIREMAN = register(
			"fireman",
			EntityType.Builder.of(FiremanEntity::new, MobCategory.CREATURE)
					.sized(0.6F, 1.95F)
					.eyeHeight(1.74F)
					.clientTrackingRange(8)
					.updateInterval(3)
	);

	private ModEntities() {
	}

	/** Loads this class so static entity registrations run during mod initialization. */
	public static void initialize() {
	}

	private static <T extends net.minecraft.world.entity.Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, CopsAndRobbers.id(name));
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
	}
}
