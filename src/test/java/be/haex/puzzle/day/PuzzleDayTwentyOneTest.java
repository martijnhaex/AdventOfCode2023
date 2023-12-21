package be.haex.puzzle.day;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PuzzleDayTwentyOneTest {

	@Test
	void solvePartOne() {
		assertEquals(2_736, new PuzzleDayTwentyOne("puzzleDayTwentyOneExampleOne.txt").solvePartOne());
		assertEquals(3_746, new PuzzleDayTwentyOne().solvePartOne());
	}

	@Test
	void solvePartTwo() {
		assertEquals(623_540_829_615_589L, new PuzzleDayTwentyOne().solvePartTwo());
	}
}