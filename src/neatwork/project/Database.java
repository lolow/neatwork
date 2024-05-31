package neatwork.project;

import neatwork.file.*;

import neatwork.utils.*;

import java.util.*;


/**
 * Base de donnee des orifices et diameters
 * @author L. DROUET
 * @version 1.0
 */
public class Database extends Observable {
    public static final int MODIF_DIAMETER = 0;
    public static final int MODIF_ORIFICE = 1;
    private AbstractFileManager fileManager;
    private Properties properties;
    private Hashtable diamList;
    private Vector oriList;

    public Database(AbstractFileManager fileManager, Properties properties) {
        this.fileManager = fileManager;
        this.properties = properties;
        setOrificesContent(readData(properties.getProperty("file.orifices", 
                    "orifices.db"), "/neatwork/orifices.db"));  
        setDiametersContent(readData(properties.getProperty("file.diameters", 
                    "diameters.db"), "/neatwork/diameters.db"));  
    }

    public void setOrificesContent(String content) {
        Vector data = Tools.readCSV(content);
        oriList = new Vector();

        Enumeration e = data.elements();

        while (e.hasMoreElements()) {
            Vector t = (Vector) e.nextElement();

            if (t.size() == 1) {
                addOrifice(t);
            }
        }

        Collections.sort(oriList, new Orifice());
        setChanged();
        notifyObservers(new Integer(MODIF_ORIFICE));
    }

    public void setDiametersContent(String content) {
        Vector data = Tools.readCSV(content);
        diamList = new Hashtable();

        Enumeration e = data.elements();

        while (e.hasMoreElements()) {
            Vector t = (Vector) e.nextElement();

            if (t.size() == 7) {
                addDiameter(t);
            }
        }

        setChanged();
        notifyObservers(new Integer(MODIF_DIAMETER));
    }

    private String readData(String firstfile, String defaultfile) {
        String content = fileManager.readDbFile(firstfile);

        if (content.equals("")) { 
            content = Tools.readStream(getClass().getResourceAsStream(defaultfile));
        }

        return content;
    }

    public void saveOrifices(String content) {
        fileManager.writeDbFile(properties.getProperty("file.orifices", 
                "orifices.db"), content); 
    }

    public void saveDiameters(String content) {
        fileManager.writeDbFile(properties.getProperty("file.diameters", 
                "diameters.db"), content); 
    }

    public Enumeration getDiameters() {
        Vector temp = new Vector(diamList.values());
        Collections.sort(temp, new Diameter());

        return temp.elements();
    }

    //renvoi le diametre de la base qui correspond au caracteristiques
    public Diameter getDiameter(Diameter diameter) {
        Enumeration e = getDiameters();

        while (e.hasMoreElements()) {
            Diameter item = (Diameter) e.nextElement();

            if (item.isSame(diameter)) {
                return item;
            }
        }

        return null;
    }

    public Hashtable getDiametersTable() {
        return diamList;
    }

    public List getOrifices() {
        return oriList;
    }

    /** ajoute le diameter*/
    public void addDiameter(Vector v) {
        boolean isOk = true;
        Diameter d = new Diameter();

        //nominal
        d.setNominal(v.get(0).toString());

        //sdr
        try {
            d.setSdr(Double.parseDouble(v.get(1).toString()));
        } catch (NumberFormatException e) {
            isOk = false;
        }

        //diameter
        try {
            d.setDiameter(Double.parseDouble(v.get(2).toString()));
        } catch (NumberFormatException e) {
            isOk = false;
        }

        //cost
        try {
            d.setCost(Double.parseDouble(v.get(3).toString()));
        } catch (NumberFormatException e) {
            isOk = false;
        }

        //max length
        try {
            d.setMaxLength(Double.parseDouble(v.get(4).toString()));
        } catch (NumberFormatException e) {
            isOk = false;
        }

        //type
        try {
            d.setType(Integer.parseInt(v.get(5).toString()));

            if ((d.getType() > Diameter.typeName.length) || (d.getType() < 1)) {
                isOk = false;
            }
        } catch (NumberFormatException e) {
            isOk = false;
        }

        //roughness
        try {
            d.setRoughness(Double.parseDouble(v.get(6).toString()));
        } catch (NumberFormatException e) {
            isOk = false;
        }

        //ajout
        if (isOk) {
            diamList.put(firstValideRef(), d);
        }
    }

    public String firstValideRef() {
        int cpt = 0;
        boolean find = true;

        while (find) {
            cpt++;
            find = diamList.keySet().contains("D" + cpt); 
        }

        return "D" + cpt; 
    }

    /** ajoute l'orifice*/
    public void addOrifice(Vector v) {
        boolean isOk = true;
        Orifice o = new Orifice();

        //diameter
        try {
            o.setDiameter(Double.parseDouble(v.get(0).toString()));
        } catch (NumberFormatException e) {
            isOk = false;
        }

        //orifice exists ?
        if (Tools.findIndex(oriList, o) != -1) {
            isOk = false;
        }

        //ajout
        if (isOk) {
            oriList.add(o);
        }
    }

    /** get properties*/
    public Properties getProperties() {
        return properties;
    }
}
