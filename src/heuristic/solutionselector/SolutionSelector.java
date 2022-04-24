package heuristic.solutionselector;

import heuristic.util.Solution;
import util.NestedWriter;
import util.StatisticPrinter;

import java.util.List;
import java.util.random.RandomGenerator;

public interface SolutionSelector extends StatisticPrinter {

	void init(RandomGenerator rng, Solution[] solutions);
	default void init(final RandomGenerator rng){init(rng,null);}
	Solution select(double progress);

	Solution select(double progress, List<? extends Solution> population);

	@Override
	default void printStats(final NestedWriter output) {
		output.println(toString());
	}
}
