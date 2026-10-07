package reivajh06;

import java.io.File;

public class Kata2 {

	static void main() {
		MobLoader mobLoader = new MobLoader(new File("src/main/resources/Mobs.csv"));
		mobLoader.extractMobs();
	}
}
