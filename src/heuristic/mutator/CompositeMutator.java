package heuristic.mutator;

import heuristic.util.Solution;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import lombok.val;
import util.NestedWriter;

import java.util.HashMap;
import java.util.random.RandomGenerator;

public final class CompositeMutator extends SolutionMutator{
	private final HashMap<Solution,SolutionStats> stats=new HashMap<>();
	@Setter
	private SolutionMutator[] inners={new MutateLSMutator(), new MutateLSMutator()};
	@Setter private int[] thresholds={5};


	@Override
	public void mutate(final Solution source, final Solution destination) {
		val stat=stats.get(source);
		if(Math.abs(source.value()-stat.getLastScore())<1.0e-8){
			stat.setLastScore(source.value());
			stat.setWithoutImprovement(0);
		}
	}

	@Override
	protected void initInner(final RandomGenerator rng, final Solution[] population) {
		for(final var inner:inners)inner.init(rng,localSearches,mutations,rrs,crossovers, population, progressFunction);
		for(final var solution:population)
			stats.put(solution,new SolutionStats(solution.value(),0));
	}

	@Override
	public void printStats(final NestedWriter output) {

	}

	@Data
	@AllArgsConstructor
	private static class SolutionStats{
		private double lastScore;
		private int withoutImprovement;
	}
}
