package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PuzzleDayFifteenTest {

	private final Puzzle<Integer> puzzle = new PuzzleDayFifteen();

	@Test
	void solvePartOne() {
		assertEquals(1320, puzzle.solvePartOne());
	}

	@Test
	void solvePartTwo() {
		assertEquals(145, puzzle.solvePartTwo());
	}
}