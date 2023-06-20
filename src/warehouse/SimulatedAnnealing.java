package warehouse;

import lombok.val;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

public final class SimulatedAnnealing {
	private JInstance instance = null;
	private double startTemp = 0;

	public int[] bestS = null;
	public int bestVal = Integer.MAX_VALUE;

	public void loadInstance(final int number) throws IOException {
		val env = System.getenv("WARE_DATA");
		val relative = Path.of("data", "instances");
		final Path dir;
		if (env == null) dir = relative;
		else dir = Path.of(env).resolve(relative);
		instance = InstanceParser.parse(dir.resolve(String.format("%d.dat", number)));
		final var sol = IntStream.range(0, instance.jobCount()).toArray();
		Utils.shuffle(sol, RandomGenerator.getDefault());
		var max = Utils.computeSchedule(sol, instance, true);
		var min = max;
		for (var i = 0; i < instance.jobCount(); i++)
			for (var j = 0; j < i; j++) {
				swap(sol, i, j);
				final var res = Utils.computeSchedule(sol, instance, true);
				swap(sol, i, j);
				min = Math.min(min, res);
				max = Math.max(max, res);
			}
		for (var i = 0; i < instance.jobCount(); i++)
			for (var j = 0; j < instance.jobCount(); j++)
				if (i != j) {
					move(sol, i, j);
					final var res = Utils.computeSchedule(sol, instance, true);
					move(sol, j, i);
					min = Math.min(min, res);
					max = Math.max(max, res);
				}
		startTemp = ((double) (max - min)) / 2;
	}

	public int run(final Settings settings) {
		final var rng = new Random();
		final var solution = IntStream.range(0, instance.jobCount()).toArray();
		Utils.shuffle(solution, RandomGenerator.getDefault());
		var value = Utils.computeSchedule(solution, instance, true);
		var minVal = value;
		final var power = Math.pow((-startTemp * Math.log(1.0e-3)), (-1.0 / (settings.searchTries)));
		var temperature = startTemp;
		for (var t = 0; t < settings.searchTries; t++) {
			boolean isSwap = false;
			int bestI = 0, bestJ = 0, bestScore = Integer.MAX_VALUE;
			for (var att = 0; att < settings.samples(); att++) {
				final var i = rng.nextInt(solution.length);
				int j;
				do {
					j = rng.nextInt(solution.length);
				} while (j == i);
				if (rng.nextBoolean()) {
					swap(solution, i, j);
					final var newVal = Utils.computeSchedule(solution, instance, true);
					if (newVal < bestScore) {
						bestScore = newVal;
						isSwap = true;
						bestI = i;
						bestJ = j;
					}
					swap(solution, i, j);
					if (newVal < minVal) minVal = newVal;
				} else {
					move(solution, i, j);
					final var newVal = Utils.computeSchedule(solution, instance, true);
					if (newVal < bestScore) {
						bestScore = newVal;
						isSwap = false;
						bestI = i;
						bestJ = j;
					}
					move(solution, j, i);
					if (newVal < minVal) minVal = newVal;
				}
			}
			if (rng.nextDouble() < Math.exp((value - bestScore) / temperature)) {
				value = bestScore;
				if (isSwap) swap(solution, bestI, bestJ);
				else move(solution, bestI, bestJ);
			}
			if (minVal < bestVal) {
				bestVal = minVal;
				bestS = solution.clone();
			}
			temperature *= power;
		}
		return minVal;
	}

	private static void swap(final int[] destination, final int i1, final int i2) {
		final var tmp = destination[i1];
		destination[i1] = destination[i2];
		destination[i2] = tmp;
	}

	private static void move(final int[] destination, final int from, final int to) {
		final var tmp = destination[from];
		if (to < from) System.arraycopy(destination, to, destination, to + 1, from - to);
		else System.arraycopy(destination, from + 1, destination, from, to - from);
		destination[to] = tmp;
	}

	public record Settings(int searchTries, int samples) {}
}
