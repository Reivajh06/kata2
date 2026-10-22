import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import reivajh06.CachedMobsCsv;
import reivajh06.loaders.FileMobLoader;
import reivajh06.exceptions.LoaderException;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileMobLoaderTest {

    private final FileMobLoader loader = new FileMobLoader();

    @TempDir
    Path tempDir;

    @Test
    void loadsCompleteCsvFromByteArrayInputStream() {
        InputStream input = new ByteArrayInputStream(StreamTestData.csvBytes());
        assertCompleteCsv(loader.loadCsv(input));
    }

    @Test
    void loadsCompleteCsvFromFileInputStream() throws IOException {
        Path file = StreamTestData.saveCsv(tempDir);
        InputStream input = new FileInputStream(file.toFile());
        assertCompleteCsv(loader.loadCsv(input));
    }

    @Test
    void loadsCompleteCsvFromBufferedFileInputStream() throws IOException {
        Path file = StreamTestData.saveCsv(tempDir);
        InputStream input = new BufferedInputStream(new FileInputStream(file.toFile()));
        assertCompleteCsv(loader.loadCsv(input));
    }

    @Test
    void loadsCompleteCsvFromBufferedByteArrayInputStream() {
        InputStream input = new BufferedInputStream(new ByteArrayInputStream(StreamTestData.csvBytes()));
        assertCompleteCsv(loader.loadCsv(input));
    }

    @Test
    void loadsCompleteCsvFromLocalUrl() throws IOException {
        Path file = StreamTestData.saveCsv(tempDir);
        InputStream input = file.toUri().toURL().openStream();
        assertCompleteCsv(loader.loadCsv(input));
    }

    @Test
    void loadsCompleteCsvFromClasspathResource() {
        InputStream input = getClass().getResourceAsStream("/minecraft_mobs.csv");
        assertNotNull(input, "Missing src/main/resources/minecraft_mobs.csv");
        assertCompleteCsv(loader.loadCsv(input));
    }

    @Test
    void loadsAllCachedMobsFromMemory() {
        List<String> loaded = loader.loadCsv(new ByteArrayInputStream(
                CachedMobsCsv.CACHED_MOBS_CSV.getBytes(StandardCharsets.UTF_8)));
        assertEquals(StreamTestData.EXPECTED_ROW_COUNT, loaded.size());
        List<String> normalizedRows = StreamTestData.ROWS.stream()
                .map(line -> line.replace("\r", ""))
                .toList();
        assertEquals(normalizedRows, loaded);
    }

    @Test
    void returnsIdenticalRowsFromDifferentInputStreams() throws IOException {
        Path file = StreamTestData.saveCsv(tempDir);
        List<String> fromFile = loader.loadCsv(new FileInputStream(file.toFile()));
        List<String> fromMemory = loader.loadCsv(new ByteArrayInputStream(StreamTestData.csvBytes()));
        List<String> fromBufferedFile = loader.loadCsv(
                new BufferedInputStream(new FileInputStream(file.toFile())));

        assertCompleteCsv(fromFile);
        assertEquals(fromFile, fromMemory);
        assertEquals(fromFile, fromBufferedFile);
    }

    @Test
    void emptyStreamLoadsNoRows() {
        assertTrue(loader.loadCsv(new ByteArrayInputStream(new byte[0])).isEmpty());
    }

    @Test
    void skipsHeaderRow() {
        List<String> loaded = loader.loadCsv(new ByteArrayInputStream(StreamTestData.csvBytes()));
        assertFalse(loaded.contains(StreamTestData.header()));
    }

    @Test
    void trailingNewlineDoesNotAddRows() {
        byte[] csv = (new String(StreamTestData.csvBytes(), StandardCharsets.UTF_8) + "\n").getBytes(StandardCharsets.UTF_8);
        assertCompleteCsv(loader.loadCsv(new ByteArrayInputStream(csv)));
    }

    @Test
    void blankLineInTheMiddleDoesNotTruncateTheFile() {
        List<String> rows = new ArrayList<>(StreamTestData.ROWS);
        rows.add(10, "");

        List<String> loaded = loader.loadCsv(new ByteArrayInputStream(StreamTestData.csvWith(rows)));
        assertEquals(StreamTestData.ROWS, loaded.stream().filter(line -> !line.isBlank()).toList());
    }

    @Test
    void preservesNonAsciiCharacters() {
        String row = "1,piglín_ñandú,Hostile,Random,16,8,2020-06-23T00:00:00,1.16.0,";
        List<String> loaded = loader.loadCsv(new ByteArrayInputStream(StreamTestData.csvWith(List.of(row))));

        assertEquals(List.of(row), loaded);
    }

    @Test
    void windowsLineEndingsLoadSameRowsAsUnixLineEndings() {
        byte[] crlf = new String(StreamTestData.csvBytes(), StandardCharsets.UTF_8)
                .replace("\n", "\r\n")
                .getBytes(StandardCharsets.UTF_8);

        assertCompleteCsv(loader.loadCsv(new ByteArrayInputStream(crlf)));
    }

    @Test
    void nullStreamRaisesLoaderException() {
        assertThrows(LoaderException.class, () -> loader.loadCsv(null));
    }

    @Test
    void closesTheSourceStream() {
        AtomicBoolean closed = new AtomicBoolean(false);
        InputStream input = new ByteArrayInputStream(StreamTestData.csvBytes()) {
            @Override
            public void close() throws IOException {
                closed.set(true);
                super.close();
            }
        };

        loader.loadCsv(input);
        assertTrue(closed.get());
    }

    private static void assertCompleteCsv(List<String> loaded) {
        assertEquals(StreamTestData.EXPECTED_ROW_COUNT, loaded.size());
        assertEquals(StreamTestData.ROWS, loaded);
    }
}
