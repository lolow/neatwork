package neatwork.core.defs;


/**
 * Description des diametres, ancienne version
 */
public class Diameters {
    public String nominal;
    public double diam;
    public double SDR;
    public int type;
    public double rugosite;
    public double pression; // Correspond au denivele max autorise
    public double cost;
    public double summary = 0;
    public double p;
    public double q;
    public double beta;
    public double a = 1.5;

    public Diameters(String nominal, double diam, double SDR, double pression,
        double cost, int type, double rugosite, double p, double q, double beta) {
        this.nominal = nominal;
        this.diam = diam;
        this.pression = pression;
        this.cost = cost;
        this.SDR = SDR;
        this.type = type;
        this.rugosite = rugosite;
        this.p = p;
        this.q = q;
        this.beta = beta;
    }

    public String toString() {
        return "(nominal = " + nominal + ", diam = " + diam + ", length = " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        pression + ", cost = " + cost + ", SDR = " + SDR + "type = " + type + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        ", rugosite =" + rugosite + ", p = " + p + ", q = " + q + ", beta = " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        beta + ")"; //$NON-NLS-1$
    }
}
