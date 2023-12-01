package be.haex.puzzle;

import be.haex.puzzle.day.PuzzleDayOne;

import java.util.Map;
import java.util.Optional;

public class PuzzleSolver {

	private static final Map<Integer, Puzzle<?>> PUZZLES;

	public static void main(String[] args) {
		var day = Integer.valueOf(args[0]);

		System.out.println("Advent of Code 2023 --- Day " + day);

		var puzzle = loadPuzzle(day);
		System.out.println("Your puzzle answer for Part One is " + puzzle.solvePartOne());
		System.out.println("Your puzzle answer for Part Two is " + puzzle.solvePartTwo());
	}

	private static Puzzle<?> loadPuzzle(Integer dayOfAdvent) {
		return Optional.ofNullable(PUZZLES.get(dayOfAdvent))
				.orElseThrow(() -> new IllegalArgumentException("Cannot load puzzle for day <" + dayOfAdvent + "> of advent 2023!"));
	}

	static {
		PUZZLES = Map.ofEntries(
				Map.entry(1, new PuzzleDayOne())
		);
	}
}
