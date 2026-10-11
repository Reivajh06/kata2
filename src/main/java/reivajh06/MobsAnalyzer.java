package reivajh06;

import reivajh06.model.Mob;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
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

	public List<String> filterMobs(Predicate<Mob> mobFilter) {
		return mobs.stream()
				.filter(mobFilter)
				.map(Mob::name)
				.toList();
	}

	public <T> Map<T, Integer> groupFrequencies(Map<T, List<String>> group) {
		return group.entrySet().stream()
				.map(e -> Map.entry(e.getKey(), e.getValue().size()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}
}
