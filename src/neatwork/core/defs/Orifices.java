package neatwork.core.defs;

import java.util.Comparator;


/** definition d'un orifice*/
public class Orifices implements Comparator<Object> {
    public final static double MAXDIAM = 10000;
    public double diam;

    public Orifices(double diam) {
        this.diam = diam;
    }

    public String toString() {
        return "[" + diam + "]";  
    }

    public int compare(Object o1, Object o2) {
        Double d1 = new Double(((Orifices) o1).diam);
        Double d2 = new Double(((Orifices) o2).diam);

        return d1.compareTo(d2);
    }
}
