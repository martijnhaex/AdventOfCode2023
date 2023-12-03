package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PuzzleDayThreeTest {

	private final Puzzle<Integer> puzzle = new PuzzleDayThree();

	@Test
	void solvePartOne() {
		assertEquals(4361, puzzle.solvePartOne());
	}

	@Test
	void solvePartTwo() {
		assertEquals(467_835, puzzle.solvePartTwo());
	}
}