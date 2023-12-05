package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PuzzleDayFiveTest {

	private final Puzzle<Long> puzzle = new PuzzleDayFive();

	@Test
	void solvePartOne() {
		assertEquals(35, puzzle.solvePartOne());
	}

	@Test
	void solvePartTwo() {
		assertEquals(46, puzzle.solvePartTwo());
	}
}