package reivajh06;

import reivajh06.model.Mob;

import java.util.List;

public record ParseResult(List<Mob> mobs, List<ParseError> parsingErrors) {
}
