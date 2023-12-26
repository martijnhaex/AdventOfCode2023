package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PuzzleDayTwentyTwoTest {

	private final Puzzle<Long> puzzle = new PuzzleDayTwentyTwo();

	@Test
	void solvePartOne() {
		assertEquals(5, puzzle.solvePartOne());
	}

	@Test
	void solvePartTwo() {
		assertEquals(7, puzzle.solvePartTwo());
	}
}