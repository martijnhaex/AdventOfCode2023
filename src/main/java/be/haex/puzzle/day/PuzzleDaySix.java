package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class PuzzleDaySix implements Puzzle<Long> {

	@Override
	public Long solvePartOne() {
		return readRaces(input -> input.split("\\s+"))
				.map(Race::determineWaysToBeatRecord)
				.reduce(1L, (a, b) -> a * b);
	}

	private Stream<Race> readRaces(Function<String, String[]> interpreter) {
		var content = readContentOfInputFile("puzzleDaySix.txt");
		var times = parseAsLongs(content.get(0), interpreter);
		var distance = parseAsLongs(content.get(1), interpreter);

		return IntStream.range(0, times.size())
				.mapToObj(index -> new Race(times.get(index), distance.get(index)));
	}

	private List<Long> parseAsLongs(String input, Function<String, String[]> interpreter) {
		return Arrays.stream(interpreter.apply(normalize(input)))
				.map(Long::parseLong)
				.toList();
	}

	private String normalize(String input) {
		return input.replace("Time:", "")
				.replace("Distance:", "")
				.trim();
	}

	@Override
	public Long solvePartTwo() {
		return readRaces(input -> new String[]{input.replaceAll("\\s+", "")})
				.map(Race::determineWaysToBeatRecord)
				.reduce(1L, (a, b) -> a * b);
	}

	private record Race(long timeInMilliseconds, long distanceInMillimeters) {

		public long determineWaysToBeatRecord() {
			var waysToBeatRecord = 0L;
			for (var millisecondsHoldingChargingButton = 0L; millisecondsHoldingChargingButton <= timeInMilliseconds; millisecondsHoldingChargingButton++) {
				var travelTimeInMilliseconds = timeInMilliseconds - millisecondsHoldingChargingButton;

				if (travelTimeInMilliseconds * millisecondsHoldingChargingButton > distanceInMillimeters) {
					waysToBeatRecord++;
				}
			}

			return waysToBeatRecord;
		}
	}
}
