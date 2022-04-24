package heuristic.mutator;

import heuristic.heuristicchooser.HeuristicChooser;
import heuristic.heuristicchooser.ImproveToTimeChooserExt;
import heuristic.heuristicchooser.MeanImproveChooserExt;
import heuristic.util.Solution;
import lombok.Setter;
import lombok.val;
import util.NestedWriter;

import java.util.function.Supplier;
import java.util.random.RandomGenerator;

public final class MutateToLSMutator extends SolutionMutator {
	private Solution tmp;
	private HeuristicChooser[] localChoosers = null;
	@Setter
	private HeuristicChooser mutationChooser = new MeanImproveChooserExt();
	@Setter
	private Supplier<HeuristicChooser> localFactory = ImproveToTimeChooserExt::new;

	@Override
	protected void initInner(final RandomGenerator rng, final Solution[] population) {
		mutationChooser.init(rng, allMutations.length);
		localChoosers = new HeuristicChooser[allMutations.length];
		for (var i = 0; i < allMutations.length; i++) {
			val chooser = localFactory.get();
			chooser.init(rng, localSearches.length);
			localChoosers[i] = chooser;
		}
	}

	@Override
	public void mutate(final Solution source, final Solution destination) {
		final var progress = progressFunction.getAsDouble();

		final var mutationIndex = mutationChooser.choose(progress);
		val localChooser = localChoosers[mutationIndex];
		final var localSearchIndex = localChooser.choose(progress);
		final var mutation = allMutations[mutationIndex];
		final var localSearch = localSearches[localSearchIndex];

		final var oldScore = source.value();
		mutation.apply(source, destination);
		final var newScore = localSearch.apply(destination, destination);
		final var isSame = destination.isSameAs(source);
		localChooser.update(localSearchIndex, oldScore, newScore, isSame, localSearch.getLastRunningTime());
		mutationChooser.update(mutationIndex, oldScore, newScore, isSame, mutation.getLastRunningTime());
	}

	@Override
	public void printStats(final NestedWriter output) {
		output.println("Mutate to LS Mutator {");
		val scoped = output.getScoped();
		scoped.printLine("Local choosers: [");
		{
			val newScoped = scoped.getScoped();
			for (final var chooser : localChoosers) chooser.printStats(newScoped);
		}
		scoped.printLine(']');
		scoped.printIndented("Mutation chooser: ");
		mutationChooser.printStats(scoped);
		output.printLine('}');
	}
}
