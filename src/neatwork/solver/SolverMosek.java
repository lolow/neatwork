package neatwork.solver;


public class SolverMosek {

  //MAKE DESIGN
  public void lp(int cont, int numvar, int numanz, int[] bkc, double[] blc, double[] buc, int[] bkx, double[] blx,
			double[] bux, int[] ptrb, int[] ptre, int[] sub, double[] val, double[] xx, double[] c) {

	  try
	  {
		  // Make mosek environment. 
		  env  = new mosek.Env ();
		      
		  // Create a task object. 
		  task = new mosek.Task (env, 0, 0);

	      // Directs the log task stream to the user specified
	      // method task_msg_obj.stream
	      task.set_Stream(
	        mosek.Env.streamtype.log,
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
	        task.putacol(j,                     /* Variable (column) index.*/
	                     asub[j],               /* Row index of non-zeros in column j.*/
	                     aval[j]);              /* Non-zero Values of column j. */
	      }

	      // Set the bounds on constraints.
	      // blc[i] <= constraint_i <= buc[i] 
	      for(int i=0; i<numcon; ++i)
	        task.putconbound(i,bkc[i],blc[i],buc[i]);

	      // Input the objective sense (minimize/maximize)
	      task.putobjsense(mosek.Env.objsense.maximize);

	      // Solve the problem
	      task.optimize();
	                
	      // Print a summary containing information
	      // about the solution for debugging purposes
	      task.solutionsummary(mosek.Env.streamtype.msg);
	      
	      // Get status information about the solution
	      mosek.Env.solsta solsta[] = new mosek.Env.solsta[1];
	      task.getsolsta(mosek.Env.soltype.bas,solsta);

	      switch(solsta[0])
	      {
	      case optimal:
	      case near_optimal:
	        task.getxx(mosek.Env.soltype.bas, // Request the basic solution.
	                   xx);
	        
	        System.out.println("Optimal primal solution\n");
	        for(int j = 0; j < numvar; ++j)
	          System.out.println ("x[" + j + "]:" + xx[j]);
	        break;
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
	    finally
	    {
	      if (task != null) task.dispose ();
	      if (env  != null)  env.dispose ();
	    }
		
		
		glp_prob lp;
        glp_smcp parm;
        int ret;

        try {
        	
            // Create problem
            lp = GLPK.glp_create_prob();
            
            //GLPK.glp_term_out(GLPKConstants.GLP_OFF); 
            //disable terminal output
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
            
            GLPK.delete_intArray(ia);
            GLPK.delete_intArray(ja);
            GLPK.delete_doubleArray(ar);
            
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
            } else {
            	System.out.println("The problem could not be solved");
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