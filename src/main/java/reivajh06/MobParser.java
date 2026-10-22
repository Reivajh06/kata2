package reivajh06;

import reivajh06.model.BehaviorType;
import reivajh06.model.Mob;
import reivajh06.model.ReproductiveRequirement;
import reivajh06.model.SpawnBehavior;

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
		String[] mobDetails = line.split(delimiter, -1);

		if(mobDetails.length < NFIELDS) return Optional.empty();

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

	private String parsingErrorMessage(int nLine, String line) {
		return "Parsing error at line %s: %s".formatted(nLine, line);
	}
}
