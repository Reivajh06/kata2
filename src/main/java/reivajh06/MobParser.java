package reivajh06;

import reivajh06.model.BehaviorType;
import reivajh06.model.Mob;
import reivajh06.model.ReproductiveRequirement;
import reivajh06.model.SpawnBehavior;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.List;

public class MobParser {

	public List<Mob> parseAllLines(Collection<String> lines) {
		return lines.stream()
				.map(this::parseLine)
				.toList();
	}

	public Mob parseLine(String line) {
		String[] mobDetails = line.split(",");

		int id = Integer.parseInt(mobDetails[0]);

		String name = mobDetails[1].trim();
		BehaviorType behaviorType = BehaviorType.valueOf(mobDetails[2].trim().replace(" ", "_").toUpperCase());
		SpawnBehavior spawnBehavior = SpawnBehavior.valueOf(mobDetails[3].trim().replace(" ", "_").toUpperCase());

		int healthPoints = Integer.parseInt(mobDetails[4].trim());
		int maxDamage = mobDetails[5].isBlank() ? 0 : Integer.parseInt(mobDetails[5].trim());

		String[] date = mobDetails[6].substring(0, 10).split("-");

		LocalDate debutDate = LocalDate.of(Integer.parseInt(date[0]), Month.of(Integer.parseInt(date[1])), Integer.parseInt(date[2]));

		String version = mobDetails[7].trim();

		ReproductiveRequirement reproductiveRequirement = mobDetails.length == 8 || mobDetails[8].isBlank() ?
				ReproductiveRequirement.NONE
				: ReproductiveRequirement.valueOf(mobDetails[8].trim().replace(" ", "_").toUpperCase());

		return new Mob(
				id,
				name,
				behaviorType,
				spawnBehavior,
				healthPoints,
				maxDamage,
				debutDate,
				version,
				reproductiveRequirement
		);
	}
}
