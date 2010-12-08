package neatwork.solver;

import neatwork.Messages;

import neatwork.project.*;

import java.util.*;


/**
 * classe abstraite d�finissant un solver
 * @author L. DROUET
 * @version 1.0
 */
public abstract class AbstractSolver extends Observable {
    public static final int MODIF_PROGRESS = 0;
    public static final int MODIF_STATUS = 1;

    //STATUS
    private String status = ""; //$NON-NLS-1$

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

    /** renvoie le nombre de secondes depuis 1970*/
    protected long getTick() {
        Date date = new Date();

        return date.getTime() / 1000;
    }

    /** renvoie un string format� correspondant au temps pass� nombre de secondes depuis 1970*/
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
            ? (hours + Messages.getString("AbstractSolver._h")) : "") + //$NON-NLS-1$ //$NON-NLS-2$
            ((minutes > 0)
            ? (minutes + Messages.getString("AbstractSolver._min")) : "") +
            secs + Messages.getString("AbstractSolver._sec"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        return Messages.getString("AbstractSolver.0_sec"); //$NON-NLS-1$
    }

    //MAKE DESIGN

    /** execute un make design*/
    public abstract void makeDesign(Topographie topographie,
        String orificesSet, String diametersSet, Hashtable loadFactor,
        Vector constraints,double hsource);

    /** renvoie le r�sultat de design*/
    public abstract String getDesignContentMakeDesign();

    /** renvoie les pressions des noeuds estim�s d'un design */
    public abstract Vector getNodePressureMakeDesign();

    //SIMULATION

    /** execute une simulation*/
    public abstract void simulation(Design design, Properties parameters,
        Hashtable faucetRef);

    /** renvoie le contenu de la simulation */
    public abstract String getSimulationContent();
}
