package neatwork.utils;

import java.util.*;

/**
 * Permet de comparer des vecteurs par leur elements indice
 * @author L. DROUET
 * @version 1.0
 */
public class VectorComparator implements Comparator {
    private int indice;

    public VectorComparator(int indice) {
        this.indice = indice;
    }

    public int compare(Object p1, Object p2) {
        Vector v1 = (Vector) p1;
        Vector v2 = (Vector) p2;

        return ((Comparable) v1.get(indice)).compareTo(v2.get(indice));
    }
}
