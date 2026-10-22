package reivajh06.loaders;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public interface Loader {

	int EOF = -1;

	List<String> load();

	default List<String> load(InputStream sourceStream) throws IOException {
		List<String> lines = new ArrayList<>();

		try (sourceStream) {
			String line;

			while (!(line = nextLine(sourceStream)).isEmpty()) lines.add(line);
		}

		return lines;
	}

	private String nextLine(InputStream inputStream) throws IOException {
		byte byteRead;

		StringBuilder line = new StringBuilder();

		while(true) {
			byteRead = (byte) inputStream.read();

			if(byteRead == EOF) break;
			if((char) byteRead == '\n') break;

			line.append((char)byteRead);
		}

		return line.toString().replace("\r", "");
	}
}
