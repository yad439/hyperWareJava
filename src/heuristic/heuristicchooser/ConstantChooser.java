package heuristic.heuristicchooser;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import util.NestedWriter;

import java.util.random.RandomGenerator;

@ToString
@RequiredArgsConstructor
public final class ConstantChooser implements HeuristicChooser{
	private final int value;

	@Override
	public void init(final RandomGenerator rng, final int candidatesNumber) {}

	@Override
	public int choose(final double progress) {return value;}

	@Override
	public void update(final int chosen, final double oldScore, final double newScore, final boolean isSame,
	                   final int elapsedTime) {}

	@Override
	public void printStats(final NestedWriter output) {
		output.println(toString());
	}
}
