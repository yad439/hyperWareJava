package heuristic.solutionselector;

import heuristic.util.Solution;
import lombok.ToString;
import lombok.val;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.random.RandomGenerator;

@ToString(callSuper = true)
public final class BestTruncator extends AbstractTruncator {

	public BestTruncator(final boolean usePrevious, final boolean alwaysUseBest) {super(usePrevious, alwaysUseBest);}

	@Override
	public void init(final RandomGenerator rng) {}

	@Override
	public Iterable<Solution> truncate(final List<? extends Solution> solutionList, final int bound, final double progress) {
		val array=new ArrayList<Solution>(solutionList);
		array.sort(Comparator.comparingDouble(Solution::score));
		return array.subList(0,bound);
	}
}
