package heuristic.solutionselector;

import heuristic.util.Solution;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Arrays;
import java.util.Comparator;

@ToString
@RequiredArgsConstructor
public final class ExtremaSelector extends AbstractSolutionSelector{
	private final boolean best;

	@Override
	public Solution select(final double progress) {
		if(best)return Arrays.stream(solutions).min(Comparator.comparingDouble(Solution::value)).orElseThrow();
		else return Arrays.stream(solutions).max(Comparator.comparingDouble(Solution::value)).orElseThrow();
	}
}
