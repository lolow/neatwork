package neatwork.solver;

public interface OptimizerType {
	
	public void mainlp(int NbNodes, int cont, int numvar, int numanz,
			int[] bkc, double[] blc, double[] buc, int[] bkx, double[] blx,
			double[] bux, int[] ptrb, int[] ptre, int[] sub, double[] val,
			double[] xx, double[] c);
	
	public void mainnlp(int cont, int numvar, int numanz, int NbPipes,
			int[] bkc, double[] blc, double[] buc, int[] bkx, int[] ptrb,
			int[] ptre, double[] blx, double[] bux, double[] x, double[] y,
			double[] c, int[] sub, double[] val, double[] PipesConst,
			double[] TapsConst1, double[] TapsConst2, double[] oprfo,
			double[] oprgo, double[] oprho, int[] opro, int[] oprjo);
}
