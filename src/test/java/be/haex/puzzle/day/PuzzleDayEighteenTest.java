package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PuzzleDayEighteenTest {

	private final Puzzle<Long> puzzle = new PuzzleDayEighteen();

	@Test
	void solvePartOne() {
		assertEquals(62, puzzle.solvePartOne());
	}

	@Test
	void solvePartTwo() {
		assertEquals(952_408_144_115L, puzzle.solvePartTwo());
	}
}