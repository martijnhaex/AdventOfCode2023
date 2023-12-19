package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public class PuzzleDayNineteen implements Puzzle<Long> {

	@Override
	public Long solvePartOne() {
		return readMachine()
				.acceptedParts()
				.mapToLong(Part::score)
				.sum();
	}

	private Machine readMachine() {
		var input = readContentOfInputFile("puzzleDayNineteen.txt");
		var emptyLineIndex = input.indexOf("");
		var workflows = input.subList(0, emptyLineIndex)
				.stream()
				.map(Workflow::parse)
				.collect(toMap(Workflow::name, identity()));
		var parts = input.subList(emptyLineIndex + 1, input.size())
				.stream()
				.map(Part::parse)
				.toList();

		return new Machine(workflows, parts);
	}

	@Override
	public Long solvePartTwo() {
		return readMachine()
				.guessAcceptedParts();
	}

	private record Machine(Map<String, Workflow> workflows, List<Part> parts) {

		private static final String START_WORKFLOW_NAME = "in";

		private Stream<Part> acceptedParts() {
			return parts.stream()
					.filter(accepted());
		}

		private Predicate<Part> accepted() {
			return part -> {
				var workflow = workflows.get(START_WORKFLOW_NAME);

				Outcome outcome;
				do {
					outcome = workflow.evaluate(part);

					if (outcome instanceof Outcome.SendTo sendTo) {
						workflow = workflows.get(sendTo.workflowName());
					} else {
						workflow = null;
					}
				} while (workflow != null);

				return outcome instanceof Outcome.Accepted;
			};
		}

		private long guessAcceptedParts() {
			var magicPart = MagicPart.maximum();
			var workflow = workflows.get(START_WORKFLOW_NAME);

			return calculateCombinationsFor(magicPart, workflow.rules());
		}

		private long calculateCombinationsFor(MagicPart magicPart, List<Rule> rules) {
			var rule = rules.getFirst();

			return switch (rule) {
				case Rule.Condition condition ->
						calculateCombinationsFor(magicPart, condition, rules.subList(1, rules.size()));
				case Rule.Statement statement -> calculateCombinationsFor(statement.outcome(), magicPart);
			};
		}

		private long calculateCombinationsFor(MagicPart magicPart, Rule.Condition condition, List<Rule> remainingRules) {
			var category = condition.category();
			var operator = condition.operator();
			var threshold = condition.threshold();
			var outcome = condition.outcome();

			var truthy = magicPart.apply(operator, threshold, category);
			var falsy = operator == Operator.LESS_THAN
					? magicPart.apply(Operator.GREATER_THAN_OR_EQUAL_TO, threshold, category)
					: magicPart.apply(Operator.LESS_THAN_OR_EQUAL_TO, threshold, category);

			return calculateCombinationsFor(outcome, truthy) + calculateCombinationsFor(falsy, remainingRules);
		}

		private long calculateCombinationsFor(Outcome outcome, MagicPart magicPart) {
			return switch (outcome) {
				case Outcome.Accepted ignored -> magicPart.combinations();
				case Outcome.Rejected ignored -> 0;
				case Outcome.SendTo sendTo -> {
					var nextWorkflow = workflows.get(sendTo.workflowName());

					yield calculateCombinationsFor(magicPart, nextWorkflow.rules());
				}
				default -> throw new IllegalStateException("Unexpected statement outcome: " + outcome);
			};
		}
	}

	private record Workflow(String name, List<Rule> rules) {

		private static final Pattern PATTERN = Pattern.compile("^(?<name>[a-z]*)\\{(?<rules>.*)}$");

		public static Workflow parse(String input) {
			var matcher = PATTERN.matcher(input);

			if (!matcher.matches()) {
				throw new IllegalArgumentException("Invalid workflow input: " + input);
			}

			return new Workflow(
					matcher.group("name"),
					Arrays.stream(matcher.group("rules").split(","))
							.map(Rule::parse)
							.toList()
			);
		}

		public Outcome evaluate(Part part) {
			var ruleEngine = new LinkedList<>(rules);

			Outcome outcome;
			do {
				var rule = ruleEngine.pop();

				outcome = rule.evaluate(part);
			} while (outcome instanceof Outcome.Continue);

			return outcome;
		}
	}

	private sealed interface Rule permits Rule.Condition, Rule.Statement {

		static Rule parse(String input) {
			return Condition.parse(input)
					.or(() -> Statement.parse(input))
					.orElseThrow(() -> new IllegalArgumentException("No rule found for " + input));
		}

		Outcome evaluate(Part part);

		record Condition(Category category, Operator operator, int threshold, Outcome outcome) implements Rule {

			private static final Pattern PATTERN = Pattern.compile("^(?<category>[xmas])(?<operator>[<>])(?<threshold>\\d+):(?<next>[a-zA-Z]+)$");

			public static Optional<Rule> parse(String input) {
				var matcher = PATTERN.matcher(input);

				if (!matcher.matches()) {
					return Optional.empty();
				}

				return Optional.of(new Condition(
						Category.from(matcher.group("category")),
						Operator.from(matcher.group("operator")),
						Integer.parseInt(matcher.group("threshold")),
						Outcome.parse(matcher.group("next"))
				));
			}

			@Override
			public Outcome evaluate(Part part) {
				var score = part.score(category);

				return isTruthy(score)
						? outcome
						: new Outcome.Continue();
			}

			private boolean isTruthy(int score) {
				return switch (operator) {
					case LESS_THAN -> score < threshold;
					case GREATER_THAN -> score > threshold;
					default -> throw new IllegalStateException("Unexpected operator: " + operator);
				};
			}
		}

		record Statement(Outcome outcome) implements Rule {

			public static Optional<Rule> parse(String input) {
				return Optional.of(new Statement(Outcome.parse(input)));
			}

			@Override
			public Outcome evaluate(Part part) {
				return outcome;
			}
		}
	}

	private enum Operator {
		GREATER_THAN(">"),
		GREATER_THAN_OR_EQUAL_TO(">="),
		LESS_THAN("<"),
		LESS_THAN_OR_EQUAL_TO("<=");

		private final String code;

		Operator(String code) {
			this.code = code;
		}

		public static Operator from(String code) {
			return Arrays.stream(Operator.values())
					.filter(operator -> operator.code.equals(code))
					.findFirst()
					.orElseThrow(() -> new IllegalArgumentException("Unknown operator: " + code));
		}
	}

	private sealed interface Outcome permits Outcome.Accepted, Outcome.Continue, Outcome.Rejected, Outcome.SendTo {

		static Outcome parse(String input) {
			return Accepted.parse(input)
					.or(() -> Rejected.parse(input))
					.or(() -> SendTo.parse(input))
					.orElseThrow(() -> new IllegalArgumentException("No outcome found for " + input));
		}

		record Accepted() implements Outcome {

			private static final String PATTERN = "A";

			public static Optional<Outcome> parse(String input) {
				if (PATTERN.equals(input)) {
					return Optional.of(new Accepted());
				} else {
					return Optional.empty();
				}
			}
		}

		record Continue() implements Outcome {
		}

		record Rejected() implements Outcome {

			private static final String PATTERN = "R";

			public static Optional<Outcome> parse(String input) {
				if (PATTERN.equals(input)) {
					return Optional.of(new Rejected());
				} else {
					return Optional.empty();
				}
			}
		}

		record SendTo(String workflowName) implements Outcome {

			public static Optional<Outcome> parse(String input) {
				return Optional.of(new SendTo(input));
			}
		}
	}

	private record Part(Map<Category, Rating> ratings) {

		private static final Pattern PATTERN = Pattern.compile("^\\{(?<categories>.*)}$");

		public static Part parse(String input) {
			var matcher = PATTERN.matcher(input);

			if (!matcher.matches()) {
				throw new IllegalArgumentException("Invalid part input: " + input);
			}

			return new Part(
					Arrays.stream(matcher.group("categories").split(","))
							.map(Rating::parse)
							.collect(toMap(Rating::category, identity()))
			);
		}

		public int score(Category category) {
			return ratings.get(category).score();
		}

		public int score() {
			return ratings.values()
					.stream()
					.mapToInt(Rating::score)
					.sum();
		}
	}

	private record Rating(Category category, int score) {

		private static final Pattern PATTERN = Pattern.compile("^(?<category>[xmas])=(?<score>\\d+)$");

		public static Rating parse(String input) {
			var matcher = PATTERN.matcher(input);

			if (!matcher.matches()) {
				throw new IllegalArgumentException("Invalid rating input: " + input);
			}

			return new Rating(
					Category.from(matcher.group("category")),
					Integer.parseInt(matcher.group("score"))
			);
		}
	}

	private enum Category {
		EXTREMELY_COOL_LOOKING,
		MUSICAL,
		AERODYNAMIC,
		SHINY;

		public static Category from(String character) {
			return switch (character) {
				case "x" -> EXTREMELY_COOL_LOOKING;
				case "m" -> MUSICAL;
				case "a" -> AERODYNAMIC;
				case "s" -> SHINY;
				default -> throw new IllegalArgumentException("Unknown category: " + character);
			};
		}
	}

	private record MagicPart(Map<Category, RatingRange> ranges) {

		public static MagicPart maximum() {
			var ranges = Arrays.stream(Category.values())
					.map(category -> Map.entry(category, RatingRange.fourThousand()))
					.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

			return new MagicPart(ranges);
		}

		public MagicPart apply(Operator operator, int threshold, Category category) {
			return new MagicPart(ranges.entrySet()
					.stream()
					.map(entry -> {
						if (category == entry.getKey()) {
							return Map.entry(entry.getKey(), entry.getValue().apply(operator, threshold));
						} else {
							return entry;
						}
					})
					.collect(toMap(Map.Entry::getKey, Map.Entry::getValue)));
		}

		public long combinations() {
			return ranges.values()
					.stream()
					.mapToLong(RatingRange::count)
					.reduce(1, (a, b) -> a * b);
		}
	}

	private record RatingRange(int minimum, int maximum) {

		public static RatingRange fourThousand() {
			return new RatingRange(1, 4_000);
		}

		public RatingRange apply(Operator operator, int threshold) {
			return switch (operator) {
				case GREATER_THAN -> greaterThan(threshold);
				case GREATER_THAN_OR_EQUAL_TO -> greaterThanOrEqualTo(threshold);
				case LESS_THAN -> lessThan(threshold);
				case LESS_THAN_OR_EQUAL_TO -> lessThanOrEqualTo(threshold);
			};
		}

		private RatingRange greaterThan(int threshold) {
			return greaterThanOrEqualTo(threshold + 1);
		}

		private RatingRange greaterThanOrEqualTo(int threshold) {
			return new RatingRange(Math.max(threshold, minimum), maximum);
		}

		private RatingRange lessThan(int threshold) {
			return lessThanOrEqualTo(threshold - 1);
		}

		private RatingRange lessThanOrEqualTo(int threshold) {
			return new RatingRange(minimum, Math.min(threshold, maximum));
		}

		public int count() {
			return maximum - minimum + 1;
		}
	}
}
