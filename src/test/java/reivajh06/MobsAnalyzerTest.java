package reivajh06;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reivajh06.model.BehaviorType;
import reivajh06.model.Mob;
import reivajh06.model.SpawnBehavior;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("MobsAnalyzer: resumen calculado a partir de una colección de entidades")
class MobsAnalyzerTest {

	private final List<Mob> mobs = TestData.parse(TestData.memoryLoader(TestData.SMALL_CSV));
	private final MobsAnalyzer analyzer = new MobsAnalyzer(mobs);

	@Test
	@DisplayName("Agrupa los nombres de los mobs por comportamiento")
	void groupsNamesByBehavior() {
		Map<BehaviorType, List<String>> groups = analyzer.groupBy(Mob::behaviorType);

		assertEquals(Map.of(
				BehaviorType.PEACEFUL, List.of("bat", "chicken"),
				BehaviorType.HOSTILE, List.of("blaze", "creeper"),
				BehaviorType.CONDITIONAL, List.of("dolphin")
		), groups);
	}

	@Test
	@DisplayName("Calcula la frecuencia de cada grupo")
	void computesGroupFrequencies() {
		assertEquals(TestData.SMALL_CSV_FREQUENCIES, analyzer.groupFrequencies(analyzer.groupBy(Mob::behaviorType)));
	}

	@Test
	@DisplayName("Agrupa por cualquier columna significativa")
	void groupsByAnyColumn() {
		Map<SpawnBehavior, Integer> frequencies = analyzer.groupFrequencies(analyzer.groupBy(Mob::spawnBehavior));

		assertEquals(Map.of(
				SpawnBehavior.RANDOM, 3,
				SpawnBehavior.ON_LOAD, 1,
				SpawnBehavior.NIGHT, 1
		), frequencies);
	}

	@Test
	@DisplayName("Filtra mobs según un predicado")
	void filtersMobs() {
		assertEquals(List.of("blaze", "creeper"), analyzer.filterMobs(m -> m.healthPoints() >= 20));
	}

	@Test
	@DisplayName("Una colección vacía produce un resumen vacío")
	void emptyCollectionProducesEmptySummary() {
		MobsAnalyzer empty = new MobsAnalyzer(List.of());

		assertTrue(empty.groupBy(Mob::behaviorType).isEmpty());
		assertTrue(empty.groupFrequencies(empty.groupBy(Mob::behaviorType)).isEmpty());
		assertTrue(empty.filterMobs(_ -> true).isEmpty());
	}
}
