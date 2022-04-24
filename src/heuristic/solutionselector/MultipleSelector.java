package heuristic.solutionselector;

import heuristic.util.Solution;
import lombok.ToString;

import java.util.Arrays;
import java.util.List;
import java.util.random.RandomGenerator;

@ToString
public final class MultipleSelector implements SolutionSelector {
	private final SolutionSelector[] selectors;
	private final double[] stages;

	@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
	public MultipleSelector(final SolutionSelector[] selectors, final double[] stages) {
		this.selectors = selectors;
		this.stages = stages;
	}

	@Override
	public void init(final RandomGenerator rng, final Solution[] solutions) {
		for (final var selector : selectors) selector.init(rng, solutions);
	}

	@Override
	public Solution select(final double progress) {
		final var index = Arrays.binarySearch(stages, progress);
		final var actualIndex = index >= 0 ? index : -index - 1;
		return selectors[actualIndex].select(progress);
	}

	@Override
	public Solution select(final double progress, final List<? extends Solution> population) {
		final var index = Arrays.binarySearch(stages, progress);
		final var actualIndex = index >= 0 ? index : -index - 1;
		return selectors[actualIndex].select(progress,population);
	}
}
