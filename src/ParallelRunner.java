import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;
import davidChescEPH.DavidChescEPH;
import extension.ExtendedHyperHeuristic;
import extension.ExtendedProblemDomain;
import heuristic.GenerationalGeneticAlgorithm;
import heuristic.PopulationBasedHeuristic;
import heuristic.PopulationBasedStagedHeuristic;
import heuristic.StationaryGeneticAlgorithm;
import heuristic.baseline.FairShareILS;
import heuristic.baseline.NoRestartFairShareILS;
import heuristic.mutator.ExhaustiveMutator;
import heuristic.mutator.MutationOnlyMutator;
import lombok.val;
import warehouse.WarehouseScheduling;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

public final class ParallelRunner {

//	static {
//		try {
//			log = new PrintWriter(new BufferedWriter(new FileWriter("oresults.txt", StandardCharsets.UTF_8, true)));
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//	}

	private static final PrintWriter out = new PrintWriter(System.out, true, Charset.defaultCharset());
	private static final PrintWriter log=out;

	private static final boolean detail = true;

	private ParallelRunner() {}

	public static void main(final String[] args) throws IOException, InterruptedException {// 26, 31
		final var rng = RandomGenerator.getDefault();
//		val threadPool = Executors.newFixedThreadPool(Math.min(Runtime.getRuntime().availableProcessors(), 32));
		val threadPool = Executors.newFixedThreadPool(6);
//		val instances = new int[]{5, 15, 20, 28, 37};

//		final var threadPool= Executors.newFixedThreadPool(32);
		/*final var annealing=new SimulatedAnnealing();

		annealing.loadInstance(26);
//		final var threadPool= Executors.newWorkStealingPool();
//		for(val n:new int[]{1, 2, 3, 4, 8, 16, 32, 128, 512}){
		final var settings=new SimulatedAnnealing.Settings(2_500_000,1);
		final var results = IntStream.range(0, 32)
		                             .mapToObj(i ->
				                                       threadPool.submit(
						                                       () ->annealing.run(settings)))
		                             .toList();
		final var results2 = results.stream().mapToDouble(ParallelRunner::futureGet).toArray();
		Arrays.sort(results2);
//		out.println(inst);
//		log.println(inst);
			log.print(Arrays.stream(results2).min().orElseThrow());
			log.print(' ');
			log.print(Arrays.stream(results2).max().orElseThrow());
			log.print(' ');
			log.print(Arrays.stream(results2).average().orElseThrow());//3363.0 4087.0 3671.548387096774
			log.print(' ');
			log.println(results2[results2.length/2]);//4407.0 4627.0 4491.96875 4492.0*/
//		}
//		}
//		log.flush();
//		threadPool.shutdown();
////		final var problem = new WarehouseScheduling(1234);
////		problem.loadInstance(20);
////		final var rng= RandomGenerator.getDefault();
		/*final var heuristics = List.<Supplier<ExtendedHyperHeuristic>>of(
////				new ExampleHyperHeuristic1(5678),
////				new ExampleHyperHeuristic2(5678),
////				new Examples.ExampleHyperHeuristic1(5678),
////				new Examples.ExampleHyperHeuristic2(5678),
////				new ExampleHyperHeuristic3(5678),
////				new heuristics.AcceptAllHH(5678),
////				new heuristics.AcceptNoWorseHH(5678),
//				() -> new FairShareILS(rng.nextLong()),//3710.0 4797.0 4325.709677419355
//				() -> new NoRestartFairShareILS(rng.nextLong()),
////				() -> new AnnealingHeuristic(rng.nextInt()),//3985.0 4465.0 4236.4838709677415
////				() -> new LocalSearchBasedHeuristic(rng.nextLong()),//3906.0 5022.0 4401.4838709677415
////				()-> new UntypedHeuristic(rng.nextLong()),
				() -> new PopulationBasedHeuristic(rng.nextLong())//3543.0 4338.0 3951.516129032258
//				()->new PopulationExhaustiveHeuristic(rng.nextLong()),//3543.0 4338.0 3951.516129032258
////				()->new StagedPopulationHeuristic(rng.nextLong()),//3543.0 4338.0 3951.516129032258
////				()->new MutationOnlyAnnealing(rng.nextLong()),//3688.0 4286.0 4031.2258064516127
//				() -> new StationaryGeneticAlgorithm(rng.nextLong())
////				RlHeuristic::new
		                                                                );*/
//
////		final var threadPool = Executors.newFixedThreadPool(8);
//
////		final Supplier<ProblemDomain> problemFactory = () -> {
////			final var problem = new BinPacking(rng.nextLong());
////			problem.loadInstance(10);
////			return problem;
////		};
//		val taskPool=Executors.newFixedThreadPool(2);
//		/*for(final var chooser:List.<Supplier<HeuristicChooser>>of(
//				MeanImproveChooserExt::new,
//				MabChooser::new,
//				LrpChooser::new,
//				()->new MeanImproveChooserExt(false),
//				()->new MabChooser(0.5,true),
//		ImproveToTimeChooserExt::new,
//				()->new ImproveToTimeChooserExt(false)
//		)) {*/
//		val domain=new WarehouseScheduling(0);
//		val hs=domain.getHeuristicsOfType(ProblemDomain.HeuristicType.CROSSOVER);
//		for(var i=0;i<hs.length;i++){
//			val finalI = i;
//			final Supplier<ExtendedHyperHeuristic> heuristicFactory = () -> new PopulationBasedHeuristic(rng.nextLong())
//					.setWithoutImprovementLimit(16)
//				.setSolutionChooser(new RandomSelector())
//					.setDepthOfSearch(0.5)
//					.setIntensityOfMutation(1.0)
//					.setMutator(new LSToMutateMutator())
//					.setMutator(new MutateLSMutator().setMutationChooser(new ConstantChooser(finalI)))
//					.setCrossChooser(new ConstantChooser(finalI))
//					.setIntensityOfMutation(0.9);*/
//			final int finalI = i;
//			final Supplier<ExtendedHyperHeuristic> heuristicFactory = () -> new StationaryGeneticAlgorithm(rng.nextLong())
//					.setLocalChooser(new ImproveToTimeChooserExt(false))
//					.setMutationChooser()
//				.setAcceptor(new ContinuousAnnealingAcceptor(1.0e-3, false, 0.0))
//					.setCrossoverChooser(new ConstantChooser(finalI))
		;
//		final Supplier<ExtendedHyperHeuristic> heuristicFactory = () -> new OldPopulationHeuristic(rng.nextLong());
//		final var threadPool = Executors.newWorkStealingPool();
		/*val domains=List.<Supplier<ProblemDomain>>of(
				()->new SAT(rng.nextLong()),
				()->new BinPacking(rng.nextLong()),
				()->new FlowShop(rng.nextLong()),
				()->new TSP(rng.nextLong()),
				()->new VRP(rng.nextLong()),
				()->new PersonnelScheduling(rng.nextLong())
		                                                );
			for(final var dom:domains){
				for(final var inum:new int[]{0,2,5,8,10}){
					final Supplier<ProblemDomain> domCons=()->{
						val d=dom.get();
						d.loadInstance(inum);
						return d;
					};
//		for(final var inst:instances){
				for(final var heuristicFactory:heuristics){
////		for(final var heuristicFactory:heuristics) {
////		runOther(problemFactory, heuristicFactory,threadPool);
////			System.out.print(heuristicFactory.get().toString());
////			System.out.print(": ");
////			System.out.println(chooser.get());
////			println(d);
					taskPool.submit(()->{
						try{
							val problemDomain = domCons.get();
						final String str;
						if(problemDomain instanceof TSP t) str="TSP "+ inum + " " + heuristicFactory.get();
							else str = problemDomain + " " + inum + " " + heuristicFactory.get();
//						val str=inst+" "+heuristicFactory.get();
						out.println(str);
//		runWare(inst, heuristicFactory, threadPool, str);
						runOther(domCons,heuristicFactory,threadPool,str);
						}catch (RuntimeException e){
							e.printStackTrace();
						}
					});
////			System.out.println();
		}
		}
		}
			taskPool.shutdown();
		taskPool.awaitTermination(1,TimeUnit.DAYS);
			log.flush();*/
		/*final Supplier<ExtendedHyperHeuristic> heuristicFactory = () -> new PopulationBasedStagedHeuristic(rng.nextLong())
//				.setDepthOfSearch(0.6772594059610966)
//						.setIntensityOfMutation(0.982379594260675)
//				.setMutator(new MutationOnlyMutator())
				.setThresholds(new int[]{10})
				;*/
		val heuristics= List.<Supplier<ExtendedHyperHeuristic>>of(
				()->new FairShareILS(rng.nextLong()),
				()-> new NoRestartFairShareILS(rng.nextLong()),
				() -> new PopulationBasedStagedHeuristic(rng.nextLong()),
				() -> new PopulationBasedHeuristic(rng.nextLong()),
				() -> new StationaryGeneticAlgorithm(rng.nextLong()),
				()->new GenerationalGeneticAlgorithm(rng.nextLong()),
				()->new PopulationBasedHeuristic(rng.nextLong()).setMutator(new MutationOnlyMutator()),
				()->new PopulationBasedHeuristic(rng.nextLong()).setMutator(new ExhaustiveMutator())
		                                                         );
		/*for(final var heuristicFactory:heuristics){
		runWare(26, heuristicFactory, threadPool, heuristicFactory.get().toString());
		}*/
		val inst=new WarehouseScheduling(rng.nextLong(),true);//4450.0	4788.0	4673.75	4701.0; 5378.0	6038.0	5632.53125	5621.0
		inst.loadInstance(31);
		final Supplier<ProblemDomain> dom= inst::copy;
		runOther(dom,()->new DavidChescEPH(rng.nextLong()), threadPool, "");

		/*val domain=new WarehouseScheduling(rng.nextLong(), true);
		domain.loadInstance(20);
		val heuristics=new ArrayList<AnnealingListHeuristic>(32);
		for(var i=0;i<64;i++){
			val heuristic=new AnnealingListHeuristic(rng.nextLong()).setTestSize(5);
			heuristic.setTimeLimit(1_000*60*15);
			heuristic.loadProblemDomain(domain.copy());
			heuristics.add(heuristic);
		}
		threadPool.invokeAll(heuristics.stream().map(h->{
			Callable<Long> r=()->h.run();
			return r;
		}).toList());
		val best=heuristics.stream().max(Comparator.comparingDouble(AnnealingListHeuristic::getBestValue)).orElseThrow().getBestHeuristic();
		out.println("Testing");*/
//		final Supplier<ExtendedHyperHeuristic> heuristicFactory =()->new AnnealingListHeuristic(rng.nextLong(),best.copy()).setReinit(true).setTestSize(3);
//		final Supplier<ExtendedHyperHeuristic> heuristicFactory =()->new GenerationalGeneticAlgorithm(rng.nextLong());
//		runWare(20, heuristicFactory, threadPool, "");
		threadPool.shutdown();
		log.flush();
	}

	private static void runWare(final int num, final Supplier<? extends ExtendedHyperHeuristic> heuristicFactory,
	                            final ExecutorService threadPool, final String header) {
		final var rng = RandomGenerator.getDefault();
		final var problem = new WarehouseScheduling(rng.nextLong(),true);
		problem.loadInstance(num);

		execute(() -> runExtHeuristic(problem.copy(), heuristicFactory.get(), 2_800_000),
		        32, threadPool, header);
	}

	private static void runOther(final Supplier<? extends ProblemDomain> problemFactory,
	                             final Supplier<? extends HyperHeuristic> heuristicFactory,
	                             final ExecutorService threadPool, final String header) {
		execute(() -> runHeuristic(problemFactory.get(), heuristicFactory.get(), 30_000),
		        32, threadPool, header);
	}

	private static void execute(final Supplier<RunResult> taskGenerator, final int runCount,
	                            final ExecutorService threadPool, final String header) {

		final var startTime = Instant.now();
		final var futures = IntStream.range(0, runCount)
		                             .mapToObj(i -> threadPool.submit(taskGenerator::get)).toList();
		final var results = futures.stream().map(ParallelRunner::futureGet).toList();
		final var finishTime = Instant.now();
		final var elapsed = Duration.between(startTime, finishTime);
		if (detail) printResults(results, elapsed, header);
		else printMeanOnly(results);
	}

	private static void printResults(final Collection<RunResult> results, final Duration elapsed, final String header) {
		final var succeed = results.stream().filter(r -> r.exception == null).toList();
		final var succeedCount = succeed.size();
		final var scores = succeed.stream().mapToDouble(RunResult::score).toArray();
		Arrays.sort(scores);
		final var min = Arrays.stream(scores).min().orElse(-1.0);
		final var max = Arrays.stream(scores).max().orElse(-1.0);
		final var avg = Arrays.stream(scores).average().orElse(-1.0);
		final double median;
		if(scores.length!=0)median = scores[scores.length / 2];
		else median=-1.0;
		final var exceptions = results.stream().filter(r -> r.exception != null).findAny();
		synchronized (log) {
			log.println(header);
			//noinspection AutoBoxing
			log.printf("Succeed: %d Elapsed: %d.%ds\nScores: %s\t%s\t%s\t%s\n", succeedCount, elapsed.toSeconds(),
			           elapsed.toMillisPart(), min, max, avg, median);
			exceptions.ifPresent(runResult -> runResult.exception.printStackTrace(log));
		}
	}

	private static void printMeanOnly(final Collection<RunResult> results) {
		final var exceptions = results.stream().filter(r -> r.exception != null).findAny();
		if (exceptions.isPresent()) System.out.print("Error");
		else log.print(results.stream().mapToDouble(RunResult::score).average().orElseThrow());
	}

	private static <T> T futureGet(final Future<T> future) {
		try {
			return future.get();
		} catch (final InterruptedException | ExecutionException e) {
			//noinspection ProhibitedExceptionThrown
			throw new RuntimeException(e);
		}
	}

	private static RunResult runExtHeuristic(final ExtendedProblemDomain domain, final ExtendedHyperHeuristic heuristic,
	                                         final int timeLimit) {
		heuristic.loadProblemDomain(domain);
		heuristic.setTimeLimit(1);
		heuristic.setInternalLimit(timeLimit);
		final var writer = new CharArrayWriter();
		try {
			final var time = heuristic.runWithInternal();
			domain.printStats(new PrintWriter(writer));
			return new RunResult(heuristic.getBestSolutionValue(), time, null, writer.toString());
		} catch (final RuntimeException e) {
			return new RunResult(0.0, 0, e, null);
		}
	}

	private static RunResult runHeuristic(final ProblemDomain domain, final HyperHeuristic heuristic,
	                                      final int timeLimit) {
		heuristic.loadProblemDomain(domain);
		heuristic.setTimeLimit(timeLimit);
		try {
			final var time = heuristic.run();
			return new RunResult(heuristic.getBestSolutionValue(), time, null, null);
		} catch (final RuntimeException e) {
			return new RunResult(0.0, 0, e, null);
		}
	}

	private static void print(final Object... args) {
		for (final var arg : args) {
			System.out.print(arg);
			System.out.print(' ');
		}
	}

	private static void println(final Object... args) {
		print(args);
		System.out.println();
	}

	record RunResult(double score, long time, Exception exception, String info) {}
}
