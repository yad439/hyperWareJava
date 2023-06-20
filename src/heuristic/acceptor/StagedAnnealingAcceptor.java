package heuristic.acceptor;

import heuristic.util.Utils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import util.NestedWriter;

import java.util.random.RandomGenerator;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

@ToString
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class StagedAnnealingAcceptor implements Acceptor {
	final double[] stages;
	final double[] coefficients;
	final boolean recalculateTemperature;
	final double startTolerance;
	@ToString.Exclude
	private RandomGenerator rng = null;
	@Setter
	double startTemperature = 0;

	public static double endToCooling(final int  stagesNumber, final double endCoefficient){
		return Math.pow(endCoefficient,-stagesNumber);
	}

	public StagedAnnealingAcceptor(final int stagesNumber, final double coolingCoefficient,
	                               final boolean recalculateTemperature, final double startTolerance) {
		this(IntStream.range(1, stagesNumber).mapToDouble(i -> (double) i / stagesNumber).toArray(),
		     DoubleStream.iterate(1.0, p -> p * coolingCoefficient).limit(stagesNumber).toArray(),
		     recalculateTemperature, startTolerance);
	}

	@Override
	public boolean shouldAccept(final double solution, final double previousSolution, final boolean same,
	                            final double progress) {
		if (solution <= previousSolution) return true;

		return rng.nextDouble() < Math.exp(
				(previousSolution - solution) / (startTemperature * coefficients[Utils.binarySearchLess(stages,
				                                                                                        progress)]));
	}

	@Override
	public boolean isRestartNeeded() {
		return false;
	}

	@Override
	public void init(final RandomGenerator rng) {
		this.rng = rng;
	}

	@Override
	public void restart(final double solution) {
		if (recalculateTemperature) startTemperature = solution * startTolerance;
	}

	@Override
	public Acceptor copySettings() {
		return new StagedAnnealingAcceptor(stages, coefficients, recalculateTemperature,
		                                   startTolerance).setStartTemperature(startTemperature);
	}

	@Override
	public Acceptor copyState() {
		return copySettings();
	}

	@Override
	public void printStats(final NestedWriter output) {output.println(toString());}
}
