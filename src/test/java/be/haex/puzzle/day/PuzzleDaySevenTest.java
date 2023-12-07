package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PuzzleDaySevenTest {

	private final Puzzle<Integer> puzzle = new PuzzleDaySeven();

	@Test
	void solvePartOne() {
		assertEquals(6440, puzzle.solvePartOne());
	}

	@Test
	void solvePartTwo() {
		assertEquals(5905, puzzle.solvePartTwo());
	}
}