package heuristic.acceptor;

import lombok.ToString;
import util.NestedWriter;

import java.util.random.RandomGenerator;

@ToString
public final class AlwaysAccept implements Acceptor{
	@Override
	public boolean shouldAccept(final double solution, final double previousSolution, final boolean same,
	                            final double progress) {return true;}

	@Override
	public boolean isRestartNeeded() {return false;}

	@Override
	public void init(final RandomGenerator rng) {}

	@Override
	public void restart(final double solution) {}

	@Override
	public Acceptor copySettings() {return this;}

	@Override
	public Acceptor copyState() {return this;}

	@Override
	public void printStats(final NestedWriter output) {
		output.println(toString());
	}
}
