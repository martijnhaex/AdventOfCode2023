package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static java.util.stream.Collectors.joining;

public class PuzzleDayOne implements Puzzle<Integer> {

	private static final String INPUT_FILE_NAME = "puzzleDayOne.txt";

	private final String partOneInputFileName;
	private final String partTwoInputFileName;

	PuzzleDayOne(String partOneInputFileName, String partTwoInputFileName) {
		this.partOneInputFileName = partOneInputFileName;
		this.partTwoInputFileName = partTwoInputFileName;
	}

	public PuzzleDayOne() {
		this(INPUT_FILE_NAME, INPUT_FILE_NAME);
	}

	@Override
	public Integer solvePartOne() {
		var calibrationStrategy = new NoneCalibrationStrategy();

		return CalibrationDocument.from(() -> readCalibrationLines(partOneInputFileName))
				.sumCalibrationValues(calibrationStrategy);
	}

	private List<CalibrationDocument.Line> readCalibrationLines(String fileName) {
		return streamPuzzleInput(fileName)
				.map(CalibrationDocument.Line::new)
				.toList();
	}

	@Override
	public Integer solvePartTwo() {
		var calibrationStrategy = new TextToDigitCalibrationStrategy();

		return CalibrationDocument.from(() -> readCalibrationLines(partTwoInputFileName))
				.sumCalibrationValues(calibrationStrategy);
	}

	private record CalibrationDocument(List<Line> lines) {

		public static CalibrationDocument from(Supplier<List<Line>> reader) {
			return new CalibrationDocument(reader.get());
		}

		public int sumCalibrationValues(UnaryOperator<String> calibrationStrategy) {
			return lines.stream()
					.mapToInt(line -> line.calibrationValue(calibrationStrategy))
					.sum();
		}

		private record Line(String content) {

			public int calibrationValue(UnaryOperator<String> calibrationStrategy) {
				var digits = determineDigits(calibrationStrategy);

				return switch (digits.size()) {
					case 0 -> 0;
					case 1 -> concatenate(digits.getFirst(), digits.getFirst());
					default -> concatenate(digits.getFirst(), digits.getLast());
				};
			}

			private List<Integer> determineDigits(UnaryOperator<String> calibrationStrategy) {
				return calibrationStrategy.apply(content)
						.chars()
						.filter(Character::isDigit)
						.mapToObj(Character::getNumericValue)
						.toList();
			}

			private int concatenate(Integer firstDigit, Integer secondDigit) {
				return Integer.parseInt("%s%s".formatted(firstDigit, secondDigit));
			}
		}
	}

	private static class NoneCalibrationStrategy implements UnaryOperator<String> {

		@Override
		public String apply(String value) {
			return value;
		}
	}

	private static class TextToDigitCalibrationStrategy implements UnaryOperator<String> {

		private static final Map<String, Integer> WRITTEN_NUMBERS = Map.of(
				"one", 1,
				"two", 2,
				"three", 3,
				"four", 4,
				"five", 5,
				"six", 6,
				"seven", 7,
				"eight", 8,
				"nine", 9
		);

		@Override
		public String apply(String value) {
			var digits = new ArrayList<Integer>();

			var previousCharacters = new ArrayList<Character>();
			for (int i = 0; i < value.length(); i++) {
				var currentCharacter = value.charAt(i);

				if (Character.isDigit(currentCharacter)) {
					digits.add(Character.getNumericValue(currentCharacter));
					previousCharacters.clear();
				} else {
					previousCharacters.add(currentCharacter);

					endsWithAWrittenNumber(previousCharacters)
							.ifPresent(digits::add);
				}
			}

			return digits.stream()
					.map(String::valueOf)
					.collect(joining());
		}

		private Optional<Integer> endsWithAWrittenNumber(List<Character> characters) {
			var text = characters.stream()
					.map(String::valueOf)
					.collect(joining());

			return WRITTEN_NUMBERS.entrySet()
					.stream()
					.filter(entry -> text.endsWith(entry.getKey()))
					.findFirst()
					.map(Map.Entry::getValue);
		}
	}
}
