package heuristic.heuristicchooser;

import heuristic.util.Utils;
import lombok.val;
import util.NestedWriter;

import java.util.Arrays;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

public final class MabChooser implements HeuristicChooser {
	private RandomGenerator rng;
	private int[] counts;
	private double[] improves;
	private final double balanceCoefficient;
	private final boolean isRandom;

	public MabChooser(){this(0.5, false);}

	public MabChooser(final double balanceCoefficient, final boolean isRandom) {
		this.balanceCoefficient = balanceCoefficient;
		this.isRandom = isRandom;
	}

	@Override
	public void init(final RandomGenerator rng, final int candidatesNumber) {
		this.rng = rng;
		this.counts = new int[candidatesNumber];
		this.improves = new double[candidatesNumber];
	}

	@Override
	public int choose(final double progress) {
		final var scores= IntStream.range(0,counts.length)
		                           .mapToDouble(i-> Utils.divOr(improves[i],counts[i],0)
		                                            + balanceCoefficient * Math.sqrt(
														   2*Utils.divOr(Math.log(Arrays.stream(counts).sum())
		                                                        ,counts[i],100))).toArray();
		if(isRandom)return Utils.selectWithProbability(rng, scores);
		return Utils.argmax(scores);
	}

	@Override
	public void update(final int chosen, final double oldScore, final double newScore, final boolean isSame, final int elapsedTime) {
		final var r=(oldScore-newScore)/oldScore;
		improves[chosen]+=r;
		counts[chosen]++;
	}

	@Override
	public String toString() {
		return "MabChooser(" +
		       "isRandom=" + isRandom +
		       ",balanceCoefficient=" + balanceCoefficient +
		       ')';
	}

	@Override
	public void printStats(final NestedWriter output) {
		output.print(toString());
		output.print('{');
		val scoped=output.getScoped();
		scoped.formatLine("improves=%s",Arrays.toString(improves));
		scoped.formatLine("counts=%s",Arrays.toString(counts));
		output.printLine('}');
	}
}
