package heuristic;

import AbstractClasses.ProblemDomain;
import heuristic.acceptor.ContinuousAnnealingAcceptor;
import heuristic.util.ChainHelper;
import heuristic.util.ChainedHeuristic;
import heuristic.util.Heuristic;
import heuristic.util.Solution;
import heuristic.util.SolutionAllocator;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public final class AnnealingListHeuristic extends HeuristicAdapter {
	private List<Heuristic> heuristics = null;
	private ChainedHeuristic currentHeuristic = null;
	@Getter
	private ChainedHeuristic bestHeuristic = null;
	private Solution[] solutions;
	private Solution tmp;
	private double value;
	@Getter private double bestValue;
	private ContinuousAnnealingAcceptor acceptor = new ContinuousAnnealingAcceptor(1.0e-4, false, 0.2);
	@Setter private int testSize = 1;
	@Setter private boolean reinit = false;
	@Setter private int maxListSize=10;

	public AnnealingListHeuristic(final long seed) {super(seed);}

	public AnnealingListHeuristic(final long seed, final ChainedHeuristic currentHeuristic) {
		super(seed);
		this.currentHeuristic = currentHeuristic;
	}

	@Override
	protected void allocateSolutions(final SolutionAllocator allocator) {
		solutions = allocator.allocate(testSize);
		tmp = allocator.allocate();
	}

	@Override
	protected void initialize(final ProblemDomain domain) {
		domain.setDepthOfSearch(1.0);
		heuristics = Stream.concat(Arrays.stream(allMutations), Arrays.stream(localSearches)).toList();
		if (currentHeuristic == null) currentHeuristic = ChainHelper.randomChain(rng, heuristics, maxListSize);
		else currentHeuristic.setDomain(domain);
		for (final var solution : solutions) solution.initialize();
		acceptor.init(rng);
		val mean = Arrays.stream(solutions).mapToDouble(Solution::value).average().orElseThrow();
		acceptor.restart(mean);
		acceptor.setStartTemperature(mean);
		value = Arrays.stream(solutions).mapToDouble(s -> currentHeuristic.apply(s, tmp)).average().orElseThrow();
		bestHeuristic = currentHeuristic.copy();
		bestValue = value;
	}

	@Override
	protected void iterate() {
		val change = ChainHelper.randomChange(rng, currentHeuristic, heuristics);
		change.apply(currentHeuristic);
		if (reinit) for (final var solution : solutions) solution.initialize();
		val newValue = Arrays.stream(solutions)
		                     .mapToDouble(s -> currentHeuristic.apply(s, tmp))
		                     .average()
		                     .orElseThrow();
		if (newValue < bestValue) {
			bestValue = newValue;
			bestHeuristic = currentHeuristic.copy();
		}
		if (acceptor.shouldAccept(newValue, value, false, getProgress()))
			value = newValue;
		else change.revert(currentHeuristic);
	}

	@Override
	public String toString() {
		return "Annealing list heuristic";
	}
}
