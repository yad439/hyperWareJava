package heuristic.util;

import AbstractClasses.ProblemDomain;
import lombok.Getter;
import lombok.experimental.Accessors;

public class Solution{
	private final ProblemDomain domain;
	@Accessors(fluent = true) @Getter
	private final int solutionIndex;

	Solution(final ProblemDomain domain, final int solutionIndex) {
		this.domain = domain;
		this.solutionIndex = solutionIndex;
	}

	public void initialize() {domain.initialiseSolution(solutionIndex);}

	public double value() {return score();}

	public double score(){return domain.getFunctionValue(solutionIndex);}

	public void copyTo(final Solution other) {
		assert other.domain==domain;
		domain.copySolution(solutionIndex, other.solutionIndex);
	}

	public boolean isSameAs(final Solution other) {
		assert other.domain==domain;
		return domain.compareSolutions(solutionIndex, other.solutionIndex);
	}
}
