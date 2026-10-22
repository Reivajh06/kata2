package reivajh06;

import reivajh06.model.Mob;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

public class Kata2 {

	static void main() throws FileNotFoundException {
		MobLoader mobLoader = new MobLoader();

		List<String> lines = mobLoader.loadCsv(new FileInputStream("src/main/resources/minecraft_mobs.csv"));
		List<String> linesByteArray = mobLoader.loadCsv(new ByteArrayInputStream(CachedMobsCsv.CACHED_MOBS_CSV.getBytes()));

		MobParser parser = new MobParser(",");

		List<Mob> mobs = parser.parseAllLines(lines);
		List<Mob> mobsByteArray = parser.parseAllLines(linesByteArray);
		MobsAnalyzer statistics = new MobsAnalyzer(mobs);
		MobsAnalyzer statisticsByteArray = new MobsAnalyzer(mobsByteArray);

		var x = statistics.filterMobs(mob -> mob.debutDate().getYear() == 2011);
		var y = statisticsByteArray.filterMobs(mob -> mob.debutDate().getYear() == 2011);

		MobWriter mobWriter = new MobWriter(System.out);

		mobWriter.write(x)
				.write(y);

		mobWriter = new MobWriter(new FileOutputStream("src/main/resources/mobByDebutYear"));
		mobWriter.write(x);
	}
}
