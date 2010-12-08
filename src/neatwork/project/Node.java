package neatwork.project;

import neatwork.Messages;

import neatwork.utils.*;

import java.util.Comparator;


/**
 * Classe qui definit un noeud
 * @author L. DROUET
 * @version 1.0
 */
public class Node implements Comparator, Same {
    public static final int TYPE_RESERVOIR = 0;
    public static final int TYPE_DISPATCH = 1;
    public static final int TYPE_FAUCET = 2;
    private String name = "node"; //$NON-NLS-1$
    private double coordX = 0;
    private double coordY = 0;
    private double height = 0;
    private int type = TYPE_DISPATCH;
    private int nbTaps = 0;
    private double orifice = 0;
    private double orificeComercial = 0;

    public Node() {
    }

    public Node(Node n) {
        this.name = n.getName();
        this.coordX = n.getCoordX();
        this.coordY = n.getCoordY();
        this.height = n.getHeight();
        this.type = n.getType();
        this.nbTaps = n.getNbTaps();
        this.orifice = n.getOrifice();
        this.orificeComercial = n.getComercialOrifice();
    }

    public Node(String name) {
        this.name = name;
    }

    public String toString() {
        return Messages.getString("Node.Node") + name; //$NON-NLS-1$
    }

    public boolean isSame(Object o) {
        return o.toString().equals(this.toString());
    }

    public void setName(String newName) {
        name = newName;
    }

    public String getName() {
        return name;
    }

    public void setCoordX(double newCoordX) {
        coordX = newCoordX;
    }

    public double getCoordX() {
        return coordX;
    }

    public void setCoordY(double newCoordY) {
        coordY = newCoordY;
    }

    public double getCoordY() {
        return coordY;
    }

    public void setHeight(double newHeight) {
        height = newHeight;
    }

    public double getHeight() {
        return height;
    }

    public void setType(int newType) {
        type = newType;
    }

    public int getType() {
        return type;
    }

    public void setNbTaps(int newNbTaps) {
        nbTaps = newNbTaps;
    }

    public int getNbTaps() {
        return nbTaps;
    }

    public void setOrifice(double orifice) {
        this.orifice = orifice;
    }

    public double getOrifice() {
        return orifice;
    }

    public void setComercialOrifice(double comercialOrifice) {
        this.orificeComercial = comercialOrifice;
    }

    public double getComercialOrifice() {
        return orificeComercial;
    }

    //trie par type
    public int compare(Object node1, Object node2) {
        return ((Node) node1).getType() - ((Node) node2).getType();
    }

    //getNameType
    public static String getNameType(int type) {
        switch (type) {
        case TYPE_DISPATCH:
            return Messages.getString("Node.BRANCHING_NODE"); //$NON-NLS-1$

        case TYPE_FAUCET:
            return Messages.getString("Node.FAUCET_NODE"); //$NON-NLS-1$

        case TYPE_RESERVOIR:
            return Messages.getString("Node.TANK"); //$NON-NLS-1$

        default:
            return ""; //$NON-NLS-1$
        }
    }
}
