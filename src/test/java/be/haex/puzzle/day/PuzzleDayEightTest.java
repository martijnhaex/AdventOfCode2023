package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PuzzleDayEightTest {

	private final Puzzle<Long> puzzle = new PuzzleDayEight("puzzleDayEightPartOne.txt", "puzzleDayEightPartTwo.txt");

	@Test
	void solvePartOne() {
		assertEquals(6, puzzle.solvePartOne());
	}

	@Test
	void solvePartTwo() {
		assertEquals(6, puzzle.solvePartTwo());
	}
}