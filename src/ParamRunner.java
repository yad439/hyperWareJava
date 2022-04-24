import heuristic.StationaryGeneticAlgorithm;
import lombok.val;
import util.ParamConfig;
import warehouse.WarehouseScheduling;

import java.time.Duration;
import java.time.Instant;

final class ParamRunner {
	private ParamRunner() {}

	public static void main(final String[] args) {
		val config = ParamConfig.parse(args);
		val depth = Double.parseDouble(config.params().get("depth"));
		val intensity = Double.parseDouble(config.params().get("intensity"));
		val domain = new WarehouseScheduling(config.seed());
		domain.loadInstance(Integer.parseInt(config.instance()));
		val heuristic = new StationaryGeneticAlgorithm(config.seed())
				.setDepthOfSearch(depth)
				.setIntensityOfMutation(intensity);
		heuristic.loadProblemDomain(domain);
		heuristic.setTimeLimit((int) (config.cutoffTime() * 1000));
		heuristic.setInternalLimit(config.cutoffLength());
		val start= Instant.now();
		val time = heuristic.run();
		val end=Instant.now();
		//noinspection AutoBoxing
		System.out.printf("Result of this algorithm run: SAT, %d, %d, %s, %d", Duration.between(start,end).toSeconds(),
		                  config.cutoffLength(), heuristic.getBestSolutionValue(), config.seed());
	}
}
