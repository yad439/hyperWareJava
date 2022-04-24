package heuristic.acceptor;

import util.Stateful;
import util.StatisticPrinter;

import java.util.random.RandomGenerator;

public interface Acceptor extends StatisticPrinter, Stateful<Acceptor> {
	boolean shouldAccept(double solution, double previousSolution, boolean same, double progress);

	boolean isRestartNeeded();

	void init(RandomGenerator rng);

	void restart(double solution);
}
