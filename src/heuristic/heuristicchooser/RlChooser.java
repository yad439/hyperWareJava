package heuristic.heuristicchooser;

import com.github.chen0040.rl.learning.qlearn.QLearner;
import lombok.ToString;
import lombok.val;
import util.NestedWriter;
import util.Stateful;

import java.util.random.RandomGenerator;

@ToString
public final class RlChooser implements HeuristicChooser, Stateful<RlChooser> {
	private RandomGenerator rng;
	private QLearner agent=null;

	@Override
	public void init(final RandomGenerator rng, final int candidatesNumber) {
		this.rng=rng;
		if(agent==null)agent=new QLearner(candidatesNumber+1, candidatesNumber);
	}

	@Override
	public int choose(final double progress) {
		return choose(progress,-1);
	}

	public int choose(final double progress, final int previous) {
		val index = agent.selectAction(previous + 1).getIndex();
		if(index==-1) return rng.nextInt(agent.getModel().getActionCount());
		return index;
	}

	@Override
	public void update(final int chosen, final double oldScore, final double newScore, final boolean isSame, final int elapsedTime) {
		update(-1,chosen,oldScore,newScore,isSame,elapsedTime);
	}

	public void update(final int previous,final int chosen, final double oldScore, final double newScore, final boolean isSame, final int elapsedTime) {
		agent.update(previous+1,chosen,chosen+1,(oldScore-newScore)/elapsedTime);
	}

	@Override
	public RlChooser copySettings() {
		return new RlChooser();
	}

	@Override
	public RlChooser copyState() {
		val result=new RlChooser();
		result.agent=agent.makeCopy();
		return result;
	}

	@Override
	public void printStats(final NestedWriter output) {
		output.print(toString());
		output.println('{');
		val scoped=output.getScoped();
		scoped.formatLine("agent_Q = %s",agent.getModel().getQ());
		output.printLine('}');
	}
}
