package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PuzzleDayThirteenTest {

	private final Puzzle<Long> puzzle = new PuzzleDayThirteen();

	@Test
	void solvePartOne() {
		assertEquals(405, puzzle.solvePartOne());
	}

	@Test
	void solvePartTwo() {
		assertEquals(400, puzzle.solvePartTwo());
	}
}