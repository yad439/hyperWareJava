package heuristic.util;

import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.Arrays;
import java.util.random.RandomGenerator;
import java.util.stream.DoubleStream;

@UtilityClass
public class Utils {
	public int selectWithProbability(final RandomGenerator rng,final double[] probabilities) {
		final var sum = Arrays.stream(probabilities).sum();
		if(sum==0.0){
			return rng.nextInt(probabilities.length);
		}
		final var selector = rng.nextDouble(sum);
		var partialSum = 0.0;
		var choice = -1;
		while (partialSum < selector) {
			choice++;
			partialSum += probabilities[choice];
		}
		return choice;
	}

	public int argmax(final double[] values){
		var maxInd=0;
		var maxVal=values[0];
		for(var i=1;i<values.length;i++)
			if(values[i]>maxVal){
				maxInd=i;
				maxVal=values[i];
			}
		return maxInd;
	}

	public int argmax(final DoubleStream values){
		final var iter=values.iterator();
		var maxInd=0;
		var maxVal=iter.nextDouble();
		for (var i=1;iter.hasNext();i++){
			final var val=iter.nextDouble();
			if(val>maxVal){
				maxInd=i;
				maxVal=val;
			}
		}
		return maxInd;
	}

	public int argmin(final int[] values){
		var minInd=0;
		var minVal=values[0];
		for(var i=1;i<values.length;i++)
			if(values[i]<minVal){
				minInd=i;
				minVal=values[i];
			}
		return minInd;
	}

	public double divOr(final double x, final int y, final double or){
		if(y==0)return or;
		return x/y;
	}

	public int binarySearchLess(final int[] array, int value){
		val index=Arrays.binarySearch(array,value);
		return index>=0 ? index : -index - 1;
	}
}
