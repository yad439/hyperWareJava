package extension;

import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;

public abstract class ExtendedHyperHeuristic extends HyperHeuristic {
	private boolean useDomainTime=false;
	private ExtendedProblemDomain extendedDomain=null;
	private long internalLimit=0;

	protected ExtendedHyperHeuristic(final long seed) {super(seed);}

	protected ExtendedHyperHeuristic() {}

	protected double getProgress() {
		return (double) getElapsedTime() / getTimeLimit();
	}

	public void setInternalLimit(final long internalLimit) {
		this.internalLimit = internalLimit;
	}

	public long runWithInternal(){
		useDomainTime=true;
		extendedDomain.setUseInternalTime(true);
		return super.run();
	}

	@Override
	public void loadProblemDomain(final ProblemDomain problemdomain) {
		super.loadProblemDomain(problemdomain);
		if(problemdomain instanceof ExtendedProblemDomain extended)extendedDomain=extended;
		else extendedDomain=null;
	}

	@Override
	public long getTimeLimit() {
		if(useDomainTime)return internalLimit;
		return super.getTimeLimit();
	}

	@Override
	public long getElapsedTime() {
		if(useDomainTime)return extendedDomain.getTotalInternalTime();
		return super.getElapsedTime();
	}

	@Override
	protected boolean hasTimeExpired() {
		if(!useDomainTime)return super.hasTimeExpired();
		return extendedDomain.getTotalInternalTime()>=internalLimit;
	}

	@Override
	public long run() {
		useDomainTime=false;
		if(extendedDomain!=null)extendedDomain.setUseInternalTime(false);
		return super.run();
	}
}
