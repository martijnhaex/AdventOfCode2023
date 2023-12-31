package be.haex.puzzle;

import be.haex.puzzle.day.PuzzleDayEight;
import be.haex.puzzle.day.PuzzleDayEighteen;
import be.haex.puzzle.day.PuzzleDayEleven;
import be.haex.puzzle.day.PuzzleDayFifteen;
import be.haex.puzzle.day.PuzzleDayFive;
import be.haex.puzzle.day.PuzzleDayFour;
import be.haex.puzzle.day.PuzzleDayFourteen;
import be.haex.puzzle.day.PuzzleDayNine;
import be.haex.puzzle.day.PuzzleDayNineteen;
import be.haex.puzzle.day.PuzzleDayOne;
import be.haex.puzzle.day.PuzzleDaySeven;
import be.haex.puzzle.day.PuzzleDaySeventeen;
import be.haex.puzzle.day.PuzzleDaySix;
import be.haex.puzzle.day.PuzzleDaySixteen;
import be.haex.puzzle.day.PuzzleDayTen;
import be.haex.puzzle.day.PuzzleDayThirteen;
import be.haex.puzzle.day.PuzzleDayThree;
import be.haex.puzzle.day.PuzzleDayTwelve;
import be.haex.puzzle.day.PuzzleDayTwenty;
import be.haex.puzzle.day.PuzzleDayTwentyFour;
import be.haex.puzzle.day.PuzzleDayTwentyOne;
import be.haex.puzzle.day.PuzzleDayTwentyThree;
import be.haex.puzzle.day.PuzzleDayTwentyTwo;
import be.haex.puzzle.day.PuzzleDayTwo;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class PuzzleSolver {

	private static final Map<Integer, Puzzle<?>> PUZZLES;

	public static void main(String[] args) {
		var day = Integer.valueOf(args[0]);

		System.out.println("Advent of Code 2023 --- Day " + day);

		var puzzle = loadPuzzle(day);

		var solutionPartOne = solve(puzzle, Puzzle::solvePartOne);
		System.out.printf("Your puzzle answer for Part One is %s (in %s)%n", solutionPartOne.value(), solutionPartOne.duration());

		var solutionPartTwo = solve(puzzle, Puzzle::solvePartTwo);
		System.out.printf("Your puzzle answer for Part Two is %s (in %s)%n", solutionPartTwo.value(), solutionPartTwo.duration());
	}

	private static Puzzle<?> loadPuzzle(Integer dayOfAdvent) {
		return Optional.ofNullable(PUZZLES.get(dayOfAdvent))
				.orElseThrow(() -> new IllegalArgumentException("Cannot load puzzle for day <" + dayOfAdvent + "> of advent 2023!"));
	}

	private static Solution<?> solve(Puzzle<?> puzzle, Function<Puzzle<?>, ?> solver) {
		var start = Instant.now();
		var solution = solver.apply(puzzle);
		var stop = Instant.now();

		return new Solution<>(solution, Duration.between(start, stop));
	}

	private record Solution<T>(T value, Duration duration) {
	}

	static {
		PUZZLES = Map.ofEntries(
				Map.entry(1, new PuzzleDayOne()),
				Map.entry(2, new PuzzleDayTwo()),
				Map.entry(3, new PuzzleDayThree()),
				Map.entry(4, new PuzzleDayFour()),
				Map.entry(5, new PuzzleDayFive()),
				Map.entry(6, new PuzzleDaySix()),
				Map.entry(7, new PuzzleDaySeven()),
				Map.entry(8, new PuzzleDayEight()),
				Map.entry(9, new PuzzleDayNine()),
				Map.entry(10, new PuzzleDayTen()),
				Map.entry(11, new PuzzleDayEleven()),
				Map.entry(12, new PuzzleDayTwelve()),
				Map.entry(13, new PuzzleDayThirteen()),
				Map.entry(14, new PuzzleDayFourteen()),
				Map.entry(15, new PuzzleDayFifteen()),
				Map.entry(16, new PuzzleDaySixteen()),
				Map.entry(17, new PuzzleDaySeventeen()),
				Map.entry(18, new PuzzleDayEighteen()),
				Map.entry(19, new PuzzleDayNineteen()),
				Map.entry(20, new PuzzleDayTwenty()),
				Map.entry(21, new PuzzleDayTwentyOne()),
				Map.entry(22, new PuzzleDayTwentyTwo()),
				Map.entry(23, new PuzzleDayTwentyThree()),
				Map.entry(24, new PuzzleDayTwentyFour())
		);
	}
}
