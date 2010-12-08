package neatwork.project;

import neatwork.utils.*;


/**
 * Classe qui definit un tuyau
 * @author L. DROUET
 * @version 1.0
 */
public class Pipe implements Same {
    private double length;
    private double length1;
    private double length2;
    private String end;
    private String begin;
    private String refDiam1;
    private String refDiam2;

    public Pipe() {
    }

    public Pipe(Pipe p) {
        this.setLength(p.getLength());
        this.setBegin(p.getBegin());
        this.setEnd(p.getEnd());
    }

    public Pipe(String toString) {
        setBegin(toString.substring(6, toString.lastIndexOf(" -> "))); //$NON-NLS-1$
        setEnd(toString.substring(toString.lastIndexOf(" -> ") + 4)); //$NON-NLS-1$
    }

    public String toString() {
        return "Arc : " + getBegin() + " -> " + getEnd(); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /** un pipe est unique par son sens*/
    public boolean isSame(Object o) {
        return (o.toString().equals(this.toString()) ||
        o.toString().equals(this.toinvString()));
    }

    private String toinvString() {
        return "Arc : " + getEnd() + " -> " + getBegin(); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void setLength(double newLength) {
        length = newLength;
    }

    public double getLength() {
        return length;
    }

    public void setLength1(double newLength) {
        length1 = newLength;
    }

    public double getLength1() {
        return length1;
    }

    public void setLength2(double newLength) {
        length2 = newLength;
    }

    public double getLength2() {
        return length2;
    }

    public void setBegin(String newBegin) {
        begin = newBegin;
    }

    public String getBegin() {
        return begin;
    }

    public void setEnd(String newEnd) {
        end = newEnd;
    }

    public String getEnd() {
        return end;
    }

    public void setRefDiam1(String newrefDiam1) {
        refDiam1 = newrefDiam1;
    }

    public String getRefDiam1() {
        return refDiam1;
    }

    public void setRefDiam2(String newrefDiam2) {
        refDiam2 = newrefDiam2;
    }

    public String getRefDiam2() {
        return refDiam2;
    }
}
