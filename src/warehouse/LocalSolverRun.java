package warehouse;

import localsolver.LSExternalArgumentValues;
import localsolver.LocalSolver;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.io.IOException;
import java.nio.file.Path;

@UtilityClass
public class LocalSolverRun {
	public void main(String[] args) throws IOException {
		val instance= new NativeInstance(InstanceParser.parse(Path.of("data/warehouse/38.txt")));
		val n = instance.jobCount();
		try(val solver=new LocalSolver()){
			val model=solver.getModel();
			val func=model.intExternalFunction(vals->helper(vals,instance,n));
			func.getExternalContext().setLowerBound(3808);
			val variables=model.listVar(n);
			model.constraint(model.eq(model.count(variables), n));
			model.minimize(model.call(func,variables));
			model.close();

			solver.getParam().setTimeLimit(400);
			solver.solve();
		}
	}

	private int helper(final LSExternalArgumentValues argumentValues, final Instance instance, final int n){
		val perm=argumentValues.getCollectionValue(0);
		if(perm.count()==0)return Integer.MAX_VALUE;
		val permArr= new int[n];
		val tmp=new long[n];
		perm.copyTo(tmp);
		for(var i=0;i<instance.jobCount();i++)
			permArr[i]=Math.toIntExact(tmp[i]);
		assert Utils.isPermutation(permArr);
		return instance.evaluate(permArr);
	}
}
