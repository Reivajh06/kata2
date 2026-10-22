package reivajh06;

import reivajh06.exceptions.WriterException;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

public class MobWriter implements Closeable {

	private final OutputStream outputStream;

	public MobWriter(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	public MobWriter write(Object obj) {
		write(obj.toString().getBytes());

		return this;
	}

	public MobWriter write(byte[] bytes) {
		try {
			outputStream.write(bytes);

		} catch(IOException e) {
			throw new WriterException("Error while writing data to destination", e);
		}

		return this;
	}

	@Override
	public void close() throws IOException {
		outputStream.close();
	}
}
