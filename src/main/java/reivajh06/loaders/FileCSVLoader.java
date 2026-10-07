package reivajh06.loaders;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileCSVLoader implements MobLoader {

	private static final int EOF = -1;

	private final File source;

	public FileCSVLoader(File source) {
		this.source = source;
	}

	@Override
	public List<String> load() {
		List<String> lines = new ArrayList<>();

		try(var inputStream = new FileInputStream(source)) {
			String line;

			skipHeader(inputStream);

			while(!(line = nextLine(inputStream)).isEmpty()) lines.add(line);

		} catch(Exception e) {
			System.err.println("ERROR WHILE LOADING FILE");
			e.printStackTrace();
		}

		return lines;
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
