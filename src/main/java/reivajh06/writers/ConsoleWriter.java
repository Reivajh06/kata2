package reivajh06.writers;

import java.io.IOException;
import java.io.OutputStream;

public class ConsoleWriter implements MobWriter {

	@Override
	public void write(Object obj) {
		write(obj.toString());
	}

	@Override
	public void write(String data) {
		write(data.getBytes());
	}

	@Override
	public void write(byte[] bytes) {
		try {
			OutputStream outputStream = System.out;
			outputStream.write(bytes);

		} catch(IOException e) {
			e.printStackTrace();
			System.out.println("Error while writing data on the console");
		}
	}
}
