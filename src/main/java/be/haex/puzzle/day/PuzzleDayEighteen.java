package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.function.Predicate.not;

public class PuzzleDayEighteen implements Puzzle<Long> {

	@Override
	public Long solvePartOne() {
		return readDigPlan(Instruction::fromWYSIWYG)
				.calculateDiggedAreaSize(new AreaSizeStrategy.FloodFill('#'));
	}

	private DigPlan readDigPlan(Function<String, Instruction> parseInstruction) {
		return DigPlan.parse(
				readContentOfInputFile("puzzleDayEighteen.txt"),
				parseInstruction
		);
	}

	@Override
	public Long solvePartTwo() {
		return readDigPlan(Instruction::fromColor)
				.calculateDiggedAreaSize(new AreaSizeStrategy.Shoelace());
	}

	private record DigPlan(List<Instruction> instructions) {

		public static DigPlan parse(List<String> input, Function<String, Instruction> parseInstruction) {
			var instructions = input.stream()
					.map(parseInstruction)
					.toList();

			return new DigPlan(instructions);
		}

		public long calculateDiggedAreaSize(AreaSizeStrategy strategy) {
			return strategy.calculateAreaSize(instructions);
		}
	}

	private record Instruction(Direction direction, int meters, String color) {

		private static final Pattern PATTERN = Pattern.compile("^(?<direction>[UDLR])\\s(?<meters>\\d+)\\s\\((?<color>#[a-z0-9]+)\\)$");

		public static Instruction fromWYSIWYG(String input) {
			return parse(
					input,
					matcher -> new Instruction(
							Direction.from(matcher.group("direction")),
							Integer.parseInt(matcher.group("meters")),
							matcher.group("color")
					)
			);
		}

		private static Instruction parse(String input, Function<Matcher, Instruction> createInstruction) {
			var matcher = PATTERN.matcher(input);

			if (!matcher.matches()) {
				throw new IllegalArgumentException("Invalid instruction input: %s".formatted(input));
			}

			return createInstruction.apply(matcher);
		}

		public static Instruction fromColor(String input) {
			return parse(
					input,
					matcher -> {
						var color = matcher.group("color");
						var encodedMeters = color.substring(1, 6);
						var encodedDirection = color.substring(6, 7);

						return new Instruction(
								Direction.from(Integer.parseInt(encodedDirection, 16)),
								Integer.parseInt(encodedMeters, 16),
								color
						);
					}
			);
		}
	}

	private enum Direction {
		UP,
		DOWN,
		LEFT,
		RIGHT;

		public static Direction from(String character) {
			return switch (character) {
				case "U" -> UP;
				case "D" -> DOWN;
				case "L" -> LEFT;
				case "R" -> RIGHT;
				default -> throw new IllegalArgumentException("Unknown direction: " + character);
			};
		}

		public static Direction from(int radial) {
			return switch (radial) {
				case 3 -> UP;
				case 1 -> DOWN;
				case 2 -> LEFT;
				case 0 -> RIGHT;
				default -> throw new IllegalArgumentException("Unknown direction: " + radial);
			};
		}
	}

	private sealed interface AreaSizeStrategy permits AreaSizeStrategy.FloodFill, AreaSizeStrategy.Shoelace {

		long calculateAreaSize(List<Instruction> instructions);

		record FloodFill(char fillCharacter) implements AreaSizeStrategy {

			@Override
			public long calculateAreaSize(List<Instruction> instructions) {
				var grid = InfiniteGrid.empty(fillCharacter);

				var start = new Position(0, 0);
				grid.draw(start);

				for (var instruction : instructions) {
					var end = start.move(instruction.direction(), instruction.meters());

					grid.draw(start, end);

					start = end;
				}

				grid.floodFill(new Position(1, 1));

				return grid.size();
			}
		}

		record Shoelace() implements AreaSizeStrategy {

			@Override
			public long calculateAreaSize(List<Instruction> instructions) {
				var route = new ArrayList<Position>();
				var start = new Position(0, 0);
				var outline = 0L;

				for (var instruction : instructions) {
					var end = start.move(instruction.direction(), instruction.meters());

					outline += start.distance(end);
					route.add(end);
					start = end;
				}

				return areaSize(route) + outline / 2 + 1;
			}

			private long areaSize(List<Position> route) {
				var n = route.size();
				var area = 0L;

				for (var i = 0; i < n; i++) {
					var j = (i + 1) % n;
					area += route.get(i).x * (long) route.get(j).y - route.get(j).x * (long) route.get(i).y;
				}

				return Math.abs(area) / 2;
			}
		}
	}

	private record Position(int x, int y) {

		public static List<Position> between(Position start, Position end) {
			var positions = new ArrayList<Position>();

			var dx = end.x - start.x;
			var dy = end.y - start.y;
			var steps = Math.max(Math.abs(dx), Math.abs(dy));

			for (var step = 0; step <= steps; step++) {
				var x = start.x + step * dx / steps;
				var y = start.y + step * dy / steps;

				positions.add(new Position(x, y));
			}

			return positions;
		}

		public Position move(Direction direction, int meters) {
			return switch (direction) {
				case UP -> new Position(x - meters, y);
				case DOWN -> new Position(x + meters, y);
				case LEFT -> new Position(x, y - meters);
				case RIGHT -> new Position(x, y + meters);
			};
		}

		public int distance(Position other) {
			return Math.abs(other.x - x) + Math.abs(other.y - y);
		}
	}

	private record InfiniteGrid(Map<Position, Character> values, char filledCharacter) {

		public static InfiniteGrid empty(char filledCharacter) {
			return new InfiniteGrid(new HashMap<>(), filledCharacter);
		}

		public void draw(Position position) {
			values.put(position, filledCharacter);
		}

		public void draw(Position position, char value) {
			values.put(position, value);
		}

		public void draw(Position start, Position end) {
			draw(start, end, filledCharacter);
		}

		public void draw(Position start, Position end, char value) {
			Position.between(start, end).forEach(position -> draw(position, value));
		}

		public void floodFill(Position start) {
			floodFill(start, filledCharacter);
		}

		public void floodFill(Position start, char filledCharacter) {
			floodFill(start, character -> character != filledCharacter, filledCharacter);
		}

		public void floodFill(Position start, Predicate<Character> shouldBeFilled, char filledCharacter) {
			var checked = new HashSet<Position>();
			var toBeChecked = new LinkedList<Position>();
			toBeChecked.add(start);

			while (!toBeChecked.isEmpty()) {
				var position = toBeChecked.pop();
				var characterAtPosition = read(position);

				if (shouldBeFilled.test(characterAtPosition)) {
					draw(position, filledCharacter);

					checked.add(position);

					Arrays.stream(Direction.values())
							.map(direction -> position.move(direction, 1))
							.filter(not(checked::contains))
							.forEach(toBeChecked::add);
				}
			}
		}

		public Character read(Position position) {
			return Optional.ofNullable(values.get(position)).orElse((char) 0);
		}

		public int size() {
			return values.size();
		}
	}
}
