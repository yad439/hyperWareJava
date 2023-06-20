package heuristic.util;

import AbstractClasses.ProblemDomain;
import lombok.Getter;
import lombok.Setter;

public class EnumStatefulSolution extends Solution {
	@Getter
	@Setter
	private State state = State.UNKNOWN;

	public EnumStatefulSolution(final ProblemDomain domain, final int solutionIndex) {
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
		if(other instanceof EnumStatefulSolution stateful) stateful.state=state;
	}

	public enum State {UNKNOWN, INITIALIZED, MUTATED, CROSSED, LOCAL_OPT, STUCK}
}
