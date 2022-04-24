package heuristic.mutator;

import heuristic.util.CrossoverHeuristic;
import heuristic.util.Heuristic;
import heuristic.util.Solution;
import heuristic.util.SolutionAllocator;
import util.StatisticPrinter;

import java.util.Arrays;
import java.util.function.DoubleSupplier;
import java.util.random.RandomGenerator;
import java.util.stream.Stream;

public abstract class SolutionMutator implements StatisticPrinter {
	protected Heuristic[] localSearches = null;
	protected Heuristic[] mutations = null;
	protected Heuristic[] rrs = null;
	protected CrossoverHeuristic[] crossovers = null;
	protected Heuristic[] allMutations = null;
	protected DoubleSupplier progressFunction = null;

	public void allocateBufferSolutions(final SolutionAllocator allocator) {}

	public final void init(final RandomGenerator rng, final Heuristic[] localSearches, final Heuristic[] mutations,
	                       final Heuristic[] rrs, final CrossoverHeuristic[] crossovers, final Solution[] population,
	                       final DoubleSupplier progress) {
		this.localSearches = localSearches;
		this.mutations = mutations;
		this.rrs = rrs;
		this.crossovers = crossovers;
		this.allMutations = Stream.concat(Arrays.stream(mutations), Arrays.stream(rrs)).toArray(Heuristic[]::new);
		this.progressFunction = progress;
		initInner(rng, population);
	}

	public abstract void mutate(final Solution source, final Solution destination);

	protected abstract void initInner(final RandomGenerator rng, final Solution[] population);

}
