import reivajh06.MobParser;
import reivajh06.MobsAnalyzer;
import reivajh06.loaders.FileCSVMobLoader;
import reivajh06.MobLoader;
import reivajh06.model.BehaviorType;
import reivajh06.model.Mob;

void main() {
	MobLoader loader = new FileCSVMobLoader(new File("src/main/resources/minecraft_mobs.csv"));

	List<String> lines = loader.loadCsv();

	System.out.println(lines);
	System.out.println();

	MobParser mobParser = new MobParser(',');
	List<Mob> mobs = mobParser.parseAllLines(lines);

	System.out.println(mobs);
	System.out.println();

	MobsAnalyzer mobsAnalyzer = new MobsAnalyzer(mobs);

	System.out.println(mobsAnalyzer.mobsByBehavior(BehaviorType.HOSTILE));
}
