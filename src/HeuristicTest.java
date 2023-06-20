import heuristic.PopulationBasedHeuristic;
import heuristic.mutator.RlMutator;
import lombok.val;
import util.NestedWriter;
import warehouse.WarehouseScheduling;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.random.RandomGenerator;

public final class HeuristicTest {
	private HeuristicTest() {}

	public static void main(final String[] args) throws IOException {

		/*final var annealing=new SimulatedAnnealing();
		annealing.loadInstance(20);
		final var startTime=System.currentTimeMillis();
		final var result=annealing.run(new SimulatedAnnealing.Settings(2_400_000));
		final var elapsed=System.currentTimeMillis()-startTime;
		System.out.printf("%d %d",result,elapsed);*/

		final var rng = RandomGenerator.getDefault();
//		final var problem = new TSP(1234);
		final var problem = new WarehouseScheduling(rng.nextLong(), false);
		problem.loadInstance(26);

		/*final var heuristics = new ExtendedHyperHeuristic[]{
//				new ExampleHyperHeuristic1(5678),
//				new ExampleHyperHeuristic2(5678),
//				new Examples.ExampleHyperHeuristic1(5678),
//				new Examples.ExampleHyperHeuristic2(5678),
//				new ExampleHyperHeuristic3(5678),
//				new heuristics.AcceptAllHH(5678),
//				new heuristics.AcceptNoWorseHH(5678),
//				new FairShareILS(439),
//				new heuristics.NoRestartFairShareILS(5678),
//				new AnnealingHeuristic(5678),
//				new LocalSearchBasedHeuristic(439),
//				new UntypedHeuristic(439),
				new PopulationBasedHeuristic(rng.nextLong())
//				new PopulationExhaustiveHeuristic(rng.nextLong())
//				new StationaryGeneticAlgorithm(rng.nextLong())
//new RlHeuristic()
		};*/
//		val heuristic=new StationaryGeneticAlgorithm(rng.nextLong());
		val heuristic=new PopulationBasedHeuristic(rng.nextLong()).setMutator(new RlMutator(4,false));
//		val heuristic=new AnnealingListHeuristic(rng.nextLong());
		final var systemWriter = new PrintWriter(System.out, false, Charset.defaultCharset());
//		for (final var heuristic : heuristics) {
			heuristic.loadProblemDomain(problem);
			heuristic.setTimeLimit(400_000);
			heuristic.setInternalLimit(3_000_000);
			final var startTime = System.currentTimeMillis();
			final var time = heuristic.runWithInternal();
//			final var time = heuristic.run();
			final var elapsed = System.currentTimeMillis() - startTime;
			System.out.print(heuristic.getBestSolutionValue());
			System.out.print(' ');
			System.out.print(Arrays.stream(heuristic.getFitnessTrace()).min());
			System.out.print(' ');
			System.out.print(time);
			System.out.print(' ');
			System.out.println(elapsed);
			heuristic.printStats(new NestedWriter(systemWriter));
//			System.out.println(Arrays.toString(problem.getHeuristicCallRecord()));
//			System.out.println(Arrays.toString(problem.getheuristicCallTimeRecord()));
//			problem.printStats(systemWriter);
//			val h2=new AnnealingListHeuristic(rng.nextLong(),heuristic.getBestHeuristic());
			problem.reset();
//			h2.loadProblemDomain(problem);
//		h2.setTimeLimit(5_000);
//		h2.run();
//		System.out.println(h2.getBestSolutionValue());
//			heuristic.printStats(new NestedWriter(systemWriter));
//			problem.ps();
			/*try(val writer=new PrintWriter("out/statistics.txt", StandardCharsets.UTF_8)){
				val maxlen=Heuristics.crossScores.stream().mapToInt(ArrayList::size).max().getAsInt();
				for(var i=0;i<maxlen;i++){
					writer.print(i);
					writer.print(' ');
					for(var j=0;j<Heuristics.crossScores.size();j++){
						if(Heuristics.crossScores.get(j).size()>i)
							writer.print(Heuristics.crossScores.get(j).get(i));
						writer.print(' ');
					}
					writer.print('\n');
				}
			}*/
//			problem.reset();
//		}
		systemWriter.flush();
		/*final var annealing=new SimulatedAnnealing();
		annealing.loadInstance(26);
		val time= Instant.now();
		final var settings=new SimulatedAnnealing.Settings(2_800_000,1);
		val end=Instant.now();
		System.out.println(annealing.run(settings));
		System.out.println(annealing.bestVal);
		System.out.println(Arrays.toString(annealing.bestS));
		System.out.println(Duration.between(time,end).toSeconds());*/
	}
}
