package heuristic.solutionselector;

import heuristic.util.Solution;

import java.util.List;
import java.util.random.RandomGenerator;

abstract class AbstractSolutionSelector implements SolutionSelector {
	protected Solution[] solutions=null;

	AbstractSolutionSelector(){}

	@Deprecated
	AbstractSolutionSelector(final Solution[] solutions){
		this.solutions=solutions;
	}

	@Override
	@Deprecated
	public Solution select(final double progress, final List<? extends Solution> population) {
		solutions= population.toArray(Solution[]::new);
		return select(progress);
	}

	@Override
	public void init(final RandomGenerator rng, final Solution[] solutions) {
		this.solutions=solutions;
	}
}
