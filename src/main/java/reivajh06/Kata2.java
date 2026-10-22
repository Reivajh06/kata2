package reivajh06;

import reivajh06.loaders.Loader;
import reivajh06.loaders.URLMobLoader;
import reivajh06.model.BehaviorType;
import reivajh06.model.Mob;

import java.util.Map;
import java.util.TreeMap;

public class Kata2 {

	static void main() {
		Loader loader = new URLMobLoader("https://raw.githubusercontent.com/Reivajh06/kata2/develop/src/main/resources/minecraft_mobs.csv");

		MobParser mobParser = new MobParser(",");

		ParseResult parseResult = mobParser.parseAllLines(loader.load());

		MobsAnalyzer analyzer = new MobsAnalyzer(parseResult.mobs());

		Map<BehaviorType, Integer> frequencies = new TreeMap<>(analyzer.groupFrequencies(analyzer.groupBy(Mob::behaviorType)));

		MobWriter writer = new MobWriter(System.out);

		writer.write("Mobs loaded: %d%n".formatted(parseResult.mobs().size()))
				.write("Mobs by behavior type: %s%n".formatted(frequencies));

		for(ParseError error : parseResult.parsingErrors()) {
			writer.write(error.errorMessage() + System.lineSeparator());
		}
	}
}
