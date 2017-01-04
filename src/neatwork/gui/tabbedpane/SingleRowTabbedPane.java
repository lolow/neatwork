package neatwork.gui.tabbedpane;

import neatwork.Messages;

import java.awt.*;

import javax.swing.*;
import javax.swing.plaf.basic.*;


/**
 * Classe qui definit un tabbedPane sur une ligne
 * ATTENTION LE RAMO
 * @author L. DROUET
 * @version 1.0
 */
public class SingleRowTabbedPane extends JTabbedPane {
    public static final String ROTATE = Messages.getString(
            "SingleRowTabbedPane.Rotate"); 
    public static final String PREVIOUS = Messages.getString(
            "SingleRowTabbedPane.Previous"); 
    public static final String NEXT = Messages.getString(
            "SingleRowTabbedPane.Next"); 
    public static final String FIRST = Messages.getString(
            "SingleRowTabbedPane.First"); 
    public static final String LEFT_SHIFT = Messages.getString(
            "SingleRowTabbedPane.Left"); 
    public static final String RIGHT_SHIFT = Messages.getString(
            "SingleRowTabbedPane.Right"); 
    public static final String LAST = Messages.getString(
            "SingleRowTabbedPane.Last"); 
    public static final int ONE_BUTTON = 1; //                  ROTATE                 ;
    public static final int TWO_BUTTONS = 2; //          PREVIOUS  |     NEXT           ;
    public static final int FOUR_BUTTONS = 4; // FIRST | LEFT_SHIFT | RIGHT_SHIFT | LAST ;
    protected int buttonPlacement;
    protected int buttonCount;
    protected JButton[] tabPaneButtons;
    protected Dimension buttonSize;
    protected int visibleCount;
    protected int visibleStartIndex;
    private final int BUTTON_WIDTH = 16;
    private final int BUTTON_HEIGHT = 17;

    public SingleRowTabbedPane() {
        this(TWO_BUTTONS, RIGHT);
    }

    public SingleRowTabbedPane(int buttonCount, int buttonPlacement) {
        setButtonPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        tabPaneButtons = createButtons(buttonCount);
        this.buttonPlacement = buttonPlacement;
        visibleStartIndex = 0;

        setUI(new SingleRowTabbedPaneUI());
    }

    public void setTabPlacement(int tabPlacement) {
        if ((tabPlacement == LEFT) || (tabPlacement == RIGHT)) {
            throw new IllegalArgumentException("not suported: LEFT and RIGHT"); 
        }

        super.setTabPlacement(tabPlacement);
    }

    public int getButtonPlacement() {
        return buttonPlacement;
    }

    public void setButtonPreferredSize(Dimension d) {
        if (d != null) {
            buttonSize = d;
        }
    }

    public Dimension getButtonPreferredSize() {
        return buttonSize;
    }

    public JButton[] getButtons() {
        return tabPaneButtons;
    }

    public int getButtonCount() {
        return buttonCount;
    }

    public void insertTab(String title, Icon icon, Component component,
        String tip, int index) {
        if (component instanceof TabbedPaneButton) {
            if (component != null) {
                component.setVisible(true);
                addImpl(component, null, -1);
            }

            return;
        }

        super.insertTab(title, icon, component, tip, index);
    }

    public boolean isVisibleTab(int index) {
        if ((visibleStartIndex <= index) &&
                (index < (visibleStartIndex + visibleCount))) {
            return true;
        } else {
            return false;
        }
    }

    public int getVisibleCount() {
        return visibleCount;
    }

    public void setVisibleCount(int visibleCount) {
        if (visibleCount < 0) {
            return;
        }

        this.visibleCount = visibleCount;
    }

    public int getVisibleStartIndex() {
        return visibleStartIndex;
    }

    public void setVisibleStartIndex(int visibleStartIndex) {
        if ((visibleStartIndex < 0) || (getTabCount() <= visibleStartIndex)) {
            return;
        }

        this.visibleStartIndex = visibleStartIndex;
    }

    protected JButton[] createButtons(int buttonCount) {
        JButton[] tabPaneButtons = null;

        switch (buttonCount) {
        case ONE_BUTTON:
            this.buttonCount = buttonCount;
            tabPaneButtons = new JButton[buttonCount];
            tabPaneButtons[0] = new PrevOrNextButton(EAST);
            tabPaneButtons[0].setActionCommand(ROTATE);

            break;

        case TWO_BUTTONS:
            this.buttonCount = buttonCount;
            tabPaneButtons = new JButton[buttonCount];
            tabPaneButtons[0] = new PrevOrNextButton(WEST);
            tabPaneButtons[0].setActionCommand(PREVIOUS);
            tabPaneButtons[1] = new PrevOrNextButton(EAST);
            tabPaneButtons[1].setActionCommand(NEXT);

            break;

        case FOUR_BUTTONS:
            this.buttonCount = buttonCount;
            tabPaneButtons = new JButton[buttonCount];
            tabPaneButtons[0] = new FirstOrLastButton(WEST);
            tabPaneButtons[0].setActionCommand(FIRST);
            tabPaneButtons[1] = new PrevOrNextButton(WEST);
            tabPaneButtons[1].setActionCommand(LEFT_SHIFT);
            tabPaneButtons[2] = new PrevOrNextButton(EAST);
            tabPaneButtons[2].setActionCommand(RIGHT_SHIFT);
            tabPaneButtons[3] = new FirstOrLastButton(EAST);
            tabPaneButtons[3].setActionCommand(LAST);

            break;

        default:}

        return tabPaneButtons;
    }

    class PrevOrNextButton extends BasicArrowButton implements TabbedPaneButton {
        public PrevOrNextButton(int direction) {
            super(direction);
        }
    }

    class FirstOrLastButton extends StopArrowButton implements TabbedPaneButton {
        public FirstOrLastButton(int direction) {
            super(direction);
        }
    }
}
