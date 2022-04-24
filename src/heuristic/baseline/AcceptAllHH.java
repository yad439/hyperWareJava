package heuristic.baseline; /**
 * @author Steven Adriaensen
 * 
 * The AA-HH hyperheuristic used in 
 * 
 * Adriaensen, Steven, Gabriela Ochoa, and Ann Now�. 
 * "A Benchmark Set Extension and Comparative Study for the HyFlex Framework." 
 * Evolutionary Computation (CEC), 2015 IEEE Congress on. IEEE, 2015.
 */


import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;

public class AcceptAllHH extends HyperHeuristic {
	
	public AcceptAllHH(long r) {
		super(r); 
	}
	
	public void solve(ProblemDomain problem) {
		int hs = problem.getNumberOfHeuristics();
		problem.initialiseSolution(0);
		while (!hasTimeExpired()) {
			int v = rng.nextInt(hs);
			problem.applyHeuristic(v, 0, 0);
		}
	}

	public String toString() {
		return "AA-HH";
	}

}
