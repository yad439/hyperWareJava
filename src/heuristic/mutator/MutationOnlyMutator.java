package heuristic.mutator;

import heuristic.heuristicchooser.HeuristicChooser;
import heuristic.heuristicchooser.MeanImproveChooserExt;
import heuristic.util.Solution;
import lombok.Setter;
import lombok.val;
import util.NestedWriter;

import java.util.random.RandomGenerator;

public class MutationOnlyMutator extends SolutionMutator{
	@Setter
	private HeuristicChooser mutationChooser = new MeanImproveChooserExt(true, true, true, 1.0, 0.2, true);

	@Override
	public void mutate(final Solution source, final Solution destination) {
		val mutationIndex = mutationChooser.choose(progressFunction.getAsDouble());
		val mutation = allMutations[mutationIndex];
		val oldScore = source.value();
		mutation.apply(source, destination);

		mutationChooser.update(mutationIndex, oldScore, destination.value(), destination.isSameAs(source),
		                       mutation.getLastRunningTime());
	}

	@Override
	protected void initInner(final RandomGenerator rng, final Solution[] population) {
		mutationChooser.init(rng, allMutations.length);
	}

	@Override
	public void printStats(final NestedWriter output) {
		output.println("Exhaustive Mutator {");
		val scoped = output.getScoped();
		scoped.printIndented("Mutation chooser: ");
		mutationChooser.printStats(scoped);
		output.printLine('}');
	}
}
