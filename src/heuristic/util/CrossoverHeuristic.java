package heuristic.util;

import AbstractClasses.ProblemDomain;
import extension.ExtendedProblemDomain;
import lombok.Getter;
import lombok.Setter;

public final class CrossoverHeuristic {
	@Getter
	@Setter
	private ProblemDomain domain;
	private int lastRunningTime=0;
	private final int heuristicIndex;

	public CrossoverHeuristic(final ProblemDomain domain, final int heuristicIndex) {
		this.domain = domain;
		this.heuristicIndex = heuristicIndex;
	}

	public double apply(final Solution parent1, final Solution parent2, final Solution destination) {
		final var times = domain.getheuristicCallTimeRecord();
		final var startTime= times[heuristicIndex];
		final var result = domain.applyHeuristic(
				heuristicIndex,
				parent1.solutionIndex(),
				parent2.solutionIndex(),
				destination.solutionIndex()
		                                   );
		lastRunningTime= times[heuristicIndex] - startTime;
		return result;
	}
	public int getLastRunningTime() {
		return lastRunningTime;
	}

	CrossoverHeuristic copy(){return new CrossoverHeuristic(domain,heuristicIndex);}

	public String toString() {
		if(domain instanceof ExtendedProblemDomain extendedDomain)return "CrossoverHeuristic(name=\"" + extendedDomain.getHeuristicName(heuristicIndex) + "\")";
		return "CrossoverHeuristic(index="+heuristicIndex+')';
	}
}
