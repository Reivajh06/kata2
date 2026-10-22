import org.junit.jupiter.api.Test;
import reivajh06.MobsAnalyzer;
import reivajh06.model.BehaviorType;
import reivajh06.model.Mob;
import reivajh06.model.ReproductiveRequirement;
import reivajh06.model.SpawnBehavior;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MobsAnalyzerTest {

    private static final List<Mob> MOBS = List.of(
            mob(1, "cow", BehaviorType.PEACEFUL, 2011),
            mob(2, "creeper", BehaviorType.HOSTILE, 2011),
            mob(3, "zombie", BehaviorType.HOSTILE, 2011),
            mob(4, "wolf", BehaviorType.CONDITIONAL, 2011),
            mob(5, "warden", BehaviorType.HOSTILE, 2022));

    private final MobsAnalyzer analyzer = new MobsAnalyzer(MOBS);

    @Test
    void groupsMobNamesByBehaviorType() {
        Map<BehaviorType, List<String>> groups = analyzer.groupBy(Mob::behaviorType);

        assertEquals(Set.of(BehaviorType.PEACEFUL, BehaviorType.HOSTILE, BehaviorType.CONDITIONAL), groups.keySet());
        assertEquals(List.of("creeper", "zombie", "warden"), groups.get(BehaviorType.HOSTILE));
        assertEquals(List.of("cow"), groups.get(BehaviorType.PEACEFUL));
        assertEquals(List.of("wolf"), groups.get(BehaviorType.CONDITIONAL));
    }

    @Test
    void groupsByDerivedKey() {
        Map<Integer, List<String>> byYear = analyzer.groupBy(mob -> mob.debutDate().getYear());

        assertEquals(List.of("cow", "creeper", "zombie", "wolf"), byYear.get(2011));
        assertEquals(List.of("warden"), byYear.get(2022));
    }

    @Test
    void computesFrequenciesOfAGrouping() {
        Map<BehaviorType, Integer> frequencies = analyzer.groupFrequencies(analyzer.groupBy(Mob::behaviorType));

        assertEquals(Map.of(
                BehaviorType.PEACEFUL, 1,
                BehaviorType.HOSTILE, 3,
                BehaviorType.CONDITIONAL, 1), frequencies);
    }

    @Test
    void frequenciesAddUpToTotalMobs() {
        Map<BehaviorType, Integer> frequencies = analyzer.groupFrequencies(analyzer.groupBy(Mob::behaviorType));
        assertEquals(MOBS.size(), frequencies.values().stream().mapToInt(Integer::intValue).sum());
    }

    @Test
    void frequenciesDoNotDependOnMobOrder() {
        List<Mob> shuffled = new ArrayList<>(MOBS);
        Collections.shuffle(shuffled, new Random(42));
        MobsAnalyzer shuffledAnalyzer = new MobsAnalyzer(shuffled);

        assertEquals(
                analyzer.groupFrequencies(analyzer.groupBy(Mob::behaviorType)),
                shuffledAnalyzer.groupFrequencies(shuffledAnalyzer.groupBy(Mob::behaviorType)));
    }

    @Test
    void filtersMobNamesByPredicate() {
        assertEquals(List.of("cow", "creeper", "zombie", "wolf"),
                analyzer.filterMobs(mob -> mob.debutDate().getYear() == 2011));
        assertTrue(analyzer.filterMobs(mob -> mob.healthPoints() > 1000).isEmpty());
    }

    @Test
    void emptyCollectionProducesEmptyResults() {
        MobsAnalyzer empty = new MobsAnalyzer(List.of());

        assertTrue(empty.groupBy(Mob::behaviorType).isEmpty());
        assertTrue(empty.groupFrequencies(empty.groupBy(Mob::behaviorType)).isEmpty());
        assertTrue(empty.filterMobs(mob -> true).isEmpty());
    }

    private static Mob mob(int id, String name, BehaviorType behaviorType, int debutYear) {
        return new Mob(id, name, behaviorType, SpawnBehavior.RANDOM, 20, 3,
                LocalDate.of(debutYear, 1, 1), "1.0.0", ReproductiveRequirement.NONE);
    }
}
