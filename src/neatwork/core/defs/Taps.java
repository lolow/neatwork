package neatwork.core.defs;

import neatwork.Messages;


/** definition of a tap*/
public class Taps {
    //DESCRIPTION
    public String taps;
    public int nbTap;
    public double faucetCoef;

    //SIMULATION
    public String select = "close"; //$NON-NLS-1$
    public double orifice = Orifices.MAXDIAM;
    public double orif_com = Orifices.MAXDIAM;
    public double orif_ideal = Orifices.MAXDIAM;

    public Taps(String taps) {
        this.taps = taps;
    }

    public String toString() {
        return Messages.getString("Taps.[_2") + taps +
        Messages.getString("Taps.,oi__3") + orif_ideal +
        Messages.getString("Taps.,oc__4") + orif_com +
        Messages.getString("Taps.]_5"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }
}
