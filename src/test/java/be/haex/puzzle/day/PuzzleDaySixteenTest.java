package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PuzzleDaySixteenTest {

	private final Puzzle<Long> puzzle = new PuzzleDaySixteen();

	@Test
	void solvePartOne() {
		assertEquals(46, puzzle.solvePartOne());
	}

	@Test
	void solvePartTwo() {
		assertEquals(51, puzzle.solvePartTwo());
	}
}