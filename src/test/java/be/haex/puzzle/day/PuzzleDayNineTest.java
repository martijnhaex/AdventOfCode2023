package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PuzzleDayNineTest {

	private final Puzzle<Long> puzzle = new PuzzleDayNine();

	@Test
	void solvePartOne() {
		assertEquals(114, puzzle.solvePartOne());
	}

	@Test
	void solvePartTwo() {
		assertEquals(2, puzzle.solvePartTwo());
	}
}