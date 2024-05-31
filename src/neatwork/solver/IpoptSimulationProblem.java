package neatwork.solver;

import org.coinor.Ipopt;

public class IpoptSimulationProblem extends Ipopt {

    // Problem sizes
    int n, m, nele_jac, nele_hess;
    int[] ptrb;
    int[] ptre;
    int[] sub;
    double[] c;
    double[] oprfo;
    double[] oprgo;
    double[] oprho;
    double[] val;
    double[] bux;
    double[] blx;
    double[] buc;
    double[] blc;
    
    /**
     * Initialize the bounds and create the native Ipopt problem.
     */
    public IpoptSimulationProblem(int numcon, int numvar, int numanz,
    		mosek.boundkey[] bkc, double[] blc, double[] buc, mosek.boundkey[] bkx, int[] ptrb,
			int[] ptre, double[] blx, double[] bux, double[] x, double[] y,
			double[] c, int[] sub, double[] val,
			double[] oprfo,
			double[] oprgo, double[] oprho, mosek.scopr[] opro, int[] oprjo) {

                
            /* Number of non-zeros in the Jacobian of the constraints */
            nele_jac = numanz;
            /* Number of non-zeros in the Hessian of the Lagrangian (lower or
             * upper triangular part only) */
            nele_hess = numvar;

            /* Index style for the irow/jcol elements */
            int index_style = Ipopt.C_STYLE;
            
            /* Class variables*/
            this.n = numvar;
            this.m = numcon;
            this.c = c;
            this.oprfo = oprfo;
            this.oprgo = oprgo;
            this.oprho = oprho;
            this.ptrb = ptrb;
            this.ptre = ptre;
            this.sub = sub;
            this.val = val;
            this.blx = blx;
            this.bux = bux;
            this.blc = blc;
            this.buc = buc;


            setStringOption("jac_c_constant","yes"); //because of the linear constraints
            setStringOption("mehrotra_algorithm","yes");
            setStringOption("linear_solver","mumps");
            setNumericOption("mumps_pivtol",1);
            setIntegerOption("print_level", 100);//0 to disable
            
            /* create the IpoptProblem */
            create(n, m, nele_jac, nele_hess, index_style);
    }

    protected boolean eval_f(int n, double[] x, boolean new_x, double[] obj_value) {
            assert n == this.n;

            // x>= 0
            for(int i = 0; i < n; i++){
            	if(x[i] < 0){
                	return false;
                }
            }

            // 
            obj_value[0] = 0;
            for(int i=0; i < n; i++){
            	obj_value[0] += c[i]*x[i];
            }
            for(int i=0; i < n-1; i++){
            	obj_value[0] += oprfo[i] * Math.pow(x[i+1] + oprho[i], oprgo[i]);
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

	protected boolean get_bounds_info(int n, double[] x_l, double[] x_u, int m, double[] g_l, double[] g_u) {
        assert n == this.n;
        assert m == this.m;
        
		for(int i=0; i < n; i++){
        	x_l[i] = blx[i];
        	x_u[i] = bux[i];
        }
		
        for(int i=0; i < m; i++){
        	g_l[i] = blc[i];
        	g_u[i] = buc[i];
        }
        
        
		return true;
	}
	
	protected boolean get_starting_point(int n, boolean init_x, double[] x, boolean init_z, double[] z_L, double[] z_U,
			int m, boolean init_lambda, double[] lambda) {
    	assert init_z == false;
    	assert init_lambda = false;
    	if( init_x ){
    		for(int i=0; i < x.length; i++){
    			x[i] = 0;
    		}
    	}
		return true;
	}

}