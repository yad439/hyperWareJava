package heuristic;

import AbstractClasses.ProblemDomain;
import extension.ExtendedHyperHeuristic;
import heuristic.acceptor.AillaAcceptor;
import heuristic.heuristicchooser.MeanImproveChooserExt;
import heuristic.heuristicchooser.RankBasedChooser;
import heuristic.solutionselector.RandomSelector;
import heuristic.util.HeuristicImpl;
import heuristic.util.Solution;
import heuristic.util.SolutionAllocator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.IntStream;

public final class PopulationExhaustiveHeuristic extends ExtendedHyperHeuristic {

	public PopulationExhaustiveHeuristic(final long seed) {super(seed);}

	@SuppressWarnings("OverlyLongMethod")
	@Override
	protected void solve(final ProblemDomain domain) {
		final var localSearches = Arrays.stream(domain.getHeuristicsOfType(ProblemDomain.HeuristicType.LOCAL_SEARCH)).mapToObj(i->new HeuristicImpl(domain,i)).toArray(HeuristicImpl[]::new);
		final var mutations = domain.getHeuristicsOfType(ProblemDomain.HeuristicType.MUTATION);
		final var rrs = domain.getHeuristicsOfType(ProblemDomain.HeuristicType.RUIN_RECREATE);
		final var allMutations = IntStream.concat(Arrays.stream(mutations), Arrays.stream(rrs)).mapToObj(i->new HeuristicImpl(domain,i)).toArray(HeuristicImpl[]::new);

		final var allocator=new SolutionAllocator(domain);
		final var newSolution=allocator.allocate();
		final var newSolution2=allocator.allocate();
		final var population=allocator.allocate(localSearches.length);
		allocator.commit();
//		domain.setMemorySize(localSearches.length + 2);
		domain.setDepthOfSearch(0.2);
		domain.setIntensityOfMutation(0.5);

		final var mutationChooser=new MeanImproveChooserExt(rng, allMutations.length);
//		final var mutationChooser = new MabDeterministicChooser(allMutations.length);
		final var times = domain.getheuristicCallTimeRecord();

//		final var mutated = Arrays.stream(mutations).mapToDouble(i -> domain.applyHeuristic(i, CURRENT_SOLUTION, CURRENT_SOLUTION)).toArray();
//		final var minVal = Math.min(initValue, Arrays.stream(mutated).min().orElseThrow());
//		final var maxVal = Math.max(initValue, Arrays.stream(mutated).max().orElseThrow());
//		final var acceptor=new AnnealingAcceptor(rng,maxVal - minVal,0.95,50);
		final var acceptors = IntStream.range(0, localSearches.length)
		                               .mapToObj(i -> new AillaAcceptor())
		                               .toList();
//		domain.initialiseSolution(NEW_SOLUTION);
//		final var startVal = domain.getFunctionValue(NEW_SOLUTION);

		for (var i = 0; i < localSearches.length; i++) {
			domain.initialiseSolution(i + 2);
			acceptors.get(i).restart(domain.getFunctionValue(i + 2));
//			acceptors.get(i).init(startVal);
//			final var newVal = domain.applyHeuristic(localSearches[i], NEW_SOLUTION, i + 1);
//			final var isSame = domain.compareSolutions(NEW_SOLUTION, i + 1);
//			localChooser.update(i, startVal, newVal, isSame, times[localSearches[i]]);
		}

		final var solutionChooser = new RandomSelector(population,rng);
		final var solutionIndices=new HashMap<Solution,Integer>(population.length);
		for(var i=0;i< population.length;i++)solutionIndices.put(population[i],i);

		while (!hasTimeExpired()) {
			final var progress = getProgress();
			final var solution = solutionChooser.select(progress);
			final var mutation = mutationChooser.choose(progress);
			final var domainMutation = allMutations[mutation];
			final var oldScore = solution.value();
			domainMutation.apply( solution, newSolution);

			final var localChooser = new RankBasedChooser(rng, localSearches.length);
			var prevScore = oldScore;
			var globalSame = true;
			while (!hasTimeExpired()) {
				final var localHeuristic = localChooser.choose(getProgress());
				if (localHeuristic == -1) break;
				final var localSearch = localSearches[localHeuristic];
				final var newScore = localSearch.apply( newSolution, newSolution2);
				final var isSame = newSolution.isSameAs(newSolution);
				globalSame &= isSame;
				localChooser.update(localHeuristic, prevScore, newScore, isSame, localSearch.getLastRunningTime());
				newSolution2.copyTo(newSolution);
				prevScore = newScore;
			}
			mutationChooser.update(mutation, oldScore, prevScore, globalSame, domainMutation.getLastRunningTime());
//			System.out.printf("%d: %f\n",localHeuristic,oldScore-newScore);
			final var index=solutionIndices.get(solution);
			if (acceptors.get(index).shouldAccept(prevScore,oldScore , globalSame, getProgress())) {
				newSolution.copyTo( solution);
			}
			if (acceptors.get(index).isRestartNeeded()) {
				solution.initialize();
				acceptors.get(index).restart(solution.value());
			}
		}
	}

	@Override
	public String toString() {
		return "Population based exhaustive heuristic";
	}
}
