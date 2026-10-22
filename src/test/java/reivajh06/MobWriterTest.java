package reivajh06;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import reivajh06.exceptions.WriterException;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("MobWriter: destino de la salida basado en OutputStream")
class MobWriterTest {

	private static final Map<String, Integer> SUMMARY = new TreeMap<>(Map.of("HOSTILE", 2, "PEACEFUL", 3));

	@TempDir
	Path tempDir;

	@Test
	@DisplayName("Escribe el resumen en un ByteArrayOutputStream")
	void writesToByteArrayOutputStream() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		try(MobWriter writer = new MobWriter(output)) {
			writer.write(SUMMARY);
		}

		assertEquals(SUMMARY.toString(), output.toString(StandardCharsets.UTF_8));
	}

	@Test
	@DisplayName("Escribe el resumen en un fichero mediante FileOutputStream")
	void writesToFile() throws IOException {
		Path file = tempDir.resolve("resumen.txt");

		try(MobWriter writer = new MobWriter(new FileOutputStream(file.toFile()))) {
			writer.write(SUMMARY);
		}

		assertEquals(SUMMARY.toString(), Files.readString(file));
	}

	@Test
	@DisplayName("Escribe el resumen en la consola (System.out)")
	void writesToConsole() {
		PrintStream originalOut = System.out;
		ByteArrayOutputStream console = new ByteArrayOutputStream();

		try {
			System.setOut(new PrintStream(console, true, StandardCharsets.UTF_8));
			new MobWriter(System.out).write(SUMMARY);
			System.out.flush();

		} finally {
			System.setOut(originalOut);
		}

		assertEquals(SUMMARY.toString(), console.toString(StandardCharsets.UTF_8));
	}

	@Test
	@DisplayName("Las escrituras encadenadas se acumulan en orden")
	void chainedWritesAreAppended() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		new MobWriter(output).write("a").write("\n").write("b".getBytes(StandardCharsets.UTF_8));

		assertEquals("a\nb", output.toString(StandardCharsets.UTF_8));
	}

	@Test
	@DisplayName("close() cierra el OutputStream subyacente")
	void closeClosesUnderlyingStream() throws IOException {
		boolean[] closed = {false};
		OutputStream output = new ByteArrayOutputStream() {
			@Override
			public void close() {
				closed[0] = true;
			}
		};

		new MobWriter(output).close();

		assertTrue(closed[0]);
	}

	@Test
	@DisplayName("Un fallo del destino se traduce en WriterException con la causa original")
	void failingDestinationThrowsWriterException() {
		IOException failure = new IOException("disco lleno");
		OutputStream broken = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				throw failure;
			}

			@Override
			public void write(byte[] b) throws IOException {
				throw failure;
			}
		};

		WriterException exception = assertThrows(WriterException.class, () -> new MobWriter(broken).write(SUMMARY));

		assertNotNull(exception.getMessage());
		assertEquals(failure, exception.getCause());
	}
}
