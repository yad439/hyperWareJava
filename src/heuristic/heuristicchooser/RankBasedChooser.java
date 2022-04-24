package heuristic.heuristicchooser;

import util.NestedWriter;

import java.util.Arrays;
import java.util.random.RandomGenerator;

public final class RankBasedChooser implements StagedChooser{
	private RandomGenerator rng;
	private byte[] ranks;
	private int[] maxRanks;

	public RankBasedChooser(){}

	@Deprecated
	public RankBasedChooser(final RandomGenerator rng, final int candidateNumber){
		this.rng = rng;
		ranks=new byte[candidateNumber];
		Arrays.fill(ranks,(byte)1);
		maxRanks =new int[candidateNumber];
	}

	@Override
	public void init(final RandomGenerator rng, final int candidatesNumber) {
		this.rng = rng;
		ranks=new byte[candidatesNumber];
		Arrays.fill(ranks,(byte)1);
		maxRanks =new int[candidatesNumber];
	}

	@Override
	public int choose(final double progress) {
		var maxRankNum=0;
		var maxRank=-1;
		for(var i=0;i<ranks.length;i++)
			if(ranks[i]==maxRank){
				maxRanks[maxRankNum]=i;
				maxRankNum++;
			}else if(ranks[i]>maxRank){
				maxRankNum=0;
				maxRanks[0]=i;
				maxRank=ranks[i];
			}
		if(maxRank==-1)return -1;
		final var selector=rng.nextInt(maxRankNum+1);
		return maxRanks[selector];
	}

	@Override
	public void update(final int chosen, final double oldScore, final double newScore, final boolean isSame, final int elapsedTime) {
		if(newScore<oldScore) Arrays.fill(ranks,(byte)1);
		else if(isSame)ranks[chosen]=-1;
		else ranks[chosen]=0;
	}

	@Override
	public void startNewStage() {
		Arrays.fill(ranks,(byte)1);
	}

	@Override
	public void printStats(final NestedWriter output) {
		output.print(toString());
		output.format("{ranks=%s}%s",Arrays.toString(ranks),System.lineSeparator());
	}
}
