import org.junit.jupiter.api.Test;
import reivajh06.MobWriter;
import reivajh06.exceptions.WriterException;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MobWriterTest {

    private static final Path OUTPUT_DIRECTORY = Path.of("target", "test-output");

    @Test
    void writesAnalyticsToByteArrayOutputStream() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try (MobWriter writer = new MobWriter(output)) {
            writer.write(StreamTestData.analyticsSummary());
        }

        assertEquals(StreamTestData.EXPECTED_SUMMARY, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void writesAnalyticsBytesToByteArrayOutputStream() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try (MobWriter writer = new MobWriter(output)) {
            writer.write(StreamTestData.analyticsSummary().getBytes(StandardCharsets.UTF_8));
        }

        assertEquals(StreamTestData.EXPECTED_SUMMARY, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void writesAnalyticsToFileOutputStream() throws IOException {
        Files.createDirectories(OUTPUT_DIRECTORY);
        Path file = OUTPUT_DIRECTORY.resolve("mob-analytics.txt");

        try (MobWriter writer = new MobWriter(new FileOutputStream(file.toFile()))) {
            writer.write(StreamTestData.analyticsSummary());
        }

        assertAnalyticsFile(file);
    }

    @Test
    void writesAnalyticsToBufferedFileOutputStream() throws IOException {
        Files.createDirectories(OUTPUT_DIRECTORY);
        Path file = OUTPUT_DIRECTORY.resolve("mob-analytics-buffered.txt");

        try (MobWriter writer = new MobWriter(new BufferedOutputStream(new FileOutputStream(file.toFile())))) {
            writer.write(StreamTestData.analyticsSummary());
        }

        assertAnalyticsFile(file);
    }

    @Test
    void writesAnalyticsToBufferedByteArrayOutputStream() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try (MobWriter writer = new MobWriter(new BufferedOutputStream(output))) {
            writer.write(StreamTestData.analyticsSummary());
        }

        assertEquals(StreamTestData.EXPECTED_SUMMARY, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void writesAnalyticsToSystemOut() {
        PrintStream originalConsole = System.out;
        ByteArrayOutputStream capturedOutput = new ByteArrayOutputStream();

        try (PrintStream simulatedConsole = new PrintStream(capturedOutput, true, StandardCharsets.UTF_8)) {
            System.setOut(simulatedConsole);
            new MobWriter(System.out).write(StreamTestData.analyticsSummary());
            System.out.flush();
        } finally {
            System.setOut(originalConsole);
        }

        assertEquals(StreamTestData.EXPECTED_SUMMARY, capturedOutput.toString(StandardCharsets.UTF_8));
    }

    @Test
    void writesNonAsciiTextAsUtf8() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try (MobWriter writer = new MobWriter(output)) {
            writer.write("piglín=1\n");
        }

        assertEquals("piglín=1\n", output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void consecutiveWritesAreAppendedInOrder() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try (MobWriter writer = new MobWriter(output)) {
            writer.write("CONDITIONAL=11\n").write("HOSTILE=33\n").write("PEACEFUL=34\n");
        }

        assertEquals(StreamTestData.EXPECTED_SUMMARY, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void closeClosesTheDestinationStream() throws IOException {
        AtomicBoolean closed = new AtomicBoolean(false);
        OutputStream output = new ByteArrayOutputStream() {
            @Override
            public void close() throws IOException {
                closed.set(true);
                super.close();
            }
        };

        new MobWriter(output).close();
        assertTrue(closed.get());
    }

    @Test
    void writeFailureRaisesWriterExceptionKeepingTheCause() {
        OutputStream failing = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                throw new IOException("Disk full");
            }
        };

        WriterException exception = assertThrows(WriterException.class,
                () -> new MobWriter(failing).write(StreamTestData.analyticsSummary()));
        assertInstanceOf(IOException.class, exception.getCause(),
                "WriterException should keep the original IOException as its cause");
    }

    private static void assertAnalyticsFile(Path file) throws IOException {
        assertTrue(Files.isRegularFile(file));
        assertEquals(StreamTestData.EXPECTED_SUMMARY, Files.readString(file, StandardCharsets.UTF_8));
    }
}
