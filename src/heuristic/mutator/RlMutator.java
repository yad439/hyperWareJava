package heuristic.mutator;

import heuristic.heuristicchooser.RlChooser;
import heuristic.util.Heuristic;
import heuristic.util.Solution;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.val;
import util.NestedWriter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;
import java.util.stream.Stream;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class RlMutator extends SolutionMutator {
	final Map<Solution, Integer> memory;
	final int batchSize;
	final RlChooser chooser = new RlChooser();
	List<Heuristic> heuristics = null;

	public RlMutator(final int batchSize, final boolean continuous) {
		this.batchSize = batchSize;
		if (continuous) memory = new HashMap<>();
		else memory = null;
	}

	@Override
	public void mutate(final Solution source, final Solution destination) {
		int state;
		final int firstChoice;
		final Heuristic.RunResult firstResult;
		if (memory == null || !memory.containsKey(source))
			firstChoice = chooser.choose(progressFunction.getAsDouble());
		else {
			val prev = memory.get(source);
			firstChoice = chooser.choose(progressFunction.getAsDouble(), prev);
		}
		{
			val heuristic = heuristics.get(firstChoice);
			firstResult = heuristic.applyTimed(source, destination);
		}
		state = firstChoice;
		if (batchSize > 1) {
			val prevs = new int[batchSize - 1];
			val choices = new int[batchSize - 1];
			val results = new Heuristic.RunResult[batchSize - 1];
			for (var i = 0; i < batchSize - 1; i++) {
				prevs[i] = state;
				choices[i] = chooser.choose(progressFunction.getAsDouble(), state);
				val heuristic = heuristics.get(choices[i]);
				results[i] = heuristic.applyTimed(destination, destination);
				state = choices[i];
			}
			val time = firstResult.time() + Arrays.stream(results).mapToInt(Heuristic.RunResult::time).sum();
			val score = destination.score();
			val same = destination.isSameAs(source);
			if (memory == null || !memory.containsKey(source))
				chooser.update(firstChoice, source.score(), score, same, time);
			else chooser.update(memory.get(source), firstChoice, source.score(), score, same, time);
			for (var i = 0; i < batchSize - 1; i++)
				chooser.update(prevs[i], choices[i], source.score(), score, same, time);
		} else {
			if (memory == null || !memory.containsKey(source))
				chooser.update(firstChoice, source.score(), destination.score(),
				               destination.isSameAs(source), firstResult.time());
			else chooser.update(memory.get(source), firstChoice, source.score(), destination.score(),
			                    destination.isSameAs(source), firstResult.time());
		}
		if (memory != null) memory.put(destination, state);
	}

	@Override
	protected void initInner(final RandomGenerator rng, final Solution[] population) {
		heuristics = Stream.concat(Arrays.stream(allMutations), Arrays.stream(localSearches)).toList();
		chooser.init(rng, heuristics.size());
	}

	@Override
	public void printStats(final NestedWriter output) {
		output.println("RL Mutator {");
		val scoped = output.getScoped();
		scoped.printIndented("Chooser: ");
		chooser.printStats(scoped);
		output.printLine('}');
	}
}
