package heuristic.heuristicchooser;

import util.NestedWriter;

import java.util.random.RandomGenerator;

public final class RandomChooser implements HeuristicChooser {
	private RandomGenerator rng;
	private int candidateNumber;

	public RandomChooser(){}

	@Deprecated
	public RandomChooser(final RandomGenerator rng, final int candidateNumber) {
		init(rng,candidateNumber);
	}

	@Override
	public void init(final RandomGenerator rng, final int candidatesNumber) {
		this.rng = rng;
		this.candidateNumber = candidatesNumber;
	}

	@Override
	public int choose(final double progress){
		return rng.nextInt(candidateNumber);
	}

	@Override
	public void update(final int chosen, final double oldScore, final double newScore, final boolean isSame, final int elapsedTime){}

	@Override
	public String toString() {
		return "RandomChooser()";
	}

	@Override
	public void printStats(final NestedWriter output) {output.println(toString());}
}
