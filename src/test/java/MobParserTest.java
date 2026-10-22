import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reivajh06.MobParser;
import reivajh06.model.BehaviorType;
import reivajh06.model.Mob;
import reivajh06.model.ReproductiveRequirement;
import reivajh06.model.SpawnBehavior;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MobParserTest {

    private final MobParser parser = new MobParser(",");

    @Test
    void parsesCompleteRowIntoEntity() {
        Mob mob = parser.parseLine("5,chicken,Peaceful,Random,4,,2011-11-18T00:00:00,1.0.0,Wheat Seeds");

        assertEquals(new Mob(
                5,
                "chicken",
                BehaviorType.PEACEFUL,
                SpawnBehavior.RANDOM,
                4,
                0,
                LocalDate.of(2011, 11, 18),
                "1.0.0",
                ReproductiveRequirement.WHEAT_SEEDS), mob);
    }

    @Test
    void parsesMultiWordEnumValues() {
        Mob mob = parser.parseLine("45,iron_golem,Conditional,Player Summoned,100,22,2012-03-01T00:00:00,1.2.1,");

        assertEquals(BehaviorType.CONDITIONAL, mob.behaviorType());
        assertEquals(SpawnBehavior.PLAYER_SUMMONED, mob.spawnBehavior());
        assertEquals(22, mob.maxDamage());
    }

    @Test
    void emptyMaxDamageDefaultsToZero() {
        Mob mob = parser.parseLine("3,bat,Peaceful,Random,6,,2012-10-25T00:00:00,1.4.2,");
        assertEquals(0, mob.maxDamage());
    }

    @Test
    void missingReproductiveRequirementIsNone() {
        Mob withTrailingDelimiter = parser.parseLine("4,blaze,Hostile,On Load,20,6,2011-11-18T00:00:00,1.0.0,");
        Mob withoutTrailingDelimiter = parser.parseLine("4,blaze,Hostile,On Load,20,6,2011-11-18T00:00:00,1.0.0");

        assertEquals(ReproductiveRequirement.NONE, withTrailingDelimiter.reproductiveRequirement());
        assertEquals(ReproductiveRequirement.NONE, withoutTrailingDelimiter.reproductiveRequirement());
    }

    @Test
    void windowsLineEndingDoesNotChangeEntity() {
        String row = "21,cow,Peaceful,Random,10,,2011-11-18T00:00:00,1.0.0,Wheat";
        assertEquals(parser.parseLine(row), parser.parseLine(row + "\r"));
    }

    @Test
    void usesConfiguredDelimiter() {
        MobParser semicolonParser = new MobParser(";");
        String row = "21;cow;Peaceful;Random;10;;2011-11-18T00:00:00;1.0.0;Wheat";

        assertEquals(parser.parseLine(row.replace(';', ',')), semicolonParser.parseLine(row));
    }

    @Test
    void parseAllLinesKeepsCountAndOrder() {
        List<Mob> mobs = parser.parseAllLines(List.of(
                "3,bat,Peaceful,Random,6,,2012-10-25T00:00:00,1.4.2,",
                "4,blaze,Hostile,On Load,20,6,2011-11-18T00:00:00,1.0.0,"));

        assertEquals(List.of("bat", "blaze"), mobs.stream().map(Mob::name).toList());
    }

    @Test
    void parseAllLinesOfEmptyCollectionIsEmpty() {
        assertTrue(parser.parseAllLines(List.of()).isEmpty());
    }

    @Test
    void parsesEveryRowOfTheDataset() {
        List<Mob> mobs = parser.parseAllLines(StreamTestData.ROWS);
        assertEquals(StreamTestData.EXPECTED_ROW_COUNT, mobs.size());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "5,chicken,Peaceful",
            "x,chicken,Peaceful,Random,4,,2011-11-18T00:00:00,1.0.0,",
            "5,chicken,Peaceful,Random,many,,2011-11-18T00:00:00,1.0.0,",
            "5,chicken,Friendly,Random,4,,2011-11-18T00:00:00,1.0.0,",
            "5,chicken,Peaceful,Sometimes,4,,2011-11-18T00:00:00,1.0.0,",
            "5,chicken,Peaceful,Random,4,,yesterday,1.0.0,",
            "5,chicken,Peaceful,Random,4,,2011-13-18T00:00:00,1.0.0,",
            "5,chicken,Peaceful,Random,4,,2011-11-18T00:00:00,1.0.0,Diamonds"
    })
    void malformedRowFailsWithMessageIdentifyingTheRow(String malformedRow) {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> parser.parseLine(malformedRow));

        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains(malformedRow),
                "The error message should identify the invalid row, but was: " + exception.getMessage());
    }

    @Test
    void malformedRowAmongValidOnesIsReported() {
        List<String> rows = List.of(
                "3,bat,Peaceful,Random,6,,2012-10-25T00:00:00,1.4.2,",
                "x,broken");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> parser.parseAllLines(rows));
        assertTrue(exception.getMessage().contains("x,broken"),
                "The error message should identify the invalid row, but was: " + exception.getMessage());
    }
}
