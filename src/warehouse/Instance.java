package warehouse;

import java.util.BitSet;

interface Instance {
	int evaluate(int[] permutation);

	int evaluatePartial(int[] permutation, int limit);

	int jobCount();

	BitSet[] itemsNeeded();
}
