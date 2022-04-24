package heuristic;

import AbstractClasses.ProblemDomain;
import extension.ExtendedHyperHeuristic;
import heuristic.acceptor.AillaAcceptor;
import heuristic.heuristicchooser.HeuristicChooser;
import heuristic.heuristicchooser.ImproveToTimeChooser;
import heuristic.util.SolutionAllocator;
import heuristic.util.StatefulSolution;
import lombok.val;

import java.util.EnumMap;

public final class UntypedHeuristic extends ExtendedHyperHeuristic {

	public UntypedHeuristic(final long seed) {
		super(seed);
	}

	@SuppressWarnings("OverlyLongMethod")
	@Override
	protected void solve(@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter") final ProblemDomain domain) {

		domain.setDepthOfSearch(0.4);
		domain.setIntensityOfMutation(0.4);

		val allocator=new SolutionAllocator(domain);
		val currentSolution=allocator.allocate();
		val newSolution=allocator.allocate();
		allocator.commit();

//		final var localChooser = new ImproveToTimeChooserExt(rng, localSearches.length);
//		final var chooser = new LrpChooser(rng, domain.getNumberOfHeuristics());
//		final var chooser = new RlChooser(domain.getNumberOfHeuristics());
		val choosers=new EnumMap<StatefulSolution.State, HeuristicChooser>(StatefulSolution.State.class);
		for(final var state:StatefulSolution.State.values()){
			val chooser=new ImproveToTimeChooser(true);
			chooser.init(rng, domain.getNumberOfHeuristics());
			choosers.put(state,chooser);
		}
		final var times = domain.getheuristicCallTimeRecord();

		currentSolution.initialize();
//		final var acceptor=new AnnealingAcceptor(rng,Math.max(0.2*initValue1,Math.abs(initValue1-initValue2)),0.95,50);
		final var acceptor = new AillaAcceptor();
		acceptor.restart(currentSolution.value());

		while (!hasTimeExpired()) {
			final var progress = getProgress();
			val chooser=choosers.get(0);
			final var heuristic = chooser.choose(progress);
			final var time = times[heuristic];
			final var oldScore = currentSolution.value();
			final var newScore = domain.applyHeuristic(heuristic, currentSolution.solutionIndex(),
			                                           newSolution.solutionIndex());
			final var isSame = newSolution.isSameAs(currentSolution);
			chooser.update(heuristic, oldScore, newScore, isSame, times[heuristic] - time);
			if (acceptor.shouldAccept(newScore,oldScore , isSame, getProgress()))
				newSolution.copyTo(currentSolution);
			if (acceptor.isRestartNeeded()) {
//				System.out.println("restart");
				currentSolution.initialize();
				acceptor.restart(currentSolution.value());
			}
		}
	}

	@Override
	public String toString() {
		return "Untyped heuristic";
	}
}
