package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PuzzleDayTwelveTest {

	private final Puzzle<Long> puzzle = new PuzzleDayTwelve();

	@Test
	void solvePartOne() {
		assertEquals(21, puzzle.solvePartOne());
	}

	@Test
	void solvePartTwo() {
		assertEquals(525_152, puzzle.solvePartTwo());
	}
}