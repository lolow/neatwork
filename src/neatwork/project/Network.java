package neatwork.project;

import neatwork.Messages;

import neatwork.utils.*;

import java.util.*;


/**
 * Classe qui fournit les methodes standard d'un reseau
 * @author L. DROUET
 * @version 1.0
 */
public class Network extends Project {
    protected List nodeList = new Vector();
    protected List pipeList = new Vector();

    public Network(int type, String name) {
        super(type, name);
    }

    //gestion des listes=====================================//
    public void addNode(Node node) {
        //si une source
        if ((node.getType() == Node.TYPE_RESERVOIR) && thereIsAlreadyASource()) {
            addInfoModif(Project.ERROR +
                Messages.getString("Network.can__t_add") + node + 
                Messages.getString("Network.___there_is_already_a_source")); 
        } else if (findIndex(nodeList, node) == -1) {
            nodeList.add(node);
            setNodesType();
        } else {
            addInfoModif(Project.ERROR +
                Messages.getString("Network.can__t_add") + node + 
                Messages.getString("Network.___existing_name")); 
        }
    }

    public Node getNode(String name) {
        Node findNode = null;
        Iterator iter = nodeList.iterator();

        while (iter.hasNext()) {
            Node node = (Node) iter.next();

            if (node.getName().equals(name)) {
                return node;
            }
        }

        return findNode;
    }

    public void addPipe(Pipe pipe) {
        if (pipe.getBegin().equals(pipe.getEnd())) {
            addInfoModif(Project.ERROR +
                Messages.getString("Network.can__t_add") + pipe + 
                Messages.getString("Network.___begin_and_end_are_same")); 
        } else if (findIndex(nodeList, new Node(pipe.getBegin())) == -1) {
            addInfoModif(Project.ERROR +
                Messages.getString("Network.can__t_add") + pipe + 
                Messages.getString("Network.___begin_doesn__t_exist")); 
        } else if (findIndex(nodeList, new Node(pipe.getEnd())) == -1) {
            addInfoModif(Project.ERROR +
                Messages.getString("Network.can__t_add") + pipe + 
                Messages.getString("Network.___end_doesn__t_exist")); 
        } else if (findIndex(pipeList, pipe) == -1) {
            pipeList.add(pipe);
            setNodesType();
        } else {
            addInfoModif(Project.ERROR +
                Messages.getString("Network.can__t_add") + pipe + 
                Messages.getString("Network.___existing_name")); 
        }
    }

    public boolean removeNode(Node node) {
        if (node.getType() == Node.TYPE_RESERVOIR) {
            return false;
        }

        return nodeList.remove(node);
    }

    public boolean removePipe(Pipe pipe) {
        return pipeList.remove(pipe);
    }

    public int getNbNodes() {
        return nodeList.size();
    }

    public int getNbNodes(int type) {
        int cpt = 0;
        Iterator e = nodeList.iterator();

        while (e.hasNext()) {
            Node item = (Node) e.next();

            if (item.getType() == type) {
                cpt++;
            }
        }

        return cpt;
    }

    public int getNbTotalTaps() {
        int cpt = 0;
        Iterator e = nodeList.iterator();

        while (e.hasNext()) {
            Node item = (Node) e.next();
            cpt += item.getNbTaps();
        }

        return cpt;
    }

    public double getMinLength() {
        double min = 150;
        Iterator e = pipeList.iterator();

        while (e.hasNext()) {
            Pipe item = (Pipe) e.next();
            min = Math.min(item.getLength(), min);
        }

        return min;
    }

    public double getMaxHeight() {
        double max = -150;
        Iterator e = nodeList.iterator();

        while (e.hasNext()) {
            Node item = (Node) e.next();
            max = Math.max(item.getHeight(), max);
        }

        return max;
    }

    public double getTotalHeightChange() {
        double min = 0;
        double max = 0;
        Iterator e = nodeList.iterator();

        while (e.hasNext()) {
            Node item = (Node) e.next();

            if (item.getHeight() > max) {
                max = item.getHeight();
            }

            if (item.getHeight() < min) {
                min = item.getHeight();
            }
        }

        return max - min;
    }

    public double getTotalLength() {
        double cpt = 0;
        Iterator e = pipeList.iterator();

        while (e.hasNext()) {
            Pipe item = (Pipe) e.next();
            cpt += item.getLength();
        }

        return cpt;
    }

    public int getNbPipes() {
        return pipeList.size();
    }

    public Iterator getNodeIterator() {
        return nodeList.iterator();
    }

    public Iterator getPipeIterator() {
        return pipeList.iterator();
    }

    private void setNodesType() {
        Iterator iter = nodeList.iterator();

        while (iter.hasNext()) {
            Node item = (Node) iter.next();

            if (item.getType() != Node.TYPE_RESERVOIR) {
                if (hasSucc(item)) {
                    item.setType(Node.TYPE_DISPATCH);
                } else {
                    item.setType(Node.TYPE_FAUCET);
                }
            }
        }
    }

    /** vide les liste de nodes et de pipes*/
    public void removeNetwork() {
        pipeList.retainAll(new Vector());
        nodeList.retainAll(new Vector());
    }

    /** renvoie -1 si pas trouve ou l'index sinon*/
    protected int findIndex(List list, Same object) {
        return Tools.findIndex(list, object);
    }

    /** renvoie true if therisalready a source*/
    public boolean thereIsAlreadyASource() {
        return (getSource() != null);
    }

    protected String getFirstPossibleNodeName(String nodeName) {
        String old = nodeName;
        int cpt = 0;

        while (findIndex(nodeList, new Node(nodeName)) != -1) {
            nodeName = old + " (" + (++cpt) + ")";  
        }

        return nodeName;
    }

    //gestion de la source==========================================//
    public Node getSource() {
        Node node = null;
        Iterator iter = getNodeIterator();

        while (iter.hasNext()) {
            Node item = (Node) iter.next();

            if (item.getType() == Node.TYPE_RESERVOIR) {
                node = item;
            }
        }

        return node;
    }

    public Node getLastNode() {
        return (Node) nodeList.get(nodeList.size() - 1);
    }

    /**ajoute une source*/
    protected void addNewSource() {
        if (getNbNodes() == 0) {
            Node node = new Node();
            node.setName(getFirstPossibleNodeName(Messages.getString("Network.Source"))); 
            node.setType(Node.TYPE_RESERVOIR);
            addNode(node);
            addInfoModif(Project.WARNING +
                Messages.getString("Network._a_source") + node.getName() + 
                Messages.getString("Network.)_has_been_added")); 
        }
    }

    //algo en O(m)
    /**renvoie true si n a au moins un successeur*/
    public boolean hasSucc(Node n) {
        boolean find = false;
        Iterator iter = pipeList.iterator();

        while ((iter.hasNext()) && (!find)) {
            find = ((Pipe) iter.next()).getBegin().equals(n.getName());
        }

        return find;
    }

    //algo en O(m)

    /**renvoie true si succ est successeur de node*/
    public boolean isSucc(String node, String succ) {
        boolean find = false;
        Iterator iter = pipeList.iterator();

        while ((iter.hasNext()) && (!find)) {
            Pipe p = (Pipe) iter.next();
            find = (p.getBegin().equals(node)) && (p.getEnd().equals(succ));
        }

        return find;
    }

    //algo en O(m)
    public boolean hasPred(Node n) {
        boolean find = false;
        Iterator iter = pipeList.iterator();

        while ((iter.hasNext()) && (!find)) {
            find = ((Pipe) iter.next()).getEnd().equals(n.getName());
        }

        return find;
    }

    //algo en O(m)
    public Node getFirstPred(Node n) {
        boolean find = false;
        boolean find2 = false;
        Iterator iter = pipeList.iterator();
        Pipe p = null;

        while ((iter.hasNext()) && (!find)) {
            p = (Pipe) iter.next();
            find = p.getEnd().equals(n.getName());
        }

        return getNode(p.getBegin());
    }

    //algo en O(m)
    public boolean nbPredIsExactly(Node n, int nbPred) {
        int cpt = 0;
        Iterator iter = pipeList.iterator();

        while (iter.hasNext() && (cpt < (nbPred + 1))) {
            if (((Pipe) iter.next()).getEnd().equals(n.getName())) {
                cpt++;
            }
        }

        return (cpt == nbPred);
    }

    //algo en O(nm)
    public boolean isATree() {
        boolean tree = true;
        Iterator iter = nodeList.iterator();

        while ((iter.hasNext()) && (tree)) {
            Node node = (Node) iter.next();

            if (node.getType() != Node.TYPE_RESERVOIR) {
                tree = nbPredIsExactly(node, 1);
            }

            if (node.getType() == Node.TYPE_RESERVOIR) {
                tree = nbPredIsExactly(node, 0);
            }
        }

        return tree;
    }
    
	/**
	 * Mets la source à la hauteur height et modifie relativement
	 * la hauteur de tous les noeuds 
	 * @return la hauteur d'origine de la source
	 */
	public double setHauteurSource(double height) {
		//Get source height
		double hsource = getSource().getHeight();
			
		//Translate all node height
		Iterator iter = nodeList.iterator();

		while (iter.hasNext()) {
			Node node = (Node) iter.next();
			node.setHeight(node.getHeight() + height - hsource);
		}
		
		return hsource;	
	}
}
