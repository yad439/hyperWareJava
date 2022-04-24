package heuristic.solutionselector;

import heuristic.util.Solution;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.val;

import java.util.Arrays;
import java.util.Comparator;
import java.util.random.RandomGenerator;

@ToString
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class PartRandomSelector extends AbstractSolutionSelector {
	double ratio;
	int count;
	boolean absolute;
	boolean best;
	@ToString.Exclude
	@NonFinal
	RandomGenerator rng = null;

	@Override
	public Solution select(final double progress) {
		val finalCount = absolute ? count : Math.toIntExact(Math.round(solutions.length * ratio));
		val solutionsCopy = solutions.clone();
		val comparator = Comparator.comparingDouble(Solution::value);
		Arrays.sort(solutionsCopy, best ? comparator : comparator.reversed());
		return solutionsCopy[rng.nextInt(finalCount)];
	}

	@Override
	@SuppressWarnings("ParameterHidesMemberVariable")
	public void init(final RandomGenerator rng, final Solution[] solutions) {
		super.init(rng, solutions);
		this.rng = rng;
	}
}
