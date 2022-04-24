package heuristic.mutator;

import heuristic.heuristicchooser.HeuristicChooser;
import heuristic.heuristicchooser.ImproveToTimeChooserExt;
import heuristic.heuristicchooser.MeanImproveChooserExt;
import heuristic.util.Solution;
import heuristic.util.SolutionAllocator;
import heuristic.util.Utils;
import lombok.Setter;
import lombok.val;
import util.NestedWriter;

import java.util.function.Supplier;
import java.util.random.RandomGenerator;

public class LSToMutateMutator extends SolutionMutator {
	private Solution tmp;
	@Setter
	private HeuristicChooser localChooser = new ImproveToTimeChooserExt(true);
	private HeuristicChooser[] mutationChoosers = null;
	@Setter
	private Supplier<HeuristicChooser> mutationFactory = MeanImproveChooserExt::new;

	@Override
	protected void initInner(final RandomGenerator rng, final Solution[] population) {
		localChooser.init(rng, localSearches.length);
		mutationChoosers = new HeuristicChooser[localSearches.length];
		for (var i = 0; i < localSearches.length; i++) {
			val chooser = mutationFactory.get();
			chooser.init(rng, allMutations.length);
			mutationChoosers[i] = chooser;
		}

		tmp.initialize();

		if (population != null && population.length >= localSearches.length) {
			final int[] applications = new int[localSearches.length];
			for (final var solution : population) {
				final var oldScore = tmp.value();
				final var heuristic = Utils.argmin(applications);
				final var newScore = localSearches[heuristic].apply(tmp, solution);
				localChooser.update(heuristic, oldScore, newScore, false,
				                    localSearches[heuristic].getLastRunningTime());
				applications[heuristic]++;
			}
		}
	}

	@Override
	public void mutate(final Solution source, final Solution destination) {
		final var progress = progressFunction.getAsDouble();

		final var localSearchIndex = localChooser.choose(progress);
		final var localSearch = localSearches[localSearchIndex];
		val mutationChooser = mutationChoosers[localSearchIndex];
		final var mutationIndex = mutationChooser.choose(progress);
		final var mutation = allMutations[mutationIndex];

		final var oldScore = source.value();
		mutation.apply(source, destination);
		final var newScore = localSearch.apply(destination, destination);
		final var isSame = destination.isSameAs(source);
		localChooser.update(localSearchIndex, oldScore, newScore, isSame, localSearch.getLastRunningTime());
		mutationChooser.update(mutationIndex, oldScore, newScore, isSame, mutation.getLastRunningTime());
	}

	@Override
	public void allocateBufferSolutions(final SolutionAllocator allocator) {
		super.allocateBufferSolutions(allocator);
		tmp = allocator.allocate();
	}

	@Override
	public void printStats(final NestedWriter output) {
		output.println("LS to mutate Mutator {");
		val scoped = output.getScoped();
		scoped.printIndented("Local chooser: ");
		localChooser.printStats(scoped);
		scoped.printLine("Mutation choosers: [");
		{
			val newScoped = scoped.getScoped();
			for (final var chooser : mutationChoosers) chooser.printStats(newScoped);
		}
		scoped.printLine(']');
		output.printLine('}');
	}
}
