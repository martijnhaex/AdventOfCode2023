package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class PuzzleDayFifteen implements Puzzle<Integer> {

	private static final ToIntFunction<String> HASH = value -> {
		var hash = 0;

		for (int character : value.toCharArray()) {
			hash += character;
			hash *= 17;
			hash %= 256;
		}

		return hash;
	};

	@Override
	public Integer solvePartOne() {
		return readSteps()
				.mapToInt(HASH)
				.sum();
	}

	private Stream<String> readSteps() {
		return readContentOfInputFile("puzzleDayFifteen.txt")
				.stream()
				.map(line -> line.split(","))
				.flatMap(Arrays::stream);
	}


	@Override
	public Integer solvePartTwo() {
		var lightBoxes = LightBoxes.empty();

		readSteps()
				.map(asLens())
				.forEach(lightBoxes::apply);

		return lightBoxes.totalFocusingPower();
	}

	private Function<String, Lens> asLens() {
		return step -> Lens.Dash.parse(step)
				.or(() -> Lens.Equals.parse(step))
				.orElseThrow(() -> new IllegalArgumentException("Invalid step: " + step));
	}

	private sealed interface Lens permits Lens.Dash, Lens.Equals {

		String label();

		record Dash(String label) implements Lens {

			private static final Pattern PATTERN = Pattern.compile("^([a-z]+)-$");

			public static Optional<Lens> parse(String value) {
				var matcher = PATTERN.matcher(value);

				if (matcher.matches()) {
					return Optional.of(new Dash(matcher.group(1)));
				} else {
					return Optional.empty();
				}
			}
		}

		record Equals(String label, int focalLength) implements Lens {

			private static final Pattern PATTERN = Pattern.compile("^([a-z]+)=(\\d+)$");

			public static Optional<Lens> parse(String value) {
				var matcher = PATTERN.matcher(value);

				if (matcher.matches()) {
					return Optional.of(new Equals(
							matcher.group(1),
							Integer.parseInt(matcher.group(2))
					));
				} else {
					return Optional.empty();
				}
			}

			public int hash() {
				return HASH.applyAsInt(label());
			}
		}
	}

	private record LightBoxes(Map<Integer, List<Lens.Equals>> values) {

		public static LightBoxes empty() {
			return new LightBoxes(HashMap.newHashMap(256));
		}

		public int totalFocusingPower() {
			return values.entrySet()
					.stream()
					.mapToInt(focusingPower())
					.sum();
		}

		public void apply(Lens lens) {
			if (lens instanceof Lens.Dash) {
				removeLens(lens.label());
			} else if (lens instanceof Lens.Equals equals) {
				addLens(equals);
			} else {
				throw new IllegalArgumentException("Invalid lens: " + lens);
			}
		}

		private void removeLens(String label) {
			values.values()
					.forEach(lenses -> lenses.removeIf(lens -> lens.label().equals(label)));
		}

		private void addLens(Lens.Equals lens) {
			var boxNumber = lens.hash();

			values.putIfAbsent(boxNumber, new ArrayList<>());
			values.computeIfPresent(boxNumber, (key, lenses) -> {
				lenses.replaceAll(lensInBox -> {
					if (lensInBox.label().equals(lens.label())) {
						return lens;
					} else {
						return lensInBox;
					}
				});

				if (!lenses.contains(lens)) {
					lenses.add(lens);
				}

				return lenses;
			});
		}

		public ToIntFunction<Map.Entry<Integer, List<Lens.Equals>>> focusingPower() {
			return entry -> {
				var boxNumber = entry.getKey() + 1;
				var lenses = entry.getValue();

				return IntStream.range(0, lenses.size())
						.map(slot -> boxNumber * (slot + 1) * lenses.get(slot).focalLength())
						.sum();
			};
		}
	}
}
