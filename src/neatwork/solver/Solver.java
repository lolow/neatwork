package neatwork.solver;

import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;
import org.gnu.glpk.GlpkException;
import org.gnu.glpk.SWIGTYPE_p_double;
import org.gnu.glpk.SWIGTYPE_p_int;
import org.gnu.glpk.glp_prob;
import org.gnu.glpk.glp_smcp;

import mosek.*;


public class Solver {
	
	//Optimize Network design
	public void lp(int cont, int numvar, int numanz, 
			boundkey[] bkc, double[] blc, double[] buc, 
			boundkey[] bkx, double[] blx, double[] bux, 
			int[] ptrb, int[] ptre, int[] sub, double[] val, 
			double[] xx, double[] c) {

		// use GLPK
		lp_glpk(cont, numvar, numanz, bkc, blc, buc, bkx, blx, bux, ptrb, ptre, sub, val, xx, c);

		// or MOSEK
		lp_mosek(cont, numvar, numanz, bkc, blc, buc, bkx, blx, bux, ptrb, ptre, sub, val, xx, c);
		

	}
	
	// Use MOSEK solver
	public void lp_mosek(int numcon, int numvar, int numanz, 
			boundkey[] bkc, double[] blc, double[] buc, 
			boundkey[] bkx, double[] blx, double[] bux, 
			int[] ptrb, int[] ptre, int[] sub, double[] val, 
			double[] xx, double[] c) {

		//Convert the sparse matrix
		int    asub[][] = new int[numvar][];
		double aval[][] = new double[numvar][];
		
        for(int col=0; col<numvar; col++){
        	asub[col] = new int[ptre[col]-ptrb[col]];
        	aval[col] = new double[ptre[col]-ptrb[col]];
        	for(int ptr = ptrb[col]; ptr < ptre[col]; ptr++){
    			asub[col][ptr-ptrb[col]] = sub[ptr];
    			aval[col][ptr-ptrb[col]] = val[ptr];
        	}            	
    	}
		
		try (
			mosek.Env  env = new Env();
			 mosek.Task task = new Task(env,0,0))
		{
			// Directs the log task stream to the user specified
			// method task_msg_obj.stream
			task.set_Stream(
					mosek.streamtype.log,
					new mosek.Stream() 
					{ public void stream(String msg) { System.out.print(msg); }});

			// Append 'numcon' empty constraints.
			// The constraints will initially have no bounds.
			task.appendcons(numcon);

			// Append 'numvar' variables.
			// The variables will initially be fixed at zero (x=0). 
			task.appendvars(numvar);

			for(int j=0; j<numvar; ++j)
			{
				// Set the linear term c_j in the objective.
				task.putcj(j,c[j]);

				// Set the bounds on variable j.
				// blx[j] <= x_j <= bux[j] 
				task.putvarbound(j,bkx[j],blx[j],bux[j]);

				// Input column j of A 
				task.putacol(j,                /* Variable (column) index.*/
						asub[j],               /* Row index of non-zeros in column j.*/
						aval[j]);              /* Non-zero Values of column j. */
			}

			// Set the bounds on constraints.
			// blc[i] <= constraint_i <= buc[i] 
			for(int i=0; i<numcon; ++i)
				task.putconbound(i,bkc[i],blc[i],buc[i]);

			// Input the objective sense (minimize/maximize)
			task.putobjsense(mosek.objsense.minimize);

			// Solve the problem
			task.optimize();

			// Print a summary containing information
			// about the solution for debugging purposes
			task.solutionsummary(mosek.streamtype.msg);

			// Get status information about the solution
			mosek.solsta solsta[] = new mosek.solsta[1];
			task.getsolsta(mosek.soltype.bas,solsta);

			switch(solsta[0])
			{
			case optimal:
			case near_optimal:
				task.getxx(mosek.soltype.bas, xx);
				//System.out.println("Optimal primal solution\n");
				//for(int j = 0; j < numvar; ++j)
				//	System.out.println ("x[" + j + "]:" + xx[j]);
				//break;
			case dual_infeas_cer:
			case prim_infeas_cer:
			case near_dual_infeas_cer:
			case near_prim_infeas_cer:  
				System.out.println("Primal or dual infeasibility certificate found.\n");
				break;
			case unknown:
				System.out.println("Unknown solution status.\n");
				break;
			default:
				System.out.println("Other solution status");
				break;
			}
		}
		catch (mosek.Exception e)
		{
			System.out.println ("An error/warning was encountered");
			System.out.println (e.toString());
			throw e;
		}

	}		

	// Use GLPK solver
	public void lp_glpk(int cont, int numvar, int numanz, 
			boundkey[] bkc, double[] blc, double[] buc, 
			boundkey[] bkx, double[] blx, double[] bux, 
			int[] ptrb, int[] ptre, int[] sub, double[] val, 
			double[] xx, double[] c) {
		
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
                case lo: // MSK_BK_LO
                	GLPK.glp_set_col_bnds(lpk, i, GLPKConstants.GLP_LO, blx[i-1], bux[i-1]); break;
                case ra: // MSK_BK_RA 
                	GLPK.glp_set_col_bnds(lpk, i, GLPKConstants.GLP_DB, blx[i-1], bux[i-1]); break;
                default:
                	break;
                }                	
            }

            // Create constraints
            GLPK.glp_add_rows(lpk, cont);
            for(int i = 1; i <= cont; i++){            	
            	switch (bkc[i-1]) {
                case up: // MSK_BK_UP
                	GLPK.glp_set_row_bnds(lpk, i, GLPKConstants.GLP_UP, blc[i-1],buc[i-1]); break;
                case fx: // MSK_BK_FX 
                	GLPK.glp_set_row_bnds(lpk, i, GLPKConstants.GLP_FX, blc[i-1],buc[i-1]); break;
                default:
                	break;
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
            	for (int j = 0; j < numvar; j++) {
                    xx[j]= GLPK.glp_get_col_prim(lpk, j + 1);
                    //System.out.println ("x[" + j + "]:" + xx[j]);
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
	public void nlp(int cont, int numvar, int numanz, int NbPipes,
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
	
}