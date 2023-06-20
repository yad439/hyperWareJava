package warehouse;

import AbstractClasses.ProblemDomain;
import extension.ExtendedProblemDomain;
import lombok.val;

import java.io.FileNotFoundException;
import java.io.IOError;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.IntStream;

@SuppressWarnings({"ClassWithTooManyMethods", "ClassNamePrefixedWithPackageName"})
public final class WarehouseScheduling extends ExtendedProblemDomain {
	private final Path dataDir;
	private static final int HEURISTIC_NUMBER = 30;
	private final Heuristic[] heuristics;
	private final CrossHeuristic[] crossoverHeuristics;
	private final Heuristics heuristicService;
	private final boolean isNative;
	private Instance instance = null;
	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	private Solution[] solutions;
	private int bestValue = Integer.MAX_VALUE;

	@Deprecated
	public WarehouseScheduling(final long seed) {this(seed, false, null);}

	public WarehouseScheduling(final long seed, final boolean isNative) {this(seed, isNative, null);}

	public WarehouseScheduling(final long seed, final boolean isNative, final String dataDir) {
		super(seed);
		this.isNative = isNative;
		if (dataDir != null) this.dataDir = Path.of(dataDir);
		else {
			val env = System.getenv("WARE_DATA");
			val relative = Path.of("data", "instances");
			if (env == null) this.dataDir = relative;
			else this.dataDir = Path.of(env).resolve(relative);
		}
		heuristicService = new Heuristics(rng);
		heuristicService.setIntensity(getIntensityOfMutation());
		heuristicService.setDepth(getDepthOfSearch());
		heuristics = new Heuristic[]{
				new Heuristic(HeuristicType.MUTATION, false, false, heuristicService::randomSwap, "Random swap"),
				new Heuristic(HeuristicType.MUTATION, false, false, heuristicService::randomMove, "Random move"),
				new Heuristic(HeuristicType.MUTATION, true, false, heuristicService::multipleSwap, "Multiple swap"),
				new Heuristic(HeuristicType.MUTATION, true, false, heuristicService::multipleMove, "Multiple move"),
				new Heuristic(HeuristicType.MUTATION, false, false, heuristicService::shuffle, "Shuffle"),
				new Heuristic(HeuristicType.MUTATION, true, false, heuristicService::shufflePart, "Shuffle part"),
				new Heuristic(HeuristicType.MUTATION, true, false, heuristicService::shuffleSpan, "Shuffle span"),
				new Heuristic(HeuristicType.RUIN_RECREATE, true, false, heuristicService::itemBased, "Item-based"),
				new Heuristic(HeuristicType.RUIN_RECREATE, true, false, heuristicService::itemBasedSpan,
				              "Item-based span"),
				new Heuristic(HeuristicType.RUIN_RECREATE, true, false, heuristicService::greedyConstruct,
				              "Greedy construct"),
				new Heuristic(HeuristicType.RUIN_RECREATE, true, false, heuristicService::greedyConstructSpan,
				              "Greedy construct span"),
				new Heuristic(HeuristicType.LOCAL_SEARCH, false, true, heuristicService::localSearch, "Local search"),
				new Heuristic(HeuristicType.LOCAL_SEARCH, true, true, heuristicService::localSearchPart,
				              "Local search part"),
				new Heuristic(HeuristicType.LOCAL_SEARCH, true, true, heuristicService::localSearchSpan,
				              "Local search span"),
				new Heuristic(HeuristicType.LOCAL_SEARCH, false, true, heuristicService::localSearchFirst,
				              "Local search first"),
				new Heuristic(HeuristicType.LOCAL_SEARCH, true, true, heuristicService::localSearchPartFirst,
				              "Local search part first"),
				new Heuristic(HeuristicType.LOCAL_SEARCH, true, true, heuristicService::localSearchSpanFirst,
				              "Local search span first"),
				new Heuristic(HeuristicType.LOCAL_SEARCH, false, true, heuristicService::randomLocalSearch,
				              "Random local search"),
				new Heuristic(HeuristicType.LOCAL_SEARCH, false, true, heuristicService::randomLocalSearchFirst,
				              "Random local search first"),
				new Heuristic(HeuristicType.LOCAL_SEARCH, false, true, heuristicService::linKernighan,
				              "Lin-Kernighan iteration"),
				new Heuristic(HeuristicType.LOCAL_SEARCH, false, true, heuristicService::linKernighanIterative,
				              "Lin-Kernighan search"),
				new Heuristic(HeuristicType.LOCAL_SEARCH, true, true, heuristicService::annealingLS,
				              "Simulated annealing search"),
				new Heuristic(HeuristicType.MUTATION, true, true, heuristicService::annealingMutation,
				              "Simulated annealing mutation")
		};
		crossoverHeuristics = new CrossHeuristic[]{
				new CrossHeuristic(false, false, heuristicService::pmxCross, "PMX crossover"),
				new CrossHeuristic(false, false, heuristicService::ox1Cross, "OX1 crossover"),
				new CrossHeuristic(false, false, heuristicService::cycleCross, "Cycle crossover"),
				new CrossHeuristic(false, false, heuristicService::oneXCross, "OneX crossover"),
				new CrossHeuristic(false, false, heuristicService::ppxCross, "PPX crossover"),
				new CrossHeuristic(true, false, heuristicService::sortCross, "Sort crossover"),
				new CrossHeuristic(true, false, heuristicService::randomSortCross, "Random sort crossover")
		};
		assert heuristics.length + crossoverHeuristics.length == HEURISTIC_NUMBER;
	}

	public int getMemorySize() {return solutions.length;}

	@Override
	public void setMemorySize(final int i) {if (solutions == null || solutions.length < i) solutions = new Solution[i];}

	@Override
	public void initialiseSolution(final int i) {
		final int[] permutation = randomPermutation();
		final var score = instance.evaluate(permutation);
		solutions[i] = new Solution(permutation, score);
		if (score < bestValue) bestValue = score;
	}

	@SuppressWarnings("SuspiciousGetterSetter")
	@Override
	public int getNumberOfHeuristics() {return HEURISTIC_NUMBER;}

	@Override
	public double applyHeuristic(final int i, final int i1, final int i2) {
		final var destination =
				solutions[i2] != null ? solutions[i2].permutation() : new int[solutions[i1].permutation().length];
		final var callCounter = new IntRef();
		final int result;
		final long elapsed;
		if (i < heuristics.length) {
			final var startTime = System.currentTimeMillis();
			result = heuristics[i].function().apply(solutions[i1].permutation(), destination, callCounter);
			elapsed = System.currentTimeMillis() - startTime;
		} else {
			final var parent1 = solutions[i1].permutation();
			final var parent2 = randomPermutation();
			final var startTime = System.currentTimeMillis();
			if (!Arrays.equals(parent1, parent2)) {
				result = crossoverHeuristics[i - heuristics.length].function()
				                                                   .apply(parent1, parent2, destination, callCounter);
			} else {
				System.arraycopy(parent1, 0, destination, 0, parent1.length);
				result = solutions[i1].length();
			}
			elapsed = System.currentTimeMillis() - startTime;
		}
		assert result != -1;
		assert Utils.isPermutation(destination);
		solutions[i2] = new Solution(destination, result);
		if (result < bestValue) bestValue = result;
		heuristicCallRecord[i]++;
		//noinspection NumericCastThatLosesPrecision
		heuristicCallTimeRecord[i] += (int) elapsed;
		internalTimes[i] += callCounter.getValue();
		totalInternalTime += callCounter.getValue();
		return result;
	}

	@Override
	public double applyHeuristic(final int i, final int i1, final int i2, final int i3) {
		if (i < heuristics.length) return applyHeuristic(i, i1, i3);

		final var child =
				solutions[i3] != null ? solutions[i3].permutation() : new int[solutions[i1].permutation().length];
		final var callCounter = new IntRef();
		final int result;
		final var startTime = System.currentTimeMillis();
		if (!Arrays.equals(solutions[i1].permutation(), solutions[i2].permutation()))
			result = crossoverHeuristics[i - heuristics.length].function()
			                                                   .apply(solutions[i1].permutation(),
			                                                          solutions[i2].permutation(), child, callCounter);
		else {
			System.arraycopy(solutions[i1].permutation(), 0, child, 0, child.length);
			result = solutions[i1].length();
		}
		final var elapsed = System.currentTimeMillis() - startTime;
		assert result != -1;
		assert Utils.isPermutation(child);
		solutions[i3] = new Solution(child, result);
		if (result < bestValue) bestValue = result;
		heuristicCallRecord[i]++;
		//noinspection NumericCastThatLosesPrecision
		heuristicCallTimeRecord[i] += (int) elapsed;
		internalTimes[i] += callCounter.getValue();
		totalInternalTime += callCounter.getValue();
		return result;
	}

	@Override
	public void copySolution(final int i, final int i1) {
		final var src = solutions[i];
		final var destArr = solutions[i1] != null ? solutions[i1].permutation() : new int[src.permutation().length];
		System.arraycopy(src.permutation(), 0, destArr, 0, destArr.length);
		solutions[i1] = new Solution(destArr, src.length());
	}

	@Override
	public String toString() {
		final var name = "Warehouse scheduling problem";
		if (instance == null) return name;
		//noinspection AutoBoxing
		return String.format("%s (n=%d)", name, instance.jobCount());
	}

	@Override
	public int getNumberOfInstances() {
		try (final var files = Files.list(dataDir)) {
			//noinspection NumericCastThatLosesPrecision
			return (int) files.count();
		} catch (final IOException e) {
			throw new IOError(e);
		}
	}

	@Override
	public String bestSolutionToString() {return solutionToString(getBest());}

	@Override
	public double getBestSolutionValue() {return getBestOfAllTime();}

	@Override
	public String solutionToString(final int i) {
		return solutions[i] != null ? Arrays.toString(solutions[i].permutation()) : "null";
	}

	@Override
	public double getFunctionValue(final int i) {
		return solutions[i] != null ? solutions[i].length() : Integer.MAX_VALUE;
	}

	@Override
	public boolean compareSolutions(final int i, final int i1) {
		if (solutions[i].length() != solutions[i1].length()) return false;
		return Arrays.equals(solutions[i].permutation(), solutions[i1].permutation());
	}

	public double getBestOfAllTime() {return bestValue;}

	public double getCurrentBest() {return getFunctionValue(getBest());}

	@Override
	public void reset() {
		super.reset();
		bestValue = Integer.MAX_VALUE;
	}

	@Override
	public String getHeuristicName(final int index) {
		if (index < heuristics.length) return heuristics[index].name();
		return crossoverHeuristics[index - heuristics.length].name();
	}

	public void setSeed(final long seed) {rng.setSeed(seed);}

	public WarehouseScheduling copy() {
		final var newCopy = new WarehouseScheduling(rng.nextLong());
		newCopy.instance = instance;
		newCopy.heuristicService.setProblem(instance);
		return newCopy;
	}

	@Override
	public void setDepthOfSearch(final double depthOfSearch) {
		super.setDepthOfSearch(depthOfSearch);
		if (heuristicService != null) heuristicService.setDepth(depthOfSearch);
	}

	@Override
	public void setIntensityOfMutation(final double intensityOfMutation) {
		super.setIntensityOfMutation(intensityOfMutation);
		if (heuristicService != null) heuristicService.setIntensity(intensityOfMutation);
	}

	@Override
	public int[] getHeuristicsOfType(final HeuristicType heuristicType) {
		return heuristicType == HeuristicType.CROSSOVER
		       ? IntStream.range(heuristics.length, heuristics.length + crossoverHeuristics.length).toArray()
		       : IntStream.range(0, heuristics.length).filter(i -> heuristics[i].type() == heuristicType).toArray();
	}

	@Override
	public int[] getHeuristicsThatUseIntensityOfMutation() {
		return IntStream.concat(
				IntStream.range(0, heuristics.length).filter(i -> heuristics[i].usesIntensity()),
				IntStream.range(heuristics.length, heuristics.length + crossoverHeuristics.length)
				         .filter(i -> crossoverHeuristics[i - heuristics.length].usesIntensity())
		                       ).toArray();
	}

	@Override
	public int[] getHeuristicsThatUseDepthOfSearch() {
		return IntStream.concat(
				IntStream.range(0, heuristics.length).filter(i -> heuristics[i].usesDepth()),
				IntStream.range(heuristics.length, heuristics.length + crossoverHeuristics.length)
				         .filter(i -> crossoverHeuristics[i - heuristics.length].usesDepth())
		                       ).toArray();
	}

//	public void ps(){
//		heuristicService.ps();
//	}

	@Override
	public void loadInstance(final int i) {
		//noinspection VariableNotUsedInsideIf
		if (instance != null) reset();
		//noinspection AutoBoxing
		final var dataPath = dataDir.resolve(String.format("%d.dat", i));
		try {
			if (Files.exists(dataPath)) {
				val jInstance = InstanceParser.parse(dataPath);
				instance = isNative ? new NativeInstance(jInstance) : jInstance;
			} else {
				val url = getClass().getClassLoader().getResource(dataPath.toString().replace('\\', '/'));
				if (url == null) throw new RuntimeException(new FileNotFoundException(dataPath.toString()));
				val jInstance = InstanceParser.parse(url);
				instance = isNative ? new NativeInstance(jInstance) : jInstance;
			}
			heuristicService.setProblem(instance);
		} catch (final IOException e) {
			throw new IOError(e);
		}
	}

	private int getBest() {
		var minInd = -1;
		var minVal = Integer.MAX_VALUE;
		for (var i = 0; i < solutions.length; i++)
			if (solutions[i] != null && solutions[i].length() < minVal) {
				minVal = solutions[i].length();
				minInd = i;
			}
		return minInd;
	}

	private int[] randomPermutation() {
		final var permutation = IntStream.range(0, instance.jobCount()).toArray();
		Utils.shuffle(permutation, rng);
		return permutation;
	}

	private record CrossHeuristic(boolean usesIntensity, boolean usesDepth, CrossFunction function, String name) {
		@FunctionalInterface
		private interface CrossFunction {
			int apply(int[] parent1, int[] parent2, int[] child, IntRef callCounter);
		}
	}

	private record Heuristic(ProblemDomain.HeuristicType type, boolean usesIntensity, boolean usesDepth,
	                         Function function, String name) {
		@FunctionalInterface
		private interface Function {
			int apply(int[] source, int[] destination, IntRef callCounter);
		}
	}

	private record Solution(int[] permutation, int length) {}
}
