package reivajh06;

import reivajh06.model.BehaviorType;
import reivajh06.model.Mob;
import reivajh06.model.ReproductiveRequirement;
import reivajh06.model.SpawnBehavior;

import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MobsAnalyzer {

	private final List<Mob> mobs;

	public MobsAnalyzer(List<Mob> mobs) {
		this.mobs = mobs;
	}

	public <K> Map<K, List<String>> groupBy(Function<Mob, K> mobDetail) {
		Map<K, List<String>> groups = new HashMap<>();

		for(Mob mob : mobs) {
			groups.computeIfAbsent(mobDetail.apply(mob), _ -> new ArrayList<>());
			groups.get(mobDetail.apply(mob)).add(mob.name());
		}

		return groups;
	}

	public List<String> mobsByBehavior(BehaviorType behavior) {
		return mobs.stream()
				.filter(m -> m.behaviorType().equals(behavior))
				.map(Mob::name)
				.toList();
	}

	public List<String> mobsBySpawnBehavior(SpawnBehavior spawnBehavior) {
		return mobs.stream()
				.filter(m -> m.spawnBehavior().equals(spawnBehavior))
				.map(Mob::name)
				.toList();
	}

	public List<String> mobsByReproductiveRequirement(ReproductiveRequirement reproductiveRequirement) {
		return mobs.stream()
				.filter(m -> m.reproductiveRequirement().equals(reproductiveRequirement))
				.map(Mob::name)
				.toList();
	}

	public List<String> mobsByVersion(String version) {
		return mobs.stream()
				.filter(m -> m.version().equals(version))
				.map(Mob::name)
				.toList();
	}

	public <T> Map<T, Integer> groupFrecuencies(Map<T, List<String>> group) {
		return group.entrySet().stream()
				.map(e -> Map.entry(e.getKey(), e.getValue().size()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}
}
