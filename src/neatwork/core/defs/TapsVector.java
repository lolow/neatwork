package neatwork.core.defs;

import java.util.*;


/** definition du vecteur de taps*/
public class TapsVector extends Vector {

	private static final long serialVersionUID = -569548333211185728L;

	public void addTaps(Taps t) {
        addElement(t);
    }

    public void addTaps(String t) {
        Taps taps = new Taps(t);
        addTaps(taps);
    }

    /** renvoie true si t est un tuyau*/
    public boolean isTaps(String t) {
        Taps taps;
        boolean bool = false;

        for (int i = 0; i < size(); i++) {
            taps = (Taps) elementAt(i);

            if (taps.taps == t) {
                bool = true;
            }
        }

        return bool;
    }

    /** renvoie la position du taps dans le vecteur ou -1 quand il n'y est pas*/
    public int getIndex(String n) {
        int index = -1;
        Taps taps;

        for (int i = 0; i < size(); i++) {
            taps = (Taps) elementAt(i);

            if (taps.taps == n) {
                index = i;
            }
        }

        return index;
    }

    /** renvoie le tap appele name*/
    public Taps getTap(String name) {
        return (Taps) get(getIndex(name));
    }
}
