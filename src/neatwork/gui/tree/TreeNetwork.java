package neatwork.gui.tree;

import neatwork.Messages;

import neatwork.project.Network;
import neatwork.project.Node;
import neatwork.project.Pipe;

import neatwork.utils.MapComparator;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;


/**
 * JTable construit avec un Network
 * @author L. DROUET
 * @version 1.0
 */
public class TreeNetwork extends JTable implements Observer, MouseListener {
    private Network network;
    private Icon icoReservoir = null;
    private Icon icoDispatch = null;
    private Icon icoFaucet = null;
    private boolean isAtree;
    private Hashtable nodePath = new Hashtable();

    public TreeNetwork(Network network) {
        setNetwork(network);
        setShowGrid(false);
        getTableHeader().setReorderingAllowed(false);
        getTableHeader().setResizingAllowed(true);
        setRowMargin(0);
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        getColumnModel().setColumnMargin(0);

        TreeCellRenderer renderer = new TreeCellRenderer(network, nodePath);
        TreeCellRenderer.color = new Hashtable();
        TreeCellRenderer.color.put(new Integer(Node.TYPE_FAUCET),
            new Color(47, 177, 209));
        TreeCellRenderer.color.put(new Integer(Node.TYPE_DISPATCH),
            new Color(200, 200, 200));
        TreeCellRenderer.color.put(new Integer(Node.TYPE_RESERVOIR),
            new Color(35, 209, 47));
        setDefaultRenderer(String.class, renderer);
        renderer.setFont(getFont());
        this.addMouseListener(this);
    }

    public void setNetwork(Network network) {
        this.network = network;
        network.addObserver(this);

        IconNode root;
        int profondeur = 0;
        Vector data = new Vector();

        if (network.isATree()) {
            isAtree = true;

            TreeNetworkModel model = new TreeNetworkModel(buildTree());
            setModel(model);
        } else {
            isAtree = false;

            Vector line = new Vector();
            line.add("_" + network.getName() +
                Messages.getString("TreeNetwork._is_not_a_tree")); //$NON-NLS-1$ //$NON-NLS-2$
            data.add(line);

            Vector header = new Vector();
            header.add(""); //$NON-NLS-1$
            setModel(new DefaultTableModel(data, header));
        }
    }

    public void search(String nodeName) {
        if (isAtree) {
            TreeNetworkModel model = (TreeNetworkModel) getModel();
            boolean find = false;
            Iterator iter = model.tableCoord.keySet().iterator();
            Point item = new Point(0, 0);

            while ((iter.hasNext()) && (!find)) {
                item = (Point) iter.next();
                find = model.tableCoord.get(item).toString().equalsIgnoreCase(nodeName);
            }

            if (find) {
                scrollRectToVisible(getCellRect(item.y, item.x, true));
            }
        }
    }

    public void update(Observable observable, Object param) {
        if (observable.getClass().isInstance(network)) {
            switch (((Integer) param).intValue()) {
            case Network.MODIF_CONTENT:
                setNetwork((Network) observable);

                break;
            }
        }
    }

    //CONSTRUCTION DE L'ARBRE
    public Hashtable buildTree() {
        nodePath.clear();

        //Step 1: classer les noeuds par profondeur
        //on copie les noeuds dans un map avec un marqueur 0
        Map map = new Hashtable();
        Iterator iter = network.getNodeIterator();

        while (iter.hasNext()) {
            Node item = (Node) iter.next();
            map.put(item.getName(), new Integer(0));
            nodePath.put(item.getName(), new Integer(0));
        }

        //on affecte les profondeurs aux noeuds
        profondeur(network.getSource(), map, 0);

        //on classe les noeuds par profondeur
        int prof = 0;
        Vector nprof = new Vector();

        while (!map.isEmpty()) {
            Vector line = new Vector();
            iter = map.keySet().iterator();

            while (iter.hasNext()) {
                String n = iter.next().toString();
                Integer p = (Integer) map.get(n);

                if (p.intValue() == prof) {
                    line.add(n);
                    iter.remove();
                }
            }

            nprof.add(line);
            prof++;
        }

        //Step 2 : Calcul du nombre de faucet par noeud
        //on copie les noeuds dans un map avec un marqueur 0
        map = new Hashtable();
        iter = network.getNodeIterator();

        while (iter.hasNext()) {
            Node item = (Node) iter.next();
            map.put(item.getName(), new Integer(0));
        }

        //on remonte \u00E0 la source chacun des faucets en incrementant
        iter = network.getNodeIterator();

        int row = 0;

        while (iter.hasNext()) {
            Node item = (Node) iter.next();

            if (item.getType() == Node.TYPE_FAUCET) {
                remonteSource(item, map);
                row++;
            }
        }

        //on trie les nprof par ordre croissant de faucets
        Enumeration e = nprof.elements();
        Comparator comparator = new MapComparator(map);

        while (e.hasMoreElements()) {
            Vector item = (Vector) e.nextElement();
            Collections.sort(item, comparator);
        }

        //Step 3 : placement optimal des noeuds dans un tableau
        // positionnement en X
        Map mapX = new Hashtable();

        for (int i = 0; i < nprof.size(); i++) {
            Vector line = (Vector) nprof.get(i);

            for (int j = 0; j < line.size(); j++) {
                mapX.put(line.get(j), new Integer(i));
            }
        }

        //positionnement en Y
        Map mapY = new Hashtable();
        mapY.put(network.getSource().getName(), new Integer(0));

        for (int i = 0; i < (nprof.size() - 1); i++) {
            Vector line = (Vector) nprof.get(i);
            Vector lineSuiv = (Vector) nprof.get(i + 1);

            for (int j = 0; j < line.size(); j++) {
                String node = line.get(j).toString();
                int pos = ((Integer) mapY.get(node)).intValue();

                //New VERSION: arbre mieux mais moins rapide
                for (int k = 0; k < lineSuiv.size(); k++) {
                    String succ = lineSuiv.get(k).toString();

                    if (network.isSucc(node, succ)) {
                        mapY.put(succ, new Integer(pos));
                        pos += (((Integer) map.get(succ)).intValue());
                    }
                }

                //DEBUG: old version moins bonne representation mais plus rapide
                //iter = network.getPipeIterator();
                //while (iter.hasNext()) {
                //  Pipe item = (Pipe) iter.next();
                //  if (item.getBegin().equals(node)) {
                //    mapY.put(item.getEnd(), new Integer(pos));
                //    pos += (((Integer)map.get(item.getEnd())).intValue());
                //  }
                //}
            }
        }

        //Step 3 Inversion des roles
        Hashtable coord = new Hashtable();
        iter = mapX.keySet().iterator();

        while (iter.hasNext()) {
            String item = iter.next().toString();
            coord.put(new Point(((Integer) mapX.get(item)).intValue(),
                    ((Integer) mapY.get(item)).intValue()), item);
        }

        //Step 4 Ajout des croisement verticaux
        //L
        for (int i = 0; i < 2; i++) {
            iter = network.getNodeIterator();

            while (iter.hasNext()) {
                Node item = (Node) iter.next();
                int y = ((Integer) mapY.get(item.getName())).intValue();
                int x = ((Integer) mapX.get(item.getName())).intValue();

                if ((y > 0) && (coord.get(new Point(x - 1, y)) == null)) {
                    coord.put(new Point(x - 1, y), "_*L"); //$NON-NLS-1$
                    x--;
                    y--;

                    Object z = coord.get(new Point(x, y));

                    while ((y > 0) && (z == null)) {
                        coord.put(new Point(x, y), "_*I"); //$NON-NLS-1$
                        y--;
                        z = coord.get(new Point(x, y));
                    }

                    if ((z.toString().equals("_*I")) || //$NON-NLS-1$
                            (z.toString().equals("_*L"))) { //$NON-NLS-1$
                        coord.put(new Point(x, y), "_*T"); //$NON-NLS-1$
                    }
                } else if ((y > 0) &&
                        (coord.get(new Point(x - 1, y)).toString().equals("_*I"))) { //$NON-NLS-1$
                    coord.put(new Point(x - 1, y), "_*T"); //$NON-NLS-1$
                }
            }
        }

        return coord;
    }

    public void profondeur(Node node, Map map, int prof) {
        map.put(node.getName(), new Integer(prof));

        Iterator iter = network.getPipeIterator();

        while (iter.hasNext()) {
            Pipe p = (Pipe) iter.next();

            if (p.getBegin().equals(node.getName())) {
                profondeur(network.getNode(p.getEnd()), map, prof + 1);
            }
        }
    }

    public void remonteSource(Node node, Map map) {
        Integer i = (Integer) map.get(node.getName());
        i = new Integer(i.intValue() + 1);
        map.put(node.getName(), i);

        if (node.getType() != Node.TYPE_RESERVOIR) {
            remonteSource(network.getFirstPred(node), map);
        }
    }

    // r�agit aux clicks
    public void mouseClicked(MouseEvent e) {
        int row = rowAtPoint(e.getPoint());
        int col = columnAtPoint(e.getPoint());
        String node = getModel().getValueAt(row, col).toString();

        if ((node.length() > 0) && (!node.substring(0, 1).equals("_"))) { //$NON-NLS-1$

            //remonte � la source
            nodePath.clear();

            Iterator iter = network.getNodeIterator();

            while (iter.hasNext()) {
                Node item = (Node) iter.next();
                nodePath.put(item.getName(), new Integer(0));
            }

            remonteSource(network.getNode(node), nodePath);
            repaint();
        }
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }
}
