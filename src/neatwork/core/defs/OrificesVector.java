package neatwork.core.defs;

import java.util.*;


/**
 * d\u00E9finition d'un vector d'orifice
 */
public class OrificesVector extends Vector {
    /**
     * constructeur
     * <p>
     * data contient les donn\u00E9es suivantes:<br>
     * diameter
     */
    public OrificesVector(Vector data) {
        for (Enumeration e = data.elements(); e.hasMoreElements();) {
            Vector line = (Vector) e.nextElement();
            addDiameters(Double.parseDouble(line.get(0).toString()));
        }
    }

    public void addDiameters(double d) {
        addElement(new Orifices(d));
    }
}
