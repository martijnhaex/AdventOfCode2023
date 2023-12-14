package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PuzzleDayFourteenTest {

	private final Puzzle<Long> puzzle = new PuzzleDayFourteen();

	@Test
	void solvePartOne() {
		assertEquals(136, puzzle.solvePartOne());
	}

	@Test
	void solvePartTwo() {
		assertEquals(64, puzzle.solvePartTwo());
	}
}