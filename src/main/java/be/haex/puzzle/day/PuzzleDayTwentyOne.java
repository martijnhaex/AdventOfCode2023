package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.function.Predicate.not;

public class PuzzleDayTwentyOne implements Puzzle<Long> {

	private static final String INPUT_FILE_NAME = "puzzleDayTwentyOne.txt";

	private final String fileName;

	PuzzleDayTwentyOne(String fileName) {
		this.fileName = fileName;
	}

	public PuzzleDayTwentyOne() {
		this(INPUT_FILE_NAME);
	}

	@Override
	public Long solvePartOne() {
		return readGardenMap()
				.countReachableGardenPlots(64);
	}

	private GardenMap readGardenMap() {
		return GardenMap.parse(readContentOfInputFile(fileName));
	}

	@Override
	public Long solvePartTwo() {
		return readGardenMap()
				.countReachableGardenPlots(26_501_365);
	}

	private record GardenMap(Position startingPosition, Map<Position, Tile> tiles) {

		public static GardenMap parse(List<String> input) {
			var tiles = new HashMap<Position, Tile>();
			Position startingPosition = null;

			for (var x = 0; x < input.size(); x++) {
				var row = input.get(x);

				for (var y = 0; y < row.length(); y++) {
					var position = new Position(x, y);
					var tileType = Tile.from(row.charAt(y));

					if (tileType == Tile.STARTING_POSITION) {
						startingPosition = position;
					}

					tiles.put(position, tileType);
				}
			}

			return new GardenMap(startingPosition, tiles);
		}

		public long countReachableGardenPlots(int amountOfSteps) {
			var gridSize = gridSize();

			if (amountOfSteps < 2 * gridSize) {
				return countReachableGardenPlots(amountOfSteps, gridSize);
			} else {
				return countReachableGardenPlotsWithQuadraticEquation(amountOfSteps, gridSize);
			}
		}

		private int gridSize() {
			var amountOfRows = tiles.keySet()
					.stream()
					.mapToInt(Position::x)
					.max()
					.orElse(0);
			var amountOfColumns = tiles.keySet()
					.stream()
					.mapToInt(Position::y)
					.max()
					.orElse(0);

			assert amountOfRows == amountOfColumns;
			assert amountOfRows != 0;

			return amountOfRows + 1;
		}

		private long countReachableGardenPlots(int amountOfSteps, int gridSize) {
			var reachedGardenPlots = new HashSet<Position>();
			reachedGardenPlots.add(startingPosition);

			for (var step = 0; step < amountOfSteps; step++) {
				var newlyReachedGardenPlots = takeStep(reachedGardenPlots, gridSize);

				reachedGardenPlots.clear();
				reachedGardenPlots.addAll(newlyReachedGardenPlots);
			}

			return reachedGardenPlots.size();
		}

		private Set<Position> takeStep(Set<Position> startingPositions, int gridSize) {
			var reachedGardenPlots = new HashSet<Position>();

			var previouslyReachedGardenPlots = new LinkedList<>(startingPositions);
			while (!previouslyReachedGardenPlots.isEmpty()) {
				var currentPosition = previouslyReachedGardenPlots.poll();

				Arrays.stream(Direction.values())
						.map(currentPosition::move)
						.filter(not(nextPosition -> tiles.get(new Position(
								(nextPosition.x() % gridSize + gridSize) % gridSize,
								(nextPosition.y() % gridSize + gridSize) % gridSize
						)) == Tile.ROCK))
						.forEach(reachedGardenPlots::add);
			}

			return reachedGardenPlots;
		}

		private long countReachableGardenPlotsWithQuadraticEquation(int amountOfSteps, int gridSize) {
			var remainder = amountOfSteps % gridSize;
			var amountOfGrids = amountOfSteps / gridSize;
			var reachedGardenPlots = new HashSet<Position>();
			reachedGardenPlots.add(startingPosition);

			var stepsTaken = 0;
			var reachedGardenPlotCounts = new ArrayList<Long>();
			for (var i = 0; i < 3; i++) {
				while (stepsTaken < gridSize * i + remainder) {
					var newlyReachedGardenPlots = takeStep(reachedGardenPlots, gridSize);

					stepsTaken++;

					reachedGardenPlots.clear();
					reachedGardenPlots.addAll(newlyReachedGardenPlots);
				}

				reachedGardenPlotCounts.add((long) reachedGardenPlots.size());
			}

			return quadraticEquation(reachedGardenPlotCounts)
					.solve(amountOfGrids);
		}

		private QuadraticEquation quadraticEquation(List<Long> reachedGardenPlotCounts) {
			var c = reachedGardenPlotCounts.get(0);
			var aPlusB = reachedGardenPlotCounts.get(1) - c;
			var fourAPlusTwoB = reachedGardenPlotCounts.get(2) - c;
			var twoA = fourAPlusTwoB - (2 * aPlusB);
			var a = twoA / 2;
			var b = aPlusB - a;

			return new QuadraticEquation(a, b, c);
		}
	}

	private enum Tile {
		GARDEN_PLOT,
		ROCK,
		STARTING_POSITION;

		public static Tile from(char code) {
			return switch (code) {
				case '.' -> GARDEN_PLOT;
				case '#' -> ROCK;
				case 'S' -> STARTING_POSITION;
				default -> throw new IllegalArgumentException("Unknown tile type: " + code);
			};
		}
	}

	private record Position(int x, int y) {

		public Position move(Direction direction) {
			return switch (direction) {
				case NORTH -> new Position(x - 1, y);
				case EAST -> new Position(x, y + 1);
				case SOUTH -> new Position(x + 1, y);
				case WEST -> new Position(x, y - 1);
			};
		}
	}

	private enum Direction {
		NORTH,
		EAST,
		SOUTH,
		WEST
	}

	private record QuadraticEquation(long a, long b, long c) {

		public long solve(long x) {
			// a * x^2 + b * x + c
			return a * (x * x) + b * x + c;
		}
	}
}
