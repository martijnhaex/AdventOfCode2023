package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class PuzzleDayTen implements Puzzle<Long> {

	private static final String INPUT_FILE_NAME = "puzzleDayTen.txt";

	private final String partOneInputFileName;
	private final String partTwoInputFileName;

	PuzzleDayTen(String partOneInputFileName, String partTwoInputFileName) {
		this.partOneInputFileName = partOneInputFileName;
		this.partTwoInputFileName = partTwoInputFileName;
	}

	public PuzzleDayTen() {
		this(INPUT_FILE_NAME, INPUT_FILE_NAME);
	}

	@Override
	public Long solvePartOne() {
		return readField(partOneInputFileName)
				.replaceStartingPoint()
				.calculateMaximumDistance();
	}

	private Field readField(String fileName) {
		return Field.parse(readContentOfInputFile(fileName));
	}

	@Override
	public Long solvePartTwo() {
		return readField(partTwoInputFileName)
				.replaceStartingPoint()
				.countEnclosedTiles();
	}

	private record Field(Pipe[][] pipes, Position startingPosition) {

		public static Field parse(List<String> input) {
			var pipes = new Pipe[input.size()][input.getFirst().length()];
			Position startingPosition = null;

			for (var rowIndex = 0; rowIndex < input.size(); rowIndex++) {
				var row = input.get(rowIndex);

				for (var columnIndex = 0; columnIndex < row.length(); columnIndex++) {
					var pipe = Pipe.from(row.charAt(columnIndex));

					pipes[rowIndex][columnIndex] = pipe;

					if (pipe == Pipe.STARTING_POSITION) {
						startingPosition = new Position(rowIndex, columnIndex);
					}
				}
			}

			return new Field(pipes, startingPosition);
		}

		public Field replaceStartingPoint() {
			var startingPointReplacementPipe = determineStartingPointReplacementPipe();
			var pipesClone = Arrays.stream(pipes).map(Pipe[]::clone).toArray(Pipe[][]::new);
			pipesClone[startingPosition.x()][startingPosition().y()] = startingPointReplacementPipe;

			return new Field(pipesClone, startingPosition);
		}

		private Pipe determineStartingPointReplacementPipe() {
			var startingPointReplacementPipes = Stream.of(
							determineConnectionBetween(startingPosition, Direction.NORTH, Direction.EAST),
							determineConnectionBetween(startingPosition, Direction.NORTH, Direction.SOUTH),
							determineConnectionBetween(startingPosition, Direction.NORTH, Direction.WEST),
							determineConnectionBetween(startingPosition, Direction.EAST, Direction.WEST),
							determineConnectionBetween(startingPosition, Direction.EAST, Direction.SOUTH),
							determineConnectionBetween(startingPosition, Direction.SOUTH, Direction.WEST)
					)
					.flatMap(Optional::stream)
					.toList();

			if (startingPointReplacementPipes.size() != 1) {
				throw new IllegalStateException("Starting point replacement pipe not found");
			} else {
				return startingPointReplacementPipes.getFirst();
			}
		}

		private Optional<Pipe> determineConnectionBetween(Position position, Direction firstDirection, Direction secondDirection) {
			var firstPipe = getPipe(position.move(firstDirection));
			var secondPipe = getPipe(position.move(secondDirection));

			return Arrays.stream(Pipe.values())
					.filter(pipe -> firstPipe.orElseThrow().connectedTo(pipe, firstDirection.opposite()) && secondPipe.orElseThrow().connectedTo(pipe, secondDirection.opposite()))
					.findAny();
		}

		public long calculateMaximumDistance() {
			var distances = calculateDistances();

			return Arrays.stream(distances)
					.flatMap(Arrays::stream)
					.mapToLong(Long::valueOf)
					.max()
					.orElse(0);
		}

		private Long[][] calculateDistances() {
			var distances = new Long[pipes.length][pipes[0].length];
			for (var distance : distances) {
				Arrays.fill(distance, -1L);
			}

			distances[startingPosition.x()][startingPosition.y()] = 0L;

			var currentPosition = startingPosition;
			var nextPositions = new ArrayList<>(determineNextPositions(startingPosition));
			while (!nextPositions.isEmpty()) {
				var currentDistance = distances[currentPosition.x()][currentPosition.y()];

				nextPositions
						.stream()
						.filter(notCalculated(distances))
						.forEach(nextPosition -> distances[nextPosition.x()][nextPosition.y()] = currentDistance + 1);

				currentPosition = nextPositions.removeFirst();

				determineNextPositions(currentPosition).stream()
						.filter(notCalculated(distances))
						.forEach(nextPositions::add);
			}

			return distances;
		}

		private List<Position> determineNextPositions(Position position) {
			return Arrays.stream(Direction.values())
					.filter(direction -> isConnected(position, direction))
					.map(position::move)
					.toList();
		}

		private boolean isConnected(Position position, Direction direction) {
			var nextPosition = position.move(direction);

			return getPipe(nextPosition)
					.map(pipe -> getPipe(position).orElseThrow().connectedTo(pipe, direction))
					.orElse(false);
		}

		private Optional<Pipe> getPipe(Position position) {
			if (position.x() < 0 || position.x() >= pipes.length || position.y() < 0 || position.y() >= pipes[0].length) {
				return Optional.empty();
			} else {
				return Optional.ofNullable(pipes[position.x()][position.y()]);
			}
		}

		private Predicate<Position> notCalculated(Long[][] distances) {
			return position -> distances[position.x()][position.y()] == -1;
		}

		public long countEnclosedTiles() {
			var distances = calculateDistances();
			var tiles = asTiles(distances);

			return Arrays.stream(tiles)
					.flatMap(Arrays::stream)
					.filter(Tile::isEnclosed)
					.count();
		}

		private Tile[][] asTiles(Long[][] distances) {
			var tiles = new Tile[pipes.length][pipes[0].length];

			for (var rowIndex = 0; rowIndex < pipes.length; rowIndex++) {
				var tile = Tile.outside();
				for (var columnIndex = 0; columnIndex < pipes[0].length; columnIndex++) {
					var position = new Position(rowIndex, columnIndex);
					var pipe = getPipe(position).orElseThrow();

					tile = distances[rowIndex][columnIndex] == -1
							? tile.next()
							: tile.next(pipe);

					tiles[rowIndex][columnIndex] = tile;
				}
			}

			return tiles;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Field field = (Field) o;
			return Arrays.deepEquals(pipes, field.pipes) && Objects.equals(startingPosition, field.startingPosition);
		}

		@Override
		public int hashCode() {
			int result = Objects.hash(startingPosition);
			result = 31 * result + Arrays.deepHashCode(pipes);
			return result;
		}

		@Override
		public String toString() {
			return new StringJoiner(", ", Field.class.getSimpleName() + "[", "]")
					.add("pipes=" + Arrays.toString(pipes))
					.add("startingPosition=" + startingPosition)
					.toString();
		}
	}

	private enum Pipe {
		VERTICAL('|', Direction.NORTH, Direction.SOUTH),
		HORIZONTAL('-', Direction.EAST, Direction.WEST),
		NINETY_DEGREE_BEND_NE('L', Direction.NORTH, Direction.EAST),
		NINETY_DEGREE_BEND_NW('J', Direction.NORTH, Direction.WEST),
		NINETY_DEGREE_BEND_SW('7', Direction.SOUTH, Direction.WEST),
		NINETY_DEGREE_BEND_SE('F', Direction.SOUTH, Direction.EAST),
		GROUND('.'),
		STARTING_POSITION('S');

		private final char character;
		private final Set<Direction> directions;

		Pipe(char character, Direction... directions) {
			this.character = character;
			this.directions = Set.of(directions);
		}

		public static Pipe from(char character) {
			return Arrays.stream(values())
					.filter(pipe -> pipe.character == character)
					.findFirst()
					.orElseThrow(() -> new IllegalArgumentException("No pipe found for character " + character));
		}

		public boolean connectedTo(Pipe other, Direction direction) {
			return directions.contains(direction) &&
					other.directions.contains(direction.opposite());
		}
	}

	private enum Direction {
		NORTH,
		EAST,
		SOUTH,
		WEST;

		public Direction opposite() {
			return switch (this) {
				case NORTH -> SOUTH;
				case EAST -> WEST;
				case SOUTH -> NORTH;
				case WEST -> EAST;
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

	private record Tile(boolean topLeft, boolean topRight, boolean bottomLeft, boolean bottomRight) {

		public static Tile outside() {
			return new Tile(false, false, false, false);
		}

		public Tile next() {
			return new Tile(topRight, topRight, topRight, topRight);
		}

		public Tile next(Pipe pipe) {
			return switch (pipe) {
				case VERTICAL -> new Tile(topRight, !topRight, bottomLeft, !topRight);
				case HORIZONTAL -> new Tile(topRight, topRight, !topRight, !topRight);
				case NINETY_DEGREE_BEND_NE -> new Tile(topRight, !topRight, topRight, topRight);
				case NINETY_DEGREE_BEND_SW -> new Tile(topRight, topRight, !topRight, topRight);
				case NINETY_DEGREE_BEND_NW -> new Tile(topRight, !topRight, !topRight, !topRight);
				case NINETY_DEGREE_BEND_SE -> new Tile(topRight, topRight, topRight, !topRight);
				default -> throw new IllegalArgumentException("Pipe " + pipe + " not supported");
			};
		}

		public boolean isEnclosed() {
			return topLeft && topRight && bottomLeft && bottomRight;
		}
	}
}
