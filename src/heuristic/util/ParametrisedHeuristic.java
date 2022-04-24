package heuristic.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Delegate;
import lombok.val;

@RequiredArgsConstructor
@AllArgsConstructor
public final class ParametrisedHeuristic implements Heuristic {
	@Delegate(excludes = NotDelegate.class)
	private final Heuristic inner;
	@Getter
	@Setter
	private double depthOfSearch;
	@Getter
	@Setter
	private double intensityOfMutation;

	@Override
	public double apply(final Solution source, final Solution destination) {
		val domain = inner.getDomain();
		domain.setIntensityOfMutation(intensityOfMutation);
		domain.setDepthOfSearch(depthOfSearch);
		return inner.apply(source, destination);
	}

	@SuppressWarnings({"InterfaceMayBeAnnotatedFunctional", "InterfaceNeverImplemented"})
	private interface NotDelegate{
		double apply(final Solution source, final Solution destination);
	}
}
