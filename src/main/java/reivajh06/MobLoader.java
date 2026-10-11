package reivajh06;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MobLoader {

	int EOF = -1;

	public List<String> loadCsv(InputStream sourceStream) {
		List<String> lines = new ArrayList<>();

		try(var inputStream = sourceStream) {
			String line;

			skipHeader(sourceStream);

			while(!(line = nextLine(sourceStream)).isEmpty()) lines.add(line);

		} catch(Exception e) {
			System.err.println("ERROR WHILE LOADING FILE");
			e.printStackTrace();
		}

		return lines;
	}

	private void skipHeader(InputStream inputStream) throws IOException {
		nextLine(inputStream);
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

		return line.toString();
	}
}
