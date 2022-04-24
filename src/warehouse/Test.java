package warehouse;

import lombok.val;

import java.io.IOException;
import java.nio.file.Path;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

class Test {
	public static void main(String[] args) throws IOException {
		/*var items=0;
		var machines=0;
		var buffer=0;*/
		/*final var inst=InstanceParser.parse(Path.of("data/warehouse/40.txt"));
//		int[] perm={0, 31, 39, 27, 18, 30, 13, 1, 12, 19, 16, 15, 48, 46, 2, 45, 6, 35, 43, 41, 32, 44, 38, 40, 5, 9, 29, 33, 11, 14, 36, 26, 47, 24, 23, 34, 10, 3, 8, 21, 25, 28, 17, 42, 22, 4, 20, 7, 37};
//		System.out.println(inst.evaluate(perm));
		final var ninst=new NativeInstance(inst);
//		System.out.println(ninst.evaluate(perm));
////		final var perms= IntStream.range(0,1_000_000).mapToObj(i->randPerm(inst.jobCount())).toArray(int[][]::new);
		final var perm=IntStream.range(0, inst.jobCount()).toArray();
		final var rng=RandomGenerator.getDefault();
		final var start=System.nanoTime();
		for (var i=0;i<100_000;i++) {
			Utils.shuffle(perm,rng);
//			final var res = Utils.computeSchedule(perm, inst, true);
			final var res2=ninst.evaluate(perm);
//			if(res2!=res)
//				throw new RuntimeException("errrrr "+i+' '+res+' '+res2);
		}
		final var elapsed=System.nanoTime()-start;
		System.out.println(elapsed);*/
		/*for(var i=0;i< 40;i++){
			val instance=InstanceParser.parse(Path.of("data/warehouse/"+(i+1)+".txt"));
			if(instance.machineCount()>machines)machines=instance.machineCount();
			if(instance.bufferSize()>buffer)buffer=instance.bufferSize();
			if(instance.itemCount()>items)items=instance.itemCount();
		}
		System.out.println("machines = " + machines);
		System.out.println("buffer = " + buffer);
		System.out.println("items = " + items);*/
		val inst=InstanceParser.parse(Path.of("data/warehouse/21.txt"));
//		val perm = IntStream.range(0, inst.jobCount()).toArray();
		int[] perm={32, 20, 39, 8, 41, 19, 17, 11, 14, 12, 13, 43, 47, 29, 48, 46, 21, 9, 6, 23, 51, 25, 22, 27, 31, 0, 26, 35, 2, 36, 5, 37, 33, 18, 49, 50, 42, 10, 7, 1, 28, 40, 16, 34, 15, 24, 44, 3, 4, 38, 30, 45};
		System.out.println(Utils.computeSchedule(perm,inst,true,52));
		System.out.println(inst.jobLengths()[33]);
		System.out.println(inst.itemsNeeded()[33]);
//		System.out.println(inst);
//		System.out.println(Arrays.toString(inst.jobLengths()));
//		for(final var it:inst.itemsNeeded())
//			System.out.println(it);
	}

	static int[] randPerm(final int len){
		final var res=IntStream.range(0, len).toArray();
		Utils.shuffle(res, RandomGenerator.getDefault());
		return res;
	}
}
