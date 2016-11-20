package neatwork.solver;

import neatwork.Messages;

import neatwork.project.*;

import java.util.*;


/**
 * Solver abstract class
 * @author L. DROUET
 * @version 1.0
 */
public abstract class AbstractSolver extends Observable {
    public static final int MODIF_PROGRESS = 0;
    public static final int MODIF_STATUS = 1;

    //STATUS
    private String status = "";

    //PROGRESS
    private int progress = 0;

    public String getStatut() {
        return status;
    }

    protected void setStatus(String status) {
        this.status = status;
        setChanged();
        notifyObservers(new Integer(MODIF_STATUS));
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
        setChanged();
        notifyObservers(new Integer(MODIF_PROGRESS));
    }

    public void init() {
        setProgress(0);
        setStatus(""); //$NON-NLS-1$
    }

    //TIME

    /** Returns the number of seconds since 1970 */
    protected long getTick() {
        Date date = new Date();

        return date.getTime() / 1000;
    }

    /** returns a formatted string for time */
    protected String getElapsedTime(long tick) {
        Date date = new Date();
        long diff = (date.getTime() / 1000) - tick;

        if (diff > 0) {
            long hours;
            long minutes;
            long secs;
            hours = diff / 3600;
            diff = diff - (hours * 3600);
            minutes = diff / 60;
            secs = diff - (minutes * 60);

            return ((hours > 0)
            ? (hours + Messages.getString("AbstractSolver._h")) : "") +
            ((minutes > 0)
            ? (minutes + Messages.getString("AbstractSolver._min")) : "") +
            secs + Messages.getString("AbstractSolver._sec");
        }

        return Messages.getString("AbstractSolver.0_sec");
    }

    //MAKE DESIGN

    /** Does a Make Design */
    public abstract void makeDesign(Topographie topographie,
        String orificesSet, String diametersSet, Hashtable loadFactor,
        Vector constraints,double hsource);

    /** Returns the result of a Make Design */
    public abstract String getDesignContentMakeDesign();

    /** Returns the estimates of the node pressures in a design */
    public abstract Vector getNodePressureMakeDesign();

    //SIMULATION

    /** Does a simulation */
    public abstract void simulation(Design design, Properties parameters,
        Hashtable faucetRef);

    /** Returns the content of a simulation */
    public abstract String getSimulationContent();
}
