package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PuzzleDayElevenTest {

	private final Puzzle<Long> puzzle = new PuzzleDayEleven();

	@Test
	void solvePartOne() {
		assertEquals(374, puzzle.solvePartOne());
	}

	@Test
	void solvePartTwo() {
		assertEquals(82_000_210, puzzle.solvePartTwo());
	}
}