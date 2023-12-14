package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.IntUnaryOperator;
import java.util.function.ToIntFunction;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

public class PuzzleDayFourteen implements Puzzle<Long> {

	@Override
	public Long solvePartOne() {
		return readPlatform()
				.tilt(Direction.NORTH)
				.calculateTotalLoad();
	}

	private Platform readPlatform() {
		return Platform.parse(readContentOfInputFile("puzzleDayFourteen.txt"));
	}

	@Override
	public Long solvePartTwo() {
		return readPlatform()
				.tilt(1_000_000_000)
				.calculateTotalLoad();
	}

	private record Platform(Space[][] spaces) {

		public static Platform parse(List<String> input) {
			var spaces = new Space[input.size()][input.getFirst().length()];

			for (var rowIndex = 0; rowIndex < spaces.length; rowIndex++) {
				var row = input.get(rowIndex);

				for (var columnIndex = 0; columnIndex < row.length(); columnIndex++) {
					spaces[rowIndex][columnIndex] = switch (row.charAt(columnIndex)) {
						case '.' -> new Space.Empty();
						case '#' -> new Space.Rock(RockType.CUBE);
						case 'O' -> new Space.Rock(RockType.ROUNDED);
						default -> throw new IllegalArgumentException("Unknown space type: " + row.charAt(columnIndex));
					};
				}
			}

			return new Platform(spaces);
		}

		public Platform tilt(Direction direction) {
			var transformer = direction.spacesTransformer();
			var spacesAfterTilt = transformer.apply(spaces);

			Arrays.stream(spacesAfterTilt)
					.parallel()
					.forEach(move(direction));

			return new Platform(transformer.apply(spacesAfterTilt));
		}

		private Consumer<Space[]> move(Direction direction) {
			return row -> {
				var startColumnIndex = direction.startColumnIndexDeterminer().applyAsInt(row);

				for (var columnIndex = startColumnIndex; direction.stopColumnDeterminer().test(row, columnIndex); columnIndex = direction.columnIndexer().applyAsInt(columnIndex)) {
					var space = row[columnIndex];

					if (space instanceof Space.Rock rock && rock.type() == RockType.ROUNDED) {
						var nextSpaceIndexer = direction.nextSpaceIndexer();
						var nextSpaceIndex = nextSpaceIndexer.applyAsInt(columnIndex);

						if (canRollTo(row, nextSpaceIndex)) {
							while (canRollTo(row, nextSpaceIndexer.applyAsInt(nextSpaceIndex))) {
								nextSpaceIndex = nextSpaceIndexer.applyAsInt(nextSpaceIndex);
							}

							row[nextSpaceIndex] = space;
							row[columnIndex] = new Space.Empty();
						}
					}
				}
			};
		}

		private boolean canRollTo(Space[] spaces, int columnIndex) {
			return columnIndex >= 0 && columnIndex < spaces.length && spaces[columnIndex] instanceof Space.Empty;
		}

		public Platform tilt(int cycles) {
			var memoization = new ArrayList<Platform>();
			var platform = this;

			var cycle = 1;
			while (cycle <= cycles) {
				platform = platform.tilt(Direction.NORTH)
						.tilt(Direction.WEST)
						.tilt(Direction.SOUTH)
						.tilt(Direction.EAST);

				if (memoization.contains(platform)) {
					var cycleLength = cycle - memoization.indexOf(platform) - 1;
					var remainingCycles = cycles - cycle;

					cycle += remainingCycles / cycleLength * cycleLength;
				}

				memoization.add(platform);
				cycle++;
			}

			return platform;
		}

		public long calculateTotalLoad() {
			return IntStream.range(0, spaces.length)
					.mapToLong(rowIndex -> countRoundedRocks(rowIndex) * (spaces.length - rowIndex))
					.sum();
		}

		private long countRoundedRocks(int rowIndex) {
			return Arrays.stream(spaces[rowIndex])
					.filter(Space.Rock.class::isInstance)
					.map(Space.Rock.class::cast)
					.filter(rock -> rock.type() == RockType.ROUNDED)
					.count();
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Platform platform = (Platform) o;
			return Arrays.deepEquals(spaces, platform.spaces);
		}

		@Override
		public int hashCode() {
			return Arrays.deepHashCode(spaces);
		}

		@Override
		public String toString() {
			return new StringJoiner(", ", Platform.class.getSimpleName() + "[", "]")
					.add("spaces=" + Arrays.toString(spaces))
					.toString();
		}
	}

	private sealed interface Space permits Space.Empty, Space.Rock {

		record Empty() implements Space {
		}

		record Rock(RockType type) implements Space {
		}
	}

	private enum RockType {
		CUBE,
		ROUNDED
	}

	private enum Direction {
		NORTH(
				transpose(),
				spaces -> 0,
				(spaces, index) -> index < spaces.length,
				index -> index + 1,
				index -> index - 1
		),
		EAST(
				copy(),
				spaces -> spaces.length - 1,
				(spaces, index) -> index >= 0,
				index -> index - 1,
				index -> index + 1
		),
		SOUTH(
				transpose(),
				spaces -> spaces.length - 1,
				(spaces, index) -> index >= 0,
				index -> index - 1,
				index -> index + 1
		),
		WEST(
				copy(),
				spaces -> 0,
				(spaces, index) -> index < spaces.length,
				index -> index + 1,
				index -> index - 1
		);

		private final UnaryOperator<Space[][]> spacesTransformer;
		private final ToIntFunction<Space[]> startColumnIndexDeterminer;
		private final BiPredicate<Space[], Integer> stopColumnDeterminer;
		private final IntUnaryOperator columnIndexer;
		private final IntUnaryOperator nextSpaceIndexer;

		Direction(UnaryOperator<Space[][]> spacesTransformer,
				  ToIntFunction<Space[]> startColumnIndexDeterminer,
				  BiPredicate<Space[], Integer> stopColumnDeterminer,
				  IntUnaryOperator columnIndexer,
				  IntUnaryOperator nextSpaceIndexer) {
			this.spacesTransformer = spacesTransformer;
			this.startColumnIndexDeterminer = startColumnIndexDeterminer;
			this.stopColumnDeterminer = stopColumnDeterminer;
			this.columnIndexer = columnIndexer;
			this.nextSpaceIndexer = nextSpaceIndexer;
		}

		private static UnaryOperator<Space[][]> transpose() {
			return spaces -> {
				var transposed = new Space[spaces[0].length][spaces.length];

				for (var i = 0; i < spaces.length; i++) {
					for (var j = 0; j < spaces[i].length; j++) {
						transposed[j][i] = spaces[i][j];
					}
				}

				return transposed;
			};
		}

		private static UnaryOperator<Space[][]> copy() {
			return spaces -> Arrays.stream(spaces)
					.map(Space[]::clone)
					.toArray(Space[][]::new);
		}

		public UnaryOperator<Space[][]> spacesTransformer() {
			return spacesTransformer;
		}

		public ToIntFunction<Space[]> startColumnIndexDeterminer() {
			return startColumnIndexDeterminer;
		}

		public BiPredicate<Space[], Integer> stopColumnDeterminer() {
			return stopColumnDeterminer;
		}

		public IntUnaryOperator columnIndexer() {
			return columnIndexer;
		}

		public IntUnaryOperator nextSpaceIndexer() {
			return nextSpaceIndexer;
		}
	}
}
