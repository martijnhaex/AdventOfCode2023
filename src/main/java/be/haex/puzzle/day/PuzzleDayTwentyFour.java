package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.ToLongFunction;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static java.math.MathContext.DECIMAL128;
import static java.math.RoundingMode.HALF_UP;

public class PuzzleDayTwentyFour implements Puzzle<Long> {

	private static final String DEFAULT_FILE_NAME = "puzzleDayTwentyFour.txt";
	private static final long DEFAULT_MAXIMUM_XY_POSITION = 400_000_000_000_000L;
	private static final long DEFAULT_MINIMUM_XY_POSITION = 200_000_000_000_000L;

	private final long maximumXYPosition;
	private final long minimumXYPosition;
	private final String fileName;

	PuzzleDayTwentyFour(long minimumXYPosition, long maximumXYPosition, String fileName) {
		this.minimumXYPosition = minimumXYPosition;
		this.maximumXYPosition = maximumXYPosition;
		this.fileName = fileName;
	}

	public PuzzleDayTwentyFour() {
		this(DEFAULT_MINIMUM_XY_POSITION, DEFAULT_MAXIMUM_XY_POSITION, DEFAULT_FILE_NAME);
	}

	@Override
	public Long solvePartOne() {
		var hailStones = readHailStones();

		return IntStream.range(0, hailStones.size())
				.mapToLong(index -> intersectsWith(
						hailStones.get(index),
						hailStones.subList(0, index)
				))
				.sum();
	}

	private List<HailStone> readHailStones() {
		return readContentOfInputFile(fileName)
				.stream()
				.map(HailStone::parse)
				.toList();
	}

	private long intersectsWith(HailStone hailStone, List<HailStone> hailStones) {
		return hailStones.stream()
				.map(hailStone::intersectsWith)
				.flatMap(Optional::stream)
				.filter(position -> position.withinBounds(minimumXYPosition, maximumXYPosition))
				.count();
	}

	@Override
	public Long solvePartTwo() {
		var hailStones = readHailStones();
		var rockPosition = determineRockPosition(hailStones);
		var hailStone = hailStones.getFirst();
		var otherHailStone = hailStones.get(1);
		var velocity = hailStone.velocity().subtract(rockPosition);
		var otherVelocity = otherHailStone.velocity().subtract(rockPosition);
		var intersection = new HailStone(hailStone.position(), velocity)
				.intersectsWith(new HailStone(otherHailStone.position(), otherVelocity));

		assert intersection.isPresent();

		var time = (intersection.get().x() - hailStone.position().x()) / velocity.x();
		var z = hailStone.position().z() + velocity.z() * time;

		return intersection.get().x() + intersection.get().y() + z;
	}

	/**
	 * For rock to hit hailstones with same (X|Y|Z) velocity,
	 * it must satisfy the following:
	 * distance % (rockVelocity - hailstoneVelocity) == 0
	 */
	private Position determineRockPosition(List<HailStone> hailStones) {
		var x = new ArrayList<Integer>();
		var y = new ArrayList<Integer>();
		var z = new ArrayList<Integer>();

		IntStream.range(0, hailStones.size() - 1)
				.forEach(i -> {
					var hailStone = hailStones.get(i);

					IntStream.range(i + 1, hailStones.size())
							.forEach(j -> {
								var otherHailStone = hailStones.get(j);

								determineVelocity(hailStone, otherHailStone, Position::x, addOrRetain(x));
								determineVelocity(hailStone, otherHailStone, Position::y, addOrRetain(y));
								determineVelocity(hailStone, otherHailStone, Position::z, addOrRetain(z));
							});
				});

		assert !x.isEmpty();
		assert !y.isEmpty();
		assert !z.isEmpty();

		return new Position(x.getFirst(), y.getFirst(), z.getFirst());
	}

	private void determineVelocity(HailStone hailStone,
								   HailStone otherHailStone,
								   ToLongFunction<Position> axesValueExtractor,
								   Consumer<Set<Integer>> velocityDetermined) {
		var vAxes1 = axesValueExtractor.applyAsLong(hailStone.velocity());
		var vAxes2 = axesValueExtractor.applyAsLong(otherHailStone.velocity());

		if (vAxes1 == vAxes2) {
			var velocity = new HashSet<Integer>();
			var axes1 = axesValueExtractor.applyAsLong(hailStone.position());
			var axes2 = axesValueExtractor.applyAsLong(otherHailStone.position());
			var difference = axes2 - axes1;

			for (var v = -1000; v <= 1000; v++) {
				if (vAxes1 != v && Math.floorMod(difference, v - vAxes1) == 0) {
					velocity.add(v);
				}
			}

			velocityDetermined.accept(velocity);
		}
	}

	private Consumer<Set<Integer>> addOrRetain(Collection<Integer> previouslyDeterminedVelocity) {
		return velocity -> {
			if (previouslyDeterminedVelocity.isEmpty()) {
				previouslyDeterminedVelocity.addAll(velocity);
			} else {
				previouslyDeterminedVelocity.retainAll(velocity);
			}
		};
	}

	private record HailStone(Position position, Position velocity) {

		private static final Pattern PATTERN = Pattern.compile("^(?<xPosition>\\d+),\\s+(?<yPosition>\\d+),\\s+(?<zPosition>\\d+)\\s+@\\s+(?<xVelocity>-?\\d+),\\s+(?<yVelocity>-?\\d+),\\s+(?<zVelocity>-?\\d+)$");

		public static HailStone parse(String input) {
			var matcher = PATTERN.matcher(input);

			if (!matcher.matches()) {
				throw new IllegalArgumentException("Invalid hailstone input: %s".formatted(input));
			}

			return new HailStone(
					new Position(
							Long.parseLong(matcher.group("xPosition")),
							Long.parseLong(matcher.group("yPosition")),
							Long.parseLong(matcher.group("zPosition"))
					),
					new Position(
							Long.parseLong(matcher.group("xVelocity")),
							Long.parseLong(matcher.group("yVelocity")),
							Long.parseLong(matcher.group("zVelocity"))
					)
			);
		}

		public Optional<Position> intersectsWith(HailStone other) {
			var line = asLine(this);
			var otherLine = asLine(other);

			return line.intersect(otherLine)
					.map(point -> new Position(point.x(), point.y(), 0));
		}

		private Line asLine(HailStone hailStone) {
			var position = hailStone.position();
			var velocity = hailStone.velocity();

			return new Line(
					new Point(position.x(), position.y()),
					new Point(velocity.x(), velocity.y())
			);
		}
	}

	private record Position(long x, long y, long z) {

		public Position subtract(Position other) {
			return new Position(
					x() - other.x(),
					y() - other.y(),
					z() - other.z()
			);
		}

		public boolean withinBounds(long minimumXY, long maximumXY) {
			return x >= minimumXY && x <= maximumXY
					&& y >= minimumXY && y <= maximumXY;
		}
	}

	private record Line(Point point, Point delta) {

		/**
		 * Formulas:
		 * - Line: y = slope * x + b
		 * - Slope: deltaY / deltaX
		 * - b: y - slope * x
		 * If we want the lines to have intersection, then the (x, y) coordinates should be the same at some point.
		 * For the two lines: y1 = slope1 * x1 + b1 and y2 = slope2 * x2 + b2 to have the same y point, we
		 * can set them equal to each other: slope1 * x1 + b1 = slope2 * x2 + b2
		 * And if (x, y) have to be the same for the 2 lines we are left with: slope1 * x + b1 = slope2 * x + b2
		 * Extracting x from the equation we get: x = (b2 - b1) / (slope1 - slope2)
		 * And y can be calculated as: y = slope1 * x + b1
		 */
		public Optional<Point> intersect(Line other) {
			var slope = calculateSlope(delta());
			var otherSlope = calculateSlope(other.delta());
			var b = BigDecimal.valueOf(point().y() - slope * point().x());
			var otherB = BigDecimal.valueOf(other.point().y() - otherSlope * other.point().x());

			if (isParallel(slope, otherSlope)) {
				if (isSameLine(b, otherB)) {
					return Optional.of(point());
				} else {
					return Optional.empty();
				}
			}

			var x = otherB.subtract(b).divide(BigDecimal.valueOf(slope - otherSlope), 3, HALF_UP);
			var y = BigDecimal.valueOf(slope).multiply(x).add(b);
			var intersection = new Point(x.longValue(), y.longValue());

			if (isOppositeDirection(intersection, this)
					|| isOppositeDirection(intersection, other)) {
				return Optional.empty();
			}

			return Optional.of(intersection);
		}

		private double calculateSlope(Point value) {
			return BigDecimal.valueOf(value.y())
					.divide(BigDecimal.valueOf(value.x()), DECIMAL128)
					.doubleValue();
		}

		private boolean isParallel(double slope, double otherSlope) {
			return slope == otherSlope;
		}

		private boolean isSameLine(BigDecimal b, BigDecimal otherB) {
			return b.compareTo(otherB) == 0;
		}

		private boolean isOppositeDirection(Point point, Line line) {
			var pointOnLine = line.point();
			var deltaOfLine = line.delta();

			return point.x() - pointOnLine.x() < 0 != deltaOfLine.x() < 0
					|| point.y() - pointOnLine.y() < 0 != deltaOfLine.y() < 0;
		}
	}

	private record Point(long x, long y) {
	}
}
