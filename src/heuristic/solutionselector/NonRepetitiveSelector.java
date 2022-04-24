package heuristic.solutionselector;

import heuristic.util.Solution;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.val;
import util.NestedWriter;

import java.util.LinkedList;
import java.util.List;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

@ToString
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public final class NonRepetitiveSelector implements BatchSelector {
	SolutionSelector firstSelector;
	SolutionSelector secondSelector;

	public NonRepetitiveSelector(final SolutionSelector selector){this(selector,selector);}

	@Override
	public void init(final RandomGenerator rng) {
		firstSelector.init(rng);
		if(secondSelector!=firstSelector)secondSelector.init(rng);
	}

	@Override
	public Pair selectPair(final List<? extends Solution> population, final double progress) {
		val first=firstSelector.select(progress, population);
		val reducedPopulation=new LinkedList<>(population);
		reducedPopulation.remove(first);
		return new Pair(first,secondSelector.select(progress,reducedPopulation));
	}

	@Override
	public Iterable<Pair> selectMultiple(final List<? extends Solution> population, final int number,
	                                     final double progress) {
		val stream= IntStream.range(0, number).mapToObj(i->selectPair(population, progress));
		return stream::iterator;
	}

	@Override
	public void printStats(final NestedWriter output) {
		output.print(toString());
		output.println('{');
		val scoped=output.getScoped();
		scoped.printIndented("firstSelector=");
		firstSelector.printStats(scoped);
		if(secondSelector!=firstSelector){
			scoped.printIndented("secondSelector=");
			secondSelector.printStats(scoped);
		}
		scoped.println('}');
	}
}
