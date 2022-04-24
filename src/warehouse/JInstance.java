package warehouse;

import java.util.BitSet;

record JInstance(int jobCount, int machineCount, int carCount, int carTravelTime, int itemCount,
                 int bufferSize, int[] jobLengths, BitSet[] itemsNeeded) implements Instance {
	@Override
	public int evaluate(final int[] permutation){
		return Utils.computeSchedule(permutation,this,true);
	}

	@Override
	public int evaluatePartial(final int[] permutation, final int limit){
		return Utils.computeSchedule(permutation,this,true,limit);
	}
}
