package heuristic;

import AbstractClasses.ProblemDomain;
import extension.ExtendedHyperHeuristic;
import heuristic.acceptor.ContinuousAnnealingAcceptor;
import heuristic.heuristicchooser.RandomChooser;

import java.util.Arrays;

@Deprecated
public final class MutationOnlyAnnealing extends ExtendedHyperHeuristic {
	private static final int CURRENT_SOLUTION = 0;
	private static final int NEW_SOLUTION = 1;

	public MutationOnlyAnnealing(final long seed) {
		super(seed);
	}

	@Override
	protected void solve(@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter") final ProblemDomain domain) {
		final var mutations = domain.getHeuristicsOfType(ProblemDomain.HeuristicType.MUTATION);

		domain.setMemorySize(2);
		domain.initialiseSolution(CURRENT_SOLUTION);
		domain.setIntensityOfMutation(0.1);

		final var initValue = domain.getFunctionValue(CURRENT_SOLUTION);
		final var mutated = Arrays.stream(mutations).mapToDouble(i -> domain.applyHeuristic(i, CURRENT_SOLUTION, CURRENT_SOLUTION)).toArray();
		final var minVal = Math.min(initValue, Arrays.stream(mutated).min().orElseThrow());
		final var maxVal = Math.max(initValue, Arrays.stream(mutated).max().orElseThrow());
		final var acceptor = new ContinuousAnnealingAcceptor(rng, (maxVal - minVal) / 2, 0.95, 50);
		acceptor.restart(domain.getFunctionValue(CURRENT_SOLUTION));

		final var times = domain.getheuristicCallTimeRecord();
		final var chooser = new RandomChooser(rng, mutations.length);
		while (!hasTimeExpired()) {
			final var heuristic = chooser.choose(getProgress());
			final var oldScore = domain.getFunctionValue(CURRENT_SOLUTION);
			final var startTime = times[mutations[heuristic]];
			final var newScore = domain.applyHeuristic(mutations[heuristic], CURRENT_SOLUTION, NEW_SOLUTION);
			final var isSame = domain.compareSolutions(CURRENT_SOLUTION, NEW_SOLUTION);
			chooser.update(heuristic, oldScore, newScore, isSame, times[mutations[heuristic]] - startTime);
			if (acceptor.shouldAccept(newScore,oldScore , isSame, getProgress()))
				domain.copySolution(NEW_SOLUTION, CURRENT_SOLUTION);
			if (acceptor.isRestartNeeded()) {
				domain.initialiseSolution(CURRENT_SOLUTION);
				acceptor.restart(domain.getFunctionValue(CURRENT_SOLUTION));
			}
		}
	}

	@Override
	public String toString() {
		return "Annealing based heuristic";
	}
}
