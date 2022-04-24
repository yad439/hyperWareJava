package heuristic.util;

import AbstractClasses.ProblemDomain;
import lombok.Getter;
import lombok.Setter;

public class StatefulSolution extends Solution {
	@Getter
	@Setter
	private State state = State.UNKNOWN;

	public StatefulSolution(final ProblemDomain domain, final int solutionIndex) {
		super(domain, solutionIndex);
	}

	@Override
	public void initialize() {
		super.initialize();
		state=State.INITIALIZED;
	}
	@Override
	public void copyTo(final Solution other) {
		super.copyTo(other);
		if(other instanceof StatefulSolution stateful)stateful.state=state;
	}

	public enum State {UNKNOWN, INITIALIZED, MUTATED, CROSSED, LOCAL_OPT, STUCK}
}
