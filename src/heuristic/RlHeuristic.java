package heuristic;

import AbstractClasses.ProblemDomain;
import com.github.chen0040.rl.learning.rlearn.RLearner;
import extension.ExtendedHyperHeuristic;
import heuristic.acceptor.ContinuousAnnealingAcceptor;
import heuristic.util.HeuristicImpl;
import heuristic.util.SolutionAllocator;
import lombok.val;

import java.util.ArrayDeque;
import java.util.stream.IntStream;

public final class RlHeuristic extends ExtendedHyperHeuristic {

	@Override
	protected void solve(final ProblemDomain domain) {
		final var totalNum=domain.getNumberOfHeuristics();
		final var heuristics= IntStream.range(0,totalNum).mapToObj(i->new HeuristicImpl(domain,i)).toList();
		final var chooser=new RLearner(10, totalNum);
		var withoutImprovement=0;
		final var allocator=new SolutionAllocator(domain);
		final var currentSolution=allocator.allocate();
		final var newSolution=allocator.allocate();
		allocator.commit();
		currentSolution.initialize();
		final var acceptor=new ContinuousAnnealingAcceptor(rng, currentSolution.value() * 0.2, 0.95, 50);
		acceptor.restart(currentSolution.value());
		final var currentScores=new ArrayDeque<Double>(11);
		final var previousScores=new ArrayDeque<Double>(11);
		final var selects=new ArrayDeque<Integer>(11);
		final var states=new ArrayDeque<Integer>(11);
		currentScores.add(currentSolution.value());
		while (!hasTimeExpired()){
			final var heuristicNumC=chooser.selectAction(withoutImprovement,null).getIndex();
			val heuristicNum=heuristicNumC!=-1?heuristicNumC:rng.nextInt(totalNum);
			final var heuristic=heuristics.get(heuristicNum);
			heuristic.apply(currentSolution,newSolution);
			states.add(withoutImprovement);
			if(newSolution.value()<currentSolution.value())withoutImprovement=0;
			else withoutImprovement++;
			if(acceptor.shouldAccept(newSolution.value(),currentSolution.value(), newSolution.isSameAs(currentSolution),getProgress()))
				newSolution.copyTo(currentSolution);
			currentScores.add(currentSolution.value());
			selects.add(heuristicNum);
			if(currentScores.size()>10)
				previousScores.add(currentScores.remove());
			if(previousScores.size()>10) {
				previousScores.remove();
				final var currBest=currentScores.stream().min(Double::compare).orElseThrow();
				final var prevBest=currentScores.stream().min(Double::compare).orElseThrow();
				final var choice=selects.remove();
				final var state=states.remove();
				chooser.update(state,choice,states.getFirst(),null,prevBest-currBest);
			}
		}
	}

	@Override
	public String toString() {
		return "Reinforcement learning heuristic";
	}
}
