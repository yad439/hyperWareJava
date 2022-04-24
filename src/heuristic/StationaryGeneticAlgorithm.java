package heuristic;

import AbstractClasses.ProblemDomain;
import heuristic.acceptor.Acceptor;
import heuristic.acceptor.AillaAcceptor;
import heuristic.acceptor.AlwaysAccept;
import heuristic.acceptor.ContinuousAnnealingAcceptor;
import heuristic.heuristicchooser.HeuristicChooser;
import heuristic.heuristicchooser.ImproveToTimeChooserExt;
import heuristic.heuristicchooser.MeanImproveChooserExt;
import heuristic.replaceselector.SolutionReplaceSelector;
import heuristic.replaceselector.WorstReplacer;
import heuristic.solutionselector.SolutionSelector;
import heuristic.solutionselector.TournamentSelector;
import heuristic.util.Solution;
import heuristic.util.SolutionAllocator;
import lombok.Setter;
import lombok.val;
import util.NestedWriter;
import util.StatisticPrinter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("ClassWithTooManyFields")
public final class StationaryGeneticAlgorithm extends HeuristicAdapter implements StatisticPrinter {

	@Setter
	private double depthOfSearch = 1.0;
	@Setter
	private double intensityOfMutation = 0.5;
	@Setter
	private HeuristicChooser localChooser = new ImproveToTimeChooserExt();
	@Setter
	private HeuristicChooser mutationChooser = new MeanImproveChooserExt();
	@Setter
	private HeuristicChooser crossoverChooser = new MeanImproveChooserExt();
	private Solution[] population = null;
	private Solution tempSolution1 = null;
	private Solution tempSolution2 = null;
	@Setter
	private Acceptor acceptor = new AlwaysAccept();
	private Map<Solution, Acceptor> acceptors = null;
	@Setter
	private SolutionSelector firstSelector = new TournamentSelector(2);
	@Setter
	private SolutionSelector secondSelector = firstSelector;
	@Setter
	private SolutionReplaceSelector solutionReplacer = new WorstReplacer();

	public StationaryGeneticAlgorithm(final long seed) {super(seed);}

	@Override
	protected void allocateSolutions(final SolutionAllocator allocator) {
		tempSolution1 = allocator.allocate();
		tempSolution2 = allocator.allocate();
		population = allocator.allocate(2 * localSearches.length);
	}

	@Override
	protected void initialize(final ProblemDomain domain) {
		domain.setDepthOfSearch(depthOfSearch);
		domain.setIntensityOfMutation(intensityOfMutation);

		localChooser.init(rng, localSearches.length);
//		final var localChooser = new LrpChooser(rng, localSearches.length);
		mutationChooser.init(rng, allMutations.length);
		crossoverChooser.init(rng, crossovers.length);

		for (final var solution : population) {
			solution.initialize();
		}

//		final var solutionChooser = new RandomSelector(population, rng);
		firstSelector.init(rng, population);
		secondSelector.init(rng, population);
//		final var solutionReplacer = new SelfReplacer();
		solutionReplacer.init(rng, population);

		var minVal = Double.MAX_VALUE;
		var maxVal = Double.MIN_VALUE;
		for (final var solution : population) {
			if (solution.value() < minVal) minVal = solution.value();
			if (solution.value() > maxVal) maxVal = solution.value();
		}
		if (acceptor instanceof ContinuousAnnealingAcceptor annealingAcceptor)
			annealingAcceptor.setStartTemperature((maxVal - minVal) / 2);
//		final var acceptor=new AillaAcceptor();
//		acceptor.init(solutionReplacer.select(0,null).value());
//		final var solutionIndices=new HashMap<Solution,Integer>(population.length);
		acceptors = new HashMap<>(population.length);
		for (var i = 0; i < population.length; i++) {
			final var solution = population[i];
			final var score = solution.value();
			localSearches[i % localSearches.length].apply(solution, solution);
			localChooser.update(i % localSearches.length, score, solution.value(), false,
			                    localSearches[i % localSearches.length].getLastRunningTime());
			final var ac = new AillaAcceptor();
			ac.restart(solution.value());
			acceptors.put(solution, ac);
		}
	}

	@Override
	protected void iterate() {
		final var progress = getProgress();
		final var parent1 = firstSelector.select(progress);
		final var parent2 = secondSelector.select(progress);
		final var localHeuristicIndex = localChooser.choose(progress);
		final var mutationIndex = mutationChooser.choose(progress);
		final var mutation = allMutations[mutationIndex];
		final var localSearch = localSearches[localHeuristicIndex];
		final var crossover = crossoverChooser.choose(progress);
		final var oldScore = crossovers[crossover].apply(parent1, parent2, tempSolution1);
		mutation.apply(tempSolution1, tempSolution2);
		final var newScore = localSearch.apply(tempSolution2, tempSolution2);
		final var isSame = tempSolution2.isSameAs(tempSolution1);
		final var isSameCross = tempSolution2.isSameAs(parent1) || tempSolution2.isSameAs(parent2);
		localChooser.update(localHeuristicIndex, oldScore, newScore, isSame, localSearch.getLastRunningTime());
		mutationChooser.update(mutationIndex, oldScore, newScore, isSame, mutation.getLastRunningTime());
		crossoverChooser.update(crossover, (parent1.value() + parent2.value()) / 2, newScore, isSameCross,
		                        crossovers[crossover].getLastRunningTime());
		final var toReplace = solutionReplacer.select(getProgress(), parent1, parent2);
		if (acceptors.get(toReplace).shouldAccept(tempSolution2.value(), toReplace.value(), false, getProgress()))
			tempSolution2.copyTo(toReplace);
	}

	@Override
	public String toString() {return "Stationary genetic algorithm";}

	@Override
	public void printStats(final NestedWriter output) {
		output.println("Stationary genetic algorithm");
		val scoped = output.getScoped();
		scoped.printIndented("local chooser: ");
		localChooser.printStats(scoped);
		scoped.printIndented("mutation chooser: ");
		mutationChooser.printStats(scoped);
		scoped.printIndented("crossover chooser: ");
		crossoverChooser.printStats(scoped);
		scoped.printIndented("first selector: ");
		firstSelector.printStats(scoped);
		scoped.printIndented("second selector: ");
		secondSelector.printStats(scoped);
		scoped.printIndented("replacer: ");
		solutionReplacer.printStats(scoped);
		scoped.printIndented("acceptor: ");
		acceptor.printStats(scoped);
		scoped.formatLine("population: %s",
		                  Arrays.toString(Arrays.stream(population).mapToDouble(Solution::value).toArray()));
	}
}
