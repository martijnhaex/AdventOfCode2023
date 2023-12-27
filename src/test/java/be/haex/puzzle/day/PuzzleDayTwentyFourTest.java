package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PuzzleDayTwentyFourTest {

	private final Puzzle<Long> examplePuzzle = new PuzzleDayTwentyFour(7, 27, "puzzleDayTwentyFourExample.txt");
	private final Puzzle<Long> realPuzzle = new PuzzleDayTwentyFour();

	@Test
	void solvePartOne() {
		assertEquals(2, examplePuzzle.solvePartOne());
		assertEquals(24_192, realPuzzle.solvePartOne());
	}

	@Test
	void solvePartTwo() {
		assertEquals(68, examplePuzzle.solvePartTwo());
		assertEquals(664_822_352_550_558L, realPuzzle.solvePartTwo());
	}
}