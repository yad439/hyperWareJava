package heuristic.solutionselector;

import heuristic.util.Solution;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.val;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.random.RandomGenerator;

@ToString(callSuper = true)
public final class NonRepetitiveTruncator extends AbstractTruncator {
	private final SolutionSelector selector;

	NonRepetitiveTruncator(final SolutionSelector selector, final boolean usePrevious, final boolean alwaysUseBest) {
		super(usePrevious, alwaysUseBest);
		this.selector = selector;
	}

	@Override
	public void init(final RandomGenerator rng) {
		selector.init(rng,null);
	}

	@Override
	public Iterable<Solution> truncate(final List<? extends Solution> solutionList, final int bound, final double progress) {
		return ()->new ExcludingIterator(new LinkedList<>(solutionList),progress,bound);
	}

	@AllArgsConstructor
	@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
	private final class ExcludingIterator implements Iterator<Solution>{
		List<Solution> options;
		double progress;
		@NonFinal int count;
		@Override
		public boolean hasNext() {
			return count!=0;
		}

		@Override
		public Solution next() {
			if(count==0 || options.isEmpty())throw new NoSuchElementException("No more elements");
			val result=selector.select(progress,options);
			options.remove(result);
			count--;
			return result;
		}
	}
}
