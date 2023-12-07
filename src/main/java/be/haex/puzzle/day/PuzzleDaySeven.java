package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Comparator.comparingInt;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.partitioningBy;

public class PuzzleDaySeven implements Puzzle<Integer> {

	@Override
	public Integer solvePartOne() {
		return readCamelCards(false).totalWinnings();
	}

	private CamelCards readCamelCards(boolean jokerRuleEnabled) {
		return CamelCards.parse(readContentOfInputFile("puzzleDaySeven.txt"), jokerRuleEnabled);
	}

	@Override
	public Integer solvePartTwo() {
		return readCamelCards(true).totalWinnings();
	}

	private record CamelCards(List<Hand> hands, boolean jokerRuleEnabled) {

		public static CamelCards parse(List<String> input, boolean jokerRuleEnabled) {
			var hands = input.stream()
					.map(line -> Hand.parse(line, jokerRuleEnabled))
					.toList();

			return new CamelCards(hands, jokerRuleEnabled);
		}

		public int totalWinnings() {
			var handsSortedOnStrength = hands.stream()
					.sorted()
					.toList();

			return IntStream.range(0, handsSortedOnStrength.size())
					.map(index -> handsSortedOnStrength.get(index).winnings(index + 1))
					.sum();
		}
	}

	private record Hand(HandType type, List<Card> cards, Bid bid,
						boolean jokerRuleEnabled) implements Comparable<Hand> {

		public static Hand parse(String input, boolean jokerRuleEnabled) {
			var parts = input.split("\\s");
			var cards = Stream.of(parts[0].split(""))
					.map(Card::from)
					.toList();
			var type = determineHandType(cards, jokerRuleEnabled);
			var bid = new Bid(Integer.parseInt(parts[1]));

			return new Hand(type, cards, bid, jokerRuleEnabled);
		}

		private static HandType determineHandType(List<Card> cards, boolean jokerRuleEnabled) {
			if (!jokerRuleEnabled) {
				return determineHandType(cards);
			} else {
				var jacks = cards.stream().collect(partitioningBy(card -> card == Card.JACK));
				var jokers = jacks.get(true).size();
				var typeWithoutJacks = determineHandType(jacks.get(false));

				if (jokers == 0) {
					return typeWithoutJacks;
				} else if (jokers >= 4) {
					return HandType.FIVE_OF_A_KIND;
				} else if (jokers == 3) {
					return switch (typeWithoutJacks) {
						case HIGH_CARD -> HandType.FOUR_OF_A_KIND;
						case ONE_PAIR -> HandType.FIVE_OF_A_KIND;
						default ->
								throw new IllegalStateException("Unexpected value with 3 jokers: " + typeWithoutJacks);
					};
				} else if (jokers == 2) {
					return switch (typeWithoutJacks) {
						case HIGH_CARD -> HandType.THREE_OF_A_KIND;
						case ONE_PAIR -> HandType.FOUR_OF_A_KIND;
						case THREE_OF_A_KIND -> HandType.FIVE_OF_A_KIND;
						default ->
								throw new IllegalStateException("Unexpected value with 2 jokers: " + typeWithoutJacks);
					};
				} else {
					return switch (typeWithoutJacks) {
						case HIGH_CARD -> HandType.ONE_PAIR;
						case ONE_PAIR -> HandType.THREE_OF_A_KIND;
						case TWO_PAIR -> HandType.FULL_HOUSE;
						case THREE_OF_A_KIND -> HandType.FOUR_OF_A_KIND;
						case FOUR_OF_A_KIND -> HandType.FIVE_OF_A_KIND;
						default ->
								throw new IllegalStateException("Unexpected value with 1 joker: " + typeWithoutJacks);
					};
				}
			}
		}

		private static HandType determineHandType(List<Card> cards) {
			return Stream.of(HandType.values())
					.sorted(comparingInt(HandType::strength).reversed())
					.filter(handType -> handType.matcher().test(cards))
					.findFirst()
					.orElseThrow();
		}

		public int winnings(int multiplier) {
			return multiplier * bid.value();
		}

		@Override
		public int compareTo(Hand o) {
			return comparingInt(Hand::strength)
					.thenComparingInt(cardStrength(List::getFirst))
					.thenComparingInt(cardStrength(values -> values.get(1)))
					.thenComparingInt(cardStrength(values -> values.get(2)))
					.thenComparingInt(cardStrength(values -> values.get(3)))
					.thenComparingInt(cardStrength(List::getLast))
					.compare(this, o);
		}

		private int strength() {
			return type().strength();
		}

		private ToIntFunction<Hand> cardStrength(Function<List<Card>, Card> cardSelector) {
			return hand -> {
				var selectedCard = cardSelector.apply(hand.cards());

				if (jokerRuleEnabled) {
					return selectedCard.alternativeStrength();
				} else {
					return selectedCard.strength();
				}
			};
		}
	}

	private enum HandType {
		FIVE_OF_A_KIND(7, cards -> cards.values().stream().max(Long::compareTo).orElse(0L) == 5),
		FOUR_OF_A_KIND(6, cards -> cards.values().stream().max(Long::compareTo).orElse(0L) == 4),
		FULL_HOUSE(5, cards -> cards.size() == 2 && cards.values().stream().mapToLong(Long::longValue).sum() == 5),
		THREE_OF_A_KIND(4, cards -> cards.values().stream().max(Long::compareTo).orElse(0L) == 3),
		TWO_PAIR(3, cards -> cards.values().stream().filter(value -> value == 2).count() == 2),
		ONE_PAIR(2, cards -> cards.values().stream().max(Long::compareTo).orElse(0L) == 2),
		HIGH_CARD(1, cards -> true);

		private final int strength;
		private final HandTypeMatcher matcher;

		HandType(int strength, HandTypeMatcher matcher) {
			this.strength = strength;
			this.matcher = matcher;
		}

		public int strength() {
			return strength;
		}

		public HandTypeMatcher matcher() {
			return matcher;
		}
	}

	@FunctionalInterface
	private interface HandTypeMatcher extends Predicate<List<Card>> {

		default boolean test(List<Card> cards) {
			var groupedCards = cards.stream()
					.collect(groupingBy(identity(), counting()));

			return test(groupedCards);
		}

		boolean test(Map<Card, Long> cards);
	}

	private enum Card {
		ACE("A", 14),
		KING("K", 13),
		QUEEN("Q", 12),
		JACK("J", 11),
		TEN("T", 9),
		NINE("9", 8),
		EIGHT("8", 7),
		SEVEN("7", 6),
		SIX("6", 5),
		FIVE("5", 4),
		FOUR("4", 3),
		THREE("3", 2),
		TWO("2", 1);

		private final String value;
		private final int strength;

		Card(String value, int strength) {
			this.value = value;
			this.strength = strength;
		}

		public static Card from(String value) {
			return Arrays.stream(values())
					.filter(card -> card.value.equals(value))
					.findFirst()
					.orElseThrow(() -> new IllegalArgumentException("Unknown card value: " + value));
		}

		public int strength() {
			return strength;
		}

		public int alternativeStrength() {
			if (this == JACK) {
				return 0;
			} else {
				return strength;
			}
		}
	}

	private record Bid(int value) {
	}
}
