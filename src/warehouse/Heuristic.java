package warehouse;

import AbstractClasses.ProblemDomain;

record Heuristic(ProblemDomain.HeuristicType type, boolean usesIntensity, boolean usesDepth, Function function) {
	@FunctionalInterface
	interface Function{
		int apply(int[] source, int[] destination);
	}
}