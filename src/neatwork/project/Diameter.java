package neatwork.project;

import neatwork.Messages;

import neatwork.utils.Same;

import java.util.Comparator;


/**
 * Classe definissant un tuyau comercial
 * @author L. DROUET
 * @version 1.0
 */
public class Diameter implements Same, Comparator {
    public final static int TYPE_PVC = 1; //index à 1 pour garder la compatibilité avec old neatwork
    public final static int TYPE_IRON = 2;
    public static String[] typeName = {
        "", Messages.getString("Diameter.PVC"),
        Messages.getString("Diameter.IRON")
    }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    private String nominal;
    private double sdr;
    private double diameter;
    private double cost;
    private double maxLength;
    private int type;
    private double roughness;
    private double p;
    private double q;
    private double beta;

    public Diameter() {
    }

    //getter
    public String getNominal() {
        return nominal;
    }

    public double getSdr() {
        return sdr;
    }

    public double getDiameter() {
        return diameter;
    }

    public double getCost() {
        return cost;
    }

    public double getMaxLength() {
        return maxLength;
    }

    public int getType() {
        return type;
    }

    public double getRoughness() {
        return roughness;
    }

    public double getP() {
        return p;
    }

    public double getQ() {
        return q;
    }

    public double getBeta() {
        return beta;
    }

    //setter
    public void setNominal(String newNominal) {
        nominal = newNominal;
    }

    public void setSdr(double newSdr) {
        sdr = newSdr;
    }

    public void setDiameter(double newDiameter) {
        diameter = newDiameter;
    }

    public void setCost(double newCost) {
        cost = newCost;
    }

    public void setMaxLength(double newMaxLength) {
        maxLength = newMaxLength;
    }

    public void setType(int newType) {
        type = newType;
    }

    public void setRoughness(double newRoughness) {
        roughness = newRoughness;
    }

    public void setP(double newP) {
        p = newP;
    }

    public void setQ(double newQ) {
        q = newQ;
    }

    public void setBeta(double newBeta) {
        beta = newBeta;
    }

    public boolean isSame(Object o) {
        Diameter d = (Diameter) o;

        return (d.getDiameter() == getDiameter()) && (d.getSdr() == getSdr()) &&
        (d.getType() == getType());
    }

    public int compare(Object d1, Object d2) {
        double v1 = ((Diameter) d1).getDiameter();
        double v2 = ((Diameter) d2).getDiameter();

        if (v1 < v2) {
            return -1;
        } else if (v2 < v1) {
            return 1;
        } else {
            return 0;
        }
    }
}
