package neatwork.project;

import neatwork.Messages;

import java.util.*;


/**
 * Cette classe définit un projet quelconque
 * @author L. DROUET
 * @version 1.0
 */
public class Project extends Observable {
    public final static int TYPE_TOPO = 0;
    public final static int TYPE_DESIGN = 1;
    public final static int TYPE_SIMU = 2;
    public final static int MODIF_CONTENT = 0;
    public final static int MODIF_PROPERTIES = 1;
    public final static String ERROR = Messages.getString("Project.error"); 
    public final static String WARNING = Messages.getString("Project.warning"); 
    private int type;
    private String name;
    private String description;
    private String content;
    private String infoModif;
    private Properties properties;
    private boolean hasChanged;

    public Project(int type, String name) {
        this.type = type;
        this.name = name;
        description = ""; 
        content = ""; 
    }

    //getter
    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getContent() {
        return content;
    }

    public Properties getProperties() {
        return properties;
    }

    public boolean hasChanged() {
        return hasChanged;
    }

    //setter
    public void setType(int type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setContent(String content) {
        this.content = content;
        setHasChanged(true);
        setChanged();
        notifyObservers(new Integer(MODIF_CONTENT));
    }

    public void setHasChanged(boolean changed) {
        this.hasChanged = changed;
    }

    public void setProperties(Properties prop) {
        if (properties == null) {
            properties = new Properties();
        }

        properties.putAll(prop);
        setHasChanged(true);
        setChanged();
        notifyObservers(new Integer(MODIF_PROPERTIES));
    }

    /** renvoie true si le projet a meme nom et type*/
    public boolean hasSameIdentifiant(Project p) {
        return (p.getName().equals(name)) && (p.getType() == type);
    }

    /** renvoie un nom court*/
    public String getShortName() {
        if (name.length() > 15) {
            return name.substring(0, 13) + "..."; 
        }

        return name;
    }

    //erreur modif
    public void freeInfoModif() {
        infoModif = ""; 
    }

    public void addInfoModif(String newInfoModif) {
        infoModif += (newInfoModif + "\n"); 
    }

    public String getInfoModif() {
        return infoModif;
    }

    /** renvoie true si p1 et p2 sont le meme set de properties*/
    public boolean sameProperties(Properties p1, Properties p2) {
        if (p1.keySet().size() != p2.keySet().size()) {
            return false;
        }

        boolean same = true;
        Enumeration e = p1.propertyNames();

        while ((e.hasMoreElements()) && (same)) {
            String prop = e.nextElement().toString();
            same = (p1.getProperty(prop) != null) &&
                (p2.getProperty(prop) != null) &&
                (p1.getProperty(prop).equals(p2.getProperty(prop)));
        }

        return same;
    }
}
