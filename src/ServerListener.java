import heuristic.GenerationalGeneticAlgorithm;
import lombok.val;
import util.Json;
import warehouse.WarehouseScheduling;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.random.RandomGenerator;

public final class ServerListener {
	private ServerListener() {}

	public static void main(final String[] args) {
		val threadPool = Executors.newCachedThreadPool();
		try (val server = new ServerSocket(7681)) {
			while (true) {
				val connection = server.accept();
				threadPool.submit(() -> serve(connection));
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
		threadPool.shutdown();
	}

	private static void serve(final Socket connection) {
		try (connection) {
			System.out.println("connected");
			val request = Json.parse(new InputStreamReader(new BufferedInputStream(connection.getInputStream()),
			                                               StandardCharsets.UTF_8));
			System.out.println("run");
			val rng = RandomGenerator.getDefault();
			val domain = new WarehouseScheduling(rng.nextInt(), false);
			domain.loadInstance(26);
			final var depth = request.getDouble("depth");
			final var intensity = request.getDouble("intensity");
			System.out.println(depth);
			System.out.println(intensity);
			val heuristic = new GenerationalGeneticAlgorithm(rng.nextLong())
					.setDepthOfSearch(depth)
					.setIntensityOfMutation(intensity);
			heuristic.loadProblemDomain(domain);
			heuristic.setTimeLimit(1);
			heuristic.setInternalLimit(3_000_000);
			heuristic.runWithInternal();
			val result = heuristic.getBestSolutionValue();
			System.out.println(result);
			val output = new PrintWriter(new BufferedOutputStream(connection.getOutputStream()), false,
			                             StandardCharsets.UTF_8);
			output.println(result);
			output.flush();
		} catch (final Throwable e) {
			e.printStackTrace();
		}
	}
}
