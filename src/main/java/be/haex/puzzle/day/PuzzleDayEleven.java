package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class PuzzleDayEleven implements Puzzle<Long> {

	@Override
	public Long solvePartOne() {
		return readUniverse()
				.expand(1)
				.shortestPathsBetweenGalaxies()
				.stream()
				.mapToLong(PathBetweenGalaxies::distance)
				.sum();
	}

	private Universe readUniverse() {
		return Universe.parse(readContentOfInputFile("puzzleDayEleven.txt"));
	}

	@Override
	public Long solvePartTwo() {
		return readUniverse()
				.expand(1_000_000)
				.shortestPathsBetweenGalaxies()
				.stream()
				.mapToLong(PathBetweenGalaxies::distance)
				.sum();
	}

	private record Universe(Character[][] originalMap, List<Galaxy> galaxies) {

		public static Universe parse(List<String> values) {
			var map = new Character[values.size()][values.getFirst().length()];
			var galaxies = new ArrayList<Galaxy>();

			var galaxyIndex = 1;
			for (var rowIndex = 0; rowIndex < values.size(); rowIndex++) {
				var row = map[rowIndex];

				for (var columnIndex = 0; columnIndex < values.get(rowIndex).length(); columnIndex++) {
					var value = values.get(rowIndex).charAt(columnIndex);

					row[columnIndex] = value;

					if (value == '#') {
						galaxies.add(new Galaxy(galaxyIndex++, new Position(rowIndex, columnIndex)));
					}
				}
			}

			return new Universe(map, galaxies);
		}

		public Universe expand(int expansionSize) {
			var normalizedExpansionSize = Math.max(1, expansionSize - 1);

			return expandVertically(normalizedExpansionSize).expandHorizontally(normalizedExpansionSize);
		}

		private Universe expandVertically(int expansionSize) {
			var rowsWithOnlyOpenSpaces = determineRowsWithOnlyOpenSpaces();
			var expandedGalaxies = galaxies.stream()
					.map(expandVertically(expansionSize, rowsWithOnlyOpenSpaces))
					.toList();

			return new Universe(originalMap, expandedGalaxies);
		}

		private List<Integer> determineRowsWithOnlyOpenSpaces() {
			return IntStream.range(0, originalMap.length)
					.filter(rowIndex -> onlyOpenSpaces(originalMap[rowIndex]))
					.boxed()
					.toList();
		}

		private boolean onlyOpenSpaces(Character[] spaces) {
			return Arrays.stream(spaces).allMatch(character -> character == '.');
		}

		private UnaryOperator<Galaxy> expandVertically(int expansionSize, List<Integer> expandingRows) {
			return galaxy -> {
				var currentPosition = galaxy.position();
				var applicableExpansions = expandingRows.stream()
						.filter(expandingRow -> expandingRow < currentPosition.x())
						.count();

				return new Galaxy(
						galaxy.name(),
						new Position(
								(int) (currentPosition.x() + applicableExpansions * expansionSize),
								currentPosition.y()
						)
				);
			};
		}

		private Universe expandHorizontally(int expansionSize) {
			var columnsWithOnlyOpenSpaces = determineColumnsWithOnlyOpenSpaces();
			var expandedGalaxies = galaxies.stream()
					.map(expandHorizontally(expansionSize, columnsWithOnlyOpenSpaces))
					.toList();

			return new Universe(originalMap, expandedGalaxies);
		}

		private List<Integer> determineColumnsWithOnlyOpenSpaces() {
			return IntStream.range(0, originalMap[0].length)
					.filter(this::onlyOpenSpaces)
					.boxed()
					.toList();
		}

		private boolean onlyOpenSpaces(int index) {
			return Arrays.stream(originalMap)
					.map(spaces -> spaces[index])
					.allMatch(character -> character == '.');
		}

		private UnaryOperator<Galaxy> expandHorizontally(int expansionSize, List<Integer> expandingColumns) {
			return galaxy -> {
				var currentPosition = galaxy.position();
				var applicableExpansions = expandingColumns.stream()
						.filter(expandingColumn -> expandingColumn < currentPosition.y())
						.count();

				return new Galaxy(
						galaxy.name(),
						new Position(
								currentPosition.x(),
								(int) (currentPosition.y() + applicableExpansions * expansionSize)
						)
				);
			};
		}

		public List<PathBetweenGalaxies> shortestPathsBetweenGalaxies() {
			return cartesianProductGalaxies(galaxies).toList();
		}

		private Stream<PathBetweenGalaxies> cartesianProductGalaxies(List<Galaxy> galaxies) {
			if (galaxies.isEmpty()) {
				return Stream.empty();
			}

			var pathsBetweenGalaxies = new ArrayList<PathBetweenGalaxies>();

			var galaxy = galaxies.getFirst();
			var otherGalaxies = galaxies.subList(1, galaxies.size());
			for (var otherGalaxy : otherGalaxies) {
				pathsBetweenGalaxies.add(new PathBetweenGalaxies(galaxy, otherGalaxy));
			}

			return Stream.concat(
					pathsBetweenGalaxies.stream(),
					cartesianProductGalaxies(otherGalaxies)
			);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Universe universe = (Universe) o;
			return Arrays.deepEquals(originalMap, universe.originalMap) && Objects.equals(galaxies, universe.galaxies);
		}

		@Override
		public int hashCode() {
			int result = Objects.hash(galaxies);
			result = 31 * result + Arrays.deepHashCode(originalMap);
			return result;
		}

		@Override
		public String toString() {
			return new StringJoiner(", ", Universe.class.getSimpleName() + "[", "]")
					.add("originalMap=" + Arrays.toString(originalMap))
					.add("galaxies=" + galaxies)
					.toString();
		}
	}

	record Galaxy(int name, Position position) {
	}

	private record Position(int x, int y) {
	}

	private record PathBetweenGalaxies(Galaxy from, Galaxy to) {

		public int distance() {
			var ac = Math.abs(to.position.y() - from.position().y());
			var cb = Math.abs(to.position.x() - from.position().x());

			return ac + cb;
		}
	}
}
