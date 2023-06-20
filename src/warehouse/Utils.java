package warehouse;

import lombok.val;
import net.mintern.primitive.Primitive;
import net.mintern.primitive.comparators.IntComparator;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Deque;
import java.util.random.RandomGenerator;

import static java.lang.Math.max;
import static java.lang.Math.min;

final class Utils {
	private Utils() {}

//	private static ThreadLocal<ComputeCache> localCache = new ThreadLocal<>();

	static int computeSchedule(final int[] permutation, final JInstance problem, final boolean sortReturns) {
		return computeSchedule(permutation, problem, sortReturns, problem.jobCount());
	}

	@SuppressWarnings({"OverlyNestedMethod", "OverlyComplexMethod", "OverlyLongMethod"})
	static int computeSchedule(final int[] permutation, final JInstance problem, final boolean sortReturns,
	                           final int limit) {
		/*var cache=localCache.get();
		if(cache==null || cache.instance!=problem){
			cache=new ComputeCache(
					problem,
					new ArrayDeque<>(),
					new BitSet(problem.itemCount()),
					new BitSet(problem.itemCount()),
					new PairCache(),
					new int[problem.machineCount()],
					new int[problem.itemCount()],
					new int[problem.itemCount()],
					new int[problem.bufferSize()]
			);
			localCache.set(cache);
		}
		final var sums = cache.sums;
		Arrays.fill(sums,0);
		final var inUseCars = cache.inUseCars;
		inUseCars.clear();
		final var bufferState = cache.bufferState;
		bufferState.clear();
		final var lockTime = cache.lockTime;
		Arrays.fill(lockTime,0);
		final var nexts = cache.nexts;
		final var minLocks = cache.minLocks;
		final var itemsLeft = cache.itemsLeft;*/
		final var sums = new int[problem.machineCount()];
		final var inUseCars = new ArrayDeque<Pair>();
		final var bufferState = new BitSet(problem.itemCount());
		final var lockTime = new int[problem.itemCount()];
		final var nexts = new int[problem.itemCount()];
		final var minLocks = new int[problem.bufferSize()];
		final var itemsLeft = new BitSet(problem.itemCount());
		var carsAvailable = problem.carCount();
		var availableFromTime = 0;

		final var pairCache = new PairCache();
//		final var pairCache=cache.pairCache;
		final IntComparator byNextComparator = (i1, i2) -> Integer.compare(nexts[i2], nexts[i1]);

		for (var ind = 0; ind < limit; ind++) {
			final var job = permutation[ind];
			itemsLeft.clear();
			itemsLeft.or(problem.itemsNeeded()[job]);
			itemsLeft.andNot(bufferState);
			while (!itemsLeft.isEmpty()) {
				while (carsAvailable == 0) {
					final var ret = inUseCars.removeFirst();
					availableFromTime = ret.releaseTime;
					carsAvailable += ret.count;
					pairCache.store(ret);
				}
				final var itemsInBuffer = bufferState.cardinality();
				if (itemsInBuffer < problem.bufferSize()) {
					final var carsUsed = min(carsAvailable,
					                         min(problem.bufferSize() - itemsInBuffer, itemsLeft.cardinality()));
					carsAvailable -= carsUsed;
					pushOrAdd(inUseCars, availableFromTime + problem.carTravelTime(), carsUsed, pairCache);
					transfer(itemsLeft, bufferState, carsUsed);
				} else {
					var minLocksLen = 0;
					var minLockTime = Integer.MAX_VALUE;
//					final var itr = bufferState.stream().iterator();
					for (var item = bufferState.nextSetBit(0); item >= 0; item = bufferState.nextSetBit(item + 1)) {
//					while (itr.hasNext()){
//						final var item = itr.nextInt();
						if (problem.itemsNeeded()[job].get(item)) continue;
						if (lockTime[item] < minLockTime && minLockTime > availableFromTime + problem.carTravelTime()) {
							minLockTime = lockTime[item];
							minLocksLen = 1;
							minLocks[0] = item;
						} else if (lockTime[item] == minLockTime
						           || lockTime[item] <= availableFromTime + problem.carTravelTime()) {
							minLocksLen += 1;
							minLocks[minLocksLen - 1] = item;
							if (lockTime[item] > minLockTime) minLockTime = lockTime[item];
						}
					}
					if (sortReturns) {
						for (var i = 0; i < minLocksLen; i++) {
							final var item = minLocks[i];
							if(nexts[item] <= ind){
								var nxt = -1;
								for (var j = ind + 1; j < permutation.length; j++)
									if (problem.itemsNeeded()[j].get(item)) {
										nxt = j;
										break;
									}
								nexts[item] = nxt == -1 ? permutation.length : nxt;
							}
						}
						Primitive.sort(minLocks, 0, minLocksLen, byNextComparator, false);
					}
					while (!inUseCars.isEmpty()
					       && inUseCars.peekFirst().releaseTime <= minLockTime - problem.carTravelTime()) {
						final var ret = inUseCars.removeFirst();
						availableFromTime = ret.releaseTime;
						carsAvailable += ret.count;
						pairCache.store(ret);
					}
					availableFromTime = max(availableFromTime, minLockTime - problem.carTravelTime());
					final var changesNum = min(carsAvailable, min(minLocksLen, itemsLeft.cardinality()));

					carsAvailable -= changesNum;
					pushOrAdd(inUseCars, availableFromTime + 2 * problem.carTravelTime(), changesNum, pairCache);
					for (var i = 0; i < changesNum; i++) {
						bufferState.clear(minLocks[i]);
					}
					transfer(itemsLeft, bufferState, changesNum);
				}
			}
			var machine = 0;
			int minTime = sums[0];
			for (var i = 0; i < sums.length; i++) {
				if (sums[i] <= availableFromTime + problem.carTravelTime()) {
					machine = i;
					break;
				}
				if (sums[i] < minTime) {
					minTime = sums[i];
					machine = i;
				}
			}
			final var startTime = max(sums[machine], availableFromTime + problem.carTravelTime());
			sums[machine] = startTime + problem.jobLengths()[job];
//			final var itr = problem.itemsNeeded()[job].stream().iterator();
//			while (itr.hasNext()) {
//				final var item = itr.nextInt();
			final var itms = problem.itemsNeeded()[job];
			for (var item = itms.nextSetBit(0); item >= 0; item = itms.nextSetBit(item + 1))
				lockTime[item] = max(lockTime[item], startTime + problem.jobLengths()[job]);

		}
		//noinspection OptionalGetWithoutIsPresent
		return Arrays.stream(sums).max().getAsInt();
	}

	static int[][] computeDistances(final Instance problem) {
		// val n=problem.jobCount();
		val itemsNeeded=problem.itemsNeeded();
		val n=itemsNeeded.length;
		final var dist = new int[n][n];
		for (var i = 0; i < n; i++)
			for (var j = 0; j < n; j++) {
				final var tmp = (BitSet) itemsNeeded[i].clone();
				tmp.xor(itemsNeeded[j]);
				dist[i][j] = tmp.cardinality();
			}
		return dist;
	}

	static void shuffle(final int[] permutation, final RandomGenerator rng) {
		for (int j = permutation.length - 1; j > 0; j--) {
			final var ind = rng.nextInt(permutation.length);
			final var tmp = permutation[j];
			permutation[j] = permutation[ind];
			permutation[ind] = tmp;
		}
	}

	static void shuffle(final int[] permutation, final int from, final int to, final RandomGenerator rng) {
		for (int j = to - 1; j > from; j--) {
			final var ind = rng.nextInt(from, to);
			final var tmp = permutation[j];
			permutation[j] = permutation[ind];
			permutation[ind] = tmp;
		}
	}

	static void shuffle(final IntArrayView permutation, final RandomGenerator rng) {
		for (int j = permutation.size() - 1; j > 0; j--) {
			final var ind = rng.nextInt(permutation.size());
			final var tmp = permutation.get(j);
			permutation.set(j, permutation.get(ind));
			permutation.set(ind, tmp);
		}
	}

	static boolean isPermutation(final int[] array) {
		//noinspection LabeledStatement
outer:
		for (var i = 0; i < array.length; i++) {
			for (final int k : array)
				if (k == i) //noinspection ContinueStatementWithLabel
					continue outer;
			return false;
		}
		return true;
	}

	static double damerauLevenshteinDistance(final int[] first, final int[] second) {
		assert first.length == second.length;
		val n = first.length;
		val SUBSTITUTION_COST = 0.5;
		val INSERTION_COST = 0.5;
		val DELETION_COST = 0.5;
		val TRANSPOSITION_COST = 1.0;

		val da = new int[n];
		val d = new double[n + 2][n + 2];
		d[0][0] = 2 * n;
		for (var i = 0; i <= n; i++) {
			d[i + 1][0] = 2 * n;
			d[i + 1][1] = (double) i / 2;
			d[0][i + 1] = 2 * n;
			d[1][i + 1] = (double) i / 2;
		}
		for (var i = 0; i < n; i++) {
			var db = 0;
			for (var j = 0; j < n; j++) {
				val k = da[second[j]];
				val l = db;
				final double cost;
				if (first[i] == second[j]) {
					cost = 0.0;
					db = j;
				} else {
					cost = SUBSTITUTION_COST;
				}
				d[i + 2][j + 2] = min(
						min(d[i + 1][j + 1] + cost,
						    d[i + 2][j + 1] + INSERTION_COST),
						min(d[i + 1][j + 2] + DELETION_COST,
						    d[k + 1][l + 1] + (i - k - 1) + TRANSPOSITION_COST + (j - l - 1))
				                     );
			}
			da[first[i]] = i;
		}
		return d[n + 1][n + 1];
	}

	private static void pushOrAdd(final Deque<Pair> queue, final int time, final int count, final PairCache cache) {
		if (!queue.isEmpty() && queue.peekLast().releaseTime == time) {
			queue.peekLast().count += count;
		} else queue.addLast(cache.construct(time, count));
	}

	private static void transfer(final BitSet from, final BitSet to, final int count) {
//		from.stream().limit(count).peek(to::set).forEach(from::clear);
		var prev = 0;
		for (var i = 0; i < count; i++) {
			final var itm = from.nextSetBit(prev);
			assert itm != -1;
			from.clear(itm);
			to.set(itm);
			prev = itm + 1;
		}
	}


	private static class Pair {
		int releaseTime;
		int count;

		Pair(final int releaseTime, final int count) {
			this.releaseTime = releaseTime;
			this.count = count;
		}
	}

	private static class PairCache {
		private final ArrayDeque<Pair> cache;

		PairCache() {
			cache = new ArrayDeque<>();
		}

		PairCache(final int size) {
			cache = new ArrayDeque<>(size);
		}

		Pair construct(final int time, final int count) {
			if (cache.isEmpty()) return new Pair(time, count);
			final var result = cache.pollLast();
			result.releaseTime = time;
			result.count = count;
			return result;
		}

		void store(final Pair pair) {
			cache.addLast(pair);
		}
	}

	private record ComputeCache(
			Instance instance,
			ArrayDeque<Pair> inUseCars,
			BitSet bufferState,
			BitSet itemsLeft,
			PairCache pairCache,
			int[] sums,
			int[] lockTime,
			int[] nexts,
			int[] minLocks
	) {}

}
