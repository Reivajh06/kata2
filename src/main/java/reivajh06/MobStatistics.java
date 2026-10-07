package reivajh06;

import reivajh06.model.BehaviorType;
import reivajh06.model.Mob;

import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MobStatistics {

	private final List<Mob> mobs;

	public MobStatistics(List<Mob> mobs) {
		this.mobs = mobs;
	}

	public Map<BehaviorType, List<String>> groupByBehavior() {
		Map<BehaviorType, List<String>> groups = new HashMap<>();

		for(Mob mob : mobs) {
			BehaviorType behavior = mob.behaviorType();

			groups.computeIfAbsent(behavior, _ -> new ArrayList<>());
			groups.get(behavior).add(mob.name());
		}

		return groups;
	}

	public Map<Year, List<String>> groupByDebutYear() {
		Map<Year, List<String>> groups = new HashMap<>();

		for(Mob mob : mobs) {
			LocalDate debutDate = mob.debutDate();

			groups.computeIfAbsent(Year.of(debutDate.getYear()), _ -> new ArrayList<>());
			groups.get(Year.of(debutDate.getYear())).add(mob.name());
		}

		return groups;
	}

	public Map<String, List<String>> groupByVersion() {
		Map<String, List<String>> groups = new HashMap<>();

		for(Mob mob : mobs) {
			String version = mob.minecraftVersion();

			groups.computeIfAbsent(version, _ -> new ArrayList<>());
			groups.get(version).add(mob.name());
		}

		return groups;
	}
}
