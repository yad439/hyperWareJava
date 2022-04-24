package warehouse;

import lombok.val;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.BitSet;
import java.util.Scanner;

final class InstanceParser {
	private InstanceParser(){}

	static JInstance parse(final Path file) throws IOException {
		try(final var input=new Scanner(file, StandardCharsets.US_ASCII)){
			return parse(input);
		}
	}

	static JInstance parse(final URL file) throws IOException {
		try(val input=new Scanner(new BufferedInputStream(file.openStream()), StandardCharsets.US_ASCII)) {
			return parse(input);
		}
	}

	private static JInstance parse(final Scanner input) {
		final var jobCount= input.nextInt();
		final var machineCount= input.nextInt();
		final var carCount= input.nextInt();
		final var bufferSize= input.nextInt();
		final var itemCount= input.nextInt();
		final var travelTime= input.nextInt();
		final var jobLengths=new int[jobCount];
		for(int i=0;i<jobCount;i++)jobLengths[i]= input.nextInt();
		final var itemsNeeded=new BitSet[jobCount];
		for(var i=0;i<jobCount;i++){
			final var count= input.nextInt();
			final var set=new BitSet(count);
			for(var j=0;j<count;j++)set.set(input.nextInt() - 1);
			itemsNeeded[i]=set;
		}
		return new JInstance(jobCount, machineCount, carCount, travelTime, itemCount, bufferSize, jobLengths,
		                     itemsNeeded);
	}
}
