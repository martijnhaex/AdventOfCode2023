package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.function.BinaryOperator.maxBy;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;

public class PuzzleDayTwo implements Puzzle<Integer> {

	@Override
	public Integer solvePartOne() {
		var gameContent = new CubeSet(List.of(new Cube("red", 12), new Cube("green", 13), new Cube("blue", 14)));

		return readGames()
				.filter(game -> game.isValid(gameContent))
				.mapToInt(Game::id)
				.sum();
	}

	private Stream<Game> readGames() {
		return readContentOfInputFile("puzzleDayTwo.txt")
				.stream()
				.map(Game::parse);
	}

	@Override
	public Integer solvePartTwo() {
		return readGames()
				.map(Game::minimumRequiredCubeSet)
				.mapToInt(cubeSet -> cubeSet.cubes().stream().mapToInt(Cube::amount).reduce(1, (a, b) -> a * b))
				.sum();
	}

	private record Game(int id, List<CubeSet> sets) {

		private static final Pattern GAME_ID_PATTERN = Pattern.compile("^Game (\\d+):([ \\d+ \\w+,?]+;?)+");

		public static Game parse(String input) {
			var gameIdMatcher = GAME_ID_PATTERN.matcher(input);

			if (!gameIdMatcher.matches()) {
				throw new IllegalArgumentException("Invalid game input: %s".formatted(input));
			}

			var id = Integer.parseInt(gameIdMatcher.group(1));
			var sets = Arrays.stream(input.substring(input.indexOf(":") + 1).split(";"))
					.map(CubeSet::parse)
					.toList();

			return new Game(id, sets);
		}

		public boolean isValid(CubeSet gameContent) {
			return sets.stream()
					.allMatch(set -> set.isValid(gameContent));
		}

		public CubeSet minimumRequiredCubeSet() {
			var cubes = sets.stream()
					.map(CubeSet::cubes)
					.flatMap(Collection::stream)
					.collect(groupingBy(Cube::color, reducing(maxBy(comparing(Cube::amount)))))
					.values()
					.stream()
					.flatMap(Optional::stream)
					.toList();

			return new CubeSet(cubes);
		}
	}

	private record CubeSet(List<Cube> cubes) {

		public static CubeSet parse(String input) {
			var cubes = Arrays.stream(input.split(","))
					.map(Cube::parse)
					.toList();

			return new CubeSet(cubes);
		}

		public boolean isValid(CubeSet gameContent) {
			return cubes.stream()
					.allMatch(cube -> gameContent.cubes().stream().filter(content -> content.color().equals(cube.color)).findFirst().filter(content -> content.amount() >= cube.amount).isPresent());
		}
	}

	private record Cube(String color, int amount) {

		private static final Pattern CUBE_PATTERN = Pattern.compile("(\\d+) (\\w+)");

		public static Cube parse(String input) {
			var cubeMatcher = CUBE_PATTERN.matcher(input.trim());

			if (!cubeMatcher.matches()) {
				throw new IllegalArgumentException("Invalid cube input: %s".formatted(input));
			}

			return new Cube(
					cubeMatcher.group(2),
					Integer.parseInt(cubeMatcher.group(1))
			);
		}
	}
}
