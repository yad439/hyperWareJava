package warehouse;

import lombok.Setter;
import lombok.val;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

@SuppressWarnings({"ClassWithTooManyMethods", "OverlyComplexClass"})
final class Heuristics {
	private final RandomGenerator rng;
	private Instance problem = null;
	private int[][] distances = null;
	@Setter
	private double intensity = 0;
	@Setter
	private double depth = 0;

//	public static ArrayList<ArrayList<Integer>> crossScores=new ArrayList<>();

	Heuristics(final RandomGenerator rng) {
		this.rng = rng;
	}

	private static void swap(final int[] destination, final int i1, final int i2) {
		final var tmp = destination[i1];
		destination[i1] = destination[i2];
		destination[i2] = tmp;
	}

	private static void move(final int[] destination, final int from, final int to) {
		final var tmp = destination[from];
		if (to < from) System.arraycopy(destination, to, destination, to + 1, from - to);
		else System.arraycopy(destination, from + 1, destination, from, to - from);
		destination[to] = tmp;
	}

	private static void copyInsert(final int[] source, final int[] destination, final int[] positions,
	                               final int[] values) {
		assert positions.length == values.length;
		System.arraycopy(source, 0, destination, 0, positions[0]);
		for (var i = 0; i < positions.length - 1; i++) {
			destination[positions[i]] = values[i];
			System.arraycopy(source, positions[i] + 1, destination, positions[i] + 1,
			                 positions[i + 1] - positions[i] - 1);
		}
		destination[positions[positions.length - 1]] = values[values.length - 1];
		System.arraycopy(source, positions[positions.length - 1] + 1, destination, positions[positions.length - 1] + 1,
		                 source.length - positions[positions.length - 1] - 1);
	}

	private static int find(final int[] array, final int value) {
		for (var i = 0; i < array.length; i++) if (array[i] == value) return i;
		return -1;
	}

	void setProblem(final Instance problem) {
		this.problem = problem;
		distances = Utils.computeDistances(problem);
	}

	int randomSwap(final int[] source, final int[] destination, final IntRef callCounter) {
		//noinspection ArrayEquality
		if (source != destination) System.arraycopy(source, 0, destination, 0, source.length);
		randomSwap(destination);
		callCounter.increment();
		return problem.evaluate(destination);
	}

	int randomMove(final int[] source, final int[] destination, final IntRef callCounter) {
		final var from = rng.nextInt(source.length);
		int to;
		do to = rng.nextInt(source.length);
		while (to == from);
		//noinspection ArrayEquality
		if (source == destination) {
			move(source, from, to);
		} else {
			final var minimum = Math.min(from, to);
			final var maximum = Math.max(from, to);
			System.arraycopy(source, 0, destination, 0, minimum);
			if (to < from) System.arraycopy(source, to, destination, to + 1, from - to);
			else System.arraycopy(source, from + 1, destination, from, to - from);
			System.arraycopy(source, maximum + 1, destination, maximum + 1, source.length - maximum - 1);
			destination[to] = source[from];
		}
		callCounter.increment();
		return problem.evaluate(destination);
	}

	int multipleSwap(final int[] source, final int[] destination, final IntRef callCounter) {
		//noinspection ArrayEquality
		if (source != destination) System.arraycopy(source, 0, destination, 0, source.length);
		for (var i = 0; i < intensity * source.length; i++) {
			randomSwap(destination);
		}
		callCounter.increment();
		return problem.evaluate(destination);
	}

	int multipleMove(final int[] source, final int[] destination, final IntRef callCounter) {
		//noinspection ArrayEquality
		if (source != destination) System.arraycopy(source, 0, destination, 0, source.length);
		for (var i = 0; i < intensity * source.length; i++) {
			final var from = rng.nextInt(source.length);
			int to;
			do {
				to = rng.nextInt(source.length);
			} while (to == from);
			move(destination, from, to);
		}
		callCounter.increment();
		return problem.evaluate(destination);
	}

	int shuffle(final int[] source, final int[] destination, final IntRef callCounter) {
		//noinspection ArrayEquality
		if (source != destination) System.arraycopy(source, 0, destination, 0, source.length);
		Utils.shuffle(destination, rng);
		callCounter.increment();
		return problem.evaluate(destination);
	}

	int shufflePart(final int[] source, final int[] destination, final IntRef callCounter) {
		//noinspection ArrayEquality
		if (source != destination) System.arraycopy(source, 0, destination, 0, source.length);
		final var positions = IntStream.range(0, source.length).filter(t -> rng.nextDouble() < intensity).toArray();
		if (positions.length <= 1) {
			callCounter.increment();
			return problem.evaluate(destination);
		}
		Utils.shuffle(new PositionedIntArrayView(destination, positions), rng);
		callCounter.increment();
		return problem.evaluate(destination);
	}

	int shuffleSpan(final int[] source, final int[] destination, final IntRef callCounter) {
		//noinspection ArrayEquality
		if (source != destination) System.arraycopy(source, 0, destination, 0, source.length);
		@SuppressWarnings("NumericCastThatLosesPrecision") final var len = (int) Math.round(intensity * source.length);
		if (len == 0) {
			callCounter.increment();
			return problem.evaluate(destination);
		}
		if (len == destination.length) return shuffle(destination, destination, callCounter);
		final var from = rng.nextInt(destination.length - len + 1);
		Utils.shuffle(destination, from, from + len, rng);
		callCounter.increment();
		return problem.evaluate(destination);
	}

	int itemBased(final int[] source, final int[] destination, final IntRef callCounter) {
		//noinspection ArrayEquality
		if (source != destination) System.arraycopy(source, 0, destination, 0, source.length);
		final var positions = IntStream.range(0, source.length).filter(t -> rng.nextDouble() < intensity).toArray();
		if (positions.length <= 1) {
			callCounter.increment();
			return problem.evaluate(destination);
		}
		itemBased(new PositionedIntArrayView(destination, positions));
		callCounter.increment();
		return problem.evaluate(destination);
	}

	int itemBasedSpan(final int[] source, final int[] destination, final IntRef callCounter) {
		//noinspection ArrayEquality
		if (source != destination) System.arraycopy(source, 0, destination, 0, source.length);
		@SuppressWarnings("NumericCastThatLosesPrecision") final var len = (int) Math.round(intensity * source.length);
		if (len == 0) {
			callCounter.increment();
			return problem.evaluate(destination);
		}
		final var from = rng.nextInt(destination.length - len + 1);
		itemBased(new IntArraySpan(destination, from, len));
		callCounter.increment();
		return problem.evaluate(destination);
	}

	private void itemBased(final IntArrayView view) {
		for (var i = 0; i < view.size(); i++) {
			if (i == 0 && view.getRealIndex(0) == 0) {
				final var valI = rng.nextInt(view.size());
				if (valI != 0) view.swap(valI, 0);
				continue;
			}
			int minDist = Integer.MAX_VALUE;
			int minInd = -1;
			for (var j = i; j < view.size(); j++)
				if (distances[view.get(i, -1)][view.get(j)] < minDist) {
					minDist = distances[view.get(i, -1)][view.get(j)];
					minInd = j;
				}
			if (minInd != i) view.swap(minInd, i);
		}
	}

	int localSearch(final int[] source, final int[] destination, final IntRef callCounter) {
		//noinspection ArrayEquality
		if (source != destination) System.arraycopy(source, 0, destination, 0, source.length);
		return localSearch(new FullIntArrayView(destination), callCounter);
	}

	int localSearchFirst(final int[] source, final int[] destination, final IntRef callCounter) {
		//noinspection ArrayEquality
		if (source != destination) System.arraycopy(source, 0, destination, 0, source.length);
		return localSearchFirst(new FullIntArrayView(destination), callCounter);
	}

	private int localSearch(final IntArrayView solution, final IntRef callCounter) {
		callCounter.increment();
		var currentValue = problem.evaluate(solution.getBuffer());
		final var maxIter = Math.toIntExact(Math.round(depth * solution.size()));
		for (var t = 0; t < maxIter; t++) {
			var bestValue = Integer.MAX_VALUE;
			boolean bestIsSwap = false;
			int bestI = -1, bestJ = -1;
			for (var i = 0; i < solution.size(); i++)
				for (var j = 0; j < i; j++) {
					solution.swap(i, j);
					callCounter.increment();
					final var newValue = problem.evaluate(solution.getBuffer());
					solution.swap(i, j);
					if (newValue < bestValue) {
						bestValue = newValue;
						bestIsSwap = true;
						bestI = i;
						bestJ = j;
					}
				}
			for (var i = 0; i < solution.size(); i++)
				for (var j = 0; j < solution.size(); j++)
					if (i != j) {
						solution.move(i, j);
						callCounter.increment();
						final var newValue = problem.evaluate(solution.getBuffer());
						solution.move(j, i);
						if (newValue < bestValue) {
							bestValue = newValue;
							bestIsSwap = false;
							bestI = i;
							bestJ = j;
						}
					}
			if (bestValue < currentValue) {
				if (bestIsSwap) {
					solution.swap(bestI, bestJ);
				} else {
					solution.move(bestI, bestJ);
				}
				currentValue = bestValue;
			} else break;
		}
		return currentValue;
	}

	private int localSearchFirst(final IntArrayView solution, final IntRef callCounter) {
		callCounter.increment();
		var currentValue = problem.evaluate(solution.getBuffer());
		final var maxIter = Math.toIntExact(Math.round(depth * solution.size()));
outer:
		for (var t = 0; t < maxIter; t++) {
			for (var i = 1; i < solution.size(); i++)
				for (var j = 0; j < i; j++) {
					solution.swap(i, j);
					callCounter.increment();
					final var newValue = problem.evaluate(solution.getBuffer());
					if (newValue < currentValue) {
						currentValue = newValue;
						//noinspection ContinueStatementWithLabel
						continue outer;
					} else solution.swap(i, j);
				}
			for (var i = 0; i < solution.size(); i++)
				for (var j = 0; j < solution.size(); j++)
					if (i != j) {
						solution.move(i, j);
						callCounter.increment();
						final var newValue = problem.evaluate(solution.getBuffer());
						if (newValue < currentValue) {
							currentValue = newValue;
							//noinspection ContinueStatementWithLabel
							continue outer;
						} else solution.move(j, i);
					}
			break;
		}
		return currentValue;
	}

	int localSearchPart(final int[] source, final int[] destination, final IntRef callCounter) {
		//noinspection ArrayEquality
		if (source != destination) System.arraycopy(source, 0, destination, 0, source.length);
		final var positions = IntStream.range(0, source.length).filter(t -> rng.nextDouble() < intensity).toArray();
		if (positions.length <= 1) {
			callCounter.increment();
			return problem.evaluate(destination);
		}
		return localSearch(new PositionedIntArrayView(destination, positions), callCounter);
	}

	int localSearchPartFirst(final int[] source, final int[] destination, final IntRef callCounter) {
		//noinspection ArrayEquality
		if (source != destination) System.arraycopy(source, 0, destination, 0, source.length);
		final var positions = IntStream.range(0, source.length).filter(t -> rng.nextDouble() < intensity).toArray();
		if (positions.length <= 1) {
			callCounter.increment();
			return problem.evaluate(destination);
		}
		return localSearchFirst(new PositionedIntArrayView(destination, positions), callCounter);
	}

	int localSearchSpan(final int[] source, final int[] destination, final IntRef callCounter) {
		//noinspection ArrayEquality
		if (source != destination) System.arraycopy(source, 0, destination, 0, source.length);
		final var len = Math.toIntExact(Math.round(intensity * source.length));
		if (len == 0) {
			callCounter.increment();
			return problem.evaluate(destination);
		}
		if (len == destination.length) return localSearch(destination, destination, callCounter);
		final var from = rng.nextInt(destination.length - len + 1);
		return localSearch(new IntArraySpan(destination, from, len), callCounter);
	}

	int localSearchSpanFirst(final int[] source, final int[] destination, final IntRef callCounter) {
		//noinspection ArrayEquality
		if (source != destination) System.arraycopy(source, 0, destination, 0, source.length);
		final var len = Math.toIntExact(Math.round(intensity * source.length));
		if (len == 0) {
			callCounter.increment();
			return problem.evaluate(destination);
		}
		if (len == destination.length) return localSearchFirst(destination, destination, callCounter);
		final var from = rng.nextInt(destination.length - len + 1);
		return localSearchFirst(new IntArraySpan(destination, from, len), callCounter);
	}

	int randomLocalSearch(final int[] source, final int[] destination, final IntRef callCounter) {
		//noinspection ArrayEquality
		if (source != destination) System.arraycopy(source, 0, destination, 0, source.length);
		callCounter.increment();
		var currentValue = problem.evaluate(destination);
		//noinspection NumericCastThatLosesPrecision
		final var maxIter = (int) Math.round(depth * source.length);
		final var neighborhoodSize = depth * (2 * destination.length * destination.length);
		for (var t = 0; t < maxIter; t++) {
			var bestValue = Integer.MAX_VALUE;
			boolean bestIsSwap = false;
			int bestI = -1, bestJ = -1;
			for (var k = 0; k < neighborhoodSize; k++) {
				final var i = rng.nextInt(destination.length);
				int j;
				do {
					j = rng.nextInt(destination.length);
				} while (j == i);
				if (rng.nextBoolean()) {
					swap(destination, i, j);
					callCounter.increment();
					final var newValue = problem.evaluate(destination);
					swap(destination, i, j);
					if (newValue < bestValue) {
						bestValue = newValue;
						bestIsSwap = true;
						bestI = i;
						bestJ = j;
					}
				} else {
					move(destination, i, j);
					callCounter.increment();
					final var newValue = problem.evaluate(destination);
					move(destination, j, i);
					if (newValue < bestValue) {
						bestValue = newValue;
						bestIsSwap = false;
						bestI = i;
						bestJ = j;
					}
				}
			}
			if (bestValue < currentValue) {
				if (bestIsSwap) {
					swap(destination, bestI, bestJ);
				} else {
					move(destination, bestI, bestJ);
				}
				currentValue = bestValue;
			} else break;
		}
		return currentValue;
	}

	int randomLocalSearchFirst(final int[] source, final int[] destination, final IntRef callCounter) {
		System.arraycopy(source, 0, destination, 0, source.length);
		callCounter.increment();
		var currentValue = problem.evaluate(destination);
		final var maxIter = Math.toIntExact(Math.round(depth * source.length));
		final var neighborhoodSize = depth * (2 * destination.length * destination.length);
outer:
		for (var t = 0; t < maxIter; t++) {
			for (var k = 0; k < neighborhoodSize; k++) {
				final var i = rng.nextInt(destination.length);
				int j;
				do {
					j = rng.nextInt(destination.length);
				} while (j == i);
				if (rng.nextBoolean()) {
					swap(destination, i, j);
					callCounter.increment();
					final var newValue = problem.evaluate(destination);
					if (newValue < currentValue) {
						currentValue = newValue;
						//noinspection ContinueStatementWithLabel
						continue outer;
					} else swap(destination, i, j);
				} else {
					move(destination, i, j);
					callCounter.increment();
					final var newValue = problem.evaluate(destination);
					if (newValue < currentValue) {
						currentValue = newValue;
						//noinspection ContinueStatementWithLabel
						continue outer;
					} else move(destination, j, i);
				}
			}
			break;
		}
		return currentValue;
	}

	private int greedyConstruct(final IntArrayView permutation) {
		var callApprox = 0.0;
		for (var i = 0; i < permutation.size() - 1; i++) {
			var minInd = i;
			callApprox += (double) permutation.getRealIndex(i) / permutation.getBuffer().length;
			var minLen = problem.evaluatePartial(permutation.getBuffer(), permutation.getRealIndex(i));
			for (var j = i + 1; j < permutation.size(); j++) {
				permutation.swap(i, j);
				callApprox += (double) permutation.getRealIndex(i) / permutation.getBuffer().length;
				final var currLen = problem.evaluatePartial(permutation.getBuffer(),
				                                            permutation.getRealIndex(i));
				permutation.swap(i, j);
				if (currLen < minLen) {
					minLen = currLen;
					minInd = j;
				}
			}
			if (minInd != i) permutation.swap(i, minInd);
		}
		//noinspection NumericCastThatLosesPrecision
		return (int) callApprox;
	}

	int greedyConstruct(final int[] source, final int[] destination, final IntRef callCounter) {
		//noinspection ArrayEquality
		if (source != destination) System.arraycopy(source, 0, destination, 0, source.length);
		final var positions = IntStream.range(0, source.length).filter(t -> rng.nextDouble() < intensity).toArray();
		if (positions.length <= 1) {
			callCounter.increment();
			return problem.evaluate(destination);
		}
		final var evals = greedyConstruct(new PositionedIntArrayView(destination, positions));
		callCounter.add(evals + 1);
		return problem.evaluate(destination);
	}

	int greedyConstructSpan(final int[] source, final int[] destination, final IntRef callCounter) {
		//noinspection ArrayEquality
		if (source != destination) System.arraycopy(source, 0, destination, 0, source.length);
		final var len = Math.toIntExact(Math.round(intensity * source.length));
		if (len == 0) {
			callCounter.increment();
			return problem.evaluate(destination);
		}
		final var from = rng.nextInt(destination.length - len + 1);
		final var evals = greedyConstruct(new IntArraySpan(destination, from, len));
		callCounter.add(evals + 1);
		return problem.evaluate(destination);
	}

	int linKernighan(final int[] source, final int[] destination, final IntRef callCounter) {
		val current = source.clone();
		//noinspection ArrayEquality
		if (source != destination) System.arraycopy(source, 0, destination, 0, source.length);
		var score = problem.evaluate(destination);
		callCounter.increment();
		val iterations = Math.toIntExact(Math.round(source.length * depth));
		val neighSize = Math.toIntExact(Math.round(source.length * source.length * 0.1));
		for (var t = 0; t < iterations; t++) {
			int bestI = -1, bestJ = -1, bestScore = Integer.MAX_VALUE;
			boolean bestSwap = false;

			for (var neighIter = 0; neighIter < neighSize; neighIter++) {
				val i = rng.nextInt(current.length);
				int j;
				do {
					j = rng.nextInt(current.length);
				} while (j == i);
				if (rng.nextBoolean()) {
					swap(current, i, j);
					callCounter.increment();
					val newValue = problem.evaluate(current);
					if (newValue < bestScore) {
						bestScore = newValue;
						bestI = i;
						bestJ = j;
						bestSwap = true;
					}
					swap(current, i, j);
				} else {
					move(current, i, j);
					callCounter.increment();
					val newValue = problem.evaluate(current);
					if (newValue < bestScore) {
						bestScore = newValue;
						bestI = i;
						bestJ = j;
						bestSwap = false;
					}
					move(current, j, i);
				}
			}
			if (bestSwap) swap(current, bestI, bestJ);
			else move(current, bestI, bestJ);

			if (bestScore < score) {
				score = bestScore;
				System.arraycopy(current, 0, destination, 0, current.length);
			}
		}
		return score;
	}

	int linKernighanIterative(final int[] source, final int[] destination, final IntRef callCounter) {
		//noinspection ArrayEquality
		if (source != destination) System.arraycopy(source, 0, destination, 0, source.length);
		val current = new int[destination.length];
		val iterations = Math.toIntExact(Math.round(source.length * depth));
		val neighSize = Math.toIntExact(Math.round(source.length * source.length * 0.1));
		callCounter.increment();
		var score = problem.evaluate(destination);
		for (var t1 = 0; t1 < iterations; t1++) {
			System.arraycopy(destination, 0, current, 0, destination.length);
			for (var t2 = 0; t2 < iterations; t2++) {
				int bestI = -1, bestJ = -1, bestScore = Integer.MAX_VALUE;
				boolean bestSwap = false;

				for (var neighIter = 0; neighIter < neighSize; neighIter++) {
					val i = rng.nextInt(current.length);
					int j;
					do {
						j = rng.nextInt(current.length);
					} while (j == i);
					if (rng.nextBoolean()) {
						swap(current, i, j);
						callCounter.increment();
						val newValue = problem.evaluate(current);
						if (newValue < bestScore) {
							bestScore = newValue;
							bestI = i;
							bestJ = j;
							bestSwap = true;
						}
						swap(current, i, j);
					} else {
						move(current, i, j);
						callCounter.increment();
						val newValue = problem.evaluate(current);
						if (newValue < bestScore) {
							bestScore = newValue;
							bestI = i;
							bestJ = j;
							bestSwap = false;
						}
						move(current, j, i);
					}
				}
				if (bestSwap) swap(current, bestI, bestJ);
				else move(current, bestI, bestJ);

				if (bestScore < score) {
					score = bestScore;
					System.arraycopy(current, 0, destination, 0, current.length);
				}
			}
		}
		return score;
	}

	int annealingLS(final int[] source, final int[] destination, final IntRef callCounter) {
		//noinspection ArrayEquality
		if (source != destination) System.arraycopy(source, 0, destination, 0, source.length);
		val current = destination.clone();
		var score = problem.evaluate(current);
		var bestScore = score;
		callCounter.increment();
		val iterations = Math.toIntExact(Math.round(source.length * source.length * depth));
		var temp = score * intensity / 4;
		val power = Math.pow((-temp * Math.log(1.0e-3)), (-1.0 / iterations));
		for (var t = 0; t < iterations; t++) {
			final var i = rng.nextInt(current.length);
			int j;
			do {
				j = rng.nextInt(current.length);
			} while (j == i);
			if (rng.nextBoolean()) {
				swap(current, i, j);
				callCounter.increment();
				final var newValue = problem.evaluate(current);
				if (rng.nextDouble() < Math.exp((newValue - score) / temp)) score = newValue;
				else swap(current, i, j);
			} else {
				move(current, i, j);
				callCounter.increment();
				final var newValue = problem.evaluate(current);
				if (rng.nextDouble() < Math.exp((newValue - score) / temp)) score = newValue;
				else move(current, j, i);
			}
			if (score < bestScore) {
				bestScore = score;
				System.arraycopy(current, 0, destination, 0, current.length);
			}
			temp *= power;
		}
		return bestScore;
	}

	int annealingMutation(final int[] source, final int[] destination, final IntRef callCounter) {
		//noinspection ArrayEquality
		if (source != destination) System.arraycopy(source, 0, destination, 0, source.length);
		var score = problem.evaluate(destination);
		callCounter.increment();
		val iterations = Math.toIntExact(Math.round(source.length * 2 * depth));
		var temp = score * intensity / 2;
		val power = Math.pow((-temp * Math.log(1.0e-2)), (-1.0 / iterations));
		for (var t = 0; t < iterations; t++) {
			final var i = rng.nextInt(destination.length);
			int j;
			do {
				j = rng.nextInt(destination.length);
			} while (j == i);
			if (rng.nextBoolean()) {
				swap(destination, i, j);
				callCounter.increment();
				val newValue = problem.evaluate(destination);
				if (rng.nextDouble() < Math.exp((newValue - score) / temp)) score = newValue;
				else swap(destination, i, j);
			} else {
				move(destination, i, j);
				callCounter.increment();
				val newValue = problem.evaluate(destination);
				if (rng.nextDouble() < Math.exp((newValue - score) / temp)) score = newValue;
				else move(destination, j, i);
			}
			temp *= power;
		}
		return score;
	}

	int ox1Cross(final int[] parent1Candidate, final int[] parent2Candidate, final int[] child,
	             final IntRef callCounter) {
		assert parent1Candidate.length == parent2Candidate.length;
		assert parent1Candidate.length == child.length;
		@SuppressWarnings("ArrayEquality") final var parent1 =
				parent1Candidate != child ? parent1Candidate : parent1Candidate.clone();
		@SuppressWarnings("ArrayEquality") final var parent2 =
				parent2Candidate != child ? parent2Candidate : parent2Candidate.clone();
		final var interval = randomInterval(child.length);
		System.arraycopy(parent1, interval.from, child, interval.from, interval.to - interval.from);
		final var copied = new BitSet(parent1.length);
		for (var i = interval.from; i < interval.to; i++) copied.set(parent1[i]);
		int j = interval.from != 0 ? 0 : interval.to;
		for (final var k : parent2)
			if (!copied.get(k)) {
				child[j] = k;
				j++;
				if (j == interval.from) j = interval.to;
			}
		callCounter.increment();
		return problem.evaluate(child);
	}

	int pmxCross(final int[] parent1Candidate, final int[] parent2Candidate, final int[] child,
	             final IntRef callCounter) {
		assert parent1Candidate.length == parent2Candidate.length;
		assert parent1Candidate.length == child.length;
		@SuppressWarnings("ArrayEquality") final var parent1 =
				parent1Candidate != child ? parent1Candidate : parent1Candidate.clone();
		@SuppressWarnings("ArrayEquality") final var parent2 =
				parent2Candidate != child ? parent2Candidate : parent2Candidate.clone();
		final var interval = randomInterval(parent1.length);
		System.arraycopy(parent1, interval.from(), child, interval.from(), interval.to() - interval.from());
		final var copied = new BitSet(parent1.length);
		for (var i = interval.from; i < interval.to; i++) copied.set(parent1[i]);
		for (var i = interval.from(); i < interval.to(); i++) {
			if (copied.get(parent2[i])) continue;
			var index = i;
			var cont = true;
			while (cont) {
				final var val2 = parent1[index];
				final var index2 = find(parent2, val2);
				if (interval.from() <= index2 && index2 < interval.to()) index = index2;
				else {
					child[index2] = parent2[i];
					copied.set(parent2[i]);
					cont = false;
				}
			}
		}
		for (var i = 0; i < child.length; i++)
			if (!copied.get(parent2[i])) child[i] = parent2[i];
		callCounter.increment();
		return problem.evaluate(child);
	}

	int cycleCross(final int[] parent1Candidate, final int[] parent2Candidate, final int[] child,
	               final IntRef callCounter) {
		assert parent1Candidate.length == parent2Candidate.length;
		assert parent1Candidate.length == child.length;
		@SuppressWarnings("ArrayEquality") final var parent1 =
				parent1Candidate != child ? parent1Candidate : parent1Candidate.clone();
		@SuppressWarnings("ArrayEquality") final var parent2 =
				parent2Candidate != child ? parent2Candidate : parent2Candidate.clone();
		final var copied = new BitSet(parent1.length);
		var takeFirst = rng.nextBoolean();
		for (var i = 0; i < child.length; i++) {
			if (copied.get(i)) continue;
			child[i] = takeFirst ? parent1[i] : parent2[i];
			copied.set(i);
			var ind = find(parent1, parent2[i]);
			while (ind != i) {
				assert !copied.get(ind);
				child[ind] = takeFirst ? parent1[ind] : parent2[ind];
				copied.set(ind);
				ind = find(parent1, parent2[ind]);
			}
			takeFirst = !takeFirst;
		}
		callCounter.increment();
		return problem.evaluate(child);
	}

	int oneXCross(final int[] parent1Candidate, final int[] parent2Candidate, final int[] child,
	              final IntRef callCounter) {
		assert parent1Candidate.length == parent2Candidate.length;
		assert parent1Candidate.length == child.length;
		@SuppressWarnings("ArrayEquality") final var parent1 =
				parent1Candidate != child ? parent1Candidate : parent1Candidate.clone();
		@SuppressWarnings("ArrayEquality") final var parent2 =
				parent2Candidate != child ? parent2Candidate : parent2Candidate.clone();
		final var copied = new BitSet(parent1.length);
		final var point = rng.nextInt(1, child.length);
		for (var i = 0; i < point; i++) {
			child[i] = parent1[i];
			copied.set(parent1[i]);
		}
		var j = point;
		for (var i = 0; i < parent2.length && j < child.length; i++) {
			if (!copied.get(parent2[i])) {
				child[j] = parent2[i];
				j++;
			}
		}
		callCounter.increment();
		return problem.evaluate(child);
	}

	int ppxCross(final int[] parent1Candidate, final int[] parent2Candidate, final int[] child,
	             final IntRef callCounter) {
		assert parent1Candidate.length == parent2Candidate.length;
		assert parent1Candidate.length == child.length;
		@SuppressWarnings("ArrayEquality") final var parent1 =
				parent1Candidate != child ? parent1Candidate : parent1Candidate.clone();
		@SuppressWarnings("ArrayEquality") final var parent2 =
				parent2Candidate != child ? parent2Candidate : parent2Candidate.clone();
		final var copied = new BitSet(parent1.length);
		for (var i = 0; i < child.length; i++) {
			final var donor = rng.nextBoolean() ? parent1 : parent2;
			var ind = 0;
			while (copied.get(donor[ind])) ind++;
			child[i] = donor[ind];
			copied.set(donor[ind]);
		}
		callCounter.increment();
		return problem.evaluate(child);
	}

	@Deprecated
	int sortCross0(final int[] parent1Candidate, final int[] parent2Candidate, final int[] child,
	               final IntRef callCounter) {
		assert parent1Candidate.length == parent2Candidate.length;
		assert parent1Candidate.length == child.length;
		@SuppressWarnings("ArrayEquality") final var parent2 =
				parent2Candidate != child ? parent2Candidate : parent2Candidate.clone();
		callCounter.increment();
		var bestScore = Integer.MAX_VALUE;
		final var currentSolution = parent1Candidate.clone();
		final var parent2Inverse = new int[parent2.length];
		for (var i = 0; i < parent2Candidate.length; i++) parent2Inverse[parent2[i]] = i;
//		val scores=new ArrayList<Integer>();
//		scores.add(Utils.computeSchedule(currentSolution,problem,true));
		while (true) {
			var canStop = true;
			var outOfOrder = 0;
			for (var i = 0; i < currentSolution.length; i++)
				if (currentSolution[i] != parent2[i])
					if (currentSolution[parent2Inverse[currentSolution[i]]] == parent2[i]) {
						swap(currentSolution, i, parent2Inverse[currentSolution[i]]);
//						cnt++;
						callCounter.increment();
						final var newScore = problem.evaluate(currentSolution);
//						scores.add(newScore);
						if (newScore < bestScore) {
							bestScore = newScore;
							System.arraycopy(currentSolution, 0, child, 0, currentSolution.length);
						}
					} else {
						canStop = false;
						outOfOrder = i;
					}
			if (canStop) {
				assert Arrays.equals(currentSolution, parent2);
				break;
			}
			swap(currentSolution, outOfOrder, parent2Inverse[currentSolution[outOfOrder]]);
			callCounter.increment();
			final var newScore = problem.evaluate(currentSolution);
//			scores.add(newScore);
			if (newScore < bestScore) {
				bestScore = newScore;
				System.arraycopy(currentSolution, 0, child, 0, currentSolution.length);
			}
		}
//		crossScores.add(scores);
		return bestScore;
	}

	int sortCross(final int[] parent1, final int[] parent2, final int[] child, final IntRef callCounter) {
		assert parent1.length == parent2.length;
		assert parent1.length == child.length;
		var bestScore = Integer.MAX_VALUE;
		val currentSolution = parent1.clone();
		val sortSequence = computeSortSequence(parent1, parent2);
		val sequenceLen = sortSequence.size();
		@SuppressWarnings("NumericCastThatLosesPrecision") val allowedIteration = (int) ((sequenceLen - 1) * intensity
		                                                                                 / 2);
		val upperBound = sequenceLen - allowedIteration;
		var iteration = 0;
		for (final var change : sortSequence) {
			if (iteration >= upperBound) break;
			apply(currentSolution, change);
			if (allowedIteration <= iteration) {
				val score = problem.evaluate(currentSolution);
				callCounter.increment();
				if (score < bestScore) {
					bestScore = score;
					System.arraycopy(currentSolution, 0, child, 0, currentSolution.length);
				}
			}
			iteration++;
		}
		return bestScore;
	}

	int randomSortCross(final int[] parent1, final int[] parent2, final int[] child, final IntRef callCounter) {
		assert parent1.length == parent2.length;
		assert parent1.length == child.length;
		val currentSolution = parent1.clone();
		val sortSequence = computeSortSequence(parent1, parent2);
		val sequenceLen = sortSequence.size();
		@SuppressWarnings("NumericCastThatLosesPrecision") val allowedIteration = (int) ((sequenceLen - 1) * intensity
		                                                                                 / 2);
		val upperBound = sequenceLen - allowedIteration;
		val choice = rng.nextInt(allowedIteration, upperBound);
		var iteration = 0;
		for (final var change : sortSequence) {
			apply(currentSolution, change);
			if (iteration == choice) {
				System.arraycopy(currentSolution, 0, child, 0, currentSolution.length);
				callCounter.increment();
				return problem.evaluate(child);
			}
			iteration++;
		}
		assert false;
		return -1;
	}

	private static Collection<Change> computeSortSequence(final int[] source, final int[] destination) {
		assert source.length == destination.length;
		val result = new ArrayList<Change>(source.length);
		val inverseDestination = new int[destination.length];
		for (var i = 0; i < destination.length; i++) inverseDestination[destination[i]] = i;
		val currentPermutation = source.clone();
		while (true) {
			var canStop = true;
			var outOfOrder = 0;
			for (var i = 0; i < currentPermutation.length; i++)
				if (currentPermutation[i] != destination[i])
					if (currentPermutation[inverseDestination[currentPermutation[i]]] == destination[i]) {
						swap(currentPermutation, i, inverseDestination[currentPermutation[i]]);
						result.add(new Change(Change.Type.SWAP, i, inverseDestination[currentPermutation[i]]));
					} else {
						canStop = false;
						outOfOrder = i;
					}
			if (canStop) {
				assert Arrays.equals(currentPermutation, destination);
				break;
			}
			swap(currentPermutation, outOfOrder, inverseDestination[currentPermutation[outOfOrder]]);
			result.add(new Change(Change.Type.SWAP, outOfOrder, inverseDestination[currentPermutation[outOfOrder]]));
		}
		return result;
	}

	/*public void ps(){
		val done=steps.stream().mapToInt(Pair::done).boxed().toList();
		System.out.println(done.stream().min(Integer::compareTo).get());
		System.out.println(done.stream().max(Integer::compareTo).get());
		System.out.println(done.stream().mapToInt(Integer::intValue).average().getAsDouble());
	}*/

	private void randomSwap(final int[] destination) {
		final var i1 = rng.nextInt(destination.length);
		int i2;
		do {
			i2 = rng.nextInt(destination.length);
		} while (i2 == i1);
		swap(destination, i1, i2);
	}

	private static void apply(final int[] array, final Change change) {
		switch (change.type()) {
			case SWAP -> swap(array, change.i(), change.j());
			case MOVE -> move(array, change.i(), change.j());
		}
	}

	private Interval randomInterval(final int upperBound) {
		final var v1 = rng.nextInt(upperBound);
		final var v2 = rng.nextInt(upperBound);
		return new Interval(Math.min(v1, v2), Math.max(v1, v2));
	}

	private record Interval(int from, int to) {}

	private record Change(Type type, int i, int j) {
		enum Type {SWAP, MOVE}
	}

//	private record Pair(int done,int of){}
}
