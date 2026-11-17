package reivajh06.kata2;

import reivajh06.kata2.model.Mob;

import java.util.List;

public record ParseResult(List<Mob> mobs, List<ParseError> parsingErrors) {
}
