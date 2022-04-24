package heuristic.heuristicchooser;

import heuristic.util.Utils;
import util.NestedWriter;

import java.util.Arrays;
import java.util.random.RandomGenerator;

public final class LrpChooser implements HeuristicChooser {
	private RandomGenerator rng;
	private double[] probabilities;
	private final double coefficient;

	public LrpChooser(final double coefficient) {
		this.coefficient=coefficient;
	}

	public LrpChooser(){this(0.05);}

	@Deprecated
	public LrpChooser(final RandomGenerator rng, final int candidateNumber, final double coefficient) {
		this.rng = rng;
		probabilities = new double[candidateNumber];
		Arrays.fill(probabilities, 1.0 / candidateNumber);
		this.coefficient=coefficient;
	}

	@Deprecated
	public LrpChooser(final RandomGenerator rng, final int candidateNumber){this(rng,candidateNumber,0.05);}

	@Override
	public void init(final RandomGenerator rng, final int candidatesNumber) {
		this.rng = rng;
		probabilities = new double[candidatesNumber];
		Arrays.fill(probabilities, 1.0 / candidatesNumber);
	}

	@Override
	public int choose(final double progress) {
		return Utils.selectWithProbability(rng, probabilities);
	}

	@Override
	public void update(final int chosen, final double oldScore, final double newScore, final boolean isSame, final int elapsedTime) {
		if (newScore < oldScore) {
			probabilities[chosen] = coefficient * (1 - probabilities[chosen]) + probabilities[chosen];
			for (var j = 0; j < probabilities.length; j++)
				if (j != chosen) probabilities[j] = probabilities[j] - coefficient * probabilities[j];
		}
	}

	@Override
	public String toString() {
		return "LrpChooser(" +
		       "coefficient=" + coefficient +
		       ')';
	}

	@Override
	public void printStats(final NestedWriter output) {
		output.print(toString());
		output.format("{probabilities=$s}$s",Arrays.toString(probabilities),System.lineSeparator());
	}
}
