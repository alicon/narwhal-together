package dev.alicon.mushroomyorkie.entity;

import dev.alicon.mushroomyorkie.MushroomTheYorkie;
import dev.alicon.mushroomyorkie.item.ModItems;
import dev.alicon.mushroomyorkie.pet.FlightTrickPolicy;
import dev.alicon.mushroomyorkie.pet.PetNeeds;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariant;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariants;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Tameable Yorkie companion entity that adapts Minecraft events to Mushroom's pet state. */
public final class MushroomYorkieEntity extends net.minecraft.world.entity.TamableAnimal {
	private static final float TINY_DOG_PITCH = 1.35F;
	private static final int NIGHT_START = 13_000;
	private static final int NIGHT_END = 23_000;
	private static final int NEEDS_INTERVAL_TICKS = 200;
	private static final int BARK_INTERVAL_TICKS = 100;
	private static final int DOOR_SEARCH_RADIUS = 14;
	private static final int NIGHT_WAKE_TICKS = 20 * 20;
	private static final int DOUBLE_CLICK_TICKS = 8;
	private static final double PEACEFUL_MOB_SEARCH_RADIUS = 10.0D;

	/** Synced flight trick value for no trick. */
	public static final int FLIGHT_TRICK_NONE = 0;
	/** Synced flight trick value for a barrel roll. */
	public static final int FLIGHT_TRICK_BARREL_ROLL = 1;
	/** Synced flight trick value for a loop-de-loop. */
	public static final int FLIGHT_TRICK_LOOP = 2;
	/** Number of ticks a visual flight trick lasts. */
	public static final int FLIGHT_TRICK_DURATION_TICKS = 36;

	private static final EntityDataAccessor<Integer> DATA_FLIGHT_TRICK_TYPE = SynchedEntityData.defineId(MushroomYorkieEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_FLIGHT_TRICK_TICKS = SynchedEntityData.defineId(MushroomYorkieEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> DATA_SLEEPING = SynchedEntityData.defineId(MushroomYorkieEntity.class, EntityDataSerializers.BOOLEAN);
	private static final String HUNGER_KEY = "Hunger";
	private static final String POTTY_KEY = "Potty";
	private static final String MOOD_KEY = "Mood";
	private static final String ENERGY_KEY = "Energy";
	private static final String NIGHT_WAKE_TICKS_KEY = "NightWakeTicks";
	private static final String PEACEFUL_MOB_BARK_MUTED_DAY_KEY = "PeacefulMobBarkMutedDay";
	private static final double CREATIVE_FLIGHT_FOLLOW_DISTANCE_SQ = 6.25D;
	private static final double CREATIVE_FLIGHT_SPEED = 0.22D;

	private PetNeeds needs = new PetNeeds();
	private int nightWakeTicks;
	private int lastInteractTick = -DOUBLE_CLICK_TICKS;
	private UUID lastInteractPlayer;
	private boolean ownerHadTreat;
	private boolean treatBarkSuppressed;
	private long peacefulMobBarkMutedDay = -1L;

	/**
	 * Creates a Mushroom Yorkie entity instance.
	 *
	 * @param entityType registered Yorkie entity type
	 * @param level world the entity belongs to
	 */
	public MushroomYorkieEntity(EntityType<? extends MushroomYorkieEntity> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_FLIGHT_TRICK_TYPE, FLIGHT_TRICK_NONE);
		builder.define(DATA_FLIGHT_TRICK_TICKS, 0);
		builder.define(DATA_SLEEPING, false);
	}

	/**
	 * Defines baseline attributes for the tiny companion entity.
	 *
	 * @return attribute builder registered during mod initialization
	 */
	public static AttributeSupplier.Builder createAttributes() {
		return Animal.createAnimalAttributes()
				.add(Attributes.MAX_HEALTH, 12.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.34D)
				.add(Attributes.FOLLOW_RANGE, 24.0D)
				.add(Attributes.STEP_HEIGHT, 1.0D);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new SleepAtNightGoal(this));
		this.goalSelector.addGoal(2, new NightStirGoal(this));
		this.goalSelector.addGoal(3, new IndoorPottyWarningGoal(this));
		this.goalSelector.addGoal(4, new BarkAtPeacefulMobsGoal(this));
		this.goalSelector.addGoal(5, new SitWhenOrderedToGoal(this));
		this.goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.25D, 2.0F, 1.0F));
		this.goalSelector.addGoal(7, new RandomStrollGoal(this, 0.9D, 80));
		this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
	}

	@Override
	public boolean isFood(ItemStack stack) {
		return stack.is(ModItems.YORKIE_TREAT);
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

			if (this.isFood(stack)) {
				if (!this.level().isClientSide()) {
					if (!this.isTame()) {
						if (MushroomTheYorkie.oneMushroomPerPlayer() && hasLoadedMushroomOwnedBy((ServerLevel) this.level(), player)) {
							player.displayClientMessage(Component.translatable("message.mushroom_yorkie.one_only"), true);
							return InteractionResult.SUCCESS;
						}

						this.tame(player);
						this.setOrderedToSit(false);
					this.level().broadcastEntityEvent(this, (byte) 7);
					player.displayClientMessage(Component.translatable("message.mushroom_yorkie.tamed"), true);
				}

				this.feedTreat(player, hand, stack);
			}

			return InteractionResult.SUCCESS;
		}

		if (this.isTame() && this.isOwnedBy(player) && stack.isEmpty()) {
			if (!this.level().isClientSide()) {
				if (this.isMushroomSleeping()) {
					this.handleSleepingInteract(player);
					return InteractionResult.SUCCESS;
				}

				this.setOrderedToSit(!this.isOrderedToSit());
				player.displayClientMessage(
						Component.translatable(this.isOrderedToSit()
								? "message.mushroom_yorkie.sit"
								: "message.mushroom_yorkie.follow"),
						true
				);
			}

			return InteractionResult.SUCCESS;
		}

		return super.mobInteract(player, hand);
	}

	private void feedTreat(Player player, InteractionHand hand, ItemStack stack) {
		this.usePlayerItem(player, hand, stack);
		this.needs.feedTreat();
		this.treatBarkSuppressed = true;
		this.heal(3.0F);
		this.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5F, 1.6F);
		this.performTreatTrick();
	}

	private void performTreatTrick() {
		int trick = this.random.nextInt(4);
		switch (trick) {
			case 0 -> this.playSound(cuteWolfSounds().pantSound().value(), 0.45F, 1.45F);
			case 1 -> this.playSound(cuteWolfSounds().pantSound().value(), 0.5F, 1.45F);
			case 2 -> this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.28D, 0.0D));
			default -> this.playSound(cuteWolfSounds().growlSound().value(), 0.45F, 1.55F);
		}

		if (this.level() instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 0.5D, this.getZ(), 4, 0.25D, 0.2D, 0.25D, 0.0D);
		}
	}

	private void handleSleepingInteract(Player player) {
		boolean doubleClick = this.lastInteractPlayer != null
				&& this.lastInteractPlayer.equals(player.getUUID())
				&& this.tickCount - this.lastInteractTick <= DOUBLE_CLICK_TICKS;
		this.lastInteractPlayer = player.getUUID();
		this.lastInteractTick = this.tickCount;

		if (doubleClick) {
			this.nightWakeTicks = NIGHT_WAKE_TICKS;
			this.setSleeping(false);
			this.playSound(cuteWolfSounds().pantSound().value(), 0.35F, 1.45F);
		}
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		this.followFlyingOwner();
		this.tickNightBehavior(level);
		this.tickTreatBark(level);

		if (this.tickCount % NEEDS_INTERVAL_TICKS != 0) {
			return;
		}

		boolean outside = level.canSeeSky(this.blockPosition());
		this.needs.tickNeeds(outside, this.isOrderedToSit());
	}

	@Override
	protected boolean canFlyToOwner() {
		return this.ownerIsCreativeFlying();
	}

	private void tickNightBehavior(ServerLevel level) {
		boolean sleepingAtNight = this.shouldSleepAtNight(level);
		if (!sleepingAtNight) {
			this.setSleeping(false);
			if (!isNight(level)) {
				this.nightWakeTicks = 0;
			}
			return;
		}

		if (this.nightWakeTicks > 0) {
			this.nightWakeTicks--;
			this.setSleeping(false);
			return;
		}

		this.setSleeping(true);
	}

	private void tickTreatBark(ServerLevel level) {
		LivingEntity owner = this.getOwner();
		boolean ownerHasTreat = this.isTame() && owner != null && owner.isHolding(ModItems.YORKIE_TREAT);
		if (!ownerHasTreat) {
			this.ownerHadTreat = false;
			return;
		}

		if (!this.ownerHadTreat) {
			this.treatBarkSuppressed = false;
		}

		this.ownerHadTreat = true;
		if (!this.treatBarkSuppressed && !this.isMushroomSleeping() && this.tickCount % BARK_INTERVAL_TICKS == 0) {
			this.bark();
		}
	}

	private boolean shouldSleepAtNight(ServerLevel level) {
		return this.isTame() && !this.ownerIsCreativeFlying() && isNight(level) && this.isInside(level);
	}

	private boolean shouldAskToGoOutside(ServerLevel level) {
		return this.isTame()
				&& !this.ownerIsCreativeFlying()
				&& !this.shouldSleepAtNight(level)
				&& this.isInside(level)
				&& this.needs.shouldWarnPotty();
	}

	private boolean isInside(ServerLevel level) {
		return !level.canSeeSky(this.blockPosition());
	}

	private boolean isMushroomSleeping() {
		return this.entityData.get(DATA_SLEEPING);
	}

	private void setSleeping(boolean sleeping) {
		this.entityData.set(DATA_SLEEPING, sleeping);
		this.setInSittingPose(sleeping);
		if (sleeping) {
			this.getNavigation().stop();
			if (this.level() instanceof ServerLevel level) {
				BlockPos doorPos = findNearestDoor(level, this.blockPosition());
				if (doorPos != null) {
					this.facePosition(Vec3.atBottomCenterOf(doorPos));
				}
			}
		}
	}

	public boolean isCurledUpSleeping() {
		return this.isMushroomSleeping();
	}

	public static boolean hasLoadedMushroomOwnedBy(ServerLevel level, Player player) {
		return !level.getEntities(
				EntityTypeTest.forClass(MushroomYorkieEntity.class),
				yorkie -> yorkie.isOwnedBy(player)
		).isEmpty();
	}

	private static boolean isNight(ServerLevel level) {
		long dayTime = level.getDayTime() % 24_000L;
		return dayTime >= NIGHT_START && dayTime <= NIGHT_END;
	}

	private static long currentDay(ServerLevel level) {
		return level.getDayTime() / 24_000L;
	}

	private static boolean isDoorBottom(BlockState state) {
		return DoorBlock.isWoodenDoor(state) && state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER;
	}

	private boolean peacefulMobBarkingMutedToday(ServerLevel level) {
		return this.peacefulMobBarkMutedDay == currentDay(level);
	}

	private void bark() {
		this.playSound(cuteWolfSounds().ambientSound().value(), 0.5F, 1.45F);
	}

	private void facePosition(Vec3 target) {
		Vec3 delta = target.subtract(this.position());
		if (delta.horizontalDistanceSqr() < 1.0E-4D) {
			return;
		}

		float yaw = (float) (Math.atan2(delta.z, delta.x) * 180.0D / Math.PI) - 90.0F;
		this.setYRot(yaw);
		this.yBodyRot = yaw;
		this.yHeadRot = yaw;
		this.yRotO = yaw;
	}

	private static BlockPos findNearestDoor(ServerLevel level, BlockPos origin) {
		BlockPos closest = null;
		double closestDistance = Double.MAX_VALUE;
		for (BlockPos pos : BlockPos.withinManhattan(origin, DOOR_SEARCH_RADIUS, 3, DOOR_SEARCH_RADIUS)) {
			if (!isDoorBottom(level.getBlockState(pos))) {
				continue;
			}

			double distance = pos.distSqr(origin);
			if (distance < closestDistance) {
				closest = pos.immutable();
				closestDistance = distance;
			}
		}

		return closest;
	}

	private void followFlyingOwner() {
		boolean followingFlyingOwner = !this.isOrderedToSit() && this.ownerIsCreativeFlying();
		this.setNoGravity(followingFlyingOwner);
		this.updateFlightTrick(followingFlyingOwner);
		if (!followingFlyingOwner) {
			return;
		}

		LivingEntity owner = this.getOwner();
		if (owner == null || owner.level() != this.level()) {
			return;
		}

		this.fallDistance = 0.0F;
		Vec3 target = owner.position()
				.add(owner.getLookAngle().scale(-1.4D))
				.add(0.0D, -0.8D, 0.0D);
		Vec3 delta = target.subtract(this.position());
		this.faceFlightTarget(delta);
		if (delta.lengthSqr() <= CREATIVE_FLIGHT_FOLLOW_DISTANCE_SQ) {
			this.setDeltaMovement(this.getDeltaMovement().scale(0.6D));
			return;
		}

		this.getNavigation().stop();
		Vec3 movement = delta.normalize().scale(CREATIVE_FLIGHT_SPEED);
		this.setDeltaMovement(movement);
		this.hurtMarked = true;
	}

	private void faceFlightTarget(Vec3 delta) {
		if (delta.horizontalDistanceSqr() < 1.0E-4D) {
			return;
		}

		float yaw = (float) (Math.atan2(delta.z, delta.x) * 180.0D / Math.PI) - 90.0F;
		this.setYRot(yaw);
		this.yBodyRot = yaw;
		this.yHeadRot = yaw;
		this.yRotO = yaw;
	}

	private void updateFlightTrick(boolean followingFlyingOwner) {
		if (!followingFlyingOwner) {
			this.setFlightTrick(FLIGHT_TRICK_NONE, 0);
			return;
		}

		int ticks = this.getFlightTrickTicks();
		if (ticks > 0) {
			this.entityData.set(DATA_FLIGHT_TRICK_TICKS, ticks - 1);
			return;
		}

		this.setFlightTrick(FLIGHT_TRICK_NONE, 0);
		boolean recentlyWalked = this.level().canSeeSky(this.blockPosition()) || this.needs.potty() < 60;
		if (this.random.nextDouble() < FlightTrickPolicy.trickChance(this.needs, recentlyWalked)) {
			int trickType = this.random.nextBoolean() ? FLIGHT_TRICK_BARREL_ROLL : FLIGHT_TRICK_LOOP;
			this.setFlightTrick(trickType, FLIGHT_TRICK_DURATION_TICKS);
		}
	}

	private void setFlightTrick(int type, int ticks) {
		this.entityData.set(DATA_FLIGHT_TRICK_TYPE, type);
		this.entityData.set(DATA_FLIGHT_TRICK_TICKS, ticks);
	}

	/**
	 * Current synced flight trick type.
	 *
	 * @return one of the `FLIGHT_TRICK_*` constants
	 */
	public int getFlightTrickType() {
		return this.entityData.get(DATA_FLIGHT_TRICK_TYPE);
	}

	/**
	 * Remaining synced ticks for the current flight trick.
	 *
	 * @return remaining trick ticks, or 0 when no trick is active
	 */
	public int getFlightTrickTicks() {
		return this.entityData.get(DATA_FLIGHT_TRICK_TICKS);
	}

	private boolean ownerIsCreativeFlying() {
		LivingEntity owner = this.getOwner();
		return owner instanceof Player player && player.isCreative() && player.getAbilities().flying;
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt(HUNGER_KEY, this.needs.hunger());
		output.putInt(POTTY_KEY, this.needs.potty());
		output.putInt(MOOD_KEY, this.needs.mood());
		output.putInt(ENERGY_KEY, this.needs.energy());
		output.putInt(NIGHT_WAKE_TICKS_KEY, this.nightWakeTicks);
		output.putLong(PEACEFUL_MOB_BARK_MUTED_DAY_KEY, this.peacefulMobBarkMutedDay);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
			this.needs = new PetNeeds(
					input.getIntOr(HUNGER_KEY, PetNeeds.DEFAULT_HUNGER),
					input.getIntOr(POTTY_KEY, PetNeeds.DEFAULT_POTTY),
					input.getIntOr(MOOD_KEY, PetNeeds.DEFAULT_MOOD),
					input.getIntOr(ENERGY_KEY, PetNeeds.DEFAULT_ENERGY)
			);
			this.nightWakeTicks = input.getIntOr(NIGHT_WAKE_TICKS_KEY, 0);
			this.peacefulMobBarkMutedDay = input.getLongOr(PEACEFUL_MOB_BARK_MUTED_DAY_KEY, -1L);
	}

	@Override
	protected net.minecraft.sounds.SoundEvent getAmbientSound() {
		return null;
	}

	@Override
	protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource damageSource) {
		return cuteWolfSounds().hurtSound().value();
	}

	@Override
	protected net.minecraft.sounds.SoundEvent getDeathSound() {
		return cuteWolfSounds().deathSound().value();
	}

	@Override
	public float getVoicePitch() {
		return TINY_DOG_PITCH;
	}

	@Override
	public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
		return null;
	}

	private static final class SleepAtNightGoal extends net.minecraft.world.entity.ai.goal.Goal {
		private final MushroomYorkieEntity yorkie;

		private SleepAtNightGoal(MushroomYorkieEntity yorkie) {
			this.yorkie = yorkie;
			this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			return this.yorkie.level() instanceof ServerLevel level
					&& this.yorkie.shouldSleepAtNight(level)
					&& this.yorkie.nightWakeTicks <= 0;
		}

		@Override
		public boolean canContinueToUse() {
			return this.canUse();
		}

		@Override
		public void start() {
			this.yorkie.setSleeping(true);
		}

		@Override
		public void tick() {
			this.yorkie.getNavigation().stop();
			this.yorkie.setDeltaMovement(this.yorkie.getDeltaMovement().scale(0.3D));
		}
	}

	private static final class NightStirGoal extends net.minecraft.world.entity.ai.goal.Goal {
		private final MushroomYorkieEntity yorkie;
		private int nextMoveTick;

		private NightStirGoal(MushroomYorkieEntity yorkie) {
			this.yorkie = yorkie;
			this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			return this.yorkie.level() instanceof ServerLevel level
					&& this.yorkie.shouldSleepAtNight(level)
					&& this.yorkie.nightWakeTicks > 0;
		}

		@Override
		public boolean canContinueToUse() {
			return this.canUse();
		}

		@Override
		public void start() {
			this.nextMoveTick = 0;
			this.yorkie.setSleeping(false);
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public void tick() {
			LivingEntity owner = this.yorkie.getOwner();
			if (owner != null) {
				this.yorkie.getLookControl().setLookAt(owner, 10.0F, this.yorkie.getMaxHeadXRot());
			}

			if (this.nextMoveTick-- > 0 && !this.yorkie.getNavigation().isDone()) {
				return;
			}

			Vec3 center = owner == null ? this.yorkie.position() : owner.position();
			double angle = (this.yorkie.tickCount % 120) * (Math.PI * 2.0D / 120.0D);
			double radius = owner == null ? 1.4D : 2.0D;
			this.yorkie.getNavigation().moveTo(
					center.x + Math.cos(angle) * radius,
					center.y,
					center.z + Math.sin(angle) * radius,
					0.35D
			);
			this.nextMoveTick = 55;
		}
	}

	private static final class BarkAtPeacefulMobsGoal extends net.minecraft.world.entity.ai.goal.Goal {
		private final MushroomYorkieEntity yorkie;
		private Animal target;
		private int nextSearchTick;

		private BarkAtPeacefulMobsGoal(MushroomYorkieEntity yorkie) {
			this.yorkie = yorkie;
			this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			if (!(this.yorkie.level() instanceof ServerLevel level) || !this.canChase(level)) {
				return false;
			}

			this.target = this.findTarget(level);
			return this.target != null;
		}

		@Override
		public boolean canContinueToUse() {
			if (!(this.yorkie.level() instanceof ServerLevel level) || !this.canChase(level)) {
				return false;
			}

			return this.target != null && this.target.isAlive() && this.yorkie.distanceToSqr(this.target) < 196.0D;
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public void stop() {
			this.target = null;
			this.yorkie.getNavigation().stop();
		}

			@Override
			public void tick() {
				if (this.target == null) {
					return;
				}

				LivingEntity owner = this.yorkie.getOwner();
				if (owner != null && owner.isHolding(ModItems.YORKIE_TREAT)) {
					this.yorkie.peacefulMobBarkMutedDay = currentDay((ServerLevel) this.yorkie.level());
					this.yorkie.getNavigation().moveTo(owner, 1.25D);
					return;
				}

				this.yorkie.getLookControl().setLookAt(this.target, 10.0F, this.yorkie.getMaxHeadXRot());
				this.yorkie.getNavigation().moveTo(this.target, 1.15D);
				if (this.yorkie.tickCount % BARK_INTERVAL_TICKS == 0) {
				this.yorkie.bark();
			}
		}

			private boolean canChase(ServerLevel level) {
				LivingEntity owner = this.yorkie.getOwner();
				return this.yorkie.isTame()
						&& !this.yorkie.isOrderedToSit()
						&& !this.yorkie.isMushroomSleeping()
						&& !this.yorkie.shouldAskToGoOutside(level)
						&& !this.yorkie.peacefulMobBarkingMutedToday(level)
					&& (owner == null || !owner.isHolding(ModItems.YORKIE_TREAT));
		}

		private Animal findTarget(ServerLevel level) {
			if (this.nextSearchTick-- > 0 && this.target != null && this.target.isAlive()) {
				return this.target;
			}

			this.nextSearchTick = 40;
			AABB area = this.yorkie.getBoundingBox().inflate(PEACEFUL_MOB_SEARCH_RADIUS, 4.0D, PEACEFUL_MOB_SEARCH_RADIUS);
			List<Animal> animals = level.getEntitiesOfClass(Animal.class, area, animal -> animal != this.yorkie && animal.isAlive());
			Animal closest = null;
			double closestDistance = Double.MAX_VALUE;
			for (Animal animal : animals) {
				double distance = this.yorkie.distanceToSqr(animal);
				if (distance < closestDistance) {
					closest = animal;
					closestDistance = distance;
				}
			}

			return closest;
		}
	}

	private static final class IndoorPottyWarningGoal extends net.minecraft.world.entity.ai.goal.Goal {
		private final MushroomYorkieEntity yorkie;
		private BlockPos doorPos;
		private boolean movingToDoor;
		private int nextMoveTick;

		private IndoorPottyWarningGoal(MushroomYorkieEntity yorkie) {
			this.yorkie = yorkie;
			this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			return this.yorkie.level() instanceof ServerLevel level && this.yorkie.shouldAskToGoOutside(level);
		}

		@Override
		public boolean canContinueToUse() {
			return this.canUse();
		}

		@Override
		public void start() {
			this.doorPos = this.findNearestDoor();
			this.movingToDoor = this.doorPos != null;
			this.nextMoveTick = 0;
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public void tick() {
			LivingEntity owner = this.yorkie.getOwner();
			if (owner != null) {
				this.yorkie.getLookControl().setLookAt(owner, 10.0F, this.yorkie.getMaxHeadXRot());
			}

				if (this.yorkie.tickCount % BARK_INTERVAL_TICKS == 0) {
					this.yorkie.bark();
				}

			if (this.nextMoveTick-- > 0 && !this.yorkie.getNavigation().isDone()) {
				return;
			}

			if (this.doorPos == null || this.yorkie.tickCount % (NEEDS_INTERVAL_TICKS * 3) == 0) {
				this.doorPos = this.findNearestDoor();
			}

			Vec3 target = this.nextTarget(owner);
			this.yorkie.getNavigation().moveTo(target.x, target.y, target.z, 1.0D);
			this.nextMoveTick = 45;
		}

		private Vec3 nextTarget(LivingEntity owner) {
			if (this.doorPos == null) {
				Vec3 center = owner == null ? this.yorkie.position() : owner.position();
				double angle = (this.yorkie.tickCount % 100) * (Math.PI * 2.0D / 100.0D);
				return center.add(Math.cos(angle) * 1.8D, 0.0D, Math.sin(angle) * 1.8D);
			}

			this.movingToDoor = !this.movingToDoor;
			if (this.movingToDoor || owner == null) {
				return Vec3.atBottomCenterOf(this.doorPos);
			}

			return owner.position().add(this.yorkie.random.nextDouble() - 0.5D, 0.0D, this.yorkie.random.nextDouble() - 0.5D);
		}

			private BlockPos findNearestDoor() {
				if (!(this.yorkie.level() instanceof ServerLevel level)) {
					return null;
				}

				return MushroomYorkieEntity.findNearestDoor(level, this.yorkie.blockPosition());
			}
		}

	private static WolfSoundVariant cuteWolfSounds() {
		return SoundEvents.WOLF_SOUNDS.get(WolfSoundVariants.SoundSet.CUTE);
	}
}
