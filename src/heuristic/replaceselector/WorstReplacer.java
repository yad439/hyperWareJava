package heuristic.replaceselector;

import heuristic.util.Solution;

import java.util.Arrays;
import java.util.Comparator;

public final class WorstReplacer extends ReplacerAdaptor {

	public WorstReplacer(){}

	@Deprecated
	public WorstReplacer(final Solution[] candidates) {super(candidates);}

	@Override
	public Solution select(final double progress, final Solution generatedFrom) {return select();}

	@Override
	public Solution select(final double progress, final Solution parent1, final Solution parent2) {return select();}

	private Solution select() {
		return Arrays.stream(candidates)
		             .max(Comparator.comparingDouble(Solution::value))
		             .orElseThrow();
	}
}
