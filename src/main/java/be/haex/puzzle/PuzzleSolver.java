package be.haex.puzzle;

import be.haex.puzzle.day.PuzzleDayFive;
import be.haex.puzzle.day.PuzzleDayFour;
import be.haex.puzzle.day.PuzzleDayOne;
import be.haex.puzzle.day.PuzzleDaySeven;
import be.haex.puzzle.day.PuzzleDaySix;
import be.haex.puzzle.day.PuzzleDayThree;
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
				Map.entry(7, new PuzzleDaySeven())
		);
	}
}
