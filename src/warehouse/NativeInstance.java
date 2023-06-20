package warehouse;

import lombok.val;

import java.lang.ref.Cleaner;
import java.util.Arrays;
import java.util.BitSet;

public final class NativeInstance implements Instance {
	private static final Cleaner cleaner = Cleaner.create();
	private static final Object lock = new Object();
	private static boolean isLoaded = false;
	private final long ptr;

	NativeInstance(final JInstance jInstance) {
		synchronized (lock) {
			if (!isLoaded) {
				System.loadLibrary("warehouseInstance");
				isLoaded = true;
			}
		}

		ptr = initialize(jInstance.jobCount(), jInstance.machineCount(), jInstance.carCount(),
		                 jInstance.carTravelTime(),
		                 jInstance.itemCount(), jInstance.bufferSize(), jInstance.jobLengths(),
		                 Arrays.stream(jInstance.itemsNeeded())
		                       .map(job -> job.stream().toArray())
		                       .toArray(int[][]::new));
		val localPtr = ptr;
		cleaner.register(this, () -> dispose(localPtr));
	}

	@Override
	public int evaluate(final int[] permutation) {
		return evaluateNative(ptr, permutation);
	}

	@Override
	public int evaluatePartial(final int[] permutation, final int limit) {
		return evaluatePartialNative(ptr, permutation, limit);
	}

	@Override
	public int jobCount() {
		return jobCountNative(ptr);
	}

	@Override
	public BitSet[] itemsNeeded() {
		return Arrays.stream(itemsNeededNative(ptr)).map(job -> {
			val set = new BitSet();
			for (final var i : job) set.set(i);
			return set;
		}).toArray(BitSet[]::new);
	}

	private static native long initialize(final int jobCount, final int machineCount, final int carCount,
	                                      final int carTravelTime, final int itemCount, final int bufferSize,
	                                      final int[] jobLengths, final int[][] itemsNeeded);

	private static native int evaluateNative(final long thisPtr, final int[] permutation);

	private static native int evaluatePartialNative(final long thisPtr, final int[] permutation, final int limit);

	private static native int jobCountNative(final long thisPtr);

	private static native int[][] itemsNeededNative(final long thisPtr);

	private static native void dispose(final long thisPtr);
}
