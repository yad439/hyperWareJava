import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;
import extension.ExtendedHyperHeuristic;
import extension.ExtendedProblemDomain;
import heuristic.GenerationalGeneticAlgorithm;
import heuristic.PopulationBasedHeuristic;
import heuristic.PopulationBasedStagedHeuristic;
import heuristic.baseline.FairShareILS;
import heuristic.mutator.MutateLSMutator;
import heuristic.mutator.SolutionMutator;
import lombok.val;
import util.Pair;
import warehouse.SimulatedAnnealing;
import warehouse.WarehouseScheduling;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

public final class ParallelRunner {
/*import BinPacking.BinPacking;
import FlowShop.FlowShop;
import PersonnelScheduling.PersonnelScheduling;
import SAT.SAT;
import VRP.VRP;*/
//	private static final PrintWriter log;

	/*static {
		try {
			log = new PrintWriter(new BufferedWriter(new FileWriter("other_results2.txt", StandardCharsets.UTF_8, true)));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}*/

	private static final PrintWriter out = new PrintWriter(System.out, true, Charset.defaultCharset());
	private static final PrintWriter log = out;

	private static final boolean detail = true;
	private static final boolean ret = true;
	private static final double[] EMPTY_DOUBLES = {};

	private ParallelRunner() {}

	public static void main(final String[] args) throws IOException, InterruptedException {// 26, 31
		val maimThread = Thread.currentThread();
		val commandThread = new Thread(() -> {
			val reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
			var cont = true;
			try {
				while (cont) {
					val line = reader.readLine();
					if ("exit".equals(line)) {
						cont = false;
						maimThread.interrupt();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}, "Command Thread");
		commandThread.setDaemon(true);
		commandThread.start();

		final var rng = RandomGenerator.getDefault();
		val threadPool = Executors.newFixedThreadPool(Math.min(Runtime.getRuntime().availableProcessors(), 32 * 3));
		val taskPool = Executors.newFixedThreadPool(4);
		val domains = new int[]{20, 28, 37};;
//		val threadPool = Executors.newFixedThreadPool(6);
//		val instances = new int[]{5, 15, 20, 28, 37};

//		final var threadPool= Executors.newFixedThreadPool(32);
		final var annealing=new SimulatedAnnealing();

//		final var threadPool= Executors.newWorkStealingPool();
		for(val d:domains){
			annealing.loadInstance(d);
		final var settings=new SimulatedAnnealing.Settings(3_000_000/16,16);
		final var results = IntStream.range(0, 32)
		                             .mapToObj(i ->
				                                       threadPool.submit(
						                                       () ->annealing.run(settings)))
		                             .toList();
		final var results2 = results.stream().mapToDouble(ParallelRunner::futureGet).toArray();
			try (val writer = new PrintWriter("exps/"+d+"_annealing16" + ".dat")) {
				for(final var r:results2){
					writer.print(r);
					writer.print(' ');
				}
			}
		Arrays.sort(results2);
		out.println(d);
		log.println(d);
			log.print(Arrays.stream(results2).min().orElseThrow());
			log.print(' ');
			log.print(Arrays.stream(results2).max().orElseThrow());
			log.print(' ');
			log.print(Arrays.stream(results2).average().orElseThrow());//3363.0 4087.0 3671.548387096774
			log.print(' ');
			log.println(results2[results2.length/2]);//4407.0 4627.0 4491.96875 4492.0
		}
//		}
		log.flush();
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

		val heuristics = List.<Pair<String, Supplier<ExtendedHyperHeuristic>>>of(
				new Pair<>("Population based", () -> new PopulationBasedHeuristic(rng.nextLong())),
//				new Pair<>("Population based mutational",
//				           () -> new PopulationBasedHeuristic(rng.nextLong()).setMutator(new MutationOnlyMutator())),
//				new Pair<>("Population based ls->mut",
//				           () -> new PopulationBasedHeuristic(rng.nextLong()).setMutator(new LSToMutateMutator())),
//				new Pair<>("Population based mut->ls",
//				           () -> new PopulationBasedHeuristic(rng.nextLong()).setMutator(new MutateToLSMutator())),
//				new Pair<>("Populational local",()->new PopulationBasedHeuristic(rng.nextLong()).setMutator(new ExhaustiveMutator()))
				new Pair<>("Staged population based", () -> new PopulationBasedStagedHeuristic(rng.nextLong())),
//				new Pair<>("Staged population based 3",
//				           () -> new PopulationBasedStagedHeuristic(rng.nextLong()).setMutators(
//						           new SolutionMutator[]{new MutateLSMutator(), new MutateLSMutator(),
//						                                 new MutateLSMutator()}).setThresholds(new int[]{1, 6})),
		new Pair<>("Staged population based 10",
		           () -> new PopulationBasedStagedHeuristic(rng.nextLong()).setMutators(
				           new SolutionMutator[]{new MutateLSMutator(), new MutateLSMutator(),
				                                 new MutateLSMutator(), new MutateLSMutator(),
				                                 new MutateLSMutator(), new MutateLSMutator(),
				                                 new MutateLSMutator(), new MutateLSMutator(),
				                                 new MutateLSMutator(), new MutateLSMutator()}).setThresholds(new int[]{1, 4, 7, 10, 13, 16, 19, 22, 25})),

//				new Pair<>("Stationary genetic", () -> new StationaryGeneticAlgorithm(rng.nextLong())),
				new Pair<>("Generational genetic", () -> new GenerationalGeneticAlgorithm(rng.nextLong())),
				new Pair<>("FS-ILS", () -> new FairShareILS(rng.nextLong()))
//				new Pair<>("NR-FS-ILS", () -> new NoRestartFairShareILS(rng.nextLong()))
		                                                                        );
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
//			final Supplier<ExtendedHyperHeuristic> heuristicFactory = () -> new GenerationalGeneticAlgorithm(rng.nextLong())
//					.setMutator(new LSToMutateMutator().setLocalChooser(new MabChooser(1.0,false)))
//					.setSelector(new BatchAdaptor(new TournamentSelector(2)))
//					.setCrossoverChooser(new MabChooser())
//					.setTruncator(new BestTruncator(false,false))
//					.setPopulationSize(16)
//					.setGenerationSize(32)
//					.setWithoutImprovementLimit(16)
//				.setSolutionChooser(new RandomSelector())
//					.setDepthOfSearch(0.5)
//					.setIntensityOfMutation(1.0)
//					.setMutator(new LSToMutateMutator())
//					.setMutator(new MutateLSMutator().setMutationChooser(new ConstantChooser(finalI)))
//					.setCrossChooser(new ConstantChooser(finalI))
//					.setIntensityOfMutation(0.9);*/
		;
//			final int finalI = i;
//			final Supplier<ExtendedHyperHeuristic> heuristicFactory = () -> new PopulationBasedStagedHeuristic(rng.nextLong())
////					.setMutators(new SolutionMutator[] {new MutateToLSMutator(),new MutateLSMutator(),new MutateToLSMutator()})
////					.setThresholds(new int[]{5})
//					.setAcceptorFactory(()->new ContinuousAnnealingAcceptor(1e-4,false,1.0).setStartTemperature(1000))
//		;
//		final Supplier<ExtendedHyperHeuristic> heuristicFactory = () -> new OldPopulationHeuristic(rng.nextLong());
//		final var threadPool = Executors.newWorkStealingPool();


		/*val domainsTypes = List.<Pair<String, Supplier<ProblemDomain>>>of(
				new Pair<>("SAT", () -> new SAT(rng.nextLong())),
				new Pair<>("Bin packing", () -> new BinPacking(rng.nextLong())),
				new Pair<>("Flow shop", () -> new FlowShop(rng.nextLong())),
				new Pair<>("TSP", () -> new TSP(rng.nextLong())),
				new Pair<>("VRP", () -> new VRP(rng.nextLong())),
				new Pair<>("PS", () -> new PersonnelScheduling(rng.nextLong()))
		                                                                 );
		val domainsNumbers = new int[]{0, 2, 5, 8};
		val domains = domainsTypes.stream().flatMap(dom ->
				                                            Arrays.stream(domainsNumbers).mapToObj(
						                                            i -> new Pair<String, Supplier<ProblemDomain>>(
								                                            dom.first() + ' ' + i, () -> {
							                                            val d = dom.second().get();
							                                            d.loadInstance(i);
							                                            return d;
						                                            })
				                                                                                  )).toList();*/
//		val domains=Arrays.stream(new int[]{6,14,20,28,37}).mapToObj(i->new Pair<String,Supplier<WarehouseScheduling>>("warehouse_"+i,()->{
//			val dom=new WarehouseScheduling(rng.nextLong(), true);
//			dom.loadInstance(i);
//			return dom;
//		})).toList();
//		val domains = new int[]{20, 28, 37};
//		val resultsTable = Collections.synchronizedMap(
////				new LinkedHashMap<String, Map<String, Double>>(domains.size()));
//				new LinkedHashMap<String, Map<String, Double>>(domains.length));
//		final Function<String, Map<String, Double>> mapBuilder = s -> Collections.synchronizedMap(
//				new LinkedHashMap<>(
//						heuristics.size()));
		/*for (final var dom : domains) {
//		for(final var inst:instances){
			for (final var heuristic : heuristics) {
////		for(final var heuristicFactory:heuristics) {
////		runOther(problemFactory, heuristicFactory,threadPool);
////			System.out.print(heuristicFactory.get().toString());
////			System.out.print(": ");
////			System.out.println(chooser.get());
////			println(d);
				taskPool.submit(() -> {
					try {
//						val domName = dom.first();
						val domName = "warehouse_" + dom;
//						val str=inst+" "+heuristicFactory.get();
						val heuristicName = heuristic.first();
						val str = domName + ' ' + heuristicName;
						out.println(str);
//		runWare(inst, heuristicFactory, threadPool, str);
						val res = runWareAll(dom, heuristic.second(), threadPool, str, 3_000_000, 32);
//						val res = runOther(dom.second(), heuristic.second(), threadPool, str, 600_000, 32);

//						val dommap = resultsTable.computeIfAbsent(domName,
//						                                          mapBuilder);
//						dommap.put(heuristicName, res);
						try (val writer = new PrintWriter("exps/"+domName+"_"+heuristicName + ".dat")) {
							for(final var r:res){
								writer.print(r);
								writer.print(' ');
							}
						}
						out.println(str + " done");
					} catch (RuntimeException | FileNotFoundException e) {
						e.printStackTrace();
					}
				});
////			System.out.println();
			}
		}*/
		taskPool.shutdown();
		try {
			taskPool.awaitTermination(1, TimeUnit.DAYS);
		} catch (final InterruptedException e) {
			System.err.println("Interrupted");
			threadPool.shutdownNow();
		}
		threadPool.shutdown();
		log.flush();
		/*val domainsKeys = resultsTable.keySet().stream().toList();
		val heurKeys = resultsTable.get(domainsKeys.get(0)).keySet().stream().toList();
		try (val output = new PrintWriter("staged_results5.tsv", StandardCharsets.UTF_8)) {
			output.print("Domain\t");
			for (final var heur : heurKeys) {
				output.print(heur);
				output.print('\t');
			}
			output.println();
			for (final var dom : resultsTable.entrySet()) {
				output.print(dom.getKey());
				output.print('\t');
				for (final var heur : heurKeys) {
					output.print(dom.getValue().get(heur));
					output.print('\t');
				}
				output.println();
			}
		}*/
		/*final Supplier<ExtendedHyperHeuristic> heuristicFactory = () -> new PopulationBasedHeuristic(rng.nextLong())
//				.setDepthOfSearch(0.6772594059610966)
//						.setIntensityOfMutation(0.982379594260675)
//				.setMutator(new MutationOnlyMutator())
//				.setThresholds(new int[]{10})
				.setMutator(new RlMutator(8,false))
				;*/
		/*val heuristics= List.<Supplier<ExtendedHyperHeuristic>>of(
				()->new FairShareILS(rng.nextLong()),
				()-> new NoRestartFairShareILS(rng.nextLong()),
				() -> new PopulationBasedStagedHeuristic(rng.nextLong()),
				() -> new PopulationBasedHeuristic(rng.nextLong()),
				() -> new StationaryGeneticAlgorithm(rng.nextLong()),
				()->new GenerationalGeneticAlgorithm(rng.nextLong()),
				()->new PopulationBasedHeuristic(rng.nextLong()).setMutator(new MutationOnlyMutator()),
				()->new PopulationBasedHeuristic(rng.nextLong()).setMutator(new ExhaustiveMutator())
		                                                         );*/
		/*for(final var heuristicFactory:heuristics){
		runWare(26, heuristicFactory, threadPool, heuristicFactory.get().toString());
		}*/
		/*val inst=new WarehouseScheduling(rng.nextLong(),true);//4450.0	4788.0	4673.75	4701.0; 5378.0	6038.0	5632.53125	5621.0
		inst.loadInstance(31);
		final Supplier<ProblemDomain> dom= inst::copy;
		runOther(dom,()->new DavidChescEPH(rng.nextLong()), threadPool, "");*/

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
//		runWare(26, heuristicFactory, threadPool, "",3_000_000,32);
//		threadPool.shutdown();
//		log.flush();
	}

	private static double runWare(final int num,
	                              final Supplier<? extends ExtendedHyperHeuristic> heuristicFactory,
	                              final ExecutorService threadPool, final String header, final int limit,
	                              final int runCount) {
		final var rng = RandomGenerator.getDefault();
		final var problem = new WarehouseScheduling(rng.nextLong(), true);
		problem.loadInstance(num);

		return execute(() -> runExtHeuristic(problem.copy(), heuristicFactory.get(), limit),
		               runCount, threadPool, header);
	}

	private static double[] runWareAll(final int num,
	                              final Supplier<? extends ExtendedHyperHeuristic> heuristicFactory,
	                              final ExecutorService threadPool, final String header, final int limit,
	                              final int runCount) {
		final var rng = RandomGenerator.getDefault();
		final var problem = new WarehouseScheduling(rng.nextLong(), true);
		problem.loadInstance(num);

		return executeAll(() -> runExtHeuristic(problem.copy(), heuristicFactory.get(), limit),
		               runCount, threadPool, header);
	}

	private static double runOther(final Supplier<? extends ProblemDomain> problemFactory,
	                               final Supplier<? extends HyperHeuristic> heuristicFactory,
	                               final ExecutorService threadPool, final String header, final int limit,
	                               final int runCount) {
		return execute(() -> runHeuristic(problemFactory.get(), heuristicFactory.get(), limit),
		               runCount, threadPool, header);
	}

	private static double execute(final Supplier<RunResult> taskGenerator, final int runCount,
	                              final ExecutorService threadPool, final String header) {

		final var startTime = Instant.now();
		val tasks = IntStream.range(0, runCount)
		                     .<Callable<RunResult>>mapToObj(i -> taskGenerator::get).toList();
		try {
			final var results = threadPool.invokeAll(tasks).stream().map(ParallelRunner::futureGet).toList();
			final var finishTime = Instant.now();
			final var elapsed = Duration.between(startTime, finishTime);
			if (detail) printResults(results, elapsed, header);
			else printMeanOnly(results, header);
			if (ret) return results.stream().mapToDouble(RunResult::score).average().orElseThrow();
			else return 0.0;
		} catch (final InterruptedException e) {
			e.printStackTrace();
			return -1.0;
		}
	}

	private static double[] executeAll(final Supplier<RunResult> taskGenerator, final int runCount,
	                              final ExecutorService threadPool, final String header) {

		final var startTime = Instant.now();
		val tasks = IntStream.range(0, runCount)
		                     .<Callable<RunResult>>mapToObj(i -> taskGenerator::get).toList();
		try {
			final var results = threadPool.invokeAll(tasks).stream().map(ParallelRunner::futureGet).toList();
			final var finishTime = Instant.now();
			final var elapsed = Duration.between(startTime, finishTime);
			if (detail) printResults(results, elapsed, header);
			else printMeanOnly(results, header);
			if (ret) return results.stream().mapToDouble(RunResult::score).toArray();
			else return EMPTY_DOUBLES;
		} catch (final InterruptedException e) {
			e.printStackTrace();
			return EMPTY_DOUBLES;
		}
	}

	private static void printResults(final Collection<RunResult> results, final Duration elapsed,
	                                 final String header) {
		final var succeed = results.stream().filter(r -> r.exception == null).toList();
		final var succeedCount = succeed.size();
		final var scores = succeed.stream().mapToDouble(RunResult::score).toArray();
		Arrays.sort(scores);
		final var min = Arrays.stream(scores).min().orElse(-1.0);
		final var max = Arrays.stream(scores).max().orElse(-1.0);
		final var avg = Arrays.stream(scores).average().orElse(-1.0);
		final double median;
		if (scores.length != 0) median = scores[scores.length / 2];
		else median = -1.0;
		final var exceptions = results.stream().filter(r -> r.exception != null).findAny();
		synchronized (log) {
			log.println(header);
			//noinspection AutoBoxing
			log.printf("Succeed: %d Elapsed: %d.%ds\nScores: %s\t%s\t%s\t%s\n", succeedCount, elapsed.toSeconds(),
			           elapsed.toMillisPart(), min, max, avg, median);
			exceptions.ifPresent(runResult -> runResult.exception.printStackTrace(log));
		}
	}

	private static void printMeanOnly(final Collection<RunResult> results, final String header) {
		final var exceptions = results.stream().filter(r -> r.exception != null).findAny();
		synchronized (log) {
			log.print(header);
			log.print(": ");
			if (exceptions.isPresent()) log.println("Error");
			else log.println(results.stream().mapToDouble(RunResult::score).average().orElseThrow());
		}
	}

	private static <T> T futureGet(final Future<T> future) {
		try {
			return future.get();
		} catch (final InterruptedException | ExecutionException e) {
			//noinspection ProhibitedExceptionThrown
			throw new RuntimeException(e);
		}
	}

	private static RunResult runExtHeuristic(final ExtendedProblemDomain domain,
	                                         final ExtendedHyperHeuristic heuristic,
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
