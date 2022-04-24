package heuristic.solutionselector;

import heuristic.util.Solution;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.val;

import java.util.Comparator;
import java.util.random.RandomGenerator;

@ToString
@RequiredArgsConstructor
public final class TournamentSelector extends AbstractSolutionSelector {
	@ToString.Exclude
	private RandomGenerator rng = null;
	private final int tournamentSize;
	private final boolean best;

	public TournamentSelector(final int tournamentSize) {
		this(tournamentSize, true);
	}

	@Deprecated
	public TournamentSelector(final Solution[] solutions, final RandomGenerator rng, final int tournamentSize) {
		super(solutions);
		assert tournamentSize >= 1;
		this.rng = rng;
		this.tournamentSize = tournamentSize;
		best = true;
	}

	@Override
	public void init(final RandomGenerator rng, final Solution[] solutions) {
		super.init(rng, solutions);
		this.rng = rng;
	}

	@Override
	public Solution select(final double progress) {
		val candidates = rng.ints(tournamentSize, 0, solutions.length)
		                    .mapToObj(i -> solutions[i]);
		if (best) return candidates.min(Comparator.comparingDouble(Solution::value)).orElseThrow();
		else return candidates.max(Comparator.comparingDouble(Solution::value)).orElseThrow();
	}
}
