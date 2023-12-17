package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PuzzleDaySeventeenTest {

	private final Puzzle<Integer> puzzle = new PuzzleDaySeventeen();

	@Test
	void solvePartOne() {
		assertEquals(102, puzzle.solvePartOne());
	}

	@Test
	void solvePartTwo() {
		assertEquals(94, puzzle.solvePartTwo());
	}
}