package heuristic;

import AbstractClasses.ProblemDomain;
import extension.ExtendedHyperHeuristic;
import heuristic.acceptor.AillaAcceptor;
import heuristic.heuristicchooser.ImproveToTimeChooser;
import heuristic.heuristicchooser.MeanImproveChooserExt;
import heuristic.solutionselector.RandomSelector;
import heuristic.util.Heuristic;
import heuristic.util.HeuristicImpl;
import heuristic.util.Solution;
import heuristic.util.SolutionAllocator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.IntStream;
@Deprecated
public final class StagedPopulationHeuristic extends ExtendedHyperHeuristic {
	public StagedPopulationHeuristic(final long seed) {super(seed);}

	@SuppressWarnings("OverlyLongMethod")
	@Override
	protected void solve(final ProblemDomain domain) {
		final var localSearches = Arrays.stream(domain.getHeuristicsOfType(ProblemDomain.HeuristicType.LOCAL_SEARCH))
		                                .mapToObj(i -> new HeuristicImpl(domain, i))
		                                .toArray(Heuristic[]::new);
		;
		final var mutations = domain.getHeuristicsOfType(ProblemDomain.HeuristicType.MUTATION);
		final var rrs = domain.getHeuristicsOfType(ProblemDomain.HeuristicType.RUIN_RECREATE);
//		final var crossovers = Arrays.stream(domain.getHeuristicsOfType(ProblemDomain.HeuristicType.CROSSOVER))
//		                             .mapToObj(i -> new CrossoverHeuristic(domain, i));
//		                             .toArray(CrossoverHeuristic[]::new);

		final var allocator = new SolutionAllocator(domain);
		final var newSolution = allocator.allocate();
		final var newSolution2=allocator.allocate();
		final var population = allocator.allocate(localSearches.length);
		allocator.commit();
		domain.setDepthOfSearch(0.6);
		domain.setIntensityOfMutation(0.8);

		final var allMutations =
				IntStream.concat(Arrays.stream(mutations), Arrays.stream(rrs))
				         .mapToObj(i -> new HeuristicImpl(domain, i))//,
                         .toArray(Heuristic[]::new);

		final var localChooser = new ImproveToTimeChooser(rng, localSearches.length, false);
//		final var localChooser = new RlChooser( localSearches.length);
//		final var localChooser = new LrpChooser(rng, localSearches.length);
//		final var mutationChoosers = IntStream.range(0, localSearches.length)
//		                                      .mapToObj(i -> new MeanImproveChooserExt(rng,allMutations.length))
//		                                      .toList();
		final var mutationChooser = new MeanImproveChooserExt(rng, allMutations.length);
		final var restartMutChooser=new MeanImproveChooserExt(rng, allMutations.length);

//		final var mutated = Arrays.stream(mutations).mapToDouble(i -> domain.applyHeuristic(i, CURRENT_SOLUTION, CURRENT_SOLUTION)).toArray();
//		final var minVal = Math.min(initValue, Arrays.stream(mutated).min().orElseThrow());
//		final var maxVal = Math.max(initValue, Arrays.stream(mutated).max().orElseThrow());
//		final var acceptor=new AnnealingAcceptor(rng,maxVal - minVal,0.95,50);
		final var acceptors = IntStream.range(0, localSearches.length)
		                               .mapToObj(i -> new AillaAcceptor())
		                               .toList();
		final var solutionIndices=new HashMap<Solution,Integer>(population.length);
		newSolution.initialize();
		final var startVal = newSolution.value();

		for (var i = 0; i < localSearches.length; i++) {
//			domain.initialiseSolution(i+1);
			acceptors.get(i).restart(startVal);
			final var newVal = localSearches[i].apply(newSolution, population[i]);
			final var isSame = population[i].isSameAs(newSolution);
			localChooser.update(i, startVal, newVal, isSame, localSearches[i].getLastRunningTime());
			solutionIndices.put(population[i], i);
		}

		final var solutionChooser = new RandomSelector(population, rng);
		final var withoutImprovement=new int[population.length];
		final var prevScore=new double[population.length];
		final var bestPhaseScores=new double[population.length];
		final var softRestartChoices=new int[population.length];
		Arrays.fill(softRestartChoices,-1);

		while (!hasTimeExpired()) {
			final var progress = getProgress();
			final var solution = solutionChooser.select(progress);
			final int solutionIndex=solutionIndices.get(solution);
			final var localHeuristic = localChooser.choose(progress);
//			final var mutation = mutationChoosers.get(localHeuristic).choose(progress);
			final int mutation;
			final Heuristic domainMutation;
			final var oldScore = solution.value();
			if(withoutImprovement[solutionIndex]<10){
				mutation = mutationChooser.choose(progress);
				domainMutation = allMutations[mutation];
				domainMutation.apply(solution,newSolution);
			} else{
				mutation=-1;
				domainMutation=null;
				if(softRestartChoices[solutionIndex]!=-1)restartMutChooser.update(
						softRestartChoices[solutionIndex],
						prevScore[solutionIndex],
						solution.value(),
						false,1);
				final var mutInd=restartMutChooser.choose(progress);
				final var mut=allMutations[mutInd];
				mut.apply(solution,newSolution);
				prevScore[solutionIndex]=bestPhaseScores[solutionIndex];
				bestPhaseScores[solutionIndex]= newSolution.value();
			}

			final var localSearch = localSearches[localHeuristic];
			final var intScore=newSolution.value();
			final var newScore = localSearch.apply(newSolution, newSolution2);
			final var isSame = newSolution2.isSameAs(solution);
			final var locsame=newSolution2.isSameAs(solution);
			localChooser.update(localHeuristic, intScore, newScore, isSame, localSearch.getLastRunningTime());
//			mutationChoosers.get(localHeuristic).update(mutation, oldScore, newScore, isSame, domainMutation.getLastRunningTime());
			if(mutation!=-1)mutationChooser.update(mutation, oldScore, newScore, isSame, domainMutation.getLastRunningTime());

			if(newScore>=oldScore)withoutImprovement[solutionIndex]++;
			else withoutImprovement[solutionIndex]=0;
			if(newScore<bestPhaseScores[solutionIndex])bestPhaseScores[solutionIndex]=newScore;

			final var acceptor = acceptors.get(solutionIndex);
			if (acceptor.shouldAccept(newScore, oldScore, isSame, getProgress())) {
				newSolution.copyTo(solution);
			}
			if (acceptor.isRestartNeeded()) {
				solution.initialize();
				acceptor.restart(solution.value());
			}
		}
	}

	@Override
	public String toString() {
		return "Staged population-based heuristic";
	}
}
