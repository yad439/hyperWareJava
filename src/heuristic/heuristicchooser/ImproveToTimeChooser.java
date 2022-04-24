package heuristic.heuristicchooser;

import java.util.random.RandomGenerator;
@Deprecated
public final class ImproveToTimeChooser extends ChooserAdapter {
	public ImproveToTimeChooser(final boolean isRandom) {
		super(isRandom);
	}

	@Deprecated
	ImproveToTimeChooser(final RandomGenerator rng, final int candidateNumber) {
		super(rng, candidateNumber, true);
	}

	@Deprecated
	public ImproveToTimeChooser(final RandomGenerator rng, final int candidateNumber, final boolean isRandom) {
		super(rng, candidateNumber, isRandom);
	}

	@Override
	protected double calculateProbability(final int i) {
		return Math.max(times[i] != 0 ? 1.0 + improves[i] / times[i] : 1.0, 0.0);
	}

	@Override
	public String toString() {
		return "ImproveToTimeChooser(" +
		       "isRandom=" + isRandom +
		       ')';
	}
}
