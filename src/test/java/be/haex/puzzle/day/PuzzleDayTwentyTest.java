package be.haex.puzzle.day;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PuzzleDayTwentyTest {

	@Test
	void solvePartOne() {
		assertEquals(32_000_000, new PuzzleDayTwenty("puzzleDayTwentyExampleOne.txt").solvePartOne());
		assertEquals(11_687_500, new PuzzleDayTwenty("puzzleDayTwentyExampleTwo.txt").solvePartOne());
		assertEquals(806_332_748, new PuzzleDayTwenty("puzzleDayTwentyMyInput.txt").solvePartOne());
	}

	@Test
	void solvePartTwo() {
		assertEquals(228_060_006_554_227L, new PuzzleDayTwenty("puzzleDayTwentyMyInput.txt").solvePartTwo());
	}
}