package neatwork.solver;

import org.coinor.Ipopt;

public class SimulationProblem extends Ipopt {

    // Problem sizes
    int n, m, nele_jac, nele_hess;
    int[] ptrb;
    int[] ptre;
    int[] sub;
    double[] c;
    double[] oprfo;
    double[] oprgo;
    double[] val;
    
    /**
     * Initialize the bounds and create the native Ipopt problem.
     */
    public SimulationProblem(int cont, int numvar, int numanz, int NbPipes,
			int[] bkc, double[] blc, double[] buc, int[] bkx, int[] ptrb,
			int[] ptre, double[] blx, double[] bux, double[] x, double[] y,
			double[] c, int[] sub, double[] val, double[] PipesConst,
			double[] TapsConst1, double[] TapsConst2, double[] oprfo,
			double[] oprgo, double[] oprho, int[] opro, int[] oprjo) {

            /* Number of non-zeros in the Jacobian of the constraints */
            nele_jac = numanz;
            /* Number of non-zeros in the Hessian of the Lagrangian (lower or
             * upper triangular part only) */
            nele_hess = numvar;
            
            for(int j=NbPipes;j<numvar-1;j++){
                oprfo[j] = TapsConst2[j - NbPipes];
                oprgo[j] = 3.0;
            }
            for(int j=0;j<NbPipes+1;j++){
            	c[j] = 0;
            }
            for(int j=NbPipes + 1;j<numvar;j++){
            	c[j] = TapsConst1[j - NbPipes - 1];
            }

            /* set the number of variables and allocate space for the bounds */
            n = numvar;
            double x_L[] = new double[n];
            double x_U[] = new double[n];
            for(int i=0; i < n; i++){
                    x_L[i] = blx[i];
                    x_U[i] = bux[i];
            }

            /* set the number of constraints and allocate space for the bounds */
            m = cont;
            double g_L[] = new double[m];
            double g_U[] = new double[m];
            /* set the values of the constraint bounds */
            for(int i=0; i < m; i++){
                g_L[i] = blc[i];
                g_U[i] = buc[i];
            }
            
            /* Index style for the irow/jcol elements */
            int index_style = Ipopt.C_STYLE;
            
            /* Class variables*/
            this.c = c;
            this.oprfo = oprfo;
            this.oprgo = oprgo;
            this.ptrb = ptrb;
            this.ptre = ptre;
            this.sub = sub;
            this.val = val;

            /* create the IpoptProblem */
            //create(n, x_L, x_U, m, g_L, g_U, nele_jac, nele_hess, index_style);
            create(n, m, nele_jac, nele_hess, index_style);
            //addStrOption("jac_c_constant","yes"); //because of the linear constraints
            //addStrOption("mehrotra_algorithm","yes");
            //addStrOption("linear_solver","mumps");
            //addNumOption("mumps_pivtol",1);
            setStringOption("mehrotra_algorithm","yes");
            setStringOption("linear_solver","mumps");
            setNumericOption("mumps_pivtol",1);
            
            //addIntOption("print_level",0);
    }
    
    public double[] getInitialGuess(){
            /* allocate space for the initial point and set the values */
            double x[] = new double[n];
            for(int i=0; i < x.length; i++){
                    x[i] = 0;
            }
            return x;
    }

    protected boolean eval_f(int n, double[] x, boolean new_x, double[] obj_value) {
            assert n == this.n;

            for(int i = 0; i < n; i++){
            	if(x[i] < 0){
                	return false;
                }
            }

            obj_value[0] = 0;
            for(int i=0; i < n; i++){
            	obj_value[0] += this.c[i]*x[i];
            }
            for(int i=0; i < n-1; i++){
            	obj_value[0] += oprfo[i] * Math.pow(x[i+1], oprgo[i]);
            }

            return true;
    }

    protected boolean eval_grad_f(int n, double[] x, boolean new_x, double[] grad_f) {
            assert n == this.n;
            
            for(int i = 0; i < n; i++){
            	if(x[i] < 0){
                	return false;
                }
            }

            grad_f[0] = 0;
            for(int i=0; i < n-1; i++){
            	grad_f[i+1] = oprgo[i] * oprfo[i] * pow(x[i+1],oprgo[i]-1);
            }
            for(int i=0; i < n; i++){
            	grad_f[i] += c[i];
            }

            return true;
    }

    protected boolean eval_g(int n, double[] x, boolean new_x, int m, double[] g) {
            assert n == this.n;
            assert m == this.m;

            for(int i=0; i < m; i++){
            	g[i] = 0;
            }
            for(int col=0; col < n; col++){
            	for(int ptr = ptrb[col]; ptr < ptre[col]; ptr++){
                	g[sub[ptr]] += val[ptr] * x[col];
                }
            }
            
            return true;
    }

    protected boolean eval_jac_g(int n, double[] x, boolean new_x,
                    int m, int nele_jac, int[] iRow, int[] jCol, double[] values) {
            assert n == this.n;
            assert m == this.m;

            if (values == null) {
                    /* return the structure of the jacobian */
            		for(int col=0; col < n; col++){
                    	for(int ptr = this.ptrb[col]; ptr < this.ptre[col]; ptr++){
                    		iRow[ptr] = sub[ptr];
                            jCol[ptr] = col;
                        }
                    }
            }
            else {
                    /* return the values of the jacobian of the constraints */
            		for(int col=0; col < n; col++){
            			for(int ptr = this.ptrb[col]; ptr < this.ptre[col]; ptr++){
            				values[ptr] = val[ptr];
            			}
            		}
            }

            return true;
    }

    protected boolean eval_h(int n, double[] x, boolean new_x, double obj_factor, int m, double[] lambda, boolean new_lambda, int nele_hess, int[] iRow, int[] jCol, double[] values) {
    		assert n == this.n;
    		
    		for(int i = 0; i < n; i++){
            	if(x[i] < 0){
                	return false;
                }
            }

            if (values == null) {
                    /* return the structure. This is a symmetric matrix, fill the lower left
                     * triangle only. */
            		for(int i = 0; i < n; i++ ){
            			iRow[i] = i;
                        jCol[i] = i;
            		}
            }
            else {
                    /* return the values. This is a symmetric matrix, fill the lower left
                     * triangle only */
            		
            		values[0] = 0;
            		for(int i = 0; i < n-1; i++ ){
            			values[i+1] = obj_factor * (oprgo[i]-1)*oprgo[i]*oprfo[i]*pow(x[i+1],oprgo[i]-2);
            		}

            }
            return true;
    }
    
    public static double pow(double a, double b) {
        //final long tmp = (long) (9076650 * (a - 1) / (a + 1 + 4 * (Math.sqrt(a))) * b + 1072632447);
        //return Double.longBitsToDouble(tmp << 32);
    	return Math.pow(a,b);
    }

	@Override
	protected boolean get_bounds_info(int n, double[] x_l, double[] x_u, int m, double[] g_l, double[] g_u) {
		// TODO Auto-generated method stub
        /* set the number of variables and allocate space for the bounds */
        //n = numvar;
        //double x_L[] = new double[n];
        //double x_U[] = new double[n];
        //for(int i=0; i < n; i++){
        //        x_L[i] = blx[i];
        //        x_U[i] = bux[i];
        //}
		return false;
	}

	@Override
	protected boolean get_starting_point(int n, boolean init_x, double[] x, boolean init_z, double[] z_L, double[] z_U,
			int m, boolean init_lambda, double[] lambda) {
		// TODO Auto-generated method stub
		return false;
	}

}