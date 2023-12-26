package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.lang.Math.max;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toMap;

public class PuzzleDayTwentyThree implements Puzzle<Integer> {

	@Override
	public Integer solvePartOne() {
		return readHikingTrails()
				.longestPath(new LongestPathFindingStrategy.Naive());
	}

	private HikingTrails readHikingTrails() {
		return HikingTrails.parse(readContentOfInputFile("puzzleDayTwentyThree.txt"));
	}

	@Override
	public Integer solvePartTwo() {
		return readHikingTrails()
				.treatSlopesAsPaths()
				.longestPath(new LongestPathFindingStrategy.DFS());
	}

	private record HikingTrails(Map<Position, Tile> tiles) {

		public static HikingTrails parse(List<String> input) {
			var tiles = new HashMap<Position, Tile>();

			for (var row = 0; row < input.size(); row++) {
				var line = input.get(row);

				for (var column = 0; column < line.length(); column++) {
					var position = new Position(row, column);
					var tile = Tile.parse(line.charAt(column));

					tiles.put(position, tile);
				}
			}

			return new HikingTrails(tiles);
		}

		public HikingTrails treatSlopesAsPaths() {
			var updatedTiles = tiles.entrySet()
					.stream()
					.map(entry -> Map.entry(
							entry.getKey(),
							switch (entry.getValue()) {
								case SLOPE_UP, SLOPE_RIGHT, SLOPE_DOWN, SLOPE_LEFT -> Tile.PATH;
								default -> entry.getValue();
							}
					))
					.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

			return new HikingTrails(updatedTiles);
		}

		public int longestPath(LongestPathFindingStrategy strategy) {
			var start = determineSinglePathOnFirstRow();
			var end = determineSinglePathOnLastRow();

			return strategy.determineLongestPath(start, end, tiles);
		}

		private Position determineSinglePathOnFirstRow() {
			return singlePathOnRow(0);
		}

		private Position singlePathOnRow(int row) {
			var positions = determineAllPathsOnRow(row);

			if (positions.size() != 1) {
				throw new IllegalArgumentException("Expected exactly one path on row " + row + ", but found " + positions.size());
			}

			return positions.getFirst();
		}

		private List<Position> determineAllPathsOnRow(int row) {
			return tiles.entrySet()
					.stream()
					.filter(entry -> entry.getKey().x() == row)
					.filter(entry -> entry.getValue() == Tile.PATH)
					.map(Map.Entry::getKey)
					.toList();
		}

		private Position determineSinglePathOnLastRow() {
			var lastRow = tiles.keySet()
					.stream()
					.mapToInt(Position::x)
					.distinct()
					.max()
					.orElseThrow(() -> new IllegalArgumentException("No tiles found"));

			return singlePathOnRow(lastRow);
		}
	}

	private record Position(int x, int y) {

		private Position move(Direction direction) {
			return switch (direction) {
				case NORTH -> new Position(x - 1, y);
				case EAST -> new Position(x, y + 1);
				case SOUTH -> new Position(x + 1, y);
				case WEST -> new Position(x, y - 1);
			};
		}
	}

	private enum Tile {
		PATH,
		FOREST,
		SLOPE_UP,
		SLOPE_RIGHT,
		SLOPE_DOWN,
		SLOPE_LEFT;

		public static Tile parse(char character) {
			return switch (character) {
				case '.' -> PATH;
				case '#' -> FOREST;
				case '^' -> SLOPE_UP;
				case '>' -> SLOPE_RIGHT;
				case 'v' -> SLOPE_DOWN;
				case '<' -> SLOPE_LEFT;
				default -> throw new IllegalArgumentException("Unknown tile: " + character);
			};
		}

		public Stream<Direction> nextDirections() {
			return switch (this) {
				case PATH -> Stream.of(Direction.values());
				case FOREST -> Stream.empty();
				case SLOPE_UP -> Stream.of(Direction.NORTH);
				case SLOPE_RIGHT -> Stream.of(Direction.EAST);
				case SLOPE_DOWN -> Stream.of(Direction.SOUTH);
				case SLOPE_LEFT -> Stream.of(Direction.WEST);
			};
		}
	}

	private enum Direction {
		NORTH,
		EAST,
		SOUTH,
		WEST
	}

	private sealed interface LongestPathFindingStrategy permits LongestPathFindingStrategy.DFS, LongestPathFindingStrategy.Naive {

		int determineLongestPath(Position start, Position end, Map<Position, Tile> tiles);

		record DFS() implements LongestPathFindingStrategy {

			@Override
			public int determineLongestPath(Position start, Position end, Map<Position, Tile> tiles) {
				var graph = asGraph(start, end, tiles);

				return longestPathBetween(start, end, graph, new HashSet<>());
			}

			private Map<Position, List<Path>> asGraph(Position start, Position end, Map<Position, Tile> tiles) {
				var graph = new HashMap<Position, List<Path>>();
				var edges = Stream.concat(
						Stream.of(start, end),
						determineEdges(tiles)
				).toList();

				edges.forEach(edge -> graph.put(edge, explore(edge, edges, tiles)));

				return graph;
			}

			private Stream<Position> determineEdges(Map<Position, Tile> tiles) {
				return tiles.keySet()
						.stream()
						.filter(position -> excludeForest().test(position, tiles))
						.filter(position -> threeOrMoreEdges().test(position, tiles));
			}

			private BiPredicate<Position, Map<Position, Tile>> excludeForest() {
				return (position, tiles) -> tiles.get(position) != Tile.FOREST;
			}

			private BiPredicate<Position, Map<Position, Tile>> threeOrMoreEdges() {
				return (position, tiles) -> tiles.get(position)
						.nextDirections()
						.filter(direction -> withinBounds().and(excludeForest()).test(position.move(direction), tiles))
						.count() >= 3;
			}

			private BiPredicate<Position, Map<Position, Tile>> withinBounds() {
				return (position, tiles) -> tiles.containsKey(position);
			}

			private List<Path> explore(Position edge, List<Position> edges, Map<Position, Tile> tiles) {
				var paths = new ArrayList<Path>();
				var exploringNeeded = new ArrayDeque<Path>();
				exploringNeeded.add(new Path(edge, 0));
				var explored = new HashSet<Position>();
				explored.add(edge);

				while (!exploringNeeded.isEmpty()) {
					var path = exploringNeeded.pop();

					if (path.length() != 0 && edges.contains(path.position())) {
						paths.add(path);
						continue;
					}

					tiles.get(path.position())
							.nextDirections()
							.map(direction -> path.position().move(direction))
							.filter(nextPosition -> withinBounds().and(excludeForest()).and(notContainedIn(explored)).test(nextPosition, tiles))
							.forEach(nextPosition -> {
								exploringNeeded.push(new Path(nextPosition, path.length() + 1));
								explored.add(nextPosition);
							});
				}

				return paths;
			}

			private BiPredicate<Position, Map<Position, Tile>> notContainedIn(Collection<Position> explored) {
				return (position, tiles) -> !explored.contains(position);
			}

			private int longestPathBetween(Position start, Position end, Map<Position, List<Path>> graph, Set<Position> explored) {
				if (start.equals(end)) {
					return 0;
				}

				var longestPath = Integer.MIN_VALUE;
				explored.add(start);

				for (var path : graph.get(start)) {
					if (!explored.contains(path.position())) {
						longestPath = max(
								longestPath,
								longestPathBetween(path.position(), end, graph, explored) + path.length()
						);
					}
				}

				explored.remove(start);

				return longestPath;
			}

			private record Path(Position position, int length) {
			}
		}

		record Naive() implements LongestPathFindingStrategy {

			@Override
			public int determineLongestPath(Position start, Position end, Map<Position, Tile> tiles) {
				var exploringNeeded = new LinkedList<Path>();
				nextPaths(start, tiles).forEach(exploringNeeded::add);
				var exploringFinished = new ArrayList<Path>();

				while (!exploringNeeded.isEmpty()) {
					var path = exploringNeeded.poll();

					if (path.position().equals(end)) {
						exploringFinished.add(path);
						continue;
					}

					nextPaths(path, tiles).forEach(exploringNeeded::add);
				}

				return exploringFinished.stream()
						.mapToInt(Path::length)
						.max()
						.orElse(0);
			}

			private Stream<Path> nextPaths(Position position, Map<Position, Tile> tiles) {
				return tiles.get(position)
						.nextDirections()
						.map(position::move)
						.filter(nextPosition -> withinBounds().and(excludeForest()).test(nextPosition, tiles))
						.map(Path::new);
			}

			private BiPredicate<Position, Map<Position, Tile>> withinBounds() {
				return (position, tiles) -> tiles.containsKey(position);
			}

			private BiPredicate<Position, Map<Position, Tile>> excludeForest() {
				return (position, tiles) -> tiles.get(position) != Tile.FOREST;
			}

			private Stream<Path> nextPaths(Path path, Map<Position, Tile> tiles) {
				return nextPaths(path.position(), tiles)
						.filter(not(explored(path)))
						.map(path::next);
			}

			private Predicate<Path> explored(Path path) {
				return netxPath -> path.explored().contains(netxPath.position());
			}

			private record Path(Position position, List<Position> explored) {

				public Path(Position position) {
					this(position, List.of(position));
				}

				public Path next(Path other) {
					return new Path(
							other.position(),
							Stream.concat(explored.stream(), other.explored.stream()).toList()
					);
				}

				public int length() {
					return explored().size();
				}
			}
		}
	}
}
