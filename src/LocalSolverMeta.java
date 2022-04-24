import heuristic.GenerationalGeneticAlgorithm;
import localsolver.LSExternalArgumentValues;
import localsolver.LocalSolver;
import lombok.val;
import warehouse.WarehouseScheduling;

import java.util.random.RandomGenerator;

public final class LocalSolverMeta {
	private LocalSolverMeta() {}

	public static void main(final String[] args) {
		val domain=new WarehouseScheduling(RandomGenerator.getDefault().nextLong(), true);
		domain.loadInstance(26);
		try(val solver=new LocalSolver()){
			val model=solver.getModel();
			val func=model.doubleExternalFunction(vals->run(vals,domain));
			func.getExternalContext().enableSurrogateModeling().setEvaluationLimit(10);
			val depth=model.floatVar(0,1);
			val intensity=model.floatVar(0,1);
			model.minimize(model.call(func,depth,intensity));
			model.close();

			solver.getParam().setNbThreads(6);
			solver.solve();

			val sol=solver.getSolution();
			System.out.println(sol.getDoubleValue(depth));
			System.out.println(sol.getDoubleValue(intensity));
		}
	}

	private static double run(final LSExternalArgumentValues argumentValues, final WarehouseScheduling domain){
		val heuristic=new GenerationalGeneticAlgorithm(RandomGenerator.getDefault().nextLong())
				.setDepthOfSearch(argumentValues.getDoubleValue(0))
				.setIntensityOfMutation(argumentValues.getDoubleValue(1));
		heuristic.loadProblemDomain(domain.copy());
		heuristic.setTimeLimit(1);
		heuristic.setInternalLimit(3_000_000);
		heuristic.runWithInternal();
		return heuristic.getBestSolutionValue();
	}
}
