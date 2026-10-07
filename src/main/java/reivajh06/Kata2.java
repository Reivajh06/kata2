package reivajh06;

import reivajh06.loaders.FileCSVMobLoader;
import reivajh06.loaders.MobLoader;
import reivajh06.model.Mob;

import java.io.File;
import java.util.List;

public class Kata2 {

	static void main() {
		MobLoader mobLoader = new FileCSVMobLoader(new File("src/main/resources/minecraft_mobs.csv"));
		List<String> lines = mobLoader.loadCsv();
		System.out.println(lines.size());
		MobParser parser = new MobParser();
		List<Mob> mobs = parser.parseAllLines(lines);
		MobsAnalyzer statistics = new MobsAnalyzer(mobs);

		System.out.println(statistics.groupBySpawnBehavior());
	}
}
