package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PuzzleDaySixTest {

	private final Puzzle<Long> puzzle = new PuzzleDaySix();

	@Test
	void solvePartOne() {
		assertEquals(288, puzzle.solvePartOne());
	}

	@Test
	void solvePartTwo() {
		assertEquals(71503, puzzle.solvePartTwo());
	}
}