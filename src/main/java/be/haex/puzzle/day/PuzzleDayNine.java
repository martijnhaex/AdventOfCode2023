package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.LongBinaryOperator;
import java.util.function.ToLongFunction;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static java.util.Comparator.reverseOrder;

public class PuzzleDayNine implements Puzzle<Long> {

	@Override
	public Long solvePartOne() {
		return readOASISReport()
				.extrapolateNextHistoryValue()
				.sum();
	}

	private OASISReport readOASISReport() {
		return OASISReport.parse(readContentOfInputFile("puzzleDayNine.txt"));
	}

	@Override
	public Long solvePartTwo() {
		return readOASISReport()
				.extrapolatePreviousHistoryValue()
				.sum();
	}

	private record OASISReport(List<History> history) {

		public static OASISReport parse(List<String> input) {
			var history = input.stream()
					.map(History::parse)
					.toList();

			return new OASISReport(history);
		}

		public LongStream extrapolateNextHistoryValue() {
			return history.stream()
					.mapToLong(History::extrapolateNextValue);
		}

		public LongStream extrapolatePreviousHistoryValue() {
			return history.stream()
					.mapToLong(History::extrapolatePreviousValue);
		}
	}

	private record History(List<Long> values) {

		public static History parse(String input) {
			var values = Arrays.stream(input.split("\\s"))
					.map(Long::parseLong)
					.toList();

			return new History(values);
		}

		public long extrapolateNextValue() {
			return extrapolate(List::getLast, Long::sum);
		}

		private long extrapolate(ToLongFunction<List<Long>> identityExtractor, LongBinaryOperator operator) {
			var exptrapolatedValues = new ArrayList<List<Long>>();
			exptrapolatedValues.add(values);

			while (differentValues(exptrapolatedValues.getLast())) {
				var valuesToExtrapolate = exptrapolatedValues.getLast();

				if (valuesToExtrapolate.size() == 1) {
					exptrapolatedValues.add(List.of(0L));
				} else {
					exptrapolatedValues.add(IntStream.range(0, valuesToExtrapolate.size() - 1)
							.mapToObj(i -> valuesToExtrapolate.get(i + 1) - valuesToExtrapolate.get(i))
							.toList());
				}
			}

			return IntStream.range(0, exptrapolatedValues.size() - 1)
					.boxed()
					.sorted(reverseOrder())
					.map(exptrapolatedValues::get)
					.mapToLong(identityExtractor)
					.reduce(identityExtractor.applyAsLong(exptrapolatedValues.getLast()), operator);
		}

		private boolean differentValues(List<Long> values) {
			return values.stream().distinct().count() > 1;
		}

		public long extrapolatePreviousValue() {
			return extrapolate(List::getFirst, (a, b) -> b - a);
		}
	}
}
