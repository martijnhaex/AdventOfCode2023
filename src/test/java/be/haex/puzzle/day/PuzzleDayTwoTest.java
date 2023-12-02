package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PuzzleDayTwoTest {

	private final Puzzle<Integer> puzzle = new PuzzleDayTwo();

	@Test
	void solvePartOne() {
		assertEquals(8, puzzle.solvePartOne());
	}

	@Test
	void solvePartTwo() {
		assertEquals(2286, puzzle.solvePartTwo());
	}
}
