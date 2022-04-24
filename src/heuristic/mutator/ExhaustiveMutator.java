package heuristic.mutator;

import heuristic.heuristicchooser.HeuristicChooser;
import heuristic.heuristicchooser.MeanImproveChooserExt;
import heuristic.heuristicchooser.RankBasedChooser;
import heuristic.heuristicchooser.StagedChooser;
import heuristic.util.Solution;
import heuristic.util.SolutionAllocator;
import lombok.Setter;
import lombok.val;
import util.NestedWriter;

import java.util.random.RandomGenerator;

public class ExhaustiveMutator extends SolutionMutator {
	private Solution tmp;
	@Setter
	private HeuristicChooser mutationChooser = new MeanImproveChooserExt(true, true, true, 1.0, 0.2, true);
	private final StagedChooser localChooser = new RankBasedChooser();

	@Override
	public void mutate(final Solution source, final Solution destination) {
		val mutationIndex = mutationChooser.choose(progressFunction.getAsDouble());
		val mutation = allMutations[mutationIndex];
		val oldScore = source.value();
		mutation.apply(source, destination);

		localChooser.startNewStage();
		while (true) {
			val localSearchIndex = localChooser.choose(progressFunction.getAsDouble());
			if (localSearchIndex == -1) break;
			val localSearch = localSearches[localSearchIndex];
			val result = localSearch.applyTimed(destination, tmp);
			val isSame = tmp.isSameAs(destination);
			localChooser.update(localSearchIndex, destination.value(), result.score(), isSame, result.time());
			tmp.copyTo(destination);
		}

		mutationChooser.update(mutationIndex, oldScore, destination.value(), destination.isSameAs(source),
		                       mutation.getLastRunningTime());
	}

	@Override
	protected void initInner(final RandomGenerator rng, final Solution[] population) {
		localChooser.init(rng, localSearches.length);
		mutationChooser.init(rng, allMutations.length);
	}

	@Override
	public void allocateBufferSolutions(final SolutionAllocator allocator) {
		super.allocateBufferSolutions(allocator);
		tmp = allocator.allocate();
	}

	@Override
	public void printStats(final NestedWriter output) {
		output.println("Exhaustive Mutator {");
		val scoped = output.getScoped();
		scoped.printIndented("Local chooser: ");
		localChooser.printStats(scoped);
		scoped.printIndented("Mutation chooser: ");
		mutationChooser.printStats(scoped);
		output.printLine('}');
	}
}
