package reivajh06;

import reivajh06.loaders.FileMobLoader;
import reivajh06.loaders.Loader;

import java.io.File;

public class Kata2 {

	static void main() {
		Loader loader = new FileMobLoader(new File("src/main/resources/minecraft_mobs.csv"));

		MobParser mobParser = new MobParser(",");

		ParseResult parseResult = mobParser.parseAllLines(loader.load());

		System.out.println(parseResult.mobs());
	}
}
