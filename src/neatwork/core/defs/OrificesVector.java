package neatwork.core.defs;

import java.util.*;

/**
 * definition d'un vector d'orifice
 */
public class OrificesVector extends Vector<Orifices> {

	private static final long serialVersionUID = 3254982405440523073L;

	/**
	 * constructeur
	 * <p>
	 * data contient les donnees suivantes:<br>
	 * diameter
	 */
	public OrificesVector(Vector<Object> data) {
		for (Enumeration<?> e = data.elements(); e.hasMoreElements();) {
			Vector<Object> line = (Vector<Object>) e.nextElement();
			addDiameters(Double.parseDouble(line.get(0).toString()));
		}
	}

	public void addDiameters(double d) {
		addElement(new Orifices(d));
	}
}
