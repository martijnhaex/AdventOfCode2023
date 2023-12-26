package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;

public class PuzzleDayTwentyTwo implements Puzzle<Long> {

	@Override
	public Long solvePartOne() {
		return readSnapshot()
				.extrapolateUntilAfterFall()
				.countSafeToDisintegrate();
	}

	private Snapshot readSnapshot() {
		return Snapshot.parse(readContentOfInputFile("puzzleDayTwentyTwo.txt"));
	}

	@Override
	public Long solvePartTwo() {
		return readSnapshot()
				.extrapolateUntilAfterFall()
				.countDisintegrateChainReaction();
	}

	private record Snapshot(List<Brick> bricks) {

		private static final int BOTTOM = 1;

		public static Snapshot parse(List<String> input) {
			var bricks = input.stream()
					.map(Brick::parse)
					.sorted()
					.toList();

			return new Snapshot(bricks);
		}

		public Snapshot extrapolateUntilAfterFall() {
			var fallenBricks = letThemFall();

			for (var upper : fallenBricks) {
				for (var lower : fallenBricks) {
					if (!lower.equals(upper) && lower.overlaps(upper) && upper.directlyStackedUpon(lower)) {
						lower.supports(upper);
						upper.supportedBy(lower);
					}
				}
			}

			return new Snapshot(fallenBricks);
		}

		private List<Brick> letThemFall() {
			var fallenBricks = new HashMap<Brick, Brick>();

			for (var brick : bricks) {
				var level = BOTTOM;

				for (var check : stackedUpon(brick, fallenBricks)) {
					if (brick.overlaps(check)) {
						level = Math.max(level, check.end().z() + 1);
					}
				}

				fallenBricks.put(brick, brick.fallTo(level));
			}

			return fallenBricks.values()
					.stream()
					.sorted()
					.toList();
		}

		private List<Brick> stackedUpon(Brick brick, Map<Brick, Brick> fallenBricks) {
			return bricks.subList(0, bricks.indexOf(brick))
					.stream()
					.map(otherBrick -> fallenBricks.getOrDefault(otherBrick, otherBrick))
					.sorted()
					.toList();
		}

		public long countSafeToDisintegrate() {
			return bricks.stream()
					.filter(safeToDisintegrate())
					.count();
		}

		private Predicate<Brick> safeToDisintegrate() {
			return brick -> brick.supports()
					.stream()
					.allMatch(supportedByMoreThanOneBrick());
		}

		private Predicate<Brick> supportedByMoreThanOneBrick() {
			return brick -> brick.supportedBy().size() >= 2;
		}

		public long countDisintegrateChainReaction() {
			return bricks.stream()
					.mapToLong(chainReaction())
					.sum();
		}

		private ToLongFunction<Brick> chainReaction() {
			return brick -> {
				var chain = new LinkedList<>(brickChain(brick));
				var fallen = new HashSet<>(chain);
				fallen.add(brick);

				while (!chain.isEmpty()) {
					for (var check : chain.pop().supports()) {
						if (!fallen.contains(check) && fallen.containsAll(check.supportedBy())) {
							chain.add(check);
							fallen.add(check);
						}
					}
				}

				return fallen.size() - 1;
			};
		}

		private List<Brick> brickChain(Brick brick) {
			return brick.supports()
					.stream()
					.map(Brick::supportedBy)
					.filter(onlySupportedByOneBrick())
					.flatMap(List::stream)
					.toList();
		}

		private Predicate<List<Brick>> onlySupportedByOneBrick() {
			return supportedBy -> supportedBy.size() == 1;
		}
	}

	private static class Brick implements Comparable<Brick> {

		private final Coordinate start;
		private final Coordinate end;
		private final List<Brick> supports;
		private final List<Brick> supportedBy;

		private Brick(Coordinate start, Coordinate end) {
			this(start, end, new ArrayList<>(), new ArrayList<>());
		}

		public Brick(Coordinate start, Coordinate end, List<Brick> supports, List<Brick> supportedBy) {
			this.start = start;
			this.end = end;
			this.supports = supports;
			this.supportedBy = supportedBy;
		}

		public static Brick parse(String input) {
			var parts = input.split("~");

			return new Brick(
					Coordinate.parse(parts[0]),
					Coordinate.parse(parts[1])
			);
		}

		public Coordinate start() {
			return start;
		}

		public Coordinate end() {
			return end;
		}

		public boolean overlaps(Brick other) {
			return Math.max(start.x(), other.start().x()) <= Math.min(end.x(), other.end().x())
					&& Math.max(start.y(), other.start().y()) <= Math.min(end.y(), other.end().y());
		}

		public boolean directlyStackedUpon(Brick other) {
			return start().z() == other.end().z() + 1;
		}

		public Brick fallTo(int level) {
			return new Brick(
					new Coordinate(start.x(), start.y(), level),
					new Coordinate(end.x(), end.y(), end.z() - (start.z() - level)),
					supports,
					supportedBy
			);
		}

		public List<Brick> supports() {
			return supports;
		}

		public void supports(Brick other) {
			supports.add(other);
		}

		public List<Brick> supportedBy() {
			return supportedBy;
		}

		public void supportedBy(Brick other) {
			supportedBy.add(other);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Brick brick = (Brick) o;
			return Objects.equals(start, brick.start) && Objects.equals(end, brick.end);
		}

		@Override
		public int hashCode() {
			return Objects.hash(start, end);
		}

		@Override
		public int compareTo(Brick other) {
			return Integer.compare(start.z(), other.start().z());
		}
	}

	private record Coordinate(int x, int y, int z) {

		public static Coordinate parse(String input) {
			var parts = input.split(",");

			return new Coordinate(
					Integer.parseInt(parts[0]),
					Integer.parseInt(parts[1]),
					Integer.parseInt(parts[2])
			);
		}
	}
}
