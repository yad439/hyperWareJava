package heuristic.util;

import AbstractClasses.ProblemDomain;
import lombok.val;

public interface Heuristic {
	double apply(Solution source, Solution destination);

	default RunResult applyTimed(final Solution source, final Solution destination){
		val score=apply(source,destination);
		return new RunResult(score,getLastRunningTime());
	}

	int getLastRunningTime();

	ProblemDomain getDomain();

	void setDomain(ProblemDomain domain);

	Heuristic copy();

	public record RunResult(double score, int time){}
}
