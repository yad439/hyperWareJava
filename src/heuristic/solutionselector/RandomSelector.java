package heuristic.solutionselector;

import heuristic.util.Solution;

import java.util.random.RandomGenerator;

public final class RandomSelector extends AbstractSolutionSelector {
	private RandomGenerator rng=null;

	public RandomSelector(){}

	@Deprecated
	public RandomSelector(final Solution[] solutions, final RandomGenerator rng) {
		super(solutions);
		this.rng=rng;
	}

	@Override
	public void init(final RandomGenerator rng, final Solution[] solutions) {
		super.init(rng, solutions);
		this.rng=rng;
	}

	@Override
	public Solution select(final double progress) {
		final var choice=rng.nextInt(solutions.length);
		return solutions[choice];
	}
}
