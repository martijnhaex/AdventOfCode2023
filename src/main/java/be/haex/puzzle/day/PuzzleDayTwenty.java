package be.haex.puzzle.day;

import be.haex.puzzle.Puzzle;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public class PuzzleDayTwenty implements Puzzle<Long> {

	private static final String INPUT_FILE_NAME = "puzzleDayTwenty.txt";

	private final String fileName;

	PuzzleDayTwenty(String fileName) {
		this.fileName = fileName;
	}

	public PuzzleDayTwenty() {
		this(INPUT_FILE_NAME);
	}

	@Override
	public Long solvePartOne() {
		return readCommunicationSystem()
				.pulsesReceivedAfterPressingButton(1_000);
	}

	@Override
	public Long solvePartTwo() {
		return readCommunicationSystem()
				.minimumNumberOfButtonPressesToReceivePulseOfTypeInModule(PulseType.LOW, "rx");
	}

	private CommunicationSystem readCommunicationSystem() {
		return CommunicationSystem.parse(readContentOfInputFile(fileName));
	}

	private record CommunicationSystem(Map<String, Module> modules) {

		public static CommunicationSystem parse(List<String> input) {
			var modules = input.stream()
					.map(Module::parse)
					.collect(toMap(Module::name, identity()));

			initialize(modules);

			return new CommunicationSystem(modules);
		}

		private static void initialize(Map<String, Module> uninitializedModules) {
			var conjunctionModules = uninitializedModules.entrySet()
					.stream()
					.filter(entry -> entry.getValue() instanceof Module.Conjunction)
					.map(entry -> Map.entry(entry.getKey(), (Module.Conjunction) entry.getValue()))
					.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

			uninitializedModules.values().forEach(module -> module.destinations().forEach(destination -> {
				if (conjunctionModules.containsKey(destination)) {
					var conjunctionModule = conjunctionModules.get(destination);

					conjunctionModule.receivedPulses.put(module.name(), PulseType.LOW);
				}
			}));
		}

		public long pulsesReceivedAfterPressingButton(int times) {
			var sentPulses = new EnumMap<PulseType, Integer>(PulseType.class);
			sentPulses.put(PulseType.HIGH, 0);
			sentPulses.put(PulseType.LOW, 0);

			var broadcastModule = broadcastModule();
			var pulses = new LinkedList<Pulse>();
			IntStream.rangeClosed(1, times)
					.forEach(ignored -> {
						pulses.addAll(broadcastModule.receive(PulseType.LOW, broadcastModule.name()));

						sentPulses.computeIfPresent(PulseType.LOW, increment());

						while (!pulses.isEmpty()) {
							var pulse = pulses.poll();

							sentPulses.computeIfPresent(pulse.type(), increment());

							var module = modules.get(pulse.destinationModule());

							if (module != null) {
								pulses.addAll(module.receive(pulse.type(), pulse.senderModule()));
							}
						}
					});

			return calculateProduct(sentPulses);
		}

		private Module broadcastModule() {
			return modules.values()
					.stream()
					.filter(Module.Broadcast.class::isInstance)
					.findFirst()
					.orElseThrow();
		}

		private BiFunction<PulseType, Integer, Integer> increment() {
			return (ignore, value) -> value + 1;
		}

		private long calculateProduct(Map<PulseType, Integer> pulses) {
			return pulses.values()
					.stream()
					.mapToLong(Integer::longValue)
					.reduce(1, (a, b) -> a * b);
		}

		private long minimumNumberOfButtonPressesToReceivePulseOfTypeInModule(PulseType pulseType, String moduleName) {
			return determineCycleForReceivingPulseOfTypeInModule(pulseType, moduleName)
					.stream()
					.mapToLong(Integer::longValue)
					.reduce(1, (x, y) -> x * (y / greatestCommonDivisor(x, y)));
		}

		private Collection<Integer> determineCycleForReceivingPulseOfTypeInModule(PulseType pulseType, String moduleName) {
			var modulesForCycleDetection = findModulesForDeterminingACycleSendingTo(List.of(moduleName));

			assert modulesForCycleDetection.size() == 4;
			assert modulesForCycleDetection.stream().allMatch(Module.Conjunction.class::isInstance);

			var buttonPresses = 0;
			var broadcastModule = broadcastModule();
			var pulses = new LinkedList<Pulse>();
			var highPulsesSentAt = new HashMap<String, Integer>();
			while (true) {
				buttonPresses++;

				pulses.addAll(broadcastModule.receive(PulseType.LOW, broadcastModule.name()));

				while (!pulses.isEmpty()) {
					var pulse = pulses.poll();

					var module = modules.get(pulse.destinationModule());

					if (module != null) {
						var sendOut = module.receive(pulse.type(), pulse.senderModule());

						if (pulse.type() == pulseType && modulesForCycleDetection.stream().anyMatch(moduleForCycleDetection -> moduleForCycleDetection.name().equals(module.name()))) {
							highPulsesSentAt.putIfAbsent(module.name(), buttonPresses);
						}

						if (highPulsesSentAt.size() == modulesForCycleDetection.size()) {
							return highPulsesSentAt.values();
						}

						pulses.addAll(sendOut);
					}
				}
			}
		}

		private List<Module> findModulesForDeterminingACycleSendingTo(List<String> modulesNames) {
			var modules = findModulesSendingTo(modulesNames);

			if (modules.size() > 1) {
				return modules;
			} else {
				return findModulesForDeterminingACycleSendingTo(modules.stream().map(Module::name).toList());
			}
		}

		private List<Module> findModulesSendingTo(List<String> modulesNames) {
			return modules().values().stream()
					.filter(module -> module.destinations().stream().anyMatch(modulesNames::contains))
					.toList();
		}

		private long greatestCommonDivisor(long x, long y) {
			return y == 0 ? x : greatestCommonDivisor(y, x % y);
		}
	}

	private sealed interface Module permits Module.Broadcast, Module.Conjunction, Module.FlipFlop {

		static Module parse(String input) {
			return Module.Conjunction.parse(input)
					.or(() -> Module.FlipFlop.parse(input))
					.or(() -> Module.Broadcast.parse(input))
					.orElseThrow(() -> new IllegalArgumentException("No module found for " + input));
		}

		String name();

		List<String> destinations();

		List<Pulse> receive(PulseType pulseType, String inputModule);

		record Broadcast(String name, List<String> destinations) implements Module {

			private static final Pattern PATTERN = Pattern.compile("^(?<name>broadcaster)\\s->\\s(?<destinations>[a-z\\s,]+)$");

			public static Optional<Module> parse(String input) {
				var matcher = PATTERN.matcher(input);

				if (!matcher.matches()) {
					return Optional.empty();
				} else {
					return Optional.of(new Broadcast(
							matcher.group("name"),
							List.of(matcher.group("destinations").split(", "))
					));
				}
			}

			@Override
			public List<Pulse> receive(PulseType pulseType, String inputModule) {
				return destinations.stream()
						.map(destination -> new Pulse(name, destination, pulseType))
						.toList();
			}
		}

		record Conjunction(String name, List<String> destinations,
						   Map<String, PulseType> receivedPulses) implements Module {

			private static final Pattern PATTERN = Pattern.compile("^&(?<name>[a-z]+)\\s->\\s(?<destinations>[a-z\\s,]+)$");

			public static Optional<Module> parse(String input) {
				var matcher = PATTERN.matcher(input);

				if (!matcher.matches()) {
					return Optional.empty();
				} else {
					return Optional.of(new Conjunction(
							matcher.group("name"),
							List.of(matcher.group("destinations").split(", ")),
							new HashMap<>()
					));
				}
			}

			@Override
			public List<Pulse> receive(PulseType pulseType, String inputModule) {
				receivedPulses.put(inputModule, pulseType);

				var pulseTypeToSendOut = allHighPulses() ? PulseType.LOW : PulseType.HIGH;

				return destinations.stream()
						.map(destination -> new Pulse(name, destination, pulseTypeToSendOut))
						.toList();
			}

			private boolean allHighPulses() {
				return receivedPulses.values()
						.stream()
						.allMatch(PulseType.HIGH::equals);
			}
		}

		record FlipFlop(String name, List<String> destinations, Queue<State> state) implements Module {

			private static final Pattern PATTERN = Pattern.compile("^%(?<name>[a-z]+)\\s->\\s(?<destinations>[a-z\\s,]+)$");

			public static Optional<Module> parse(String input) {
				var matcher = PATTERN.matcher(input);

				if (!matcher.matches()) {
					return Optional.empty();
				} else {
					return Optional.of(new FlipFlop(
							matcher.group("name"),
							List.of(matcher.group("destinations").split(", ")),
							new LinkedList<>(List.of(State.OFF))
					));
				}
			}

			@Override
			public List<Pulse> receive(PulseType pulseType, String inputModule) {
				return switch (pulseType) {
					case HIGH -> emptyList();
					case LOW -> sendOutPulses();
				};
			}

			private List<Pulse> sendOutPulses() {
				var pulseTypeToSendOut = flipState().pulseType();

				return destinations.stream()
						.map(destination -> new Pulse(name, destination, pulseTypeToSendOut))
						.toList();
			}

			private State flipState() {
				var previousState = state.poll();

				state.add(requireNonNull(previousState).flip());

				return previousState;
			}

			private enum State {
				ON(PulseType.LOW),
				OFF(PulseType.HIGH);

				private final PulseType pulseType;

				State(PulseType pulseType) {
					this.pulseType = pulseType;
				}

				public PulseType pulseType() {
					return pulseType;
				}

				public State flip() {
					return switch (this) {
						case ON -> OFF;
						case OFF -> ON;
					};
				}
			}
		}
	}

	private record Pulse(String senderModule, String destinationModule, PulseType type) {
	}

	private enum PulseType {
		HIGH,
		LOW
	}
}
