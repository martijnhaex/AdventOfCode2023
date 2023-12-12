package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;

public class PuzzleDayTwelve implements Puzzle<Long> {

	@Override
	public Long solvePartOne() {
		return readField()
				.calculateArrangements();
	}

	private Field readField() {
		return Field.parse(readContentOfInputFile("puzzleDayTwelve.txt"));
	}

	@Override
	public Long solvePartTwo() {
		return readField()
				.unfold(5)
				.calculateArrangements();
	}

	private record Field(List<Springs> springs) {

		public static Field parse(List<String> input) {
			var springs = input.stream()
					.map(Springs::parse)
					.toList();

			return new Field(springs);
		}

		public long calculateArrangements() {
			return springs.stream()
					.mapToLong(Springs::calculateArrangements)
					.sum();
		}

		public Field unfold(int times) {
			var unfoldedSprings = springs.stream()
					.map(springs -> springs.unfold(times))
					.toList();

			return new Field(unfoldedSprings);
		}
	}

	private record Springs(List<Condition> conditions, List<Integer> damagedGroupSizes) {

		private static final Map<Springs, Long> MEMOIZATION = new HashMap<>();
		private static final Pattern SPRINGS_PATTERN = Pattern.compile("^([?.#]+)\\s([0-9,]+)$");

		public static Springs parse(String input) {
			var matcher = SPRINGS_PATTERN.matcher(input);

			if (!matcher.matches()) {
				throw new IllegalArgumentException("Invalid springs input: %s".formatted(input));
			}

			return new Springs(
					Arrays.stream(matcher.group(1).split(""))
							.map(Condition::from)
							.toList(),
					Arrays.stream(matcher.group(2).split(","))
							.map(Integer::parseInt)
							.toList()
			);
		}

		public long calculateArrangements() {
			return calculateArrangements(conditions, damagedGroupSizes);
		}

		private long calculateArrangements(List<Condition> conditions, List<Integer> damagedGroupSizes) {
			if (conditions.isEmpty()) {
				return damagedGroupSizes.isEmpty() ? 1 : 0;
			}

			var springs = new Springs(conditions, damagedGroupSizes);
			if (!MEMOIZATION.containsKey(springs)) {
				var condition = conditions.getFirst();
				var arrangements = switch (condition) {
					case DAMAGED -> calculateArrangementsSkippingFirstDamagedGroup(conditions, damagedGroupSizes);
					case OPERATIONAL -> calculateArrangementsSkippingFirstOperational(conditions, damagedGroupSizes);
					case UNKNOWN -> calculateArrangementsSkippingFirstUnknown(conditions, damagedGroupSizes);
				};

				MEMOIZATION.put(springs, arrangements);
			}

			return MEMOIZATION.get(springs);
		}

		private long calculateArrangementsSkippingFirstDamagedGroup(List<Condition> conditions, List<Integer> damagedGroupSizes) {
			if (damagedGroupSizes.isEmpty()) {
				return 0;
			} else {
				var damagedGroupSize = damagedGroupSizes.getFirst();

				if (sufficientConditionsForDamagedGroup(conditions, damagedGroupSize) && noOperationalInDamagedGroup(conditions, damagedGroupSize)) {
					var remainingDamagedGroupSizes = damagedGroupSizes.subList(1, damagedGroupSizes.size());

					if (damagedGroupSize == conditions.size()) {
						return remainingDamagedGroupSizes.isEmpty() ? 1 : 0;
					} else {
						return switch (conditions.get(damagedGroupSize)) {
							case DAMAGED -> 0;
							case OPERATIONAL ->
									calculateArrangementsSkippingFirstDamagedGroup(conditions, damagedGroupSize, remainingDamagedGroupSizes);
							case UNKNOWN ->
									calculateArrangementsSkippingFirstDamagedGroupAssumingOperational(conditions, damagedGroupSize, remainingDamagedGroupSizes);
						};
					}
				} else {
					return 0;
				}
			}
		}

		private boolean sufficientConditionsForDamagedGroup(List<Condition> conditions, Integer damagedGroupSize) {
			return damagedGroupSize <= conditions.size();
		}

		private boolean noOperationalInDamagedGroup(List<Condition> conditions, Integer damagedGroupSize) {
			return conditions.stream()
					.limit(damagedGroupSize)
					.allMatch(damaged().or(unknown()));
		}

		private Predicate<Condition> damaged() {
			return condition -> condition == Condition.DAMAGED;
		}

		private Predicate<Condition> unknown() {
			return condition -> condition == Condition.UNKNOWN;
		}

		private long calculateArrangementsSkippingFirstDamagedGroup(List<Condition> conditions, Integer damagedGroupSize, List<Integer> damagedGroupSizes) {
			var conditionsSkippingDamagedGroup = conditions.subList(damagedGroupSize + 1, conditions.size());

			return calculateArrangements(conditionsSkippingDamagedGroup, damagedGroupSizes);
		}

		private long calculateArrangementsSkippingFirstDamagedGroupAssumingOperational(List<Condition> conditions, Integer damagedGroupSize, List<Integer> damagedGroupSizes) {
			var conditionsSkippingDamagedGroup = conditions.subList(damagedGroupSize + 1, conditions.size());

			return calculateArrangements(
					concatenate(conditionsSkippingDamagedGroup, Condition.OPERATIONAL),
					damagedGroupSizes
			);
		}

		private List<Condition> concatenate(List<Condition> conditions, Condition condition) {
			var concatenatedConditions = new ArrayList<>(conditions);
			concatenatedConditions.addFirst(condition);

			return concatenatedConditions;
		}

		private long calculateArrangementsSkippingFirstOperational(List<Condition> conditions, List<Integer> damagedGroupSizes) {
			var conditionsSkippingFirstCondition = conditions.subList(1, conditions.size());

			return calculateArrangements(conditionsSkippingFirstCondition, damagedGroupSizes);
		}

		private long calculateArrangementsSkippingFirstUnknown(List<Condition> conditions, List<Integer> damagedGroupSizes) {
			var conditionsSkippingFirstCondition = conditions.subList(1, conditions.size());
			var operationalArrangements = calculateArrangements(
					concatenate(conditionsSkippingFirstCondition, Condition.OPERATIONAL),
					damagedGroupSizes
			);
			var damagedArrangements = calculateArrangements(
					concatenate(conditionsSkippingFirstCondition, Condition.DAMAGED),
					damagedGroupSizes
			);

			return operationalArrangements + damagedArrangements;
		}

		public Springs unfold(int times) {
			var unfoldedConditions = unfold(
					conditions.stream().map(Condition::code).collect(joining()),
					times,
					Condition.UNKNOWN.code()
			);
			var unfoldedDamagedGroupSizes = unfold(
					damagedGroupSizes.stream().map(String::valueOf).collect(joining(",")),
					times,
					","
			);

			return Springs.parse("%s %s".formatted(unfoldedConditions, unfoldedDamagedGroupSizes));
		}

		private String unfold(String value, int times, String delimiter) {
			return IntStream.range(0, times)
					.mapToObj(ignore -> value)
					.collect(joining(delimiter));
		}
	}

	private enum Condition {
		OPERATIONAL("."),
		DAMAGED("#"),
		UNKNOWN("?");

		private final String code;

		Condition(String code) {
			this.code = code;
		}

		public static Condition from(String code) {
			return Arrays.stream(values())
					.filter(condition -> condition.code.equals(code))
					.findFirst()
					.orElseThrow();
		}

		public String code() {
			return code;
		}
	}
}
