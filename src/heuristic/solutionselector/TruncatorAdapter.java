package heuristic.solutionselector;

import heuristic.util.Solution;
import lombok.ToString;
import lombok.val;

import java.util.List;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

@ToString(callSuper = true)
public final class TruncatorAdapter extends AbstractTruncator {
	private final SolutionSelector selector;

	TruncatorAdapter(final SolutionSelector selector, final boolean usePrevious, final boolean alwaysUseBest) {
		super(usePrevious, alwaysUseBest);
		this.selector = selector;
	}

	@Override
	public void init(final RandomGenerator rng) {
		selector.init(rng,null);
	}

	@Override
	public Iterable<Solution> truncate(final List<? extends Solution> solutionList, final int bound, final double progress) {
		val stream= IntStream.range(0,bound).mapToObj(i->selector.select(progress,solutionList));
		return stream::iterator;
	}
}
