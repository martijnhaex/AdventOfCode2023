package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.function.Predicate.isEqual;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;

public class PuzzleDayThirteen implements Puzzle<Long> {

	@Override
	public Long solvePartOne() {
		return readMirrors()
				.parallel()
				.map(Mirror::determineReflection)
				.mapToLong(Reflection::calculateNote)
				.sum();
	}

	private Stream<Mirror> readMirrors() {
		var content = readContentOfInputFile("puzzleDayThirteen.txt");
		var joinedContent = String.join("\n", content);

		return Arrays.stream(joinedContent.split("\n{2}")).map(Mirror::parse);
	}

	@Override
	public Long solvePartTwo() {
		return readMirrors()
				.parallel()
				.map(Mirror::determineSmudgeReflection)
				.mapToLong(Reflection::calculateNote)
				.sum();
	}

	private record Mirror(Pattern[][] patterns) {

		public static Mirror parse(String input) {
			var lines = input.split("\n");
			var patterns = new Pattern[lines.length][lines[0].length()];

			for (var i = 0; i < lines.length; i++) {
				var characters = lines[i].split("");

				for (var j = 0; j < characters.length; j++) {
					patterns[i][j] = Pattern.from(characters[j]);
				}
			}

			return new Mirror(patterns);
		}

		public Reflection determineReflection() {
			return determineHorizontalReflections(patterns, acceptAny()).or(() -> determineVerticalReflections(patterns, acceptAny())).orElseThrow();
		}

		private Optional<Reflection> determineHorizontalReflections(Pattern[][] values, Predicate<Reflection> accept) {
			return findReflections(values, accept, Reflection::row);
		}

		private Optional<Reflection> findReflections(Pattern[][] values, Predicate<Reflection> accept, IntFunction<Reflection> construct) {
			for (var patternIndex = 0; patternIndex < values.length - 1; patternIndex++) {
				var pattern = values[patternIndex];
				var nextPattern = values[patternIndex + 1];

				if (isSame(pattern, nextPattern)) {
					var leftPatterns = Arrays.copyOfRange(values, 0, patternIndex);
					var rightPatterns = Arrays.copyOfRange(values, patternIndex + 2, values.length);
					var potentialReflection = construct.apply(patternIndex + 1);

					if (reflectsOnlyHypotheticalPatterns(leftPatterns, rightPatterns) && accept.test(potentialReflection)) {
						return Optional.of(potentialReflection);
					} else {
						var minimalCommonLength = Math.min(leftPatterns.length, rightPatterns.length);
						var normalizedLeftPatterns = normalize(leftPatterns, leftPatterns.length - minimalCommonLength, leftPatterns.length - minimalCommonLength + minimalCommonLength);
						var normalizedRightPatterns = normalize(rightPatterns, 0, minimalCommonLength).reversed();

						if (normalizedLeftPatterns.equals(normalizedRightPatterns) && accept.test(potentialReflection)) {
							return Optional.of(potentialReflection);
						}
					}
				}
			}

			return Optional.empty();
		}

		private boolean isSame(Pattern[] pattern, Pattern[] otherPattern) {
			return Arrays.equals(pattern, otherPattern);
		}

		private boolean reflectsOnlyHypotheticalPatterns(Pattern[][] patterns, Pattern[][] otherPatterns) {
			return patterns.length == 0 && otherPatterns.length == 0;
		}

		private List<String> normalize(Pattern[][] patterns, int from, int to) {
			return Arrays.stream(Arrays.copyOfRange(patterns, from, to)).map(asString()).toList();
		}

		private Function<Pattern[], String> asString() {
			return pattern -> Arrays.stream(pattern).map(Pattern::code).collect(joining());
		}

		private Predicate<Reflection> acceptAny() {
			return reflection -> true;
		}

		private Optional<Reflection> determineVerticalReflections(Pattern[][] values, Predicate<Reflection> accept) {
			return findReflections(transpose(values), accept, Reflection::column);
		}

		private Pattern[][] transpose(Pattern[][] values) {
			var transposed = new Pattern[values[0].length][values.length];

			for (var i = 0; i < values.length; i++) {
				for (var j = 0; j < values[i].length; j++) {
					transposed[j][i] = values[i][j];
				}
			}

			return transposed;
		}

		public Reflection determineSmudgeReflection() {
			var originalReflection = determineReflection();

			for (var rowIndex = 0; rowIndex < patterns.length; rowIndex++) {
				var pattern = patterns[rowIndex];

				for (var columnIndex = 0; columnIndex < pattern.length; columnIndex++) {
					var patternsCopy = copy(patterns);
					patternsCopy[rowIndex][columnIndex] = patternsCopy[rowIndex][columnIndex].opposite();

					var reflection = determineHorizontalReflections(patternsCopy, exclude(originalReflection)).or(() -> determineVerticalReflections(patternsCopy, exclude(originalReflection)));

					if (reflection.isPresent()) {
						return reflection.get();
					}
				}
			}

			return originalReflection;
		}

		private Pattern[][] copy(Pattern[][] values) {
			return Arrays.stream(values).map(Pattern[]::clone).toArray(Pattern[][]::new);
		}

		public Predicate<Reflection> exclude(Reflection reflection) {
			return not(isEqual(reflection));
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Mirror mirror = (Mirror) o;
			return Arrays.deepEquals(patterns, mirror.patterns);
		}

		@Override
		public int hashCode() {
			return Arrays.deepHashCode(patterns);
		}

		@Override
		public String toString() {
			return new StringJoiner(", ", Mirror.class.getSimpleName() + "[", "]").add("patterns=" + Arrays.toString(patterns)).toString();
		}
	}

	private enum Pattern {
		ASH("."), ROCK("#");

		private final String code;

		Pattern(String code) {
			this.code = code;
		}

		public static Pattern from(String code) {
			return Arrays.stream(values()).filter(pattern -> pattern.code.equals(code)).findFirst().orElseThrow();
		}

		public String code() {
			return code;
		}

		public Pattern opposite() {
			return switch (this) {
				case ASH -> ROCK;
				case ROCK -> ASH;
			};
		}
	}

	private record Reflection(int index, ReflectionType type) {

		public static Reflection column(int index) {
			return new Reflection(index, ReflectionType.VERTICAL);
		}

		public static Reflection row(int index) {
			return new Reflection(index, ReflectionType.HORIZONTAL);
		}

		public long calculateNote() {
			return index * type.multiplier;
		}
	}

	private enum ReflectionType {
		VERTICAL(1), HORIZONTAL(100);

		private final long multiplier;

		ReflectionType(long multiplier) {
			this.multiplier = multiplier;
		}
	}
}
