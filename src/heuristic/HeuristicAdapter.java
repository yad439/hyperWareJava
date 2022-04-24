package heuristic;

import AbstractClasses.ProblemDomain;
import extension.ExtendedHyperHeuristic;
import heuristic.util.CrossoverHeuristic;
import heuristic.util.Heuristic;
import heuristic.util.HeuristicImpl;
import heuristic.util.SolutionAllocator;
import lombok.Setter;

import java.util.Arrays;
import java.util.stream.Stream;

public abstract class HeuristicAdapter extends ExtendedHyperHeuristic {
	protected Heuristic[] localSearches = null;
	protected Heuristic[] mutations = null;
	protected Heuristic[] rrs = null;
	protected CrossoverHeuristic[] crossovers = null;
	protected Heuristic[] allMutations = null;

	@Setter protected double depthOfSearch=0.5;
	@Setter protected double intensityOfMutation=0.5;

	HeuristicAdapter(final long seed) {super(seed);}

	@Override
	protected final void solve(final ProblemDomain domain) {
		domain.setDepthOfSearch(depthOfSearch);
		domain.setIntensityOfMutation(intensityOfMutation);
		loadHeuristics(domain);
		{
			final var allocator = new SolutionAllocator(domain);
			allocateSolutions(allocator);
			allocator.commit();
		}
		initialize(domain);

		while (!hasTimeExpired()) {
			iterate();
		}
	}

	protected abstract void allocateSolutions(SolutionAllocator allocator);

	protected abstract void initialize(ProblemDomain domain);

	protected abstract void iterate();

	private void loadHeuristics(final ProblemDomain domain) {
		localSearches = Arrays.stream(domain.getHeuristicsOfType(ProblemDomain.HeuristicType.LOCAL_SEARCH))
		                      .mapToObj(i -> new HeuristicImpl(domain, i))
		                      .toArray(Heuristic[]::new);
		mutations = Arrays.stream(domain.getHeuristicsOfType(ProblemDomain.HeuristicType.MUTATION))
		                  .mapToObj(i -> new HeuristicImpl(domain, i))
		                  .toArray(Heuristic[]::new);
		rrs = Arrays.stream(domain.getHeuristicsOfType(ProblemDomain.HeuristicType.RUIN_RECREATE))
		            .mapToObj(i -> new HeuristicImpl(domain, i))
		            .toArray(Heuristic[]::new);
		crossovers = Arrays.stream(domain.getHeuristicsOfType(ProblemDomain.HeuristicType.CROSSOVER))
		                   .mapToObj(i -> new CrossoverHeuristic(domain, i))
		                   .toArray(CrossoverHeuristic[]::new);
		allMutations = Stream.concat(Arrays.stream(mutations), Arrays.stream(rrs)).toArray(Heuristic[]::new);
	}
}
