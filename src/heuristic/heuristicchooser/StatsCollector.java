package heuristic.heuristicchooser;

import lombok.val;
import util.NestedWriter;

import java.util.Arrays;
import java.util.random.RandomGenerator;

public final class StatsCollector implements HeuristicChooser{
	private RandomGenerator rng;
	private int[] improves=null;
	private int[] worsens=null;
	private int candidates=0;

	@Override
	public void init(final RandomGenerator rng, final int candidatesNumber) {
		this.rng=rng;
		improves=new int[candidatesNumber];
		worsens=new int[candidatesNumber];
		candidates=candidatesNumber;
	}

	@Override
	public int choose(final double progress) {
		return rng.nextInt(candidates);
	}

	@Override
	public void update(final int chosen, final double oldScore, final double newScore, final boolean isSame,
	                   final int elapsedTime) {
		if(newScore<oldScore)improves[chosen]++;
		else if (newScore > oldScore) worsens[chosen]++;
	}

	public void printStats(){
		System.out.println(Arrays.toString(improves));
		System.out.println(Arrays.toString(worsens));
	}

	@Override
	public void printStats(final NestedWriter output) {
		output.print(toString());
		output.println('{');
		val scoped=output.getScoped();
		scoped.formatLine("improves=$s",Arrays.toString(improves));
		scoped.formatLine("worsens=$s",Arrays.toString(worsens));
		output.printLine('}');
	}
}
