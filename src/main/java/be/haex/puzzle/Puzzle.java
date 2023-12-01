package be.haex.puzzle;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public interface Puzzle<T> {

	default Stream<String> streamPuzzleInput(String fileName) {
		return readContentOfInputFile(fileName).stream();
	}

	default List<String> readContentOfInputFile(String fileName) {
		try {
			return Files.readAllLines(Paths.get(this.getClass().getClassLoader().getResource(fileName).toURI()));
		} catch (Exception e) {
			throw new IllegalArgumentException("Cannot read input file for this puzzle!");
		}
	}

	T solvePartOne();

	T solvePartTwo();
}
