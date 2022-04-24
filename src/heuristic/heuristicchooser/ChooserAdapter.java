package heuristic.heuristicchooser;

import heuristic.util.Utils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import util.NestedWriter;

import java.util.Arrays;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

@RequiredArgsConstructor
abstract class ChooserAdapter implements HeuristicChooser {
	private RandomGenerator rng;
	protected final boolean isRandom;
	protected final boolean isRelative;
	protected final boolean randomOnExhaustion;
	protected double[] improves=null;
	protected int[] counts=null;
	protected int[] times=null;

	ChooserAdapter(final boolean isRandom) {
		this.isRandom=isRandom;
		this.isRelative=false;
		this.randomOnExhaustion=true;
	}

	@Deprecated
	ChooserAdapter(final RandomGenerator rng, final int candidatesNumber, final boolean isRandom){
		this(isRandom);
		init(rng,candidatesNumber);
	}

	@Override
	public void init(final RandomGenerator rng, final int candidatesNumber) {
		this.rng=rng;
		improves = new double[candidatesNumber];
		counts = new int[candidatesNumber];
		times = new int[candidatesNumber];
	}

	@Override
	public final int choose(final double progress) {
		final var scores = IntStream.range(0, improves.length)
		                                   .mapToDouble(this::calculateProbability)
		                                   .toArray();
		if(Arrays.stream(scores).allMatch(p->p==0.0)) {
			if (randomOnExhaustion) return rng.nextInt(improves.length);
			return -1;
		}
		if(isRandom)return Utils.selectWithProbability(rng,scores);
		return Utils.argmax(scores);
	}

	@Override
	public void update(final int chosen, final double oldScore, final double newScore, final boolean isSame, final int elapsedTime) {
		times[chosen]+=elapsedTime;
		counts[chosen]++;
		val improve = oldScore - newScore;
		improves[chosen] += isRelative? improve/oldScore : improve;
	}

	protected abstract double calculateProbability(int i);

	@Override
	public void printStats(final NestedWriter output) {
		output.print(toString());
		output.println('{');
		val scoped=output.getScoped();
		scoped.formatLine("improves = %s",Arrays.toString(improves));
		scoped.formatLine("counts = %s",Arrays.toString(counts));
		scoped.formatLine("times = %s",Arrays.toString(times));
		output.printLine('}');
	}
}
