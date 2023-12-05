package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static java.util.Comparator.comparingLong;

public class PuzzleDayFive implements Puzzle<Long> {

	@Override
	public Long solvePartOne() {
		return readAlmanac()
				.lowestLocation(Collection::stream);
	}

	private Almanac readAlmanac() {
		return Almanac.parse(readContentOfInputFile("puzzleDayFive.txt"));
	}

	@Override
	public Long solvePartTwo() {
		return readAlmanac()
				.lowestLocation(seedsToBePlanted -> IntStream.range(0, seedsToBePlanted.size())
						.filter(index -> index % 2 == 0)
						.mapToObj(index -> LongStream.range(seedsToBePlanted.get(index), seedsToBePlanted.get(index) + seedsToBePlanted.get(index + 1) - 1))
						.flatMap(LongStream::boxed));
	}

	private record Almanac(List<Long> seedsToBePlanted,
						   Lookup seedToSoil,
						   Lookup soilToFertilizer,
						   Lookup fertilizerToWater,
						   Lookup waterToLight,
						   Lookup lightToTemperature,
						   Lookup temperatureToHumidity,
						   Lookup humidityToLocation) {

		public static Almanac parse(List<String> input) {
			var seedsToPlanInput = input.removeFirst();
			var emptyIndexes = IntStream.range(1, input.size())
					.filter(index -> input.get(index).isBlank())
					.boxed()
					.toList();

			return new Almanac(
					seedsToPlantFrom(seedsToPlanInput),
					Lookup.parse(input.subList(2, emptyIndexes.get(0))),
					Lookup.parse(input.subList(emptyIndexes.get(0) + 2, emptyIndexes.get(1))),
					Lookup.parse(input.subList(emptyIndexes.get(1) + 2, emptyIndexes.get(2))),
					Lookup.parse(input.subList(emptyIndexes.get(2) + 2, emptyIndexes.get(3))),
					Lookup.parse(input.subList(emptyIndexes.get(3) + 2, emptyIndexes.get(4))),
					Lookup.parse(input.subList(emptyIndexes.get(4) + 2, emptyIndexes.get(5))),
					Lookup.parse(input.subList(emptyIndexes.get(5) + 2, input.size()))
			);
		}

		private static List<Long> seedsToPlantFrom(String input) {
			return Arrays.stream(onlySpacesAndDigits(input).split("\\s"))
					.map(Long::parseLong)
					.toList();
		}

		private static String onlySpacesAndDigits(String input) {
			return input.replace("seeds:", "").trim();
		}

		public long lowestLocation(Function<List<Long>, Stream<Long>> seedsToPlantInterpreter) {
			return seedsToPlantInterpreter.apply(seedsToBePlanted)
					.parallel()
					.map(determineLocation())
					.min(comparingLong(Long::longValue))
					.orElse(0L);
		}

		private Function<Long, Long> determineLocation() {
			return seed -> {
				var soil = seedToSoil.get(seed);
				var fertilizer = soilToFertilizer.get(soil);
				var water = fertilizerToWater.get(fertilizer);
				var light = waterToLight.get(water);
				var temperature = lightToTemperature.get(light);
				var humidity = temperatureToHumidity.get(temperature);

				return humidityToLocation.get(humidity);
			};
		}
	}

	private record Lookup(List<LookupEntry> entries) {

		public static Lookup parse(List<String> input) {
			var entries = input.stream()
					.map(LookupEntry::parse)
					.toList();

			return new Lookup(entries);
		}

		public Long get(Long key) {
			return entries.stream()
					.map(entry -> entry.get(key))
					.flatMap(Optional::stream)
					.findFirst()
					.orElse(key);
		}
	}

	private record LookupEntry(long sourceCategory, long destinationCategory, long rangeLength) {

		public static LookupEntry parse(String input) {
			var elements = Arrays.stream(input.split("\\s"))
					.map(Long::parseLong)
					.toList();

			return new LookupEntry(elements.get(1), elements.get(0), elements.get(2));
		}

		public Optional<Long> get(Long key) {
			var maximumSourceCategory = sourceCategory + rangeLength - 1;

			if (key >= sourceCategory && key <= maximumSourceCategory) {
				return Optional.of(destinationCategory + key - sourceCategory);
			} else {
				return Optional.empty();
			}
		}
	}
}
