package neatwork.solver;

import org.gnu.glpk.*;

import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;
import org.gnu.glpk.GlpkException;
import org.gnu.glpk.SWIGTYPE_p_double;
import org.gnu.glpk.SWIGTYPE_p_int;
import org.gnu.glpk.glp_prob;
import org.gnu.glpk.glp_smcp;

public class Solver {

	//Optimize Network design
	public void lp(int cont, int numvar, int numanz, int[] bkc, double[] blc, double[] buc, int[] bkx, double[] blx,
			double[] bux, int[] ptrb, int[] ptre, int[] sub, double[] val, double[] xx, double[] c) {
		
        System.out.println("Using GLPK " + GLPK.glp_version());
        
        glp_prob lpk;
        glp_smcp parm;
        SWIGTYPE_p_int index;
        SWIGTYPE_p_double value;
                

        try {
        	
            // Create problem
            lpk = GLPK.glp_create_prob();
            
            //GLPK.glp_term_out(GLPKConstants.GLP_OFF); 
            //disable terminal output
            GLPK.glp_set_prob_name(lpk, "myProblem");

            // Define columns
            GLPK.glp_add_cols(lpk, numvar);
            for(int i = 1; i <= numvar; i++){
                GLPK.glp_set_col_kind(lpk, i, GLPKConstants.GLP_CV); //continuous variables
                switch (bkx[i-1]) {
                case 0: // MSK_BK_LO
                	GLPK.glp_set_col_bnds(lpk, i, GLPKConstants.GLP_LO, blx[i-1], bux[i-1]); break;
                case 4: // MSK_BK_RA 
                	GLPK.glp_set_col_bnds(lpk, i, GLPKConstants.GLP_DB, blx[i-1], bux[i-1]); break;
                }
            }

            // Create constraints
            GLPK.glp_add_rows(lpk, cont);
            for(int i = 1; i <= cont; i++){            	
            	switch (bkc[i-1]) {
                case 1: // MSK_BK_UP
                	GLPK.glp_set_row_bnds(lpk, i, GLPKConstants.GLP_UP, blc[i-1],buc[i-1]); break;
                case 2: // MSK_BK_FX 
                	GLPK.glp_set_row_bnds(lpk, i, GLPKConstants.GLP_FX, blc[i-1],buc[i-1]); break;
                }
            }
            
            
            // Add matrix values by columns
            index = GLPK.new_intArray(numvar);
            value = GLPK.new_doubleArray(numvar);
            for(int col=1; col<=numvar; col++){
            	int k = 0;
            	for(int ptr = ptrb[col-1]; ptr < ptre[col-1]; ptr++){
        			k++;
                	GLPK.intArray_setitem(index, k, sub[ptr] + 1);
                	GLPK.doubleArray_setitem(value, k, val[ptr]);
            	}            	
        		GLPK.glp_set_mat_col(lpk, col, k, index, value);
        	}
            GLPK.delete_intArray(index);
            GLPK.delete_doubleArray(value);
            
            // Define objective
            GLPK.glp_set_obj_dir(lpk, GLPKConstants.GLP_MIN);
            for(int i = 1; i <= numvar; i++){
                GLPK.glp_set_obj_coef(lpk, i, c[i-1]);
            }
            
            // Solve model
            parm = new glp_smcp();
            GLPK.glp_init_smcp(parm);
            int ret = GLPK.glp_simplex(lpk, parm);

            // Retrieve solution
            if (ret == 0) {
            	for (int i = 1; i <= numvar; i++) {
                    xx[i-1]= GLPK.glp_get_col_prim(lpk, i);
                }
            } else {
            	System.out.println("The problem could not be solved");
            }

            // Free memory
            GLPK.glp_delete_prob(lpk);
            
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
		//pb.solve(x0);
		pb.OptimizeNLP();
		
		//get x
		for(int i = 0; i < x0.length; i++){
			x[i] = x0[i];
		}
		//get y
		//double y0[] = pb.getMultConstraints();
		double y0[] = pb.getConstraintMultipliers();
		for(int i = 0; i < y.length; i++){
			if(y0[i]>0){
				y[i] = 0;
			}else{
				y[i] = -y0[i];				
			}
		}
	}
	
	/**
     * write simplex solution
     * @param lpk problem
     */
    static void write_lp_solution(glp_prob lpk) {
        int i;
        int n;
        String name;
        double val;

        name = GLPK.glp_get_obj_name(lpk);
        val = GLPK.glp_get_obj_val(lpk);
        System.out.print(name);
        System.out.print(" = ");
        System.out.println(val);
        n = GLPK.glp_get_num_cols(lpk);
        for (i = 1; i <= n; i++) {
            name = GLPK.glp_get_col_name(lpk, i);
            val = GLPK.glp_get_col_prim(lpk, i);
            System.out.print(name);
            System.out.print(" = ");
            System.out.println(val);
        }
    }
}