package reivajh06;

import reivajh06.model.BehaviorType;
import reivajh06.model.Mob;
import reivajh06.model.ReproductiveRequirement;
import reivajh06.model.SpawnBehavior;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

public class FileMobLoader {

	private static final int EOF = -1;

	private final File source;

	public FileMobLoader(File source) {
		this.source = source;
	}

	public List<Mob> extractMobs() {
		List<Mob> mobs = new ArrayList<>();

		try(var inputStream = new FileInputStream(source)) {
			String nextLine;

			skipHeader(inputStream);

			while(!(nextLine = nextLine(inputStream)).isEmpty()) mobs.add(lineToMob(nextLine));

		} catch(Exception e) {
			System.err.println("ERROR WHILE LOADING FILE");
			e.printStackTrace();
		}

		return mobs;
	}

	private void skipHeader(FileInputStream inputStream) throws IOException {
		nextLine(inputStream);
	}

	private String nextLine(FileInputStream inputStream) throws IOException {
		byte byteRead;

		StringBuilder line = new StringBuilder();

		while(true) {
			byteRead = (byte) inputStream.read();

			if(byteRead == EOF) break;
			if((char) byteRead == '\n') break;

			line.append((char)byteRead);
		}

		return line.toString();
	}

	private Mob lineToMob(String line) {
		String[] mobDetails = line.split(",");

		int id = Integer.parseInt(mobDetails[0]);

		String name = mobDetails[1].trim();
		BehaviorType behaviorType = BehaviorType.valueOf(mobDetails[2].trim().replace(" ", "_").toUpperCase());
		SpawnBehavior spawnBehavior = SpawnBehavior.valueOf(mobDetails[3].trim().replace(" ", "_").toUpperCase());

		int healthPoints = Integer.parseInt(mobDetails[4].trim());
		int maxDamage = mobDetails[5].isBlank() ? 0 : Integer.parseInt(mobDetails[5].trim());

		String[] date = mobDetails[6].substring(0, 10).split("-");

		LocalDate debutDate = LocalDate.of(Integer.parseInt(date[0]), Month.of(Integer.parseInt(date[1])), Integer.parseInt(date[2]));

		String version = mobDetails[7].trim();

		ReproductiveRequirement reproductiveRequirement = mobDetails.length == 8 || mobDetails[8].isBlank() ?
				ReproductiveRequirement.NONE
				: ReproductiveRequirement.valueOf(mobDetails[8].trim().replace(" ", "_").toUpperCase());

		return new Mob(
				id,
				name,
				behaviorType,
				spawnBehavior,
				healthPoints,
				maxDamage,
				debutDate,
				version,
				reproductiveRequirement
		);
	}
}
