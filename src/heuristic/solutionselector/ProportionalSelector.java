package heuristic.solutionselector;

import heuristic.util.Solution;
import heuristic.util.Utils;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.val;
import util.NestedWriter;

import java.util.Arrays;
import java.util.random.RandomGenerator;

@ToString
@RequiredArgsConstructor
public final class ProportionalSelector extends AbstractSolutionSelector {
	private final double worstProbability;
	private final double bestProbability;
	private final boolean globalBest;
	private final boolean globalWorst;
	@ToString.Exclude
	private RandomGenerator rng = null;
	@ToString.Exclude
	private double globalMin = Double.MAX_VALUE;
	@ToString.Exclude
	private double globalMax = Double.MIN_VALUE;

	@Override
	@SuppressWarnings("ParameterHidesMemberVariable")
	public void init(final RandomGenerator rng, final Solution[] solutions) {
		super.init(rng, solutions);
		this.rng = rng;
	}

	@Override
	public Solution select(final double progress) {
		val scores = Arrays.stream(solutions).mapToDouble(Solution::value).toArray();
		val minScore = Arrays.stream(scores).min().orElseThrow();
		val maxScore = Arrays.stream(scores).max().orElseThrow();
		if (minScore < globalMin) globalMin = minScore;
		if (maxScore > globalMax) globalMax = maxScore;
		val min = globalBest ? globalMin : minScore;
		val max = globalWorst ? globalMax : maxScore;
		for (var i = 0; i < scores.length; i++) {
			val ratio = (scores[i] - min) / (max - min);
			scores[i] = ratio * worstProbability + (1 - ratio) * bestProbability;
		}
		val choice = Utils.selectWithProbability(rng, scores);
		return solutions[choice];
	}

	@Override
	public void printStats(final NestedWriter output) {
		output.print(toString());
		output.println('{');
		val scoped = output.getScoped();
		//noinspection AutoBoxing
		scoped.formatLine("globalMin=$s, globalMax=%s", globalMin, globalMax);
		output.printLine('}');
	}
}
