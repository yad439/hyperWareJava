package heuristic.heuristicchooser;

import java.util.random.RandomGenerator;

@Deprecated
public final class MeanImproveChooser extends ChooserAdapter {

	public MeanImproveChooser() {
		super(true);
	}

	@Deprecated
	public MeanImproveChooser(final RandomGenerator rng, final int candidateNumber){
		super(rng,candidateNumber,true);
	}

	@Override
	protected double calculateProbability(final int i) {
		return Math.max(counts[i] != 0 ? 1.0 + improves[i] / counts[i] : 1.0, 0.0);
	}

	@Override
	public String toString() {
		return "MeanImproveChooser(" +
		       "isRandom=" + isRandom +
		       ')';
	}
}
