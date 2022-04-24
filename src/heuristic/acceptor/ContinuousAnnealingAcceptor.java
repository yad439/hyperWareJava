package heuristic.acceptor;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import util.NestedWriter;

import java.util.random.RandomGenerator;

@ToString
@RequiredArgsConstructor
public final class ContinuousAnnealingAcceptor implements Acceptor {
	@ToString.Exclude
	private RandomGenerator rng = null;
	@Setter
	private double startTemperature = 0;
	private final double endCoefficient;
	private final boolean recalculateTemperature;
	private final double startTolerance;

	@Deprecated
	public ContinuousAnnealingAcceptor(final double startTemperature, final double coolingCoefficient,
	                                   final int temperatureChanges) {
		this(Math.pow(coolingCoefficient, temperatureChanges), false, 0.0);
		this.startTemperature = startTemperature;
	}

	@Deprecated
	public ContinuousAnnealingAcceptor(final RandomGenerator rng, final double startTemperature,
	                                   final double coolingCoefficient, final int temperatureChanges) {
		this(startTemperature, coolingCoefficient, temperatureChanges);
		this.rng = rng;
	}

	@SuppressWarnings("ParameterHidesMemberVariable")
	@Override
	public void init(final RandomGenerator rng) {
		this.rng = rng;
	}

	@Override
	public boolean shouldAccept(final double solution, final double previousSolution, final boolean same,
	                            final double progress) {
		if (solution <= previousSolution) {
			return true;
		}

		return rng.nextDouble() < Math.exp(
				(previousSolution - solution) / (startTemperature * Math.pow(endCoefficient, progress)));
	}

	@Override
	public boolean isRestartNeeded() {return false;}

	@Override
	public void restart(final double solution) {
		if (recalculateTemperature) startTemperature = solution * startTolerance;
	}

	@Override
	public Acceptor copySettings() {
		return new ContinuousAnnealingAcceptor(endCoefficient, recalculateTemperature,
		                                       startTolerance).setStartTemperature(startTemperature);
	}

	@Override
	public Acceptor copyState() {
		return copySettings();
	}

	@Override
	public void printStats(final NestedWriter output) {
		output.println(toString());
	}
}
