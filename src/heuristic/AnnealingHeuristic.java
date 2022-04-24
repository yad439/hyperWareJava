package heuristic;

import AbstractClasses.ProblemDomain;
import extension.ExtendedHyperHeuristic;

import java.util.Arrays;
import java.util.stream.IntStream;

@Deprecated
public final class AnnealingHeuristic extends ExtendedHyperHeuristic {
	private static final int BEST_SOLUTION = 0;
	private static final int CURRENT_SOLUTION = 1;
	private static final int NEW_SOLUTION = 2;

	public AnnealingHeuristic(final int seed) {
		super(seed);
	}

	@SuppressWarnings("OverlyLongMethod")
	@Override
	protected void solve(@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter") final ProblemDomain problemDomain) {
		final var localSearches = problemDomain.getHeuristicsOfType(ProblemDomain.HeuristicType.LOCAL_SEARCH);
		final var mutations = problemDomain.getHeuristicsOfType(ProblemDomain.HeuristicType.MUTATION);
		final var rrs = problemDomain.getHeuristicsOfType(ProblemDomain.HeuristicType.RUIN_RECREATE);
		final var allMutations = IntStream.concat(Arrays.stream(mutations), Arrays.stream(rrs)).toArray();
		problemDomain.setMemorySize(3);
		problemDomain.initialiseSolution(CURRENT_SOLUTION);
		problemDomain.setDepthOfSearch(0.999);
		problemDomain.setIntensityOfMutation(0.5);
		final var initValue = problemDomain.getFunctionValue(CURRENT_SOLUTION);
		final var mutated = Arrays.stream(mutations).mapToDouble(i -> problemDomain.applyHeuristic(i, CURRENT_SOLUTION, CURRENT_SOLUTION)).toArray();
		final var minVal = Math.min(initValue, Arrays.stream(mutated).min().orElseThrow());
		final var maxVal = Math.max(initValue, Arrays.stream(mutated).max().orElseThrow());
		var temperature = maxVal - minVal;
		problemDomain.initialiseSolution(BEST_SOLUTION);
		var bestValue = problemDomain.getFunctionValue(BEST_SOLUTION);
		var currentValue = bestValue;
		problemDomain.compareSolutions(BEST_SOLUTION, CURRENT_SOLUTION);
		var currentPhase = 0.0;
		while (!hasTimeExpired()) {
			final var mutation = rng.nextInt(allMutations.length);
			final var search = rng.nextInt(localSearches.length);
			problemDomain.applyHeuristic(mutation, CURRENT_SOLUTION, NEW_SOLUTION);
			final var newValue = problemDomain.applyHeuristic(search, NEW_SOLUTION, NEW_SOLUTION);
			if (rng.nextDouble() < Math.exp((currentValue - newValue) / temperature)) {
				problemDomain.copySolution(NEW_SOLUTION, CURRENT_SOLUTION);
				currentValue = newValue;
			}
			if (newValue < bestValue) {
				problemDomain.copySolution(NEW_SOLUTION, BEST_SOLUTION);
				bestValue = newValue;
			}
			final var progress = getProgress();
			while (currentPhase < progress) {
				temperature *= 0.95;
				currentPhase += 1.0 / 50.0;
			}
		}
	}

	@Override
	public String toString() {
		return "Annealing-like hyper-heuristic";
	}
}
