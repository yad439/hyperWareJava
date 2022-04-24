package AbstractClasses;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Random;

public abstract class HyperHeuristic {
	private static final long NANOS_IN_MILLIS = ChronoUnit.MILLIS.getDuration().toNanos();
	protected Random rng;
	private long timeLimit = 0L;
	private long initialTime;
	private ThreadMXBean bean;
	private ProblemDomain problem;
	private double printfraction;
	private double printlimit;
	private int lastprint;
	private boolean initialprint;
	private double lastbestsolution = -1.0D;
	private double[] trace;
	private static final int tracecheckpoints = 101;

	protected HyperHeuristic(long seed) {
		this.rng = new Random(seed);
	}

	protected HyperHeuristic() {
		this.rng = new Random();
	}

	public void setTimeLimit(final long time_in_milliseconds) {
//      if (!timelimitset) {
		this.timeLimit = Duration.ofMillis(time_in_milliseconds).toNanos();

		this.printfraction = (double)timeLimit / (tracecheckpoints - 1);
		this.printlimit = this.printfraction;
		this.initialprint = false;
		this.lastprint = 0;
//		timelimitset = true;
//      } else {
//         System.err.println("The time limit cannot be set twice. " + this.toString());
//         System.exit(-1);
//      }

	}

	public long getTimeLimit() {
		return this.timeLimit / NANOS_IN_MILLIS;
	}

	public long getElapsedTime() {
		return this.bean == null ? 0L : (this.bean.getCurrentThreadCpuTime() - this.initialTime) / NANOS_IN_MILLIS;
	}

	public final double getBestSolutionValue() {
      /*if (this.lastbestsolution == -1.0D) {
         System.err.println("The hasTimeExpired() method has not been called yet. It must be called at least once before a call to getBestSolutionValue()");
         System.exit(1);
      }*/

		return problem.getBestSolutionValue();
	}

	public double[] getFitnessTrace() {
		return this.trace;
	}

	protected boolean hasTimeExpired() {
		final long time = this.bean.getCurrentThreadCpuTime() - this.initialTime;
		if (!this.initialprint) {
			this.initialprint = true;
			final double res = this.problem.getBestSolutionValue();
			this.trace[0] = res;
			this.lastbestsolution = res;
		} else if (time >= this.printlimit) {
			int thisprint = (int) (time / this.printfraction);
			if (thisprint > 100) {
				thisprint = 100;
			}

			final var res = this.problem.getBestSolutionValue();
			Arrays.fill(trace, lastprint + 1, thisprint + 1, res);
         /*for(int x = 0; x < thisprint - this.lastprint; ++x) {
            if (time <= this.timeLimit) {
               this.trace[this.lastprint + x + 1] = res;
               this.lastbestsolution = res;
            } else {
               this.trace[this.lastprint + x + 1] = this.lastbestsolution;
            }

            this.printlimit += this.printfraction;
         }*/

			this.lastprint = thisprint;
		}

		//         this.lastbestsolution = this.problem.getBestSolutionValue();
		return time >= this.timeLimit;
	}

	public void loadProblemDomain(ProblemDomain problemdomain) {
//      if (this.timeLimit == 0L) {
//         System.err.println("No problem instance has been loaded in the ProblemDomain object with problem.loadInstance()");
//         System.exit(1);
//      }

		this.problem = problemdomain;
	}

	public long run() {
		if (this.problem == null) {
			System.err.println("No problem domain has been loaded with loadProblemDomain()");
			System.exit(1);
		}

		if (this.timeLimit == 0L) {
			System.err.println("No time limit has been set with setTimeLimit()");
			System.exit(1);
		}

		this.trace = new double[tracecheckpoints];
		this.bean = ManagementFactory.getThreadMXBean();
		this.initialTime = this.bean.getCurrentThreadCpuTime();
		this.solve(this.problem);
		return getElapsedTime();
	}

	protected abstract void solve(ProblemDomain domain);

	public abstract String toString();
}
