package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PuzzleDayOneTest {

	private final Puzzle<Integer> puzzle = new PuzzleDayOne(
			"puzzleDayOnePartOne.txt",
			"puzzleDayOnePartTwo.txt"
	);

	@Test
	void solvePartOne() {
		assertEquals(142, puzzle.solvePartOne());
	}

	@Test
	void solvePartTwo() {
		assertEquals(281, puzzle.solvePartTwo());
	}
}
