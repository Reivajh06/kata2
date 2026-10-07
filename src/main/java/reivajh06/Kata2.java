package reivajh06;

import java.io.File;

public class Kata2 {

	static void main() {
		FileMobLoader mobLoader = new FileMobLoader(new File("src/main/resources/Mobs.csv"));
		MobStatistics mobStatistics = new MobStatistics(mobLoader.extractMobs());

		System.out.println(mobStatistics.groupByBehavior());
		System.out.println(mobStatistics.groupByDebutYear());
		System.out.println(mobStatistics.groupByVersion());
		System.out.println(mobStatistics.groupByReproductiveRequirement());
	}
}
