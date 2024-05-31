package neatwork.gui.simu;

import neatwork.solver.*;


/**
 * gere le thread de run simulation
 * @author L. DROUET
 * @version 1.0
 */
public class ThreadRunSimulation extends Thread {
    private AbstractSolver solver;
    private NewSimuDialog dialog;

    public ThreadRunSimulation(NewSimuDialog dialog, AbstractSolver solver) {
        this.dialog = dialog;
        this.solver = solver;
    }

    public void run() {
        //execute l'operation make design
        solver.simulation(dialog.getDesign(), dialog.getParameters(),
            dialog.getFaucetRef());
    }
}
