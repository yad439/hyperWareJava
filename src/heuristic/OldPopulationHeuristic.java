package heuristic;

import AbstractClasses.ProblemDomain;
import heuristic.acceptor.Acceptor;
import heuristic.acceptor.AillaAcceptor;
import heuristic.heuristicchooser.HeuristicChooser;
import heuristic.heuristicchooser.ImproveToTimeChooserExt;
import heuristic.heuristicchooser.MeanImproveChooserExt;
import heuristic.heuristicchooser.RandomChooser;
import heuristic.solutionselector.RandomSelector;
import heuristic.solutionselector.SolutionSelector;
import heuristic.util.Solution;
import heuristic.util.SolutionAllocator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

public final class OldPopulationHeuristic extends HeuristicAdapter {

	private Solution newSolution = null;
	private Solution[] population = null;
	private HeuristicChooser localChooser = null;
	private HeuristicChooser mutationChooser = null;
	private HeuristicChooser crossChooser = null;
	private SolutionSelector crossSelector = null;
	private Map<Solution, Acceptor> solutionToAcceptor = null;
	private SolutionSelector solutionChooser = null;
	private Map<Solution, Integer> solutionIndices = null;
	private int[] withoutImprovement = null;
	private double[] bests = null;

	private BiFunction<? super RandomGenerator, ? super Integer, ? extends HeuristicChooser> localChooserFactory = ImproveToTimeChooserExt::new;
	private BiFunction<? super RandomGenerator, ? super Integer, ? extends HeuristicChooser> mutChooserFactory = MeanImproveChooserExt::new;
	private BiFunction<? super RandomGenerator, ? super Integer, ? extends HeuristicChooser> crossChooserFactory = RandomChooser::new;
	private Function<? super RandomGenerator, ? extends Acceptor> acceptorFactory = r -> new AillaAcceptor();
	private int withoutImprovementLimit = 10;

	public OldPopulationHeuristic(final long seed) {super(seed);}

	@Override
	protected void allocateSolutions(final SolutionAllocator allocator) {
		newSolution = allocator.allocate();
		population = allocator.allocate(localSearches.length);
	}

	@Override
	protected void initialize(final ProblemDomain domain) {
		domain.setDepthOfSearch(0.5);
		domain.setIntensityOfMutation(1.0);

		// Stream.concat(

		localChooser = localChooserFactory.apply(rng, localSearches.length);
//		final var localChooser = new RlChooser( localSearches.length);
//		final var localChooser = new LrpChooser(rng, localSearches.length);
//		final var mutationChoosers = IntStream.range(0, localSearches.length)
//		                                      .mapToObj(i -> new MeanImproveChooserExt(rng,allMutations.length))
//		                                      .toList();
		mutationChooser = mutChooserFactory.apply(rng, allMutations.length);

		crossChooser = crossChooserFactory.apply(rng, crossovers.length);
		crossSelector = new RandomSelector(population, rng);

//		final var mutated = Arrays.stream(mutations).mapToDouble(i -> domain.applyHeuristic(i, CURRENT_SOLUTION, CURRENT_SOLUTION)).toArray();
//		final var minVal = Math.min(initValue, Arrays.stream(mutated).min().orElseThrow());
//		final var maxVal = Math.max(initValue, Arrays.stream(mutated).max().orElseThrow());
//		final var acceptor=new AnnealingAcceptor(rng,maxVal - minVal,0.95,50);
		final var acceptors = IntStream.range(0, localSearches.length)
		                               .mapToObj(i -> acceptorFactory.apply(rng))
		                               .toList();
		solutionToAcceptor = new HashMap<>(population.length);
		newSolution.initialize();
		final var startVal = newSolution.value();

		for (var i = 0; i < localSearches.length; i++) {
//			domain.initialiseSolution(i+1);
			acceptors.get(i).restart(startVal);
			final var newVal = localSearches[i].apply(newSolution, population[i]);
			final var isSame = population[i].isSameAs(newSolution);
			localChooser.update(i, startVal, newVal, isSame, localSearches[i].getLastRunningTime());
			solutionToAcceptor.put(population[i], acceptors.get(i));
		}

		solutionChooser = new RandomSelector(population, rng);
		/*solutionChooser = new MultipleSelector(
				new SolutionSelector[]{new WorstSelector(), new BestSelector()},
				new double[]{0.7, 1.0}
		);*/
		solutionChooser.init(rng,population);

		solutionIndices = new HashMap<>(population.length);
		for (var i = 0; i < population.length; i++) solutionIndices.put(population[i], i);
		withoutImprovement = new int[population.length];
		bests = Arrays.stream(population).mapToDouble(Solution::value).toArray();
	}

	@Override
	protected void iterate() {
		final var progress = getProgress();
		final var solution = solutionChooser.select(progress);
		final int solutionIndex = solutionIndices.get(solution);
		final var localHeuristic = localChooser.choose(progress);
//			final var mutation = mutationChoosers.get(localHeuristic).choose(progress);
		final var mutation = mutationChooser.choose(progress);
		final var domainMutation = allMutations[mutation];
		final var localSearch = localSearches[localHeuristic];

		final var oldScore = solution.value();
		domainMutation.apply(solution, newSolution);
		final var newScore = localSearch.apply(newSolution, newSolution);
		final var isSame = newSolution.isSameAs(solution);
		localChooser.update(localHeuristic, oldScore, newScore, isSame, localSearch.getLastRunningTime());
//			mutationChoosers.get(localHeuristic).update(mutation, oldScore, newScore, isSame, domainMutation.getLastRunningTime());
		mutationChooser.update(mutation, oldScore, newScore, isSame, domainMutation.getLastRunningTime());

		if (newSolution.value() < bests[solutionIndex]) {
			bests[solutionIndex] = solution.value();
			withoutImprovement[solutionIndex] = 0;
		} else withoutImprovement[solutionIndex]++;

		final var acceptor = solutionToAcceptor.get(solution);
		if (acceptor.shouldAccept(newScore, oldScore, isSame, getProgress())) {
			newSolution.copyTo(solution);
		}
		if (acceptor.isRestartNeeded()) {
			solution.initialize();
			acceptor.restart(solution.value());
			withoutImprovement[solutionIndex] = 0;
			bests[solutionIndex] = solution.value();
		}

		if (withoutImprovement[solutionIndex] >= withoutImprovementLimit) {
			final var newProgress = getProgress();
			final var crossIndex = crossChooser.choose(newProgress);
			final var crossover = crossovers[crossIndex];
			final var secondSolution = crossSelector.select(newProgress);
			crossover.apply(solution, secondSolution, solution);
		}
	}

	OldPopulationHeuristic setLocalChooserFactory(
			final BiFunction<? super RandomGenerator, ? super Integer, ? extends HeuristicChooser> localChooserFactory) {
		this.localChooserFactory = localChooserFactory;
		return this;
	}

	OldPopulationHeuristic setMutChooserFactory(
			final BiFunction<? super RandomGenerator, ? super Integer, ? extends HeuristicChooser> mutChooserFactory) {
		this.mutChooserFactory = mutChooserFactory;
		return this;
	}

	OldPopulationHeuristic setCrossChooserFactory(
			final BiFunction<? super RandomGenerator, ? super Integer, ? extends HeuristicChooser> crossChooserFactory) {
		this.crossChooserFactory = crossChooserFactory;
		return this;
	}

	OldPopulationHeuristic setAcceptorFactory(
			final Function<? super RandomGenerator, ? extends Acceptor> acceptorFactory) {
		this.acceptorFactory = acceptorFactory;
		return this;
	}

	OldPopulationHeuristic setWithoutImprovementLimit(final int withoutImprovementLimit) {
		this.withoutImprovementLimit = withoutImprovementLimit;
		return this;
	}

	@Override
	public String toString() {
		return "Population based heuristic";
	}
}
