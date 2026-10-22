import org.junit.jupiter.api.Test;
import reivajh06.*;
import reivajh06.loaders.FileMobLoader;
import reivajh06.model.BehaviorType;
import reivajh06.model.Mob;
import reivajh06.MobWriter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MobStreamsIntegrationTest {

    @Test
    void loadsParsesAnalyzesAndWritesUsingBufferedStreams() throws IOException {
        var input = new BufferedInputStream(new ByteArrayInputStream(StreamTestData.csvBytes()));

        List<String> lines = new FileMobLoader().loadCsv(input);
        List<Mob> mobs = new MobParser(",").parseAllLines(lines);
        MobsAnalyzer analyzer = new MobsAnalyzer(mobs);
        Map<BehaviorType, Integer> frequencies = analyzer.groupFrequencies(analyzer.groupBy(Mob::behaviorType));

        assertEquals(StreamTestData.EXPECTED_ROW_COUNT, lines.size());
        assertEquals(StreamTestData.EXPECTED_ROW_COUNT, mobs.size());
        assertEquals(Map.of(
                BehaviorType.CONDITIONAL, 11,
                BehaviorType.HOSTILE, 33,
                BehaviorType.PEACEFUL, 34), frequencies);

        String summary = frequencies.entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().name()))
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("\n", "", "\n"));

        ByteArrayOutputStream destination = new ByteArrayOutputStream();
        try (MobWriter writer = new MobWriter(new BufferedOutputStream(destination))) {
            writer.write(summary);
        }

        assertEquals(StreamTestData.EXPECTED_SUMMARY, destination.toString(StandardCharsets.UTF_8));
    }
}
