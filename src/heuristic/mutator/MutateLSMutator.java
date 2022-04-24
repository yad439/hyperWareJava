package heuristic.mutator;

import heuristic.heuristicchooser.HeuristicChooser;
import heuristic.heuristicchooser.ImproveToTimeChooserExt;
import heuristic.heuristicchooser.MeanImproveChooserExt;
import heuristic.util.Solution;
import heuristic.util.SolutionAllocator;
import heuristic.util.Utils;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import util.NestedWriter;

import java.util.random.RandomGenerator;

@RequiredArgsConstructor
public final class MutateLSMutator extends SolutionMutator {
	private final boolean preInit;
	private Solution tmp;
	@Setter
	private HeuristicChooser localChooser = new ImproveToTimeChooserExt(true, false, true, 1.0, 1.0, true);
	@Setter
	private HeuristicChooser mutationChooser = new MeanImproveChooserExt(true, false, true, 1.0, 1.0, true);

	public MutateLSMutator() {this(true);}

	@Override
	protected void initInner(final RandomGenerator rng, final Solution[] population) {
		localChooser.init(rng, localSearches.length);
		mutationChooser.init(rng, allMutations.length);
		if (preInit) {
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
	}

	@Override
	public void mutate(final Solution source, final Solution destination) {
		final var progress = progressFunction.getAsDouble();

		final var mutationIndex = mutationChooser.choose(progress);
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
	public void allocateBufferSolutions(final SolutionAllocator allocator) {
		super.allocateBufferSolutions(allocator);
		if(preInit)tmp = allocator.allocate();
	}

	@Override
	public void printStats(final NestedWriter output) {
		output.println("MutateLS Mutator {");
		val scoped = output.getScoped();
		scoped.printIndented("Local chooser: ");
		localChooser.printStats(scoped);
		scoped.printIndented("Mutation chooser: ");
		mutationChooser.printStats(scoped);
		output.printLine('}');
	}
}
