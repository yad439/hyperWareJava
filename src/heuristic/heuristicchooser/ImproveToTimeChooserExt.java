package heuristic.heuristicchooser;

import java.util.random.RandomGenerator;

public final class ImproveToTimeChooserExt extends ChooserAdapter {
	private final double unexploredCoefficient;
	private final double softenCoefficient;
	private final boolean bound;

	public ImproveToTimeChooserExt() {this(true,true,true,1.0,1.0,true);}

	@Deprecated
	public ImproveToTimeChooserExt(final boolean isRandom) {
		super(isRandom);
		unexploredCoefficient=1.0;
		softenCoefficient=1.0;
		bound=true;
	}

	public ImproveToTimeChooserExt(final boolean isRandom, final boolean isRelative, final boolean randomOnExhaustion,
	                        final double unexploredCoefficient,
	                        final double softenCoefficient, final boolean bound) {
		super(isRandom, isRelative, randomOnExhaustion);
		this.unexploredCoefficient = unexploredCoefficient;
		this.softenCoefficient = softenCoefficient;
		this.bound = bound;
	}

	@Deprecated
	public ImproveToTimeChooserExt(final RandomGenerator rng, final int candidateNumber) {
		super(rng, candidateNumber, true);
		unexploredCoefficient=1.0;
		softenCoefficient=1.0;
		bound=true;
	}

	@Override
	public void update(final int chosen, final double oldScore, final double newScore, final boolean isSame,
	                   final int elapsedTime) {
		super.update(chosen, oldScore, newScore, isSame, elapsedTime);
		if (bound && improves[chosen] < -softenCoefficient) improves[chosen] = -softenCoefficient;
	}

	@Override
	protected double calculateProbability(final int i) {
		return Math.max(times[i] != 0 ? softenCoefficient + improves[i] / times[i] : unexploredCoefficient, 0.0);
	}

	@Override
	public String toString() {
		return "ImproveToTimeChooserExt(" +
		       "unexploredCoefficient=" + unexploredCoefficient +
		       ", softenCoefficient=" + softenCoefficient +
		       ", bound=" + bound +
		       ", isRandom=" + isRandom +
		       ", isRelative=" + isRelative +
		       ", randomOnExhaustion=" + randomOnExhaustion +
		       ')';
	}
}
