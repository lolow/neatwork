package neatwork.gui.makedesign;

import neatwork.solver.*;


/**
 * ger le thread de make design
 * @author L. DROUET
 * @version 1.0
 */
public class ThreadMakeDesign extends Thread {
    private AbstractSolver solver;
    private MakeDesignDialog dialog;

    public ThreadMakeDesign(MakeDesignDialog dialog, AbstractSolver solver) {
        this.dialog = dialog;
        this.solver = solver;
    }

    public void run() {
        //execute l'operation make design
        solver.makeDesign(dialog.getTopographie(), dialog.getOrificesContent(),
            dialog.getDiametersContent(), dialog.getLoadFactors(),
            dialog.getConstraints(),dialog.getHSource());
    }
}
