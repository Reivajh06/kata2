package reivajh06;

import reivajh06.model.Mob;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MobLoader {

	private static final int EOF = -1;

	private final File source;

	public MobLoader(File source) {
		this.source = source;
	}

	public List<Mob> extractMobs() {
		List<Mob> mobs = new ArrayList<>();

		try(var inputStream = new FileInputStream(source)) {
			String nextLine;

			skipHeader(inputStream);

			while(!(nextLine = nextLine(inputStream)).isEmpty()) System.out.println(nextLine);

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
}
