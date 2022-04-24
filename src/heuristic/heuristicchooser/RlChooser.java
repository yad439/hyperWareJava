package heuristic.heuristicchooser;

import com.github.chen0040.rl.learning.qlearn.QLearner;
import lombok.ToString;
import util.NestedWriter;

import java.util.random.RandomGenerator;

@ToString
public final class RlChooser implements HeuristicChooser {
	private QLearner agent=null;

	@Override
	public void init(final RandomGenerator rng, final int candidatesNumber) {
		agent=new QLearner(candidatesNumber+1, candidatesNumber);
	}

	@Override
	public int choose(final double progress) {
		return choose(progress,-1);
	}

	public int choose(final double progress, final int previous) {
		return agent.selectAction(previous+1).getIndex();
	}

	@Override
	public void update(final int chosen, final double oldScore, final double newScore, final boolean isSame, final int elapsedTime) {
		update(-1,chosen,oldScore,newScore,isSame,elapsedTime);
	}

	public void update(final int previous,final int chosen, final double oldScore, final double newScore, final boolean isSame, final int elapsedTime) {
		agent.update(previous+1,chosen,chosen+1,(oldScore-newScore)/elapsedTime);
	}

	@Override
	public void printStats(final NestedWriter output) {
		output.println(toString());
	}
}
