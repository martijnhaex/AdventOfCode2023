package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.StringJoiner;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

public class PuzzleDaySeventeen implements Puzzle<Integer> {

	@Override
	public Integer solvePartOne() {
		return readCityMap()
				.findMinimalHeatLossPath(
						headingDetermined().and(maximumTimesHeadingInSameDirection(3)),
						canAlwaysMakeATurn(),
						arrivedAtEndOfMap()
				);
	}

	private CityMap readCityMap() {
		return CityMap.parse(readContentOfInputFile("puzzleDaySeventeen.txt"));
	}

	private Predicate<Path> headingDetermined() {
		return path -> path.heading() != null;
	}

	private Predicate<Path> maximumTimesHeadingInSameDirection(int times) {
		return path -> path.times() < times;
	}

	private Predicate<Path> canAlwaysMakeATurn() {
		return path -> true;
	}

	private BiPredicate<Path, Integer[][]> arrivedAtEndOfMap() {
		return (path, grid) -> path.position().x == grid.length - 1 && path.position().y == grid[path.position().x].length - 1;
	}

	@Override
	public Integer solvePartTwo() {
		var minimumTimesHeadingInSameDirection = minimumTimesHeadingInSameDirection(4);

		return readCityMap()
				.findMinimalHeatLossPath(
						headingDetermined().and(maximumTimesHeadingInSameDirection(10)),
						not(headingDetermined()).or(minimumTimesHeadingInSameDirection),
						arrivedAtEndOfMap().and((path, grid) -> minimumTimesHeadingInSameDirection.test(path))
				);
	}

	private Predicate<Path> minimumTimesHeadingInSameDirection(int times) {
		return path -> path.times() >= times;
	}

	private record CityMap(Integer[][] cityBlocks) {

		public static CityMap parse(List<String> input) {
			var cityBlocks = new Integer[input.size()][input.getFirst().length()];

			IntStream.range(0, input.size())
					.forEach(rowIndex -> {
						var row = input.get(rowIndex);

						IntStream.range(0, row.length())
								.forEach(columnIndex -> cityBlocks[rowIndex][columnIndex] = Character.getNumericValue(row.charAt(columnIndex)));
					});

			return new CityMap(cityBlocks);
		}

		public int findMinimalHeatLossPath(Predicate<Path> canContinueInSameHeading, Predicate<Path> canMakeTurn, BiPredicate<Path, Integer[][]> canStop) {
			var settled = new HashSet<Path>();
			var unsettled = new PriorityQueue<Path>();

			unsettled.add(new Path(new Position(0, 0), null, 0, 0));

			while (!unsettled.isEmpty()) {
				var path = unsettled.poll();

				if (canStop.test(path, cityBlocks)) {
					return path.heatLoss();
				} else if (!settled.contains(path)) {
					settled.add(path);

					if (canContinueInSameHeading.test(path)) {
						move(path, path.heading()).ifPresent(unsettled::add);
					}

					if (canMakeTurn.test(path)) {
						turn(path).forEach(unsettled::add);
					}
				}
			}

			return 0;
		}

		private Optional<Path> move(Path path, Direction heading) {
			var nextPosition = path.position().move(heading);

			if (withinMap(nextPosition)) {
				return Optional.of(new Path(
						nextPosition,
						heading,
						path.heading() == heading ? path.times() + 1 : 1,
						path.heatLoss() + cityBlocks[nextPosition.x()][nextPosition.y()]
				));
			} else {
				return Optional.empty();
			}
		}

		private boolean withinMap(Position position) {
			return position.x() >= 0 && position.x() < cityBlocks.length
					&& position.y() >= 0 && position.y() < cityBlocks[position.x()].length;
		}

		private Stream<Path> turn(Path path) {
			return Arrays.stream(Direction.values())
					.filter(not(sameHeadingAs(path)))
					.filter(not(oppositeHeadingAs(path)))
					.map(heading -> move(path, heading))
					.flatMap(Optional::stream);
		}

		private Predicate<Direction> sameHeadingAs(Path path) {
			return direction -> direction == path.heading();
		}

		private Predicate<Direction> oppositeHeadingAs(Path path) {
			return direction -> direction.opposite() == path.heading();
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			CityMap cityMap = (CityMap) o;
			return Arrays.deepEquals(cityBlocks, cityMap.cityBlocks);
		}

		@Override
		public int hashCode() {
			return Arrays.deepHashCode(cityBlocks);
		}

		@Override
		public String toString() {
			return new StringJoiner(", ", CityMap.class.getSimpleName() + "[", "]")
					.add("cityBlocks=" + Arrays.toString(cityBlocks))
					.toString();
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

	private record Path(Position position, Direction heading, int times, int heatLoss) implements Comparable<Path> {

		@Override
		public int compareTo(Path o) {
			return Integer.compare(heatLoss, o.heatLoss);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Path path = (Path) o;
			return times == path.times && Objects.equals(position, path.position) && Objects.equals(heading, path.heading);
		}

		@Override
		public int hashCode() {
			return Objects.hash(position, heading, times);
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
}
