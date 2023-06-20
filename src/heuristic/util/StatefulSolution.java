package heuristic.util;

import AbstractClasses.ProblemDomain;
import lombok.Getter;
import lombok.Setter;

public class StatefulSolution extends Solution {
	@Getter
	@Setter
	private int state = -1;

	public StatefulSolution(final ProblemDomain domain, final int solutionIndex) {
		super(domain, solutionIndex);
	}

	@Override
	public void initialize() {
		super.initialize();
		state= 0;
	}
	@Override
	public void copyTo(final Solution other) {
		super.copyTo(other);
		if(other instanceof StatefulSolution stateful) stateful.state=state;
	}
}
