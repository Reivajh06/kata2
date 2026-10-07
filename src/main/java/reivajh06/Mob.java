package reivajh06;

import java.time.LocalDate;

public record Mob(
		int id,
		String name,
		BehaviorType behaviorType,
		SpawnBehavior spawnBehavior,
		int healthPoints,
		int maxDamage,
		LocalDate debutDate,
		String minecraftVersion,
		ReproductiveRequirement reproductiveRequirement
) {
}
