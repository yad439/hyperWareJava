package extension;

import AbstractClasses.ProblemDomain;

import java.io.PrintWriter;
import java.util.Arrays;

public abstract class ExtendedProblemDomain extends ProblemDomain {
	protected final int[] internalTimes = new int[getNumberOfHeuristics()];
	protected long totalInternalTime = 0;
	private boolean useInternalTime = false;

	protected ExtendedProblemDomain(final long seed) {
		super(seed);
	}

	void setUseInternalTime(final boolean useInternalTime) {
		this.useInternalTime = useInternalTime;
	}

	public int[] getInternalCallTimes() {
		//noinspection AssignmentOrReturnOfFieldWithMutableType
		return internalTimes;
	}

	long getTotalInternalTime() {return totalInternalTime;}

	public void reset() {
		totalInternalTime = 0;
		Arrays.fill(heuristicCallRecord, 0);
		Arrays.fill(heuristicCallTimeRecord, 0);
		Arrays.fill(internalTimes, 0);
	}

	@Override
	public int[] getheuristicCallTimeRecord() {
		if (useInternalTime) return internalTimes;
		return heuristicCallTimeRecord;
	}

	public void printStats(final PrintWriter writer) {
		final var heuristicNumber = getNumberOfHeuristics();
		writer.println("Heuristic\tCall count\tTime\tInternal time");
		for (final var type : HeuristicType.values()) {
			final var heuristics = getHeuristicsOfType(type);
			if (heuristics.length != 0) {
				writer.println(type);
				for (final var i:heuristics)
					//noinspection AutoBoxing
					writer.printf("%s\t%d\t%d\t%d%s", getHeuristicName(i), heuristicCallRecord[i], heuristicCallTimeRecord[i], internalTimes[i], System.lineSeparator());
			}
		}
	}

	public String getHeuristicName(final int index){
		return String.valueOf(index);
	}
}
