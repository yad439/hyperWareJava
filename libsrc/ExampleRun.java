/*
 * author: Steven Adriaensen
 * date: 22/01/2014
 * contact: steven.adriaensen@vub.ac.be
 * affiliation: Vrije Universiteit Brussel
 */

import heuristic.baseline.FairShareILS;
import warehouse.WarehouseScheduling;

import java.util.Arrays;

public class ExampleRun {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final var domain=new WarehouseScheduling(567);
		final var heuristic=new FairShareILS(1234);
		heuristic.setTimeLimit(10_000);
		for(var i=0;i<domain.getNumberOfInstances();i++){
			domain.loadInstance(i);
			heuristic.loadProblemDomain(domain);
			heuristic.run();
			System.out.print(i);
			System.out.print(": ");
			System.out.print(heuristic.getBestSolutionValue());
			System.out.print(" ");
			System.out.println(Arrays.stream(heuristic.getFitnessTrace()).min());
		}
	}
}
