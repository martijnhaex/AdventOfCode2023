package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PuzzleDayNineteenTest {

	private final Puzzle<Long> puzzle = new PuzzleDayNineteen();

	@Test
	void solvePartOne() {
		assertEquals(19_114, puzzle.solvePartOne());
	}

	@Test
	void solvePartTwo() {
		assertEquals(167_409_079_868_000L, puzzle.solvePartTwo());
	}
}