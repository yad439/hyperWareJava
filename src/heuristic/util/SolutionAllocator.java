package heuristic.util;

import AbstractClasses.ProblemDomain;

import java.util.List;
import java.util.stream.IntStream;

public final class SolutionAllocator {
	private final ProblemDomain domain;
	private int counter = 0;

	public SolutionAllocator(final ProblemDomain domain) {
		this.domain = domain;
	}

	public Solution allocate() {
		final var result = new Solution(domain, counter);
		counter++;
		return result;
	}

	public Solution[] allocate(final int number) {
		final var result = IntStream.range(counter, counter + number).mapToObj(i -> new Solution(domain, i)).toArray(Solution[]::new);
		counter += number;
		return result;
	}

	public List<Solution> allocateList(final int number){
		counter+=number;
		return IntStream.range(0,number).mapToObj(i -> new Solution(domain, i)).toList();
	}

	public void commit() {domain.setMemorySize(counter);}
}
