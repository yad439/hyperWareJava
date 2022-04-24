package heuristic.heuristicchooser;

import heuristic.util.Utils;

import java.util.random.RandomGenerator;

public final class MeanImproveChooserExt extends ChooserAdapter {
	private final double unexploredCoefficient;
	private final double softenCoefficient;
	private final boolean bound;

	@Deprecated
	public MeanImproveChooserExt(final boolean isRandom) {
		super(isRandom);
		unexploredCoefficient=1.0;
		softenCoefficient=1.0;
		bound=true;
	}

	public MeanImproveChooserExt() {this(true,true,true,1.0,1.0,true);}

	@Deprecated
	public MeanImproveChooserExt(final RandomGenerator rng, final int candidateNumber) {
		super(rng, candidateNumber, true);
		unexploredCoefficient=1.0;
		softenCoefficient=1.0;
		bound=true;
	}

	public MeanImproveChooserExt(final boolean isRandom, final boolean isRelative, final boolean randomOnExhaustion,
	                        final double unexploredCoefficient,
	                        final double softenCoefficient, final boolean bound) {
		super(isRandom, isRelative, randomOnExhaustion);
		this.unexploredCoefficient = unexploredCoefficient;
		this.softenCoefficient = softenCoefficient;
		this.bound = bound;
	}

	@Override
	protected double calculateProbability(final int i) {
		return Math.max(softenCoefficient + Utils.divOr(improves[i] , counts[i],unexploredCoefficient-softenCoefficient), 0.0);
	}

	@Override
	public void update(final int chosen, final double oldScore, final double newScore, final boolean isSame, final int elapsedTime) {
		super.update(chosen, oldScore, newScore, isSame, elapsedTime);
		if (bound && improves[chosen] < softenCoefficient) improves[chosen] = -softenCoefficient;
	}

	@Override
	public String toString() {
		return "MeanImproveChooserExt(" +
		       "unexploredCoefficient=" + unexploredCoefficient +
		       ", softenCoefficient=" + softenCoefficient +
		       ", bound=" + bound +
		       ", isRandom=" + isRandom +
		       ", isRelative=" + isRelative +
		       ", randomOnExhaustion=" + randomOnExhaustion +
		       ')';
	}
}
