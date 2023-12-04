package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

public class PuzzleDayFour implements Puzzle<Long> {

	@Override
	public Long solvePartOne() {
		return readScratchCards()
				.mapToLong(ScratchCard::points)
				.sum();
	}

	private Stream<ScratchCard> readScratchCards() {
		return readContentOfInputFile("puzzleDayFour.txt")
				.stream()
				.map(ScratchCard::parse);
	}

	@Override
	public Long solvePartTwo() {
		var originalScratchCard = readScratchCards().toList();
		var scratchCards = originalScratchCard.stream()
				.collect(groupingBy(ScratchCard::id, counting()));

		originalScratchCard
				.forEach(scratchCard -> scratchCard.wonScratchCards()
						.forEach(wonScratchCard -> scratchCards.computeIfPresent(wonScratchCard, (id, amount) -> amount + scratchCards.get(scratchCard.id()))));

		return scratchCards.values()
				.stream()
				.mapToLong(Long::longValue)
				.sum();
	}

	private record ScratchCard(int id, List<Integer> pickedNumbers, List<Integer> winningNumbers) {

		private static final Pattern SCRATCH_CARD_PATTERN = Pattern.compile("^Card\\s+(?<id>\\d+):(?<winningNumbers>[\\d\\s]+)\\|(?<pickedNumbers>[\\d\\s]+)$");

		public static ScratchCard parse(String input) {
			var scratchCardPatternMatcher = SCRATCH_CARD_PATTERN.matcher(input);

			if (!scratchCardPatternMatcher.matches()) {
				throw new IllegalArgumentException("Invalid scratch card input: %s".formatted(input));
			}

			return new ScratchCard(
					Integer.parseInt(scratchCardPatternMatcher.group("id")),
					numbers(scratchCardPatternMatcher.group("pickedNumbers")),
					numbers(scratchCardPatternMatcher.group("winningNumbers"))
			);
		}

		private static List<Integer> numbers(String input) {
			return Stream.of(input.split(" "))
					.filter(not(String::isBlank))
					.map(Integer::parseInt)
					.toList();
		}

		public long points() {
			return (long) Math.pow(2, pickedWinningNumbers().count() - 1d);
		}

		private Stream<Integer> pickedWinningNumbers() {
			return pickedNumbers.stream()
					.filter(winningNumbers::contains);
		}

		public List<Integer> wonScratchCards() {
			return LongStream.rangeClosed(1, pickedWinningNumbers().count())
					.mapToObj(index -> id + index)
					.map(Long::intValue)
					.toList();
		}
	}
}
