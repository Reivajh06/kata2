package reivajh06;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import reivajh06.exceptions.LoaderException;
import reivajh06.loaders.FileMobLoader;
import reivajh06.loaders.Loader;
import reivajh06.loaders.URLMobLoader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Loaders: origen de los datos basado en InputStream")
class LoaderTest {

	@TempDir
	Path tempDir;

	@Test
	@DisplayName("FileMobLoader lee un fichero correcto y descarta la cabecera")
	void fileLoaderReadsRowsWithoutHeader() throws IOException {
		Path file = TestData.writeCsv(tempDir, "mobs.csv", TestData.SMALL_CSV);

		List<String> lines = new FileMobLoader(file.toFile()).load();

		assertEquals(5, lines.size());
		assertEquals("3,bat,Peaceful,Random,6,,2012-10-25T00:00:00,1.4.2,", lines.getFirst());
	}

	@Test
	@DisplayName("FileMobLoader acepta finales de línea de Windows (CRLF)")
	void fileLoaderHandlesCrlf() throws IOException {
		Path file = TestData.writeCsv(tempDir, "mobs.csv", TestData.SMALL_CSV.replace("\n", "\r\n"));

		List<String> lines = new FileMobLoader(file.toFile()).load();

		assertEquals(5, lines.size());
		assertTrue(lines.stream().noneMatch(l -> l.contains("\r")));
	}

	@Test
	@DisplayName("URLMobLoader lee el mismo contenido a través de una URL")
	void urlLoaderReadsSameRowsAsFileLoader() throws IOException {
		Path file = TestData.writeCsv(tempDir, "mobs.csv", TestData.SMALL_CSV);

		List<String> fromUrl = new URLMobLoader(file.toUri().toString()).load();
		List<String> fromFile = new FileMobLoader(file.toFile()).load();

		assertEquals(fromFile, fromUrl);
	}

	@Test
	@DisplayName("URLMobLoader lee un recurso local del classpath")
	void urlLoaderReadsClasspathResource() {
		String resource = getClass().getResource("/mobs-test.csv").toString();

		List<String> lines = new URLMobLoader(resource).load();

		assertEquals(TestData.FULL_CSV_MOBS, lines.size());
	}

	@Test
	@DisplayName("Loader lee desde un ByteArrayInputStream en memoria")
	void loaderReadsFromByteArrayInputStream() throws IOException {
		InputStream input = new ByteArrayInputStream(TestData.SMALL_CSV.getBytes(StandardCharsets.UTF_8));

		List<String> lines = TestData.memoryLoader("").load(input);

		assertEquals(TestData.HEADER, lines.getFirst());
		assertEquals(6, lines.size());
	}

	@Test
	@DisplayName("Loader cierra el InputStream que recibe")
	void loaderClosesInputStream() throws IOException {
		boolean[] closed = {false};
		InputStream input = new ByteArrayInputStream(TestData.SMALL_CSV.getBytes(StandardCharsets.UTF_8)) {
			@Override
			public void close() {
				closed[0] = true;
			}
		};

		TestData.memoryLoader("").load(input);

		assertTrue(closed[0]);
	}

	@Test
	@DisplayName("Una ruta inexistente lanza LoaderException con un mensaje que indica la ruta")
	void missingFileThrowsLoaderException() {
		File missing = tempDir.resolve("no-existe.csv").toFile();
		Loader loader = new FileMobLoader(missing);

		LoaderException exception = assertThrows(LoaderException.class, loader::load);

		assertTrue(exception.getMessage().contains(missing.getAbsolutePath()));
		assertNotNull(exception.getCause());
	}

	@Test
	@DisplayName("Una URL que apunta a un recurso inexistente lanza LoaderException")
	void unreachableUrlThrowsLoaderException() {
		String url = tempDir.resolve("no-existe.csv").toUri().toString();
		Loader loader = new URLMobLoader(url);

		LoaderException exception = assertThrows(LoaderException.class, loader::load);

		assertTrue(exception.getMessage().contains("no-existe.csv"));
	}

	@Test
	@DisplayName("Una URL con protocolo desconocido lanza LoaderException")
	void unknownProtocolThrowsLoaderException() {
		LoaderException exception = assertThrows(LoaderException.class, () -> new URLMobLoader("protocolo://servidor/mobs.csv"));

		assertTrue(exception.getMessage().contains("protocolo://servidor/mobs.csv"));
	}

	@Test
	@DisplayName("Una URL sintácticamente inválida lanza LoaderException")
	void malformedUrlThrowsLoaderException() {
		assertThrows(LoaderException.class, () -> new URLMobLoader("esto no es una url"));
	}

	@Test
	@DisplayName("Una URL relativa (sin protocolo) lanza LoaderException")
	void relativeUrlThrowsLoaderException() {
		assertThrows(LoaderException.class, () -> new URLMobLoader("mobs.csv"));
	}

	@Test
	@DisplayName("Un fichero vacío produce una lista vacía")
	void emptyFileProducesEmptyList() throws IOException {
		Path file = TestData.writeCsv(tempDir, "vacio.csv", "");

		assertEquals(List.of(), new FileMobLoader(file.toFile()).load());
	}

	@Test
	@DisplayName("Un fichero con solo cabecera produce una lista vacía")
	void headerOnlyFileProducesEmptyList() throws IOException {
		Path file = TestData.writeCsv(tempDir, "cabecera.csv", TestData.HEADER + "\n");

		assertEquals(List.of(), new FileMobLoader(file.toFile()).load());
	}
}
