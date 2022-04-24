package heuristic.util;

import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

@UtilityClass
public class ChainHelper {
	private final int[] allVariants = {0, 1, 2, 3, 4};
	private final int[] empty = {1};
	private final int[] twoEls = {0, 1, 3, 4};
	private final int[] full = {0, 2, 3, 4};

	public ChainedHeuristic randomChain(final RandomGenerator rng, final List<? extends Heuristic> all,
	                                    final int maxSize) {
		val size = rng.nextInt(1, maxSize + 1);
		val list = new ArrayList<Heuristic>(size);
		for (var i = 0; i < size; i++) list.add(all.get(rng.nextInt(all.size())));
		return new ChainedHeuristic(list, maxSize);
	}

	public Change randomChange(final RandomGenerator rng, final ChainedHeuristic chain,
	                           final List<? extends Heuristic> all) {
		final int[] list;
		if (chain.size() == 1) list = empty;
		else if (chain.size() == 2) list = twoEls;
		else if (chain.size() == chain.getMaxSize()) list = full;
		else list = allVariants;

		val ind0 = rng.nextInt(list.length);

		return switch (list[ind0]) {
			case 0 -> {
				val ind = rng.nextInt(chain.size());
				yield new Remove(ind, chain.get(ind));
			}
			case 1 -> {
				val ind = rng.nextInt(chain.size() + 1);
				val newInd = rng.nextInt(all.size());
				yield new Insert(ind, all.get(newInd));
			}
			case 2 -> {
				val ind1 = rng.nextInt(chain.size());
				int ind2;
				do {
					ind2 = rng.nextInt(chain.size());
				} while (ind2 == ind1);
				yield new Move(ind1, ind2);
			}
			case 3 -> {
				val ind1 = rng.nextInt(chain.size());
				int ind2;
				do {
					ind2 = rng.nextInt(chain.size());
				} while (ind2 == ind1);
				yield new Swap(ind1, ind2);
			}
			case 4 -> {
				val ind = rng.nextInt(chain.size());
				val newInd = rng.nextInt(all.size());
				yield new Replace(ind, chain.get(ind), all.get(newInd));
			}
			default -> throw new IllegalStateException("Impossible");
		};
	}

	public sealed interface Change {
		void apply(ChainedHeuristic chain);

		void revert(ChainedHeuristic chain);
	}

	private record Remove(int index, Heuristic value) implements Change {
		@Override
		public void apply(final ChainedHeuristic chain) {
			chain.delete(index);
		}

		@Override
		public void revert(final ChainedHeuristic chain) {
			chain.insert(value, index);
		}
	}

	private record Insert(int index, Heuristic value) implements Change {
		@Override
		public void apply(final ChainedHeuristic chain) {
			chain.insert(value, index);
		}

		@Override
		public void revert(final ChainedHeuristic chain) {
			chain.delete(index);
		}
	}

	private record Move(int from, int to) implements Change {
		@Override
		public void apply(final ChainedHeuristic chain) {
			val value = chain.delete(from);
			chain.insert(value, to);
		}

		@Override
		public void revert(final ChainedHeuristic chain) {
			chain.insert(chain.delete(to), from);
		}
	}

	private record Swap(int index1, int index2) implements Change {
		@Override
		public void apply(final ChainedHeuristic chain) {
			val value = chain.get(index1);
			chain.set(chain.get(index2), index1);
			chain.set(value, index2);
		}

		@Override
		public void revert(final ChainedHeuristic chain) {
			val value = chain.get(index2);
			chain.set(chain.get(index1), index2);
			chain.set(value, index1);
		}
	}

	private record Replace(int index, Heuristic old, Heuristic newH) implements Change {
		@Override
		public void apply(final ChainedHeuristic chain) {
			chain.set(newH, index);
		}

		@Override
		public void revert(final ChainedHeuristic chain) {
			chain.set(old, index);
		}
	}
}
