package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PuzzleDayFourTest {

	private final Puzzle<Long> puzzle = new PuzzleDayFour();

	@Test
	void solvePartOne() {
		assertEquals(13, puzzle.solvePartOne());
	}

	@Test
	void solvePartTwo() {
		assertEquals(30, puzzle.solvePartTwo());
	}
}