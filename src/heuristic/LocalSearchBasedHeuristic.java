package heuristic;

import AbstractClasses.ProblemDomain;
import extension.ExtendedHyperHeuristic;
import heuristic.acceptor.AillaAcceptor;
import heuristic.heuristicchooser.LrpChooser;
import heuristic.heuristicchooser.MeanImproveChooser;

import java.util.Arrays;
import java.util.stream.IntStream;

public final class LocalSearchBasedHeuristic extends ExtendedHyperHeuristic {
	private static final int CURRENT_SOLUTION = 0;
	private static final int NEW_SOLUTION = 1;

	public LocalSearchBasedHeuristic(final long seed) {
		super(seed);
	}

	@SuppressWarnings("OverlyLongMethod")
	@Override
	protected void solve(@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter") final ProblemDomain domain) {
		final var localSearches = domain.getHeuristicsOfType(ProblemDomain.HeuristicType.LOCAL_SEARCH);
		final var mutations = domain.getHeuristicsOfType(ProblemDomain.HeuristicType.MUTATION);
		final var rrs = domain.getHeuristicsOfType(ProblemDomain.HeuristicType.RUIN_RECREATE);
		final var allMutations = IntStream.concat(Arrays.stream(mutations), Arrays.stream(rrs)).toArray();

		domain.setMemorySize(2);
		domain.initialiseSolution(CURRENT_SOLUTION);
		domain.setDepthOfSearch(1.0);
		domain.setIntensityOfMutation(0.5);

//		final var localChooser = new ImproveToTimeChooserExt(rng, localSearches.length);
		final var localChooser = new LrpChooser();
		localChooser.init(rng, localSearches.length);
		final var mutationChoosers = IntStream.range(0, localSearches.length)
		                                      .mapToObj(i -> new MeanImproveChooser(rng, allMutations.length))
		                                      .toList();
		final var times = domain.getheuristicCallTimeRecord();

		final var initValue = domain.getFunctionValue(CURRENT_SOLUTION);
//		final var mutated = Arrays.stream(mutations).mapToDouble(i -> domain.applyHeuristic(i, CURRENT_SOLUTION, CURRENT_SOLUTION)).toArray();
//		final var minVal = Math.min(initValue, Arrays.stream(mutated).min().orElseThrow());
//		final var maxVal = Math.max(initValue, Arrays.stream(mutated).max().orElseThrow());
//		final var acceptor=new AnnealingAcceptor(rng,maxVal - minVal,0.95,50);
		final var acceptor = new AillaAcceptor();
		acceptor.restart(domain.getFunctionValue(CURRENT_SOLUTION));

		while (!hasTimeExpired()) {
			final var progress = getProgress();
			final var localHeuristic = localChooser.choose(progress);
			final var mutation = mutationChoosers.get(localHeuristic).choose(progress);
			final var domainMutation = allMutations[mutation];
			final var localSearch = localSearches[localHeuristic];
			final var oldScore = domain.getFunctionValue(CURRENT_SOLUTION);
			final var mutTime = times[domainMutation];
			final var locTime = times[localSearch];
			domain.applyHeuristic(domainMutation, CURRENT_SOLUTION, NEW_SOLUTION);
			final var newScore = domain.applyHeuristic(localSearch, NEW_SOLUTION, NEW_SOLUTION);
			final var isSame = domain.compareSolutions(CURRENT_SOLUTION, NEW_SOLUTION);
			localChooser.update(localHeuristic, oldScore, newScore, isSame, times[localSearch] - locTime);
			mutationChoosers.get(localHeuristic).update(mutation, oldScore, newScore, isSame, times[domainMutation] - mutTime);
//			System.out.printf("%d: %f\n",localHeuristic,oldScore-newScore);
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
		return "Local search based heuristic";
	}
}
