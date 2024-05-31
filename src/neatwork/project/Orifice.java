package neatwork.project;

import neatwork.utils.*;

import java.util.Comparator;


/**
 * Classe definissant un orifice standard
 * @author L. DROUET
 * @version 1.0
 */
public class Orifice implements Same, Comparator {
    private double diameter;

    public Orifice() {
    }

    public double getDiameter() {
        return diameter;
    }

    public void setDiameter(double newDiameter) {
        diameter = newDiameter;
    }

    public boolean isSame(Object o) {
        return (((Orifice) o).getDiameter() == diameter);
    }

    public int compare(Object o1, Object o2) {
        double v1 = ((Orifice) o1).getDiameter();
        double v2 = ((Orifice) o2).getDiameter();

        if (v1 < v2) {
            return -1;
        } else if (v2 < v1) {
            return 1;
        } else {
            return 0;
        }
    }
}
