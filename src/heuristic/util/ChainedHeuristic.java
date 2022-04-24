package heuristic.util;

import AbstractClasses.ProblemDomain;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public final class ChainedHeuristic implements Heuristic {
	private final List<Heuristic> chain;
	@Getter
	private final int maxSize;
	private int lastRunningTime=0;

	@Override
	public double apply(final Solution source, final Solution destination) {
		val iter=chain.iterator();
		var heuristic=iter.next();
		var result=heuristic.apply(source,destination);
		while(iter.hasNext()){
			heuristic=iter.next();
			result=heuristic.apply(source, destination);
		}
		lastRunningTime=chain.stream().mapToInt(Heuristic::getLastRunningTime).sum();
		return result;
	}

	@Override
	public int getLastRunningTime() {
		return lastRunningTime;
	}

	@Override
	public ProblemDomain getDomain() {
		return chain.get(0).getDomain();
	}

	@Override
	public void setDomain(final ProblemDomain domain) {
		for (final var heuristic : chain) heuristic.setDomain(domain);
	}

	public ChainedHeuristic copy(){
		return new ChainedHeuristic(chain.stream().map(Heuristic::copy).collect(Collectors.toCollection(ArrayList::new)), maxSize);
	}

	int size(){
		return chain.size();
	}

	void insert(final Heuristic heuristic, final int position){
		chain.add(position,heuristic);
	}

	Heuristic delete(final int position){
		return chain.remove(position);
	}

	Heuristic get(final int position){
		return chain.get(position);
	}

	void set(final Heuristic heuristic,final int position){
		chain.set(position,heuristic);
	}
}
