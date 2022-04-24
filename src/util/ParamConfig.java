package util;

import lombok.val;

import java.util.HashMap;
import java.util.Map;

public record ParamConfig(String instance, String info, double cutoffTime, int cutoffLength, int seed,
                          Map<String, String> params) {
	public static ParamConfig parse(final String[] args) {
		assert (args.length - 5) % 2 == 0;
		val params = new HashMap<String, String>((args.length - 5) / 2);
		for (var i = 5; i < args.length; i += 2) {
			assert args[i].startsWith("-");
			params.put(args[i].substring(1), args[i + 1]);
		}
		return new ParamConfig(args[0], args[1], Double.parseDouble(args[2]), Integer.parseInt(args[3]),
		                       Integer.parseInt(args[4]), params);
	}
}
