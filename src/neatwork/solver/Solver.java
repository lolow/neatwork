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
        
        //	Minimize z = -.5 * x1 + .5 * x2 - x3 + 1
        //
        //	subject to
        //	0.0 <= x1 - .5 * x2 <= 0.2
        //  -x2 + x3 <= 0.4
        //	where,
        //	0.0 <= x1 <= 0.5
        //	0.0 <= x2 <= 0.5
        //  0.0 <= x3 <= 0.5

        glp_prob lp;
        glp_prob lpk;
        glp_smcp parm;
        SWIGTYPE_p_int ind;
        SWIGTYPE_p_double value;

        try {
            // Create problem
            lp = GLPK.glp_create_prob();
            System.out.println("Problem created");
            GLPK.glp_set_prob_name(lp, "myProblem");

            // Define columns
            GLPK.glp_add_cols(lp, 3);
            GLPK.glp_set_col_name(lp, 1, "x1");
            GLPK.glp_set_col_kind(lp, 1, GLPKConstants.GLP_CV);
            GLPK.glp_set_col_bnds(lp, 1, GLPKConstants.GLP_DB, 0, .5);
            GLPK.glp_set_col_name(lp, 2, "x2");
            GLPK.glp_set_col_kind(lp, 2, GLPKConstants.GLP_CV);
            GLPK.glp_set_col_bnds(lp, 2, GLPKConstants.GLP_DB, 0, .5);
            GLPK.glp_set_col_name(lp, 3, "x3");
            GLPK.glp_set_col_kind(lp, 3, GLPKConstants.GLP_CV);
            GLPK.glp_set_col_bnds(lp, 3, GLPKConstants.GLP_DB, 0, .5);

            // Create constraints

            // Allocate memory
            ind = GLPK.new_intArray(3);
            value = GLPK.new_doubleArray(3);

            // Create rows
            GLPK.glp_add_rows(lp, 2);

            // Set row details
            GLPK.glp_set_row_name(lp, 1, "c1");
            GLPK.glp_set_row_bnds(lp, 1, GLPKConstants.GLP_DB, 0, 0.2);
            GLPK.intArray_setitem(ind, 1, 1);
            GLPK.intArray_setitem(ind, 2, 2);
            GLPK.doubleArray_setitem(value, 1, 1.);
            GLPK.doubleArray_setitem(value, 2, -.5);
            GLPK.glp_set_mat_row(lp, 1, 2, ind, value);

            GLPK.glp_set_row_name(lp, 2, "c2");
            GLPK.glp_set_row_bnds(lp, 2, GLPKConstants.GLP_UP, 0, 0.4);
            GLPK.intArray_setitem(ind, 1, 2);
            GLPK.intArray_setitem(ind, 2, 3);
            GLPK.doubleArray_setitem(value, 1, -1.);
            GLPK.doubleArray_setitem(value, 2, 1.);
            GLPK.glp_set_mat_row(lp, 2, 2, ind, value);

            // Free memory
            GLPK.delete_intArray(ind);
            GLPK.delete_doubleArray(value);

            // Define objective
            GLPK.glp_set_obj_name(lp, "z");
            GLPK.glp_set_obj_dir(lp, GLPKConstants.GLP_MIN);
            GLPK.glp_set_obj_coef(lp, 0, 1.);
            GLPK.glp_set_obj_coef(lp, 1, -.5);
            GLPK.glp_set_obj_coef(lp, 2, .5);
            GLPK.glp_set_obj_coef(lp, 3, -1);

            // Write model to file
            // GLPK.glp_write_lp(lp, null, "lp.lp");

            // Solve model
            parm = new glp_smcp();
            GLPK.glp_init_smcp(parm);
            int ret = GLPK.glp_simplex(lp, parm);

            // Retrieve solution
            if (ret == 0) {
                write_lp_solution(lp);
            } else {
                System.out.println("The problem could not be solved");
            }

            // Free memory
            GLPK.glp_delete_prob(lp);
            
        } catch (GlpkException ex) {
            ex.printStackTrace();
	    }
        
        

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
            
            // Define objective
            GLPK.glp_set_obj_dir(lpk, GLPKConstants.GLP_MIN);
            for(int i = 1; i <= numvar; i++){
                GLPK.glp_set_obj_coef(lpk, i, c[i-1]);
            }
            
            // Allocate memory
            SWIGTYPE_p_int ia = GLPK.new_intArray(numanz);
            SWIGTYPE_p_int ja = GLPK.new_intArray(numanz);
            SWIGTYPE_p_double ar = GLPK.new_doubleArray(numanz);
            
            // Create sparse matrix
            int k = 1;
            for(int col = 0; col < numvar; col++){
            	for(int ptr = ptrb[col]; ptr < ptre[col]; ptr++){
            		System.out.println(k);
            		GLPK.intArray_setitem(ia, k, sub[ptr] + 1);
            		GLPK.intArray_setitem(ja, k, col + 1);
            		GLPK.doubleArray_setitem(ar, k, val[ptr]);
            		System.out.println(GLPK.intArray_getitem(ia, k) + " " + GLPK.intArray_getitem(ja, k) + " " + GLPK.doubleArray_getitem(ar,k));
            		k++;
                }
            }
            // TODO Use glp_set_mat_col instead
            /* ind = GLPK.new_intArray(3);
            GLPK.intArray_setitem(ind, 1, 1);
            GLPK.intArray_setitem(ind, 2, 2);
            val = GLPK.new_doubleArray(3);
            GLPK.doubleArray_setitem(val, 1, 1.);
            GLPK.doubleArray_setitem(val, 2, -1.);
            GLPK.glp_set_mat_row(lp, 1, 2, ind, val);
            GLPK.delete_doubleArray(val);
			GLPK.delete_intArray(ind);
            */
            
            // Free memory
            GLPK.delete_intArray(ia);
            GLPK.delete_intArray(ja);
            GLPK.delete_doubleArray(ar);
            
            // Load the whole constraint matrix.
            GLPK.glp_load_matrix(lpk, numanz, ia, ja, ar);
            
            // write problem for debug purpose
            //GLPK.glp_write_lp(lpk, null, "/home/lolow/neatwork.lpk");

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