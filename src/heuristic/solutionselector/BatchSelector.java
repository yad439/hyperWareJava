package heuristic.solutionselector;

import heuristic.util.Solution;
import util.StatisticPrinter;

import java.util.List;
import java.util.random.RandomGenerator;

public interface BatchSelector extends StatisticPrinter {
	void init(RandomGenerator rng);
//	default void init(final RandomGenerator rng){init(rng,null);}

//	Pair selectPair(double progress);
	Pair selectPair(List<? extends Solution> population, double progress);

//	Iterable<Pair> selectMultiple(double progress,int number);
	Iterable<Pair> selectMultiple(List<? extends Solution> population, int number, double progress);

	record Pair(Solution first, Solution second){}
}
