package heuristic;

import AbstractClasses.ProblemDomain;
import heuristic.heuristicchooser.HeuristicChooser;
import heuristic.heuristicchooser.RandomChooser;
import heuristic.mutator.MutateLSMutator;
import heuristic.mutator.SolutionMutator;
import heuristic.solutionselector.BatchAdaptor;
import heuristic.solutionselector.BatchSelector;
import heuristic.solutionselector.BestTruncator;
import heuristic.solutionselector.TournamentSelector;
import heuristic.solutionselector.Truncator;
import heuristic.util.Solution;
import heuristic.util.SolutionAllocator;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.val;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
public final class GenerationalGeneticAlgorithm extends HeuristicAdapter {
	@Setter BatchSelector selector = new BatchAdaptor(new TournamentSelector(2));
	@Setter HeuristicChooser crossoverChooser = new RandomChooser();
	@Setter SolutionMutator mutator = new MutateLSMutator();
	@Setter Truncator truncator = new BestTruncator(false,true);

	@Setter int populationSize = 16;
	@Setter int generationSize = 32;

	List<Solution> population1 = null;
	List<Solution> population2 = null;
	List<Solution> tempPopulation = null;
	List<Solution> currentPopulation = null;
	Solution tempSolution = null;

	public GenerationalGeneticAlgorithm(final long seed) {
		super(seed);
	}

	@Override
	public String toString() {
		return "Generational genetic algorithm";
	}

	@Override
	protected void allocateSolutions(final SolutionAllocator allocator) {
		population1 = allocator.allocateList(populationSize);
		population2 = allocator.allocateList(populationSize);
		tempPopulation = allocator.allocateList(generationSize);
		tempSolution = allocator.allocate();
		mutator.allocateBufferSolutions(allocator);
	}

	@Override
	protected void initialize(final ProblemDomain domain) {
		selector.init(rng);
		for (final var solution : population1) solution.initialize();
		mutator.init(rng, localSearches, mutations, rrs, crossovers, population1.toArray(Solution[]::new),
		             this::getProgress);
		truncator.init(rng);
		crossoverChooser.init(rng, crossovers.length);
		currentPopulation = population1;
	}

	@Override
	protected void iterate() {
		var i = 0;
		val progress = getProgress();
		for (final var solution : selector.selectMultiple(currentPopulation, tempPopulation.size(), progress)) {
			val oldScore = Math.max(solution.first().score(), solution.second().score());
			val crossIndex = crossoverChooser.choose(progress);
			val cross = crossovers[crossIndex];
			cross.apply(solution.first(), solution.second(), tempSolution);
			val destination = tempPopulation.get(i);
			mutator.mutate(tempSolution, destination);
			val newScore = destination.score();
			crossoverChooser.update(crossIndex, oldScore, newScore, false, 1);
			i++;
		}
		assert i == generationSize;
		val newPopulation = currentPopulation == population1 ? population2 : population1;
		var j = 0;
		for (final var solution : truncator.truncate(currentPopulation, tempPopulation, currentPopulation.size(),
		                                             getProgress())) {
			solution.copyTo(newPopulation.get(j));
			j++;
		}
		assert j == populationSize;
		currentPopulation = newPopulation;
	}
}
