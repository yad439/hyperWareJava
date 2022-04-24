package heuristic.util;

import heuristic.solutionselector.SolutionSelector;
import lombok.experimental.Delegate;

public final class CrossoverToHeuristicAdapter implements Heuristic{
	@Delegate
	private final CrossoverHeuristic heuristic;
	private final SolutionSelector selector;

	public CrossoverToHeuristicAdapter(final CrossoverHeuristic heuristic, final SolutionSelector selector) {
		this.heuristic = heuristic;
		this.selector = selector;
	}

	@Override
	public double apply(final Solution source, final Solution destination) {
		return heuristic.apply(source,selector.select(0),destination);
	}

	@Override
	public Heuristic copy() {
		return new CrossoverToHeuristicAdapter(heuristic, selector);
	}
}
