package dev.alicon.narwhaltogether;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

final class TeleportTargetSelector {
	private TeleportTargetSelector() {
	}

	static <T> T nextTarget(UUID lastTarget, List<T> targets, Function<T, UUID> targetId) {
		if (targets.isEmpty()) {
			throw new IllegalArgumentException("targets must not be empty");
		}

		for (int index = 0; index < targets.size(); index++) {
			if (targetId.apply(targets.get(index)).equals(lastTarget)) {
				return targets.get((index + 1) % targets.size());
			}
		}

		return targets.getFirst();
	}
}
