package reivajh06;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import reivajh06.exceptions.LoaderException;
import reivajh06.loaders.FileMobLoader;
import reivajh06.loaders.Loader;
import reivajh06.loaders.URLMobLoader;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Casos de la sección "Verificación esperada" del enunciado de la kata 2.
 * Cada caso recorre el flujo completo: Loader (InputStream) -> MobParser -> MobsAnalyzer -> MobWriter (OutputStream).
 */
@DisplayName("Kata 2: verificación esperada")
class Kata2VerificationTest {

	private static final String EXPECTED_SUMMARY = new TreeMap<>(TestData.FULL_CSV_FREQUENCIES).toString();

	@TempDir
	Path tempDir;

	private Path fullCsv() throws IOException {
		return TestData.writeCsv(tempDir, "mobs.csv", CachedMobsCsv.CACHED_MOBS_CSV);
	}

	@Nested
	@DisplayName("Prueba con fichero correcto")
	class CorrectFile {

		@Test
		@DisplayName("Todas las filas se transforman en entidades sin errores")
		void allRowsAreParsed() throws IOException {
			ParseResult result = new MobParser(",").parseAllLines(new FileMobLoader(fullCsv().toFile()).load());

			assertEquals(TestData.FULL_CSV_MOBS, result.mobs().size());
			assertTrue(result.parsingErrors().isEmpty());
		}

		@Test
		@DisplayName("El resumen por comportamiento es el esperado")
		void summaryIsCorrect() throws IOException {
			assertEquals(EXPECTED_SUMMARY, TestData.summary(new FileMobLoader(fullCsv().toFile())));
		}

		@Test
		@DisplayName("El dataset incluido en resources es correcto")
		void bundledDatasetIsCorrect() {
			Path dataset = Path.of("src", "main", "resources", "minecraft_mobs.csv");

			assertEquals(EXPECTED_SUMMARY, TestData.summary(new FileMobLoader(dataset.toFile())));
		}
	}

	@Nested
	@DisplayName("Prueba con URL, recurso local o ByteArrayInputStream")
	class OtherOrigins {

		@Test
		@DisplayName("URL (file://)")
		void url() throws IOException {
			assertEquals(EXPECTED_SUMMARY, TestData.summary(new URLMobLoader(fullCsv().toUri().toString())));
		}

		@Test
		@DisplayName("Recurso local del classpath")
		void classpathResource() {
			String resource = getClass().getResource("/mobs-test.csv").toString();

			assertEquals(EXPECTED_SUMMARY, TestData.summary(new URLMobLoader(resource)));
		}

		@Test
		@DisplayName("ByteArrayInputStream en memoria")
		void memory() {
			assertEquals(EXPECTED_SUMMARY, TestData.summary(TestData.memoryLoader(CachedMobsCsv.CACHED_MOBS_CSV)));
		}
	}

	@Nested
	@DisplayName("Prueba escritura en consola y en fichero o ByteArrayOutputStream")
	class Destinations {

		@Test
		@DisplayName("Consola (System.out)")
		void console() {
			PrintStream originalOut = System.out;
			ByteArrayOutputStream console = new ByteArrayOutputStream();

			try {
				System.setOut(new PrintStream(console, true, StandardCharsets.UTF_8));
				writeSummary(System.out);
				System.out.flush();

			} finally {
				System.setOut(originalOut);
			}

			assertEquals(EXPECTED_SUMMARY, console.toString(StandardCharsets.UTF_8));
		}

		@Test
		@DisplayName("Fichero (FileOutputStream)")
		void file() throws IOException {
			Path output = tempDir.resolve("resumen.txt");

			try(FileOutputStream stream = new FileOutputStream(output.toFile())) {
				writeSummary(stream);
			}

			assertEquals(EXPECTED_SUMMARY, Files.readString(output));
		}

		@Test
		@DisplayName("Memoria (ByteArrayOutputStream)")
		void memory() {
			ByteArrayOutputStream output = new ByteArrayOutputStream();

			writeSummary(output);

			assertEquals(EXPECTED_SUMMARY, output.toString(StandardCharsets.UTF_8));
		}

		private void writeSummary(java.io.OutputStream destination) {
			new MobWriter(destination).write(TestData.summary(TestData.memoryLoader(CachedMobsCsv.CACHED_MOBS_CSV)));
		}
	}

	@Nested
	@DisplayName("Prueba con ruta inexistente o URL inválida")
	class InvalidOrigins {

		@Test
		@DisplayName("Ruta inexistente")
		void missingFile() {
			Path missing = tempDir.resolve("no-existe.csv");

			LoaderException exception = assertThrows(LoaderException.class, () -> new FileMobLoader(missing.toFile()).load());

			assertTrue(exception.getMessage().contains(missing.toFile().getAbsolutePath()));
		}

		@Test
		@DisplayName("URL a un recurso inexistente")
		void missingUrlResource() {
			Loader loader = new URLMobLoader(tempDir.resolve("no-existe.csv").toUri().toString());

			assertThrows(LoaderException.class, loader::load);
		}

		@Test
		@DisplayName("URL con protocolo desconocido")
		void unknownProtocol() {
			assertThrows(LoaderException.class, () -> new URLMobLoader("protocolo://servidor/mobs.csv"));
		}

		@Test
		@DisplayName("URL sintácticamente inválida")
		void malformedUrl() {
			assertThrows(LoaderException.class, () -> new URLMobLoader("esto no es una url"));
		}
	}

	@Nested
	@DisplayName("Prueba con fichero vacío")
	class EmptyFile {

		@Test
		@DisplayName("Fichero completamente vacío produce un resumen vacío")
		void emptyFile() throws IOException {
			Path file = TestData.writeCsv(tempDir, "vacio.csv", "");

			assertEquals("{}", TestData.summary(new FileMobLoader(file.toFile())));
		}

		@Test
		@DisplayName("Fichero con solo cabecera produce un resumen vacío")
		void headerOnly() throws IOException {
			Path file = TestData.writeCsv(tempDir, "cabecera.csv", TestData.HEADER + "\n");

			assertEquals("{}", TestData.summary(new FileMobLoader(file.toFile())));
		}
	}

	@Nested
	@DisplayName("Prueba con una fila mal formada")
	class MalformedRow {

		@Test
		@DisplayName("Fila con columnas de menos: se reporta y el resto se procesa")
		void missingColumns() throws IOException {
			assertMalformedRowIsReported("4,blaze,Hostile");
		}

		@Test
		@DisplayName("Fila con un número inválido: se reporta y el resto se procesa")
		void invalidNumber() throws IOException {
			assertMalformedRowIsReported("4,blaze,Hostile,On Load,veinte,6,2011-11-18T00:00:00,1.0.0,");
		}

		@Test
		@DisplayName("Fila con un valor de enumerado desconocido: se reporta y el resto se procesa")
		void unknownEnumValue() throws IOException {
			assertMalformedRowIsReported("4,blaze,Angry,On Load,20,6,2011-11-18T00:00:00,1.0.0,");
		}

		private void assertMalformedRowIsReported(String badRow) throws IOException {
			String csv = TestData.SMALL_CSV.replace("4,blaze,Hostile,On Load,20,6,2011-11-18T00:00:00,1.0.0,", badRow);
			Path file = TestData.writeCsv(tempDir, "mal-formado.csv", csv);

			ParseResult result = new MobParser(",").parseAllLines(new FileMobLoader(file.toFile()).load());

			assertEquals(4, result.mobs().size());
			assertEquals(1, result.parsingErrors().size());
			assertEquals(2, result.parsingErrors().getFirst().nLine());
			assertTrue(result.parsingErrors().getFirst().errorMessage().contains(badRow));
		}
	}

	@Nested
	@DisplayName("El resumen no cambia si se reordena el fichero o cambia el origen del InputStream")
	class SummaryInvariance {

		@Test
		@DisplayName("Reordenar las filas del fichero no cambia el resumen")
		void reorderedFile() throws IOException {
			for(long seed : List.of(1L, 7L, 42L)) {
				Path shuffled = TestData.writeCsv(tempDir, "mobs-" + seed + ".csv", TestData.shuffleRows(CachedMobsCsv.CACHED_MOBS_CSV, seed));

				assertEquals(EXPECTED_SUMMARY, TestData.summary(new FileMobLoader(shuffled.toFile())), "seed " + seed);
			}
		}

		@Test
		@DisplayName("Fichero, URL, recurso local y memoria producen el mismo resumen")
		void differentOrigins() throws IOException {
			Path file = fullCsv();

			List<Loader> loaders = List.of(
					new FileMobLoader(file.toFile()),
					new URLMobLoader(file.toUri().toString()),
					new URLMobLoader(getClass().getResource("/mobs-test.csv").toString()),
					TestData.memoryLoader(CachedMobsCsv.CACHED_MOBS_CSV)
			);

			for(Loader loader : loaders) {
				assertEquals(EXPECTED_SUMMARY, TestData.summary(loader), loader.getClass().getSimpleName());
			}
		}

		@Test
		@DisplayName("El resumen escrito es idéntico en todos los destinos")
		void differentDestinations() throws IOException {
			String summary = TestData.summary(TestData.memoryLoader(CachedMobsCsv.CACHED_MOBS_CSV));

			ByteArrayOutputStream memory = new ByteArrayOutputStream();
			new MobWriter(memory).write(summary);

			Path file = tempDir.resolve("resumen.txt");
			try(MobWriter writer = new MobWriter(new FileOutputStream(file.toFile()))) {
				writer.write(summary);
			}

			assertEquals(memory.toString(StandardCharsets.UTF_8), Files.readString(file));
		}
	}
}
