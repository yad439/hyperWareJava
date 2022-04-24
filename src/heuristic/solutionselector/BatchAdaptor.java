package heuristic.solutionselector;

import heuristic.util.Solution;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.val;
import util.NestedWriter;

import java.util.List;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

@ToString
@RequiredArgsConstructor
public final class BatchAdaptor implements BatchSelector{
	private final SolutionSelector firstSelector;
	private final SolutionSelector secondSelector;

	public BatchAdaptor(final SolutionSelector selector){this(selector,selector);}

//	@Override
	public void init(final RandomGenerator rng) {
		firstSelector.init(rng);
		if(secondSelector!=firstSelector)secondSelector.init(rng);
	}

//	@Override
	public Pair selectPair(final double progress) {
		return new Pair(firstSelector.select(progress),secondSelector.select(progress));
	}

	@Override
	public Pair selectPair(final List<? extends Solution> population, final double progress) {
		return new Pair(firstSelector.select(progress,population),secondSelector.select(progress,population));
	}

	@Override
	public Iterable<Pair> selectMultiple(final List<? extends Solution> population, final int number, final double progress) {
		val stream= IntStream.range(0,number).mapToObj(i->selectPair(population, progress));
		return stream::iterator;
	}

//	@Override
	public Iterable<Pair> selectMultiple(final double progress, final int number) {
		val stream= IntStream.range(0,number).mapToObj(i->new Pair(firstSelector.select(progress),secondSelector.select(progress)));
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
