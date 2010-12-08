package neatwork.project;

import neatwork.utils.*;

import java.util.*;


/**
 * defintion d'une simulation
 * @author L. DROUET
 * @version 1.0
 */
public class Simulation extends Project {
    Vector data;

    public Simulation(String name) {
        super(Project.TYPE_SIMU, name);
    }

    public void setContent(String content) {
        super.setContent(content);
        data = Tools.readCSV(getContent());
    }

    public Properties getProperties() {
        Properties prop = new Properties();

        for (Enumeration e = data.elements(); e.hasMoreElements();) {
            Vector t = (Vector) e.nextElement();

            //properties
            if ((t.size() == 3)) {
                prop.setProperty("simu." + t.get(0).toString() + ".value", //$NON-NLS-1$ //$NON-NLS-2$
                    t.get(1).toString());
            }
        }

        return prop;
    }

    public Vector getFlowTaps() {
        String[] type = { "S", "I", "D", "D", "D", "D", "D", "D", "I" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$

        return getLine(10, type);
    }

    public Vector getQuartileTaps() {
        String[] type = { "S", "I", "D", "D", "D", "D", "D", "D", "D" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$

        return getLine(11, type);
    }

    public Vector getSpeedPipe() {
        String[] type = { "S", "I", "D", "D" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

        return getLine(5, type);
    }

    public Vector getNodesPressure() {
        String[] type = { "S", "D", "D", "D" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

        return getLine(4, type);
    }

    private Vector getLine(int size, String[] type) {
        Vector v = new Vector();

        for (Enumeration e = data.elements(); e.hasMoreElements();) {
            Vector t = (Vector) e.nextElement();

            if (t.size() == size) {
                Vector temp = new Vector();

                for (int i = 0; i < type.length; i++) {
                    try {
                        if (type[i].equals("I")) { //$NON-NLS-1$
                            temp.add(new Integer(t.get(i).toString()));
                        } else if (type[i].equals("D")) { //$NON-NLS-1$
                            temp.add(new Double(t.get(i).toString()));
                        } else {
                            temp.add(t.get(i).toString());
                        }
                    } catch (Exception ex) {
                        temp.add(""); //$NON-NLS-1$
                    }
                }

                v.add(temp);
            }
        }

        return v;
    }
}
