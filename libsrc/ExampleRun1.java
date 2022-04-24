import heuristic.AnnealingHeuristic;
import warehouse.WarehouseScheduling;

import java.util.Arrays;

/**
 * This class shows how to run a selected hyper-heuristic on a selected problem domain.
 * It shows the minimum that must be done to test a hyper heuristic on a problem domain, and it is 
 * intended to be read before the ExampleRun2 class, which provides an example of a more complex set-up
 */
public class ExampleRun1 {

	public static void main(String[] args) {

		//create a ProblemDomain object with a seed for the random number generator
		//ProblemDomain problem = new SAT(1234);
		final var problem=new WarehouseScheduling(1234);

//		System.out.println(problem.getNumberOfInstances());

		//creates an ExampleHyperHeuristic object with a seed for the random number generator
//		HyperHeuristic hyper_heuristic_object = new ExampleHyperHeuristic1(5678);
//		final var hyper_heuristic_object= new heuristics.AcceptAllHH(5678);
//		final var hyper_heuristic_object= new heuristics.AcceptNoWorseHH(5678);
//		final var hyper_heuristic_object= new heuristics.FairShareILS(5678);
//		final var hyper_heuristic_object= new heuristics.NoRestartFairShareILS(5678);
		final var hyper_heuristic_object= new AnnealingHeuristic(5678);

		//we must load an instance within the problem domain, in this case we choose instance 2
		problem.loadInstance(18);

		//we must set the time limit for the hyper-heuristic in milliseconds, in this example we set the time limit to 1 minute
		hyper_heuristic_object.setTimeLimit(6000);

		//a key step is to assign the ProblemDomain object to the HyperHeuristic object. 
		//However, this should be done after the instance has been loaded, and after the time limit has been set
		hyper_heuristic_object.loadProblemDomain(problem);

		//now that all of the parameters have been loaded, the run method can be called.
		//this method starts the timer, and then calls the solve() method of the hyper_heuristic_object.
		hyper_heuristic_object.run();

		//obtain the best solution found within the time limit
		System.out.println(hyper_heuristic_object.getBestSolutionValue());
		System.out.println(problem.bestSolutionToString());
		for(var i=0;i<problem.getMemorySize();i++){
			System.out.println(problem.getFunctionValue(i));
			System.out.println(problem.solutionToString(i));
		}
		System.out.println(Arrays.toString(hyper_heuristic_object.getFitnessTrace()));
	}
}