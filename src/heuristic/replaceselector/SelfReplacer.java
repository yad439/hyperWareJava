package heuristic.replaceselector;

import heuristic.util.Solution;

import java.util.random.RandomGenerator;

public final class SelfReplacer implements SolutionReplaceSelector {

	@Override
	public void init(final RandomGenerator rng, final Solution[] candidates) {}

	@Override
	public Solution select(final double progress, final Solution generatedFrom) {
		return generatedFrom;
	}

	@Override
	public Solution select(final double progress, final Solution parent1, final Solution parent2) {
		if (parent1.value() < parent2.value()) return parent2;
		return parent1;
	}
}
