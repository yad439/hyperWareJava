package heuristic;

import AbstractClasses.ProblemDomain;
import heuristic.acceptor.Acceptor;
import heuristic.acceptor.AillaAcceptor;
import heuristic.heuristicchooser.HeuristicChooser;
import heuristic.heuristicchooser.RandomChooser;
import heuristic.mutator.MutateLSMutator;
import heuristic.mutator.SolutionMutator;
import heuristic.solutionselector.BestSelector;
import heuristic.solutionselector.MultipleSelector;
import heuristic.solutionselector.RandomSelector;
import heuristic.solutionselector.SolutionSelector;
import heuristic.solutionselector.WorstSelector;
import heuristic.util.Solution;
import heuristic.util.SolutionAllocator;
import lombok.Setter;
import lombok.ToString;
import lombok.val;
import util.NestedWriter;
import util.StatisticPrinter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.IntStream;

@ToString(onlyExplicitlyIncluded = true)
public final class PopulationBasedHeuristic extends HeuristicAdapter implements StatisticPrinter {

	private Solution newSolution = null;
	private Solution[] population = null;

	@Setter
	@ToString.Include
	private SolutionMutator mutator = new MutateLSMutator();
	@Setter
	@ToString.Include
	private HeuristicChooser crossChooser = new RandomChooser();
	@Setter
	@ToString.Include
	private SolutionSelector crossSelector = new RandomSelector();
	private Map<Solution, Acceptor> solutionToAcceptor = null;
	@Setter
	@ToString.Include
	private SolutionSelector solutionChooser = new MultipleSelector(
			new SolutionSelector[]{new WorstSelector(), new BestSelector()},
			new double[]{0.7}
	);
	private Map<Solution, Integer> solutionIndices = null;
	private int[] withoutImprovement = null;
	private double[] bests = null;

	@Setter
	@ToString.Include
	private Supplier<? extends Acceptor> acceptorFactory = AillaAcceptor::new;
	@Setter
	@ToString.Include
	private int withoutImprovementLimit = 10;

	@Setter
	@ToString.Include
	private double depthOfSearch = 1.0;
	@Setter
	@ToString.Include
	private double intensityOfMutation = 0.8;

	public PopulationBasedHeuristic(final long seed) {super(seed);}

	@Override
	protected void allocateSolutions(final SolutionAllocator allocator) {
		newSolution = allocator.allocate();
		population = allocator.allocate(localSearches.length);
		mutator.allocateBufferSolutions(allocator);
	}

	@Override
	protected void initialize(final ProblemDomain domain) {
		domain.setDepthOfSearch(depthOfSearch);
		domain.setIntensityOfMutation(intensityOfMutation);

		crossChooser.init(rng, crossovers.length);
		crossSelector.init(rng, population);
		solutionChooser.init(rng, population);

//		final var mutated = Arrays.stream(mutations).mapToDouble(i -> domain.applyHeuristic(i, CURRENT_SOLUTION, CURRENT_SOLUTION)).toArray();
//		final var minVal = Math.min(initValue, Arrays.stream(mutated).min().orElseThrow());
//		final var maxVal = Math.max(initValue, Arrays.stream(mutated).max().orElseThrow());
//		final var acceptor=new AnnealingAcceptor(rng,maxVal - minVal,0.95,50);
		final var acceptors = IntStream.range(0, population.length)
		                               .mapToObj(i -> acceptorFactory.get())
		                               .toList();
		for (final var acceptor : acceptors) acceptor.init(rng);
		solutionToAcceptor = new HashMap<>(population.length);
		newSolution.initialize();

		for (var i = 0; i < population.length; i++) {
			population[i].initialize();
			final var startVal = population[i].value();
			acceptors.get(i).restart(startVal);
			solutionToAcceptor.put(population[i], acceptors.get(i));
		}

		mutator.init(rng, localSearches, mutations, rrs, crossovers, population, this::getProgress);

//		final var solutionChooser = new RandomSelector(population, rng);

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

		mutator.mutate(solution, newSolution);

		if (newSolution.value() < bests[solutionIndex]) {
			bests[solutionIndex] = solution.value();
			withoutImprovement[solutionIndex] = 0;
		} else withoutImprovement[solutionIndex]++;

		final var acceptor = solutionToAcceptor.get(solution);
		if (acceptor.shouldAccept(newSolution.value(), solution.value(), newSolution.isSameAs(solution),
		                          getProgress())) {
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
//			localSearches[9].apply(solution,solution);
		}
	}

	@Override
	public void printStats(final NestedWriter output) {
		output.println("Population based heuristic");
		val scoped = output.getScoped();
		scoped.formatLine("depth of search: %s, intensity of mutation: %s", depthOfSearch, intensityOfMutation);
		scoped.formatLine("without improvement limit: %d", withoutImprovementLimit);
		scoped.printIndented("mutator: ");
		mutator.printStats(scoped);
		scoped.printIndented("crossover chooser: ");
		crossChooser.printStats(scoped);
		scoped.printIndented("solution selector: ");
		solutionChooser.printStats(scoped);
		scoped.printIndented("crossover solution selector: ");
		crossSelector.printStats(scoped);
		scoped.printLine("acceptors: [");
		{
			val arrScoped = scoped.getScoped();
			for (final var acceptor : solutionToAcceptor.values()) {
				arrScoped.indent();
				acceptor.printStats(arrScoped);
			}
		}
		scoped.formatLine("without improvement: %s", Arrays.toString(withoutImprovement));
		scoped.formatLine("bests: %s", Arrays.toString(bests));
		scoped.formatLine("population: %s",
		                  Arrays.toString(Arrays.stream(population).mapToDouble(Solution::value).toArray()));
	}
}
