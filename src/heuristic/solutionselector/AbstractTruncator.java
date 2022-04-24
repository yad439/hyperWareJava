package heuristic.solutionselector;

import heuristic.util.Solution;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.val;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

@ToString
@RequiredArgsConstructor
abstract class AbstractTruncator implements Truncator {
	private final boolean usePrevious;
	private final boolean alwaysUseBest;

	@Override
	public Iterable<Solution> truncate(final List<? extends Solution> previousPopulation,
	                                   final List<? extends Solution> solutionList, final int bound,
	                                   final double progress) {
		if (usePrevious) {
			val wholePop = new ArrayList<Solution>(previousPopulation.size() + solutionList.size());
			wholePop.addAll(solutionList);
			wholePop.addAll(previousPopulation);
			if (alwaysUseBest) {
				val best = previousPopulation.stream().min(Comparator.comparingDouble(Solution::score)).orElseThrow();
				val res = truncate(wholePop, bound - 1, progress);
				return () -> new PrependIterator(best, res.iterator());
			} else return truncate(wholePop, bound, progress);
		} else {
			if (alwaysUseBest) {
				val best = previousPopulation.stream().min(Comparator.comparingDouble(Solution::score)).orElseThrow();
				val res = truncate(solutionList, bound - 1, progress);
				return () -> new PrependIterator(best, res.iterator());
			} else return truncate(solutionList, bound, progress);
		}
	}

	protected abstract Iterable<Solution> truncate(final List<? extends Solution> solutionList, final int bound,
	                                               final double progress);

	@RequiredArgsConstructor
	@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
	private static final class PrependIterator implements Iterator<Solution> {
		Solution first;
		Iterator<Solution> next;
		@NonFinal boolean isFirst = true;

		@Override
		public boolean hasNext() {
			return isFirst || next.hasNext();
		}

		@Override
		public Solution next() {
			if (isFirst) {
				isFirst = false;
				return first;
			}
			return next.next();
		}
	}
}
