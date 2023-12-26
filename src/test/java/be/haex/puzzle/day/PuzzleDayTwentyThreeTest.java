package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PuzzleDayTwentyThreeTest {

	private final Puzzle<Integer> puzzle = new PuzzleDayTwentyThree();

	@Test
	void solvePartOne() {
		assertEquals(94, puzzle.solvePartOne());
	}

	@Test
	void solvePartTwo() {
		assertEquals(154, puzzle.solvePartTwo());
	}
}