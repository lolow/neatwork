package neatwork.project;

import java.util.*;


/**
 * Cette classe maintient les projets en cours
 * @author L. DROUET
 * @version 1.0
 */
public class ProjectManager extends Observable {
    public static final int MODIF_SETPROJECT = 0;
    public static final int MODIF_ADDPROJECT = 1;
    public static final int MODIF_REMOVEPROJECT = 2;
    public static final int MODIF_SETINDEX = 3;
    private List<Project> projects = new Vector<Project>();
    private int index = -1;

    public ProjectManager() {
    }

    /** renvoie l'index */
    public int getIndex() {
        return index;
    }

    /** renvoie l'index */
    public void setIndex(int index) {
        this.index = index;
        setChanged();
        notifyObservers(new Integer(MODIF_SETINDEX));
    }

    public Project getCurrentProject() {
        return getProject(index);
    }

    /** retourne le projet index*/
    public Project getProject(int index) {
        if ((index > -1) && (index < projects.size())) {
            return (Project) projects.get(index);
        }

        return null;
    }

    /** modifie le projet index et renvoie l'ancien*/
    public Project setProject(int index, Project project) {
        project = (Project) projects.set(index, project);
        setIndex(index);
        setChanged();
        notifyObservers(new Integer(MODIF_SETPROJECT));

        return project;
    }

    /** modifie le projet index et renvoie l'ancien*/
    public Project setCurrentProject(Project project) {
        return setProject(index, project);
    }

    /** ajoute un projet*/
    public void addProject(Project project) {
        projects.add(project);
        setChanged();
        notifyObservers(new Integer(MODIF_ADDPROJECT));
        setIndex(projects.size() - 1);
    }

    /** enleve le projet a la position index */
    public Project removeProject(int index) {
        if ((index > -1) && (index < projects.size())) {
            Project project = (Project) projects.remove(index);
            setChanged();
            notifyObservers(new Integer(MODIF_REMOVEPROJECT));

            return project;
        }

        return null;
    }

    public Project removeCurrentProject() {
        return removeProject(getIndex());
    }

    /** renvoie le nombre de projects*/
    public int getNbProject() {
        return projects.size();
    }

    /** renvoie le nombre de projects*/
    public ListIterator<Project> getProjectIterator() {
        return projects.listIterator();
    }

    /** projet deja charge et renvoie un nouveau project utilisable*/
    public String isAlreadyLoaded(Project project) {
        String oldname = project.getName();
        String name = oldname;
        int cpt = 2;

        //regarde si le nom est indice
        int i = oldname.lastIndexOf("("); 
        int j = oldname.lastIndexOf(")"); 

        if (i < j) {
            try {
                cpt = Integer.parseInt(oldname.substring(i + 1, j));
                oldname = oldname.substring(0, i - 1);
                name = oldname + " (" + String.valueOf(cpt++) + ")";  
            } catch (NumberFormatException e) {
            }
        }

        while (isInList(new Project(project.getType(), name)))
            name = oldname + " (" + String.valueOf(cpt++) + ")";  

        return name;
    }

    private boolean isInList(Project p) {
        Iterator iter = projects.iterator();
        boolean find = false;

        while (iter.hasNext()) {
            find |= ((Project) iter.next()).hasSameIdentifiant(p);
        }

        return find;
    }
}
