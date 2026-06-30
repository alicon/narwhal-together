package dev.alicon.mushroomyorkie.entity;

import java.util.EnumSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

final class NightStirGoal extends Goal {
	private final MushroomYorkieEntity yorkie;
	private int nextMoveTick;

	NightStirGoal(MushroomYorkieEntity yorkie) {
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
