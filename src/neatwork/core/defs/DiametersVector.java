package neatwork.core.defs;

import java.io.*;

import java.util.*;


/** 
 * Description d'un vecteur de diameter, ancienne version
 *
 */
public class DiametersVector extends Vector {

	private static final long serialVersionUID = 3968718726405911610L;
	private final String taFile = "t-a.properties"; 
    private final String dbaFile = "d-b-a.properties"; 

    public DiametersVector(Vector data, Properties prop) {
        double temperature = Double.parseDouble(prop.getProperty(
                    "topo.watertemp.value", "70"));  

        //transforme les celsius en fahrenheit
        temperature = ((9 * (temperature)) / 5) + 32;

        double a = calculA(temperature);

        for (Enumeration e = data.elements(); e.hasMoreElements();) {
            Vector line = (Vector) e.nextElement();

            String nominal = line.get(0).toString();
            double SDR = Double.parseDouble(line.get(1).toString());
            double diameter = Double.parseDouble(line.get(2).toString());
            double cost = Double.parseDouble(line.get(3).toString());
            double length = Double.parseDouble(line.get(4).toString());
            int type = Integer.parseInt(line.get(5).toString());
            double rugosite = Double.parseDouble(line.get(6).toString());
            double p;
            double q;
            double beta;

            switch (type) {
            //TYPE_PVC
            case 1:
                p = 2 - 0.219;
                q = 5 - 0.219;
                beta = 0.0826 * 0.235 * Math.pow(a * 1e6, -0.219);

                break;

            //TYPE_IRON
            case 2:default:

                Double ironA = new Double(0);
                Double ironB = new Double(0);
                calculIron(rugosite / diameter, ironA, ironB);
                p = 2 - ironB.doubleValue();
                q = 5 - ironB.doubleValue();
                beta = 0.0826 * ironA.doubleValue() * Math.pow(a * 1e6,
                        -ironB.doubleValue());

                break;
            }

            addDiameters(nominal, diameter, SDR, length, cost, type, rugosite,
                p, q, beta);
        }
    }

    public void addDiameters(String nominal, double diam, double SDR,
        double length, double cost, int type, double rugosite, double p,
        double q, double beta) {
        Diameters d = new Diameters(nominal, diam, SDR, length, cost, type,
                rugosite, p, q, beta);
        addElement(d);
    }

    public Diameters getDiameters(double d) {
        Diameters diam = null;
        int i = 0;
        diam = (Diameters) elementAt(i);

        while ((i < (size() - 1)) && (Math.abs(diam.diam - d) > 0.0001)) {
            i++;
            diam = (Diameters) elementAt(i);
        }

        if (Math.abs(diam.diam - d) > 0.0001) {
            return null;
        } else {
            return diam;
        }
    }

    public double getCost(double d) {
        int j = 0;
        Diameters diam = (Diameters) get(j);

        while (diam.diam != d) {
            j++;
            diam = (Diameters) get(j);
        }

        return diam.cost;
    }

    /** calcul du coef a en fonction de la temperature*/
    private double calculA(double temperature) {
        int indice;
        String ts;
        String as;
        double t1 = 0;
        double t2 = 0;
        double a1 = 0;
        double a2 = 0;
        String line;

        try {
            BufferedReader lecture = new BufferedReader(new InputStreamReader(
                        getClass().getResourceAsStream(taFile)));

            while (((line = lecture.readLine()) != null) && (line != "") && 
                    (t2 < temperature)) {
                indice = line.indexOf(","); 
                ts = line.substring(0, indice);
                as = line.substring(indice + 1, line.length());

                try {
                    t2 = Double.parseDouble(ts);
                    a2 = Double.parseDouble(as);
                } catch (NumberFormatException exce) {
                }

                ;

                if (t2 < temperature) {
                    t1 = t2;
                    a1 = a2;
                }
            }
        } catch (IOException e) {
        }

        if (t2 > temperature) {
            return a1 + (((a2 - a1) * (temperature - t1)) / (t2 - t1));
        } else {
            return a2;
        }
    }

    /** calcul de coeff pour le type IRON*/
    private void calculIron(double frac, Double ironA, Double ironB) {
        int indice;
        int indice2;
        String fracs;
        String As;
        String Bs;
        double frac1 = 0;
        double frac2 = 0;
        double A1 = 0;
        double A2 = 0;
        double B1 = 0;
        double B2 = 0;
        BufferedReader lecture = null;
        String line;
        lecture = new BufferedReader(new InputStreamReader(
                    getClass().getResourceAsStream(dbaFile)));

        try {
            while (((line = lecture.readLine()) != null) && (line != "") && 
                    (frac2 < frac)) {
                indice = line.indexOf(","); 
                indice2 = line.lastIndexOf(","); 
                fracs = line.substring(0, indice);
                Bs = line.substring(indice + 1, indice2);
                As = line.substring(indice2 + 1, line.length());

                try {
                    frac2 = Double.parseDouble(fracs);
                    B2 = Double.parseDouble(Bs);
                    A2 = Double.parseDouble(As);
                } catch (NumberFormatException exce) {
                }

                ;

                if (frac2 < frac) {
                    frac1 = frac2;
                    A1 = A2;
                    B1 = B2;
                }
            }
        } catch (IOException ex) {
        }

        if (frac2 > frac) {
            ironA = new Double(A1 +
                    (((A2 - A1) * (frac - frac1)) / (frac2 - frac1)));
            ironB = new Double(B1 +
                    (((B2 - B1) * (frac - frac1)) / (frac2 - frac1)));
        } else {
            ironA = new Double(A2);
            ironB = new Double(B2);
        }
    }
}
