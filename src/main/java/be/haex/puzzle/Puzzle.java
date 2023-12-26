package be.haex.puzzle;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public interface Puzzle<T> {

	default Stream<String> streamPuzzleInput(String fileName) {
		return readContentOfInputFile(fileName).stream();
	}

	default List<String> readContentOfInputFile(String fileName) {
		try (var resource = getClass().getClassLoader().getResourceAsStream(fileName);
			 var resourceReader = new InputStreamReader(Objects.requireNonNull(resource));
			 var bufferedReader = new BufferedReader(resourceReader)) {
			var lines = new ArrayList<String>();

			for (String line; (line = bufferedReader.readLine()) != null; ) {
				lines.add(line);
			}

			return List.copyOf(lines);
		} catch (Exception e) {
			throw new IllegalArgumentException("Cannot read input file for this puzzle!");
		}
	}

	T solvePartOne();

	T solvePartTwo();
}
