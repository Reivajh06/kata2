import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import reivajh06.CachedMobsCsv;
import reivajh06.MobLoader;
import reivajh06.MobWriter;
import reivajh06.exceptions.LoaderException;
import reivajh06.model.BehaviorType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Casos de "Verificación esperada" del enunciado de la kata 2.
 */
class Kata2VerificationTest {

    private static final Map<BehaviorType, Integer> EXPECTED_FREQUENCIES = Map.of(
            BehaviorType.CONDITIONAL, 11,
            BehaviorType.HOSTILE, 33,
            BehaviorType.PEACEFUL, 34);

    @TempDir
    Path tempDir;

    // Prueba con fichero correcto

    @Test
    void correctFileProducesExpectedSummary() throws IOException {
        Path file = StreamTestData.saveCsv(tempDir);

        assertEquals(EXPECTED_FREQUENCIES, StreamTestData.frequenciesOf(new FileInputStream(file.toFile())));
        assertEquals(StreamTestData.EXPECTED_SUMMARY, StreamTestData.summaryOf(new FileInputStream(file.toFile())));
    }

    // Prueba con URL, recurso local o ByteArrayInputStream

    @Test
    void urlProducesSameSummaryAsFile() throws IOException {
        Path file = StreamTestData.saveCsv(tempDir);
        InputStream input = file.toUri().toURL().openStream();

        assertEquals(EXPECTED_FREQUENCIES, StreamTestData.frequenciesOf(input));
    }

    @Test
    void classpathResourceProducesSameSummaryAsFile() {
        InputStream input = getClass().getResourceAsStream("/mobs-test.csv");
        assertEquals(EXPECTED_FREQUENCIES, StreamTestData.frequenciesOf(input));
    }

    @Test
    void inMemoryCsvProducesSameSummaryAsFile() {
        InputStream input = new ByteArrayInputStream(CachedMobsCsv.CACHED_MOBS_CSV.getBytes(StandardCharsets.UTF_8));
        assertEquals(EXPECTED_FREQUENCIES, StreamTestData.frequenciesOf(input));
    }

    // Prueba escritura en consola y en fichero o ByteArrayOutputStream

    @Test
    void summaryWrittenToConsoleFileAndMemoryIsIdentical() throws IOException {
        String summary = StreamTestData.summaryOf(new ByteArrayInputStream(StreamTestData.csvBytes()));

        ByteArrayOutputStream memory = new ByteArrayOutputStream();
        try (MobWriter writer = new MobWriter(memory)) {
            writer.write(summary);
        }

        Path file = tempDir.resolve("summary.txt");
        try (MobWriter writer = new MobWriter(new FileOutputStream(file.toFile()))) {
            writer.write(summary);
        }

        PrintStream originalConsole = System.out;
        ByteArrayOutputStream console = new ByteArrayOutputStream();
        try (PrintStream simulatedConsole = new PrintStream(console, true, StandardCharsets.UTF_8)) {
            System.setOut(simulatedConsole);
            new MobWriter(System.out).write(summary);
            System.out.flush();
        } finally {
            System.setOut(originalConsole);
        }

        assertEquals(StreamTestData.EXPECTED_SUMMARY, memory.toString(StandardCharsets.UTF_8));
        assertEquals(StreamTestData.EXPECTED_SUMMARY, Files.readString(file, StandardCharsets.UTF_8));
        assertEquals(StreamTestData.EXPECTED_SUMMARY, console.toString(StandardCharsets.UTF_8));
    }

    // Prueba con ruta inexistente o URL inválida

    @Test
    void nonexistentFileCannotBeOpened() {
        Path missing = tempDir.resolve("does-not-exist.csv");
        assertThrows(FileNotFoundException.class, () -> new FileInputStream(missing.toFile()));
    }

    @Test
    void urlToNonexistentFileCannotBeOpened() {
        Path missing = tempDir.resolve("does-not-exist.csv");
        assertThrows(IOException.class, () -> missing.toUri().toURL().openStream());
    }

    @Test
    void malformedUrlIsRejected() {
        assertThrows(MalformedURLException.class, () -> URI.create("htp://example.com/mobs.csv").toURL());
    }

    @Test
    void missingClasspathResourceRaisesLoaderException() {
        InputStream missingResource = getClass().getResourceAsStream("/does-not-exist.csv");
        assertThrows(LoaderException.class, () -> new MobLoader().loadCsv(missingResource));
    }

    @Test
    void readFailureRaisesLoaderExceptionKeepingTheCause() {
        InputStream failing = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Connection reset");
            }
        };

        LoaderException exception = assertThrows(LoaderException.class, () -> new MobLoader().loadCsv(failing));
        assertInstanceOf(IOException.class, exception.getCause(),
                "LoaderException should keep the original IOException as its cause");
    }

    // Prueba con fichero vacío

    @Test
    void emptyFileProducesEmptySummary() throws IOException {
        Path empty = Files.createFile(tempDir.resolve("empty.csv"));

        assertTrue(new MobLoader().loadCsv(new FileInputStream(empty.toFile())).isEmpty());
        assertEquals("", StreamTestData.summaryOf(new FileInputStream(empty.toFile())));
    }

    @Test
    void headerOnlyFileProducesEmptySummary() {
        byte[] headerOnly = (StreamTestData.header() + "\n").getBytes(StandardCharsets.UTF_8);
        assertEquals("", StreamTestData.summaryOf(new ByteArrayInputStream(headerOnly)));
    }

    // Prueba con una fila mal formada

    @Test
    void malformedRowIsReportedWithClearMessage() {
        List<String> rows = new ArrayList<>(StreamTestData.ROWS);
        rows.add(10, "x,broken");
        InputStream input = new ByteArrayInputStream(StreamTestData.csvWith(rows));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> StreamTestData.frequenciesOf(input));
        assertTrue(exception.getMessage() != null && exception.getMessage().contains("x,broken"),
                "The error message should identify the invalid row, but was: " + exception.getMessage());
    }

    // Comprueba que el resumen no cambia si se reordena el fichero o cambia el origen del InputStream

    @Test
    void summaryDoesNotChangeWhenRowsAreReordered() {
        List<String> shuffled = new ArrayList<>(StreamTestData.ROWS);
        Collections.shuffle(shuffled, new Random(2026));
        List<String> reversed = new ArrayList<>(StreamTestData.ROWS);
        Collections.reverse(reversed);

        String original = StreamTestData.summaryOf(new ByteArrayInputStream(StreamTestData.csvBytes()));

        assertEquals(original, StreamTestData.summaryOf(new ByteArrayInputStream(StreamTestData.csvWith(shuffled))));
        assertEquals(original, StreamTestData.summaryOf(new ByteArrayInputStream(StreamTestData.csvWith(reversed))));
    }

    @Test
    void summaryDoesNotChangeWithInputOrigin() throws IOException {
        Path file = StreamTestData.saveCsv(tempDir);

        String fromFile = StreamTestData.summaryOf(new FileInputStream(file.toFile()));
        String fromUrl = StreamTestData.summaryOf(file.toUri().toURL().openStream());
        String fromResource = StreamTestData.summaryOf(getClass().getResourceAsStream("/minecraft_mobs.csv"));
        String fromMemory = StreamTestData.summaryOf(new ByteArrayInputStream(StreamTestData.csvBytes()));

        assertEquals(fromFile, fromUrl);
        assertEquals(fromFile, fromResource);
        assertEquals(fromFile, fromMemory);
    }
}
