package reivajh06;

import reivajh06.loaders.Loader;
import reivajh06.model.BehaviorType;
import reivajh06.model.Mob;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

final class TestData {

	static final String HEADER = "ID,name,behaviorTypes,spawnBehavior,healthPoints,maxDamage,debutDate,minecraftVersion,reproductiveRequirement";

	static final String SMALL_CSV = HEADER + "\n" +
			"3,bat,Peaceful,Random,6,,2012-10-25T00:00:00,1.4.2,\n" +
			"4,blaze,Hostile,On Load,20,6,2011-11-18T00:00:00,1.0.0,\n" +
			"5,chicken,Peaceful,Random,4,,2011-11-18T00:00:00,1.0.0,Wheat Seeds\n" +
			"6,dolphin,Conditional,Random,10,3,2018-07-18T00:00:00,1.13.0,\n" +
			"23,creeper,Hostile,Night,20,43,2011-11-18T00:00:00,1.0.0,\n";

	static final Map<BehaviorType, Integer> SMALL_CSV_FREQUENCIES = Map.of(
			BehaviorType.PEACEFUL, 2,
			BehaviorType.HOSTILE, 2,
			BehaviorType.CONDITIONAL, 1
	);

	static final Map<BehaviorType, Integer> FULL_CSV_FREQUENCIES = Map.of(
			BehaviorType.PEACEFUL, 34,
			BehaviorType.HOSTILE, 33,
			BehaviorType.CONDITIONAL, 11
	);

	static final int FULL_CSV_MOBS = 78;

	private TestData() {}

	static Path writeCsv(Path dir, String name, String content) throws IOException {
		return Files.writeString(dir.resolve(name), content, StandardCharsets.UTF_8);
	}

	static Loader memoryLoader(String csv) {
		return new Loader() {
			@Override
			public List<String> load() {
				try {
					List<String> lines = load(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));
					if(!lines.isEmpty()) lines.removeFirst();
					return lines;

				} catch(IOException e) {
					throw new UncheckedIOException(e);
				}
			}
		};
	}

	static String shuffleRows(String csv, long seed) {
		List<String> lines = new ArrayList<>(csv.lines().toList());
		String header = lines.removeFirst();

		Collections.shuffle(lines, new Random(seed));

		return header + "\n" + String.join("\n", lines) + "\n";
	}

	static List<Mob> parse(Loader loader) {
		return new MobParser(",").parseAllLines(loader.load()).mobs();
	}

	static Map<BehaviorType, Integer> frequenciesByBehavior(List<Mob> mobs) {
		MobsAnalyzer analyzer = new MobsAnalyzer(mobs);
		return analyzer.groupFrequencies(analyzer.groupBy(Mob::behaviorType));
	}

	static String summary(Loader loader) {
		return new TreeMap<>(frequenciesByBehavior(parse(loader))).toString();
	}
}
