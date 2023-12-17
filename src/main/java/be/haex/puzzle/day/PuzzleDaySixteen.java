package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.IntFunction;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class PuzzleDaySixteen implements Puzzle<Long> {

	@Override
	public Long solvePartOne() {
		return readContraption()
				.countEnergizedTiles(new Position(0, 0), Direction.EAST);
	}

	private Contraption readContraption() {
		return Contraption.parse(readContentOfInputFile("puzzleDaySixteen.txt"));
	}

	@Override
	public Long solvePartTwo() {
		return readContraption()
				.largestCountEnergizedTiles();
	}

	private record Contraption(Tile[][] tiles) {

		public static Contraption parse(List<String> input) {
			var tiles = new Tile[input.size()][input.getFirst().length()];

			for (var rowIndex = 0; rowIndex < input.size(); rowIndex++) {
				var row = input.get(rowIndex);

				for (var columnIndex = 0; columnIndex < row.length(); columnIndex++) {
					var character = row.charAt(columnIndex) + "";

					tiles[rowIndex][columnIndex] = Tile.Empty.parse(character)
							.or(() -> Tile.Mirror.parse(character))
							.or(() -> Tile.Splitter.parse(character))
							.orElseThrow(() -> new IllegalArgumentException("Invalid tile: " + character));
				}
			}

			return new Contraption(tiles);
		}

		public long countEnergizedTiles(Position startingPosition, Direction heading) {
			var instructions = new LinkedList<Instruction>();
			var pastInstructions = new HashSet<Instruction>();

			instructions.add(new Instruction(startingPosition, heading));

			while (!instructions.isEmpty()) {
				var currentInstruction = instructions.poll();
				var currentPosition = currentInstruction.position();
				var currentTile = tiles[currentPosition.x()][currentPosition.y()];

				pastInstructions.add(currentInstruction);

				var directions = currentTile.determineDirections(currentInstruction.direction());
				for (var direction : directions) {
					var nextInstruction = currentInstruction.next(direction);

					if (withinTiles(nextInstruction) && !pastInstructions.contains(nextInstruction)) {
						instructions.add(nextInstruction);
					}
				}
			}

			return pastInstructions.stream()
					.map(Instruction::position)
					.distinct()
					.count();
		}

		private boolean withinTiles(Instruction instruction) {
			var position = instruction.position();

			return position.x() >= 0 && position.x() < tiles.length
					&& position.y() >= 0 && position.y() < tiles[position.x()].length;
		}

		public long largestCountEnergizedTiles() {
			return Stream.of(
							largestCountEnergizedTilesFromTheNorth(),
							largestCountEnergizedTilesFromTheEast(),
							largestCountEnergizedTilesFromTheSouth(),
							largestCountEnergizedTilesFromTheWest()
					)
					.max(Long::compareTo)
					.orElse(0L);
		}

		private long largestCountEnergizedTilesFromTheNorth() {
			return largestCountEnergizedTiles(
					tiles[0].length,
					index -> new Position(0, index),
					Direction.SOUTH
			);
		}

		private long largestCountEnergizedTiles(int endExclusive, IntFunction<Position> positionCreator, Direction heading) {
			return IntStream.range(0, endExclusive)
					.parallel()
					.mapToLong(index -> countEnergizedTiles(positionCreator.apply(index), heading))
					.max()
					.orElse(0);
		}

		private long largestCountEnergizedTilesFromTheEast() {
			return largestCountEnergizedTiles(
					tiles.length,
					index -> new Position(index, tiles[index].length - 1),
					Direction.WEST
			);
		}

		private long largestCountEnergizedTilesFromTheSouth() {
			return largestCountEnergizedTiles(
					tiles[0].length,
					index -> new Position(tiles.length - 1, index),
					Direction.NORTH
			);
		}

		private long largestCountEnergizedTilesFromTheWest() {
			return largestCountEnergizedTiles(
					tiles.length,
					index -> new Position(index, 0),
					Direction.EAST
			);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Contraption that = (Contraption) o;
			return Arrays.deepEquals(tiles, that.tiles);
		}

		@Override
		public int hashCode() {
			return Arrays.deepHashCode(tiles);
		}

		@Override
		public String toString() {
			return new StringJoiner(", ", Contraption.class.getSimpleName() + "[", "]")
					.add("tiles=" + Arrays.toString(tiles))
					.toString();
		}
	}

	private sealed interface Tile permits Tile.Empty, Tile.Mirror, Tile.Splitter {

		List<Direction> determineDirections(Direction direction);

		record Empty() implements Tile {

			private static final String DOT = ".";
			private static final Pattern PATTERN = Pattern.compile("\\%s".formatted(DOT));

			public static Optional<Tile> parse(String value) {
				if (PATTERN.matcher(value).matches()) {
					return Optional.of(new Empty());
				} else {
					return Optional.empty();
				}
			}

			@Override
			public List<Direction> determineDirections(Direction direction) {
				return List.of(direction);
			}

			@Override
			public String toString() {
				return DOT;
			}
		}

		record Mirror(String value) implements Tile {

			private static final String DOWNWARDS = "\\";
			private static final String UPWARDS = "/";
			private static final Pattern PATTERN = Pattern.compile("^[\\%s\\%s]$".formatted(DOWNWARDS, UPWARDS));

			public static Optional<Tile> parse(String value) {
				if (PATTERN.matcher(value).matches()) {
					return Optional.of(new Mirror(value));
				} else {
					return Optional.empty();
				}
			}

			@Override
			public List<Direction> determineDirections(Direction direction) {
				return List.of(switch (direction) {
					case NORTH -> value.equals(DOWNWARDS) ? Direction.WEST : Direction.EAST;
					case EAST -> value.equals(DOWNWARDS) ? Direction.SOUTH : Direction.NORTH;
					case SOUTH -> value.equals(DOWNWARDS) ? Direction.EAST : Direction.WEST;
					case WEST -> value.equals(DOWNWARDS) ? Direction.NORTH : Direction.SOUTH;
				});
			}

			@Override
			public String toString() {
				return value;
			}
		}

		record Splitter(String value) implements Tile {

			private static final String HORIZONTAL = "-";
			private static final String VERTICAL = "|";
			private static final Pattern PATTERN = Pattern.compile("^[%s%s]$".formatted(HORIZONTAL, VERTICAL));

			public static Optional<Tile> parse(String value) {
				if (PATTERN.matcher(value).matches()) {
					return Optional.of(new Splitter(value));
				} else {
					return Optional.empty();
				}
			}

			@Override
			public List<Direction> determineDirections(Direction direction) {
				return switch (direction) {
					case NORTH, SOUTH ->
							value.equals(VERTICAL) ? List.of(direction) : List.of(Direction.EAST, Direction.WEST);
					case EAST, WEST ->
							value.equals(HORIZONTAL) ? List.of(direction) : List.of(Direction.NORTH, Direction.SOUTH);
				};
			}

			@Override
			public String toString() {
				return value;
			}
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

	private record Instruction(Position position, Direction direction) {

		public Instruction next(Direction direction) {
			return new Instruction(position.move(direction), direction);
		}
	}

	private enum Direction {
		NORTH,
		EAST,
		SOUTH,
		WEST
	}
}
