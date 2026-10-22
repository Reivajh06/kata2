package reivajh06;

import reivajh06.model.BehaviorType;
import reivajh06.model.Mob;
import reivajh06.model.ReproductiveRequirement;
import reivajh06.model.SpawnBehavior;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MobParser {

	private static final int NFIELDS = 9;

	private final String delimiter;

	public MobParser(String delimiter) {
		this.delimiter = delimiter;
	}

	public ParseResult parseAllLines(List<String> lines) {
		List<Mob> mobs = new ArrayList<>();
		List<ParseError> errors = new ArrayList<>();

		for(int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			int nLine = i + 1;

			if(line.isEmpty()) continue;

			Optional<Mob> parseResult = parseLine(line);

			if(parseResult.isEmpty()) {
				errors.add(new ParseError(nLine, line, parsingErrorMessage(nLine, line)));
				continue;
			}

			mobs.add(parseResult.get());
		}

		return new ParseResult(mobs, errors);
	}

	public Optional<Mob> parseLine(String line) {
		int id;
		String name;
		BehaviorType behaviorType;
		SpawnBehavior spawnBehavior;
		int healthPoints;
		int maxDamage;
		LocalDate debutDate;
		String version;
		ReproductiveRequirement reproductiveRequirement;

		try {
			String[] mobDetails = line.split(delimiter, -1);

			if (mobDetails.length < NFIELDS) return Optional.empty();

			id = Integer.parseInt(mobDetails[0]);

			name = mobDetails[1].trim();
			behaviorType = parseBehavior(mobDetails[2]);
			spawnBehavior = parseSpawnBehavior(mobDetails[3].trim().replace(" ", "_"));

			healthPoints = Integer.parseInt(mobDetails[4].trim());
			maxDamage = mobDetails[5].isBlank() ? 0 : Integer.parseInt(mobDetails[5].trim());

			debutDate = parseDebutDate(mobDetails[6]);

			version = mobDetails[7].trim();

			reproductiveRequirement = mobDetails[8].isBlank() ?
					ReproductiveRequirement.NONE
					: parseReproductiveRequirement(mobDetails[8].trim().replace(" ", "_"));

		} catch(Exception e) {
			return Optional.empty();
		}

		return Optional.of(new Mob(
				id,
				name,
				behaviorType,
				spawnBehavior,
				healthPoints,
				maxDamage,
				debutDate,
				version,
				reproductiveRequirement
		));
	}

	private BehaviorType parseBehavior(String field) {
		return BehaviorType.valueOf(field.toUpperCase());
	}

	private SpawnBehavior parseSpawnBehavior(String field) {
		return SpawnBehavior.valueOf(field.toUpperCase());
	}

	private LocalDate parseDebutDate(String field) {
		String[] date = field.substring(0, 10).split("-");

		return LocalDate.of(Integer.parseInt(date[0]), Month.of(Integer.parseInt(date[1])), Integer.parseInt(date[2]));
	}

	private ReproductiveRequirement parseReproductiveRequirement(String field) {
		return ReproductiveRequirement.valueOf(field.toUpperCase());
	}

	private String parsingErrorMessage(int nLine, String line) {
		return "Parsing error at line %s: %s".formatted(nLine, line);
	}
}
