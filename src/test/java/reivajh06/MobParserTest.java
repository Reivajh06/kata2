package reivajh06;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reivajh06.model.BehaviorType;
import reivajh06.model.Mob;
import reivajh06.model.ReproductiveRequirement;
import reivajh06.model.SpawnBehavior;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("MobParser: cada fila se transforma en una entidad Mob")
class MobParserTest {

	private final MobParser parser = new MobParser(",");

	@Test
	@DisplayName("Una fila válida se convierte en un Mob con todos sus campos")
	void parsesValidLineIntoMob() {
		Optional<Mob> mob = parser.parseLine("5,chicken,Peaceful,Random,4,,2011-11-18T00:00:00,1.0.0,Wheat Seeds");

		assertEquals(Optional.of(new Mob(
				5,
				"chicken",
				BehaviorType.PEACEFUL,
				SpawnBehavior.RANDOM,
				4,
				0,
				LocalDate.of(2011, 11, 18),
				"1.0.0",
				ReproductiveRequirement.WHEAT_SEEDS
		)), mob);
	}

	@Test
	@DisplayName("Un requisito reproductivo vacío se interpreta como NONE")
	void emptyReproductiveRequirementIsNone() {
		Mob mob = parser.parseLine("4,blaze,Hostile,On Load,20,6,2011-11-18T00:00:00,1.0.0,").orElseThrow();

		assertEquals(ReproductiveRequirement.NONE, mob.reproductiveRequirement());
		assertEquals(SpawnBehavior.ON_LOAD, mob.spawnBehavior());
		assertEquals(6, mob.maxDamage());
	}

	@Test
	@DisplayName("Un fichero vacío produce un resultado vacío, sin errores")
	void emptyInputProducesEmptyResult() {
		ParseResult result = parser.parseAllLines(List.of());

		assertTrue(result.mobs().isEmpty());
		assertTrue(result.parsingErrors().isEmpty());
	}

	@Test
	@DisplayName("Las líneas en blanco se ignoran")
	void blankLinesAreIgnored() {
		ParseResult result = parser.parseAllLines(List.of(
				"",
				"3,bat,Peaceful,Random,6,,2012-10-25T00:00:00,1.4.2,"
		));

		assertEquals(1, result.mobs().size());
		assertTrue(result.parsingErrors().isEmpty());
	}

	@Test
	@DisplayName("Una fila con columnas de menos se reporta como error con su número de línea")
	void rowWithMissingColumnsIsReported() {
		ParseResult result = parser.parseAllLines(List.of(
				"3,bat,Peaceful,Random,6,,2012-10-25T00:00:00,1.4.2,",
				"4,blaze,Hostile",
				"5,chicken,Peaceful,Random,4,,2011-11-18T00:00:00,1.0.0,Wheat Seeds"
		));

		assertEquals(List.of("bat", "chicken"), result.mobs().stream().map(Mob::name).toList());
		assertInvalidRowReported(result, 2, "4,blaze,Hostile");
	}

	@Test
	@DisplayName("Una fila con un número no válido se reporta como error y no detiene el programa")
	void rowWithInvalidNumberIsReported() {
		ParseResult result = parser.parseAllLines(List.of(
				"3,bat,Peaceful,Random,six,,2012-10-25T00:00:00,1.4.2,",
				"4,blaze,Hostile,On Load,20,6,2011-11-18T00:00:00,1.0.0,"
		));

		assertEquals(List.of("blaze"), result.mobs().stream().map(Mob::name).toList());
		assertInvalidRowReported(result, 1, "3,bat,Peaceful,Random,six,,2012-10-25T00:00:00,1.4.2,");
	}

	@Test
	@DisplayName("Una fila con un valor de enumerado desconocido se reporta como error")
	void rowWithUnknownEnumValueIsReported() {
		ParseResult result = parser.parseAllLines(List.of(
				"3,bat,Friendly,Random,6,,2012-10-25T00:00:00,1.4.2,"
		));

		assertTrue(result.mobs().isEmpty());
		assertInvalidRowReported(result, 1, "3,bat,Friendly,Random,6,,2012-10-25T00:00:00,1.4.2,");
	}

	@Test
	@DisplayName("Una fila con una fecha mal formada se reporta como error")
	void rowWithInvalidDateIsReported() {
		ParseResult result = parser.parseAllLines(List.of(
				"3,bat,Peaceful,Random,6,,25/10/2012,1.4.2,"
		));

		assertTrue(result.mobs().isEmpty());
		assertInvalidRowReported(result, 1, "3,bat,Peaceful,Random,6,,25/10/2012,1.4.2,");
	}

	@Test
	@DisplayName("El separador lo decide quien crea el parser")
	void delimiterIsConfigurable() {
		MobParser tsvParser = new MobParser("\t");

		Mob mob = tsvParser.parseLine("3\tbat\tPeaceful\tRandom\t6\t\t2012-10-25T00:00:00\t1.4.2\t").orElseThrow();

		assertEquals("bat", mob.name());
	}

	private static void assertInvalidRowReported(ParseResult result, int nLine, String line) {
		assertEquals(1, result.parsingErrors().size());

		ParseError error = result.parsingErrors().getFirst();
		assertEquals(nLine, error.nLine());
		assertEquals(line, error.line());
		assertTrue(error.errorMessage().contains(String.valueOf(nLine)), "El mensaje debe indicar la línea: " + error.errorMessage());
		assertTrue(error.errorMessage().contains(line), "El mensaje debe incluir la fila: " + error.errorMessage());
	}
}
