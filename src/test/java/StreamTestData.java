import reivajh06.MobLoader;
import reivajh06.MobParser;
import reivajh06.MobsAnalyzer;
import reivajh06.model.BehaviorType;
import reivajh06.model.Mob;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class StreamTestData {

    static final int EXPECTED_ROW_COUNT = 78;
    static final byte[] CSV_BYTES = readRealCsv();
    static final List<String> ROWS = expectedDataLines(new String(CSV_BYTES, StandardCharsets.UTF_8));
    static final String EXPECTED_SUMMARY = "CONDITIONAL=11\nHOSTILE=33\nPEACEFUL=34\n";

    private StreamTestData() { }

    static byte[] csvBytes() {
        return CSV_BYTES.clone();
    }

    static Path saveCsv(Path directory) throws IOException {
        Path file = directory.resolve("minecraft_mobs.csv");
        Files.write(file, CSV_BYTES);
        return file;
    }

    static String analyticsSummary() {
        return summaryOf(new ByteArrayInputStream(csvBytes()));
    }

    static Map<BehaviorType, Integer> frequenciesOf(InputStream input) {
        List<String> lines = new MobLoader().loadCsv(input);
        List<Mob> mobs = new MobParser(",").parseAllLines(lines);
        MobsAnalyzer analyzer = new MobsAnalyzer(mobs);
        return analyzer.groupFrequencies(analyzer.groupBy(Mob::behaviorType));
    }

    static String summaryOf(InputStream input) {
        return format(frequenciesOf(input));
    }

    static String format(Map<BehaviorType, Integer> frequencies) {
        if (frequencies.isEmpty()) return "";
        return frequencies.entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().name()))
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("\n", "", "\n"));
    }

    static String header() {
        return new String(CSV_BYTES, StandardCharsets.UTF_8).split("\n")[0];
    }

    static byte[] csvWith(List<String> rows) {
        return (header() + "\n" + String.join("\n", rows)).getBytes(StandardCharsets.UTF_8);
    }

    private static List<String> expectedDataLines(String csv) {
        String[] lines = csv.split("\n");
        return Arrays.asList(lines).subList(1, lines.length);
    }

    private static byte[] readRealCsv() {
        try (InputStream resource = StreamTestData.class.getResourceAsStream("/minecraft_mobs.csv")) {
            if (resource == null) {
                throw new IllegalStateException("Missing src/main/resources/minecraft_mobs.csv");
            }
            return resource.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read minecraft_mobs.csv", e);
        }
    }
}
