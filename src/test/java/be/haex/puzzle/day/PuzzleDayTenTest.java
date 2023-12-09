package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PuzzleDayTenTest {

	private final Puzzle<Long> puzzle = new PuzzleDayTen(
			"puzzleDayTenPartOne.txt",
			"puzzleDayTenPartTwo.txt"
	);

	@Test
	void solvePartOne() {
		assertEquals(4, puzzle.solvePartOne());
	}

	@Test
	void solvePartTwo() {
		assertEquals(8, puzzle.solvePartTwo());
	}
}