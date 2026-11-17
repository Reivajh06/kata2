package reivajh06.kata2.model;

import java.time.LocalDate;

public record Mob(
		int id,
		String name,
		BehaviorType behaviorType,
		SpawnBehavior spawnBehavior,
		int healthPoints,
		int maxDamage,
		LocalDate debutDate,
		String version,
		ReproductiveRequirement reproductiveRequirement
) {
}
