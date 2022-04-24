package heuristic.replaceselector;

import heuristic.util.Solution;

import java.util.random.RandomGenerator;

abstract class ReplacerAdaptor implements SolutionReplaceSelector {
	protected Solution[] candidates=null;

	ReplacerAdaptor(){}

	@Deprecated
	ReplacerAdaptor(final Solution[] candidates){
		this.candidates=candidates;
	}

	@Override
	public void init(final RandomGenerator rng, final Solution[] candidates) {
		this.candidates=candidates;
	}
}
