package reivajh06;

import reivajh06.loaders.FileCSVLoader;
import reivajh06.loaders.MobLoader;
import reivajh06.model.Mob;

import java.io.File;
import java.util.List;

public class Kata2 {

	static void main() {
		MobLoader mobLoader = new FileCSVLoader(new File("src/main/resources/minecraft_mobs.csv"));
		List<String> lines = mobLoader.load();
		MobParser parser = new MobParser();
		List<Mob> mobs = parser.parseAllLines(lines);
		MobStatistics statistics = new MobStatistics(mobs);

		System.out.println(statistics.groupBySpawnBehavior());
	}
}
