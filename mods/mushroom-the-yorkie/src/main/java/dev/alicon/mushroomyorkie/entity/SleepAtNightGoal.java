package dev.alicon.mushroomyorkie.entity;

import java.util.EnumSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;

final class SleepAtNightGoal extends Goal {
	private final MushroomYorkieEntity yorkie;

	SleepAtNightGoal(MushroomYorkieEntity yorkie) {
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
