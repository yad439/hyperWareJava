package heuristic.replaceselector;

import heuristic.util.Solution;
import util.NestedWriter;
import util.StatisticPrinter;

import java.util.random.RandomGenerator;

public interface SolutionReplaceSelector extends StatisticPrinter {

	void init(RandomGenerator rng, Solution[] candidates);
	Solution select(double progress, Solution generatedFrom);
	Solution select(double progress, Solution parent1, Solution parent2);

	@Override
	default void printStats(final NestedWriter output) {
		output.println(toString());
	}
}
