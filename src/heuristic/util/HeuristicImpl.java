package heuristic.util;

import AbstractClasses.ProblemDomain;
import extension.ExtendedProblemDomain;
import lombok.Getter;
import lombok.Setter;

public final class HeuristicImpl implements Heuristic {
	@Getter
	@Setter
	private ProblemDomain domain;
	private int lastRunningTime = 0;
	private final int heuristicIndex;

	public HeuristicImpl(final ProblemDomain domain, final int heuristicIndex) {
		this.domain = domain;
		this.heuristicIndex = heuristicIndex;
	}

	@Override
	public double apply(final Solution source, final Solution destination) {
		final var times = domain.getheuristicCallTimeRecord();
		final var startTime = times[heuristicIndex];
		final var result = domain.applyHeuristic(heuristicIndex, source.solutionIndex(), destination.solutionIndex());
		lastRunningTime = times[heuristicIndex] - startTime;
		return result;
	}

	@Override
	public int getLastRunningTime() {
		return lastRunningTime;
	}

	@Override
	public Heuristic copy() {return new HeuristicImpl(domain, heuristicIndex);}

	@Override
	public String toString() {
		if (domain instanceof ExtendedProblemDomain extendedDomain)
			return "HeuristicImpl(name=\"" + extendedDomain.getHeuristicName(heuristicIndex) + "\")";
		return "HeuristicImpl(index=" + heuristicIndex + ')';
	}
}
