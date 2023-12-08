package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public class PuzzleDayEight implements Puzzle<Long> {

	private static final String INPUT_FILE_NAME = "puzzleDayEight.txt";

	private final String partOneInputFileName;
	private final String partTwoInputFileName;

	PuzzleDayEight(String partOneInputFileName, String partTwoInputFileName) {
		this.partOneInputFileName = partOneInputFileName;
		this.partTwoInputFileName = partTwoInputFileName;
	}

	public PuzzleDayEight() {
		this(INPUT_FILE_NAME, INPUT_FILE_NAME);
	}


	@Override
	public Long solvePartOne() {
		return readWasteland(partOneInputFileName)
				.countStepsBetween("^AAA$", "^ZZZ$");
	}

	private Wasteland readWasteland(String fileName) {
		return Wasteland.parse(readContentOfInputFile(fileName));
	}

	@Override
	public Long solvePartTwo() {
		return readWasteland(partTwoInputFileName)
				.countStepsBetween("^([0-9A-Z]{2})A$", "^([0-9A-Z]{2})Z$");
	}

	private record Wasteland(Instructions instructions, Map<String, Node> nodes) {

		public static Wasteland parse(List<String> input) {
			var instructions = Instructions.parse(input.removeFirst());
			var nodes = input.subList(1, input.size()).stream()
					.map(Node::parse)
					.collect(toMap(Node::name, identity()));

			return new Wasteland(instructions, nodes);
		}

		public long countStepsBetween(String sourceRegex, String destinationRegex) {
			var sourceNodes = selectNodes(sourceRegex);
			var steps = sourceNodes.stream()
					.map(sourceNode -> countStepsBetween(sourceNode, destinationRegex))
					.toList();

			return steps.stream()
					.mapToLong(Long::longValue)
					.reduce(1, (x, y) -> x * (y / greatestCommonDivisor(x, y)));
		}

		private List<Node> selectNodes(String regex) {
			return nodes.entrySet()
					.stream()
					.filter(entry -> entry.getKey().matches(regex))
					.map(Map.Entry::getValue)
					.toList();
		}

		private long countStepsBetween(Node node, String destinationRegex) {
			var steps = 0L;

			while (!node.name().matches(destinationRegex)) {
				node = navigate(node, instructions.get(steps));

				steps++;
			}

			return steps;
		}

		private Node navigate(Node node, Instruction instruction) {
			var elements = node.elements();

			return nodes.get(switch (instruction) {
				case LEFT -> elements.getFirst();
				case RIGHT -> elements.getLast();
			});
		}

		private long greatestCommonDivisor(long x, long y) {
			return y == 0 ? x : greatestCommonDivisor(y, x % y);
		}
	}

	private record Instructions(List<Instruction> values) {

		public static Instructions parse(String input) {
			var instructions = input.chars()
					.mapToObj(c -> (char) c)
					.map(String::valueOf)
					.map(Instruction::from)
					.toList();

			return new Instructions(instructions);
		}

		public Instruction get(long index) {
			return values.get((int) index % values.size());
		}
	}

	private enum Instruction {
		LEFT,
		RIGHT;

		public static Instruction from(String code) {
			return switch (code) {
				case "L" -> LEFT;
				case "R" -> RIGHT;
				default -> throw new IllegalArgumentException("Unknown code: " + code);
			};
		}
	}

	private record Node(String name, List<String> elements) {

		private static final Pattern NODE_PATTERN = Pattern.compile("^([0-9A-Z]+)\\s=\\s\\(([0-9A-Z]+),\\s([0-9A-Z]+)\\)$");

		public static Node parse(String input) {
			var matcher = NODE_PATTERN.matcher(input);

			if (!matcher.matches()) {
				throw new IllegalArgumentException("Invalid node input: %s".formatted(input));
			}

			return new Node(matcher.group(1), List.of(matcher.group(2), matcher.group(3)));
		}
	}
}
