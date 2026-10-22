package reivajh06;

import reivajh06.loaders.Loader;
import reivajh06.loaders.URLMobLoader;

public class Kata2 {

	static void main() {
		Loader loader = new URLMobLoader("https://raw.githubusercontent.com/Reivajh06/kata2/develop/src/main/resources/minecraft_mobs.csv");

		MobParser mobParser = new MobParser(",");

		ParseResult parseResult = mobParser.parseAllLines(loader.load());

		System.out.println(parseResult.mobs());
		System.out.println(parseResult.parsingErrors());
	}
}
