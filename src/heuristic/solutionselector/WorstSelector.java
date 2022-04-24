package heuristic.solutionselector;

import heuristic.util.Solution;

import java.util.Arrays;
import java.util.Comparator;

@Deprecated
public final class WorstSelector extends AbstractSolutionSelector {

	@Override
	public Solution select(final double progress) {
		return Arrays.stream(solutions).max(Comparator.comparingDouble(Solution::value)).orElseThrow();
	}
}
