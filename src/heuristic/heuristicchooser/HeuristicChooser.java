package heuristic.heuristicchooser;

import util.StatisticPrinter;

import java.util.random.RandomGenerator;

public interface HeuristicChooser extends StatisticPrinter {

	void init(RandomGenerator rng, int candidatesNumber);

	int choose(double progress);

	void update(int chosen, double oldScore, double newScore, boolean isSame, int elapsedTime);
}
