package neatwork.solver;

import org.gnu.glpk.*;


public class Solver {

	//MAKE DESIGN
	public void lp(int cont, int numvar, int numanz, int[] bkc, double[] blc, double[] buc, int[] bkx, double[] blx,
			double[] bux, int[] ptrb, int[] ptre, int[] sub, double[] val, double[] xx, double[] c) {
		glp_prob lp;
        glp_smcp parm;
        int ret;

        try {
            // Create problem
            lp = GLPK.glp_create_prob();
            //GLPK.glp_term_out(GLPKConstants.GLP_OFF); //disable terminal output
            GLPK.glp_set_prob_name(lp, "myProblem");

            // Define columns
            GLPK.glp_add_cols(lp, numvar);
            for(int i = 1; i <= numvar; i++){
                GLPK.glp_set_col_kind(lp, i, GLPKConstants.GLP_CV); //continuous variables
                switch (bkx[i-1]) {
                case 0: // MSK_BK_LO
                	GLPK.glp_set_col_bnds(lp, i, GLPKConstants.GLP_LO, blx[i-1], bux[i-1]); break;
                case 4: // MSK_BK_RA 
                	GLPK.glp_set_col_bnds(lp, i, GLPKConstants.GLP_DB, blx[i-1], bux[i-1]); break;
                }
            }

            // Create constraints
            GLPK.glp_add_rows(lp, cont);
            
            for(int i = 1; i <= cont; i++){
            	switch (bkc[i-1]) {
                case 1: // MSK_BK_UP
                	GLPK.glp_set_row_bnds(lp, i, GLPKConstants.GLP_UP, blc[i-1],buc[i-1]); break;
                case 2: // MSK_BK_FX 
                	GLPK.glp_set_row_bnds(lp, i, GLPKConstants.GLP_FX, blc[i-1],buc[i-1]); break;
                }
            }
            
            SWIGTYPE_p_int ia = GLPK.new_intArray(numanz);
            SWIGTYPE_p_int ja = GLPK.new_intArray(numanz);
            SWIGTYPE_p_double ar = GLPK.new_doubleArray(numanz);
            int k = 1;
            for(int col = 0; col < numvar; col++){
            	for(int ptr = ptrb[col]; ptr < ptre[col]; ptr++){
            		GLPK.intArray_setitem(ia, k, sub[ptr] + 1);
            		GLPK.intArray_setitem(ja, k, col + 1);
            		GLPK.doubleArray_setitem(ar, k, val[ptr]);
            		k++;
                }
            }
            GLPK.glp_load_matrix(lp, numanz, ia, ja, ar);

            // Define objective
            GLPK.glp_set_obj_dir(lp, GLPKConstants.GLP_MIN);
            for(int i = 1; i <= numvar; i++){
                GLPK.glp_set_obj_coef(lp, i, c[i-1]);
            }

            // Solve model
            parm = new glp_smcp();
            GLPK.glp_init_smcp(parm);
            ret = GLPK.glp_simplex(lp, parm);

            // Retrieve solution
            if (ret == 0) {
            	for (int i = 1; i <= numvar; i++) {
                    xx[i-1]= GLPK.glp_get_col_prim(lp, i);
                }
            }

            // Free memory
            GLPK.glp_delete_prob(lp);
        } catch (GlpkException ex) {
            ex.printStackTrace();
        }
	}

	//SIMULATION
	public void mainnlp(int cont, int numvar, int numanz, int NbPipes,
			int[] bkc, double[] blc, double[] buc, int[] bkx, int[] ptrb,
			int[] ptre, double[] blx, double[] bux, double[] x, double[] y,
			double[] c, int[] sub, double[] val, double[] PipesConst,
			double[] TapsConst1, double[] TapsConst2, double[] oprfo,
			double[] oprgo, double[] oprho, int[] opro, int[] oprjo) {
		SimulationProblem pb = new SimulationProblem(cont, numvar, numanz, NbPipes,
				 bkc, blc, buc, bkx, ptrb, ptre, blx, bux, x, y, c, sub, val, PipesConst,
				 TapsConst1, TapsConst2, oprfo, oprgo, oprho, opro, oprjo);
		double x0[] = pb.getInitialGuess();
		pb.solve(x0);
		//get x
		for(int i = 0; i < x0.length; i++){
			x[i] = x0[i];
		}
		//get y
		double y0[] = pb.getMultConstraints();
		for(int i = 0; i < y.length; i++){
			if(y0[i]>0){
				y[i] = 0;
			}else{
				y[i] = -y0[i];				
			}
		}
	}
}