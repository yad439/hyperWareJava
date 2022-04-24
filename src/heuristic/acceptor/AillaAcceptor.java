package heuristic.acceptor;

import heuristic.util.DoubleRingBuffer;
import lombok.val;
import util.NestedWriter;

import java.util.random.RandomGenerator;

public final class AillaAcceptor implements Acceptor {
	private final DoubleRingBuffer solutions;
	private final int iterLimit;
	private final int hardIterLimit;
	private final int worseningLimit;
	private int listPosition = 0;
	private int unacceptedIteration = 0;
	private int worseningIteration = 0;

	public AillaAcceptor(final int iterLimit, final int hardIterLimit, final int worseningLimit,
	                     final int bufferLength) {
		this.iterLimit = iterLimit;
		this.hardIterLimit = hardIterLimit;
		this.worseningLimit = worseningLimit;
		solutions = new DoubleRingBuffer(bufferLength);
	}

	public AillaAcceptor() {
		this(10, 20, 10, 10);
	}

	@Override
	public void init(final RandomGenerator rng) {}

	@Override
	public boolean shouldAccept(final double solution, final double previousSolution, final boolean same,
	                            final double progress) {
		final boolean result;
		if (solution < previousSolution) {
			if (solution < solutions.get(0)) {
				solutions.addFirst(solution);
				listPosition = 0;
				worseningIteration = 0;
			}
			unacceptedIteration = 0;
			result = true;
		} else if (unacceptedIteration >= iterLimit && listPosition < solutions.size() && solution <= solutions.get(
				listPosition)) {
			result = true;
			unacceptedIteration = 0;
			worseningIteration++;
		} else {
			result = false;
			unacceptedIteration++;
		}

		if (worseningIteration >= worseningLimit || unacceptedIteration >= hardIterLimit) {
			worseningIteration = 0;
			listPosition++;
		}
//		if (result) currentSolution = solution;
		return result;
	}

	@Override
	public boolean isRestartNeeded() {return listPosition >= solutions.size();}

	@Override
	public void restart(final double solution) {
		solutions.clear();
		solutions.addFirst(solution);
//		currentSolution = solution;
		listPosition = 0;
		unacceptedIteration = 0;
		worseningIteration = 0;
	}

	@Override
	public Acceptor copySettings() {
		return new AillaAcceptor(iterLimit,hardIterLimit,worseningLimit,solutions.capacity());
	}

	@Override
	public Acceptor copyState() {
		val result=new AillaAcceptor(iterLimit,hardIterLimit,worseningLimit,solutions.capacity());
		result.listPosition=listPosition;
		result.unacceptedIteration=unacceptedIteration;
		result.worseningIteration=worseningIteration;
		return result;
	}

	@Override
	public String toString() {
		return "AillaAcceptor(" +
		       "iterLimit=" + iterLimit +
		       ", hardIterLimit=" + hardIterLimit +
		       ", worseningLimit=" + worseningLimit +
		       ", bufferLength=" + solutions.capacity() +
		       ')';
	}

	@Override
	public void printStats(final NestedWriter output) {
		output.print(toString());
		output.println('{');
		val scoped=output.getScoped();
		scoped.formatLine("listPosition = %d",listPosition);
		scoped.formatLine("unacceptedIteration = %d",unacceptedIteration);
		scoped.formatLine("worseningIteration = %d",worseningIteration);
		output.printLine('}');
	}
}
