package neatwork.utils;

import java.awt.event.*;

import javax.swing.*;


/**
 * Classe qui implemente les actions de Neatwork
 * @author L. DROUET
 * @version 1.0
 */
public abstract class NeatworkAction extends AbstractAction {
    public NeatworkAction(String name, Icon icon, String description,
        char mnemonic) {
        this(name, icon, description, new Integer(mnemonic));
    }

    public NeatworkAction(String name, Icon icon, String description,
        Integer mnemonic) {
        setName(name);
        setSmallIcon(icon);
        setShortDescription(description);
        setMnemonic(mnemonic);
    }

    public void setName(String name) {
        putValue(Action.NAME, name);
    }

    public void setSmallIcon(Icon icon) {
        putValue(Action.SMALL_ICON, icon);
    }

    public void setShortDescription(String description) {
        putValue(Action.SHORT_DESCRIPTION, description);
    }

    public void setMnemonic(Integer mnenonic) {
        putValue(Action.MNEMONIC_KEY, mnenonic);
    }

    public abstract void actionPerformed(ActionEvent event);
}

