package heuristic.acceptor;

import heuristic.util.DoubleRingBuffer;
import lombok.val;
import util.NestedWriter;

import java.util.random.RandomGenerator;

public final class Ailla2Acceptor implements Acceptor {
	private final DoubleRingBuffer solutions;
	private final int iterLimit;
	private int listPosition = 0;
	private int count = 0;

	public Ailla2Acceptor(final int iterLimit, final int bufferLength) {
		this.iterLimit = iterLimit;
		solutions = new DoubleRingBuffer(bufferLength);
	}

	public Ailla2Acceptor() {
		this(6, 10);
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
			}
			count = 0;
			result = true;
		} else if (listPosition < solutions.size() && solution <= solutions.get(
				listPosition)) {
			result = true;
			count++;
		} else {
			result = false;
			count++;
		}

		if (count >= iterLimit) {
			count = 0;
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
		count = 0;
	}

	@Override
	public Acceptor copySettings() {
		return new Ailla2Acceptor(iterLimit, solutions.capacity());
	}

	@Override
	public Acceptor copyState() {
		val result=new Ailla2Acceptor(iterLimit, solutions.capacity());
		result.listPosition=listPosition;
		result.count = count;
		return result;
	}

	@Override
	public String toString() {
		return "Ailla2Acceptor(" +
		       "iterLimit=" + iterLimit +
		       ", bufferLength=" + solutions.capacity() +
		       ')';
	}

	@Override
	public void printStats(final NestedWriter output) {
		output.print(toString());
		output.println('{');
		val scoped=output.getScoped();
		scoped.formatLine("listPosition = %d",listPosition);
		scoped.formatLine("count = %d", count);
		output.printLine('}');
	}
}
