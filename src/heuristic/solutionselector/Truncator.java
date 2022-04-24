package heuristic.solutionselector;

import heuristic.util.Solution;

import java.util.List;
import java.util.random.RandomGenerator;

public interface Truncator {

	void init(RandomGenerator rng);

	Iterable<Solution> truncate(List<? extends Solution> previousPopulation,List<? extends Solution> solutionList, int bound, double progress);
}
