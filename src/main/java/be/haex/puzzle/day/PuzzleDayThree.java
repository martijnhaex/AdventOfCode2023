package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

public class PuzzleDayThree implements Puzzle<Integer> {

	@Override
	public Integer solvePartOne() {
		return readEngineSchematic()
				.partNumbers()
				.mapToInt(PartNumber::value)
				.sum();
	}

	private EngineSchematic readEngineSchematic() {
		return new EngineSchematic(readContentOfInputFile("puzzleDayThree.txt"));
	}

	@Override
	public Integer solvePartTwo() {
		return readEngineSchematic()
				.gears()
				.mapToInt(Gear::ratio)
				.sum();
	}

	private record EngineSchematic(List<String> content) {

		public Stream<PartNumber> partNumbers() {
			return partNumbers(not(value -> value.equals(".")));
		}

		public Stream<PartNumber> partNumbers(Predicate<String> acceptedSymbols) {
			return extractPartNumbers()
					.stream()
					.filter(adjacentToASymbol(acceptedSymbols));
		}

		private List<PartNumber> extractPartNumbers() {
			var partNumbers = new ArrayList<PartNumber>();

			PartNumber.Builder partialPartNumber = null;
			for (var rowNumber = 0; rowNumber < content.size(); rowNumber++) {
				var row = content.get(rowNumber);

				for (int columnIndex = 0; columnIndex < row.length(); columnIndex++) {
					var character = row.charAt(columnIndex);

					if (Character.isDigit(character)) {
						if (partialPartNumber == null) {
							partialPartNumber = PartNumber.Builder.partialPartNumber(new Point(rowNumber, columnIndex), String.valueOf(character));
						} else {
							partialPartNumber.appendValue(String.valueOf(character));
						}
					} else if (partialPartNumber != null) {
						partNumbers.add(partialPartNumber.build(new Point(rowNumber, columnIndex - 1)));
						partialPartNumber = null;
					}
				}

				if (partialPartNumber != null) {
					partNumbers.add(partialPartNumber.build(new Point(rowNumber, row.length() - 1)));
				}
			}

			return partNumbers;
		}

		private Predicate<PartNumber> adjacentToASymbol(Predicate<String> acceptedSymbols) {
			return partNumber -> {
				var start = partNumber.start();

				var stop = partNumber.stop();
				for (var rowIndex = start.x() - 1; rowIndex <= start.x() + 1; rowIndex++) {
					if (rowIndex < 0 || rowIndex >= content.size()) {
						continue;
					} else {
						var row = content.get(rowIndex);
						for (int j = start.y() - 1; j <= stop.y() + 1; j++) {
							if (j < 0 || j >= row.length()) {
								continue;
							} else if (rowIndex != start.x() || (j < start.y() || j > stop.y())) {
								if (acceptedSymbols.test(String.valueOf(row.charAt(j))) && !Character.isDigit(row.charAt(j))) {
									return true;
								}
							}
						}
					}
				}

				return false;
			};
		}

		public Stream<Gear> gears() {
			var partNumbers = partNumbers(value -> value.equals("*")).toList();

			return partNumbers.stream()
					.map(asGear(partNumbers))
					.flatMap(Optional::stream)
					.distinct();
		}

		private Function<PartNumber, Optional<Gear>> asGear(List<PartNumber> partNumbers) {
			return partNumber -> {
				for (var anotherPartNumber : partNumbers) {
					if (partNumber != anotherPartNumber) {
						var adjacentSymbolsPartNumber = adjacentSymbols(partNumber, value -> value.equals("*"));
						var adjacentSymbolsAnotherPartNumber = adjacentSymbols(anotherPartNumber, value -> value.equals("*"));

						if (adjacentSymbolsPartNumber.stream().anyMatch(adjacentSymbolsAnotherPartNumber::contains)) {
							return Optional.of(new Gear(List.of(partNumber, anotherPartNumber)));
						}
					}
				}

				return Optional.empty();
			};
		}

		private List<Point> adjacentSymbols(PartNumber partNumber, Predicate<String> acceptedSymbols) {
			var adjacentSymbols = new ArrayList<Point>();

			var start = partNumber.start();
			var stop = partNumber.stop();
			for (var rowIndex = start.x() - 1; rowIndex <= start.x() + 1; rowIndex++) {
				if (rowIndex < 0 || rowIndex >= content.size()) {
					continue;
				} else {
					var row = content.get(rowIndex);
					for (int j = start.y() - 1; j <= stop.y() + 1; j++) {
						if (j < 0 || j >= row.length()) {
							continue;
						} else if (rowIndex != start.x() || (j < start.y() || j > stop.y())) {
							if (acceptedSymbols.test(String.valueOf(row.charAt(j)))) {
								adjacentSymbols.add(new Point(rowIndex, j));
							}
						}
					}
				}
			}

			return adjacentSymbols;
		}
	}

	private record PartNumber(int value, Point start, Point stop) {

		private static class Builder {

			private final Point start;

			private String value;

			private Builder(Point start, String value) {
				this.start = start;
				this.value = value;
			}

			public static Builder partialPartNumber(Point start, String value) {
				return new Builder(start, value);
			}

			public Builder appendValue(String value) {
				this.value += value;
				return this;
			}

			public PartNumber build(Point stop) {
				return new PartNumber(Integer.parseInt(value), start, stop);
			}
		}
	}

	private record Point(int x, int y) implements Comparable<Point> {

		@Override
		public int compareTo(Point other) {
			var xComparison = Integer.compare(x, other.x());

			if (xComparison != 0) {
				return xComparison;
			} else {
				return Integer.compare(y, other.y());
			}
		}
	}

	private record Gear(List<PartNumber> parts) {

		public Gear(List<PartNumber> parts) {
			this.parts = parts.stream().sorted(Comparator.comparing(PartNumber::start)).toList();
		}

		public Integer ratio() {
			return parts.stream()
					.map(PartNumber::value)
					.reduce(1, (a, b) -> a * b);
		}
	}
}
