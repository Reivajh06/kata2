package reivajh06;

import java.io.File;

public class Kata2 {

	static void main() {
		FileMobLoader mobLoader = new FileMobLoader(new File("src/main/resources/Mobs.csv"));
		MobStatistics mobStatistics = new MobStatistics(mobLoader.extractMobs());

		System.out.println(mobStatistics.groupBySpawnBehavior());
	}
}
