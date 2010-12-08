package neatwork.gui.tabbedpane;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.plaf.metal.MetalTabbedPaneUI;


/**
 * Title:        Neatwork2
 * Description:  Water distribution network designer
 * Copyright:    Copyright (c) 2002
 * Company:      LOGILAB - University of Geneva
 * @author L. DROUET
 * @version 1.0
 */
public class SingleRowTabbedPaneUI extends MetalTabbedPaneUI {
    protected ActionListener[] buttonListeners;

    public void installUI(JComponent c) {
        this.tabPane = (JTabbedPane) c;
        c.setLayout(createLayoutManager());
        installDefaults();
        installComponents();
        installListeners();
        installKeyboardActions();

        runCount = 1;
        selectedRun = 0;
    }

    public void uninstallUI(JComponent c) {
        uninstallComponents();
        super.uninstallUI(c);
    }

    protected LayoutManager createLayoutManager() {
        return new SingleRowTabbedLayout(tabPane);
    }

    protected void installComponents() {
        JButton[] buttons = ((SingleRowTabbedPane) tabPane).getButtons();

        for (int i = 0; i < buttons.length; i++) {
            tabPane.add(buttons[i]);
        }
    }

    protected void uninstallComponents() {
        JButton[] buttons = ((SingleRowTabbedPane) tabPane).getButtons();

        for (int i = 0; i < buttons.length; i++) {
            tabPane.remove(buttons[i]);
        }
    }

    protected void installListeners() {
        super.installListeners();

        SingleRowTabbedPane stabPane = (SingleRowTabbedPane) tabPane;
        JButton[] buttons = stabPane.getButtons();
        int n = buttons.length;
        buttonListeners = new ActionListener[n];

        for (int i = 0; i < n; i++) {
            buttonListeners[i] = null;

            String str = buttons[i].getActionCommand();

            if (str.equals(SingleRowTabbedPane.ROTATE)) {
                buttonListeners[i] = new ShiftTabs() {
                            protected int getStartIndex() {
                                int index = sPane.getVisibleStartIndex() +
                                    sPane.getVisibleCount();

                                return (index < sPane.getTabCount()) ? index : 0;
                            }
                        };
            } else if (str.equals(SingleRowTabbedPane.PREVIOUS)) {
                buttonListeners[i] = new ShiftTabs() {
                            protected int getStartIndex() {
                                return getStartIndex(sPane.getVisibleStartIndex() -
                                    1);
                            }
                        };
            } else if (str.equals(SingleRowTabbedPane.NEXT)) {
                buttonListeners[i] = new ShiftTabs() {
                            protected int getStartIndex() {
                                return sPane.getVisibleStartIndex() +
                                sPane.getVisibleCount();
                            }
                        };
            } else if (str.equals(SingleRowTabbedPane.FIRST)) {
                buttonListeners[i] = new ShiftTabs();
            } else if (str.equals(SingleRowTabbedPane.LEFT_SHIFT)) {
                buttonListeners[i] = new ShiftTabs() {
                            protected int getStartIndex() {
                                return sPane.getVisibleStartIndex() - 1;
                            }
                        };
            } else if (str.equals(SingleRowTabbedPane.RIGHT_SHIFT)) {
                buttonListeners[i] = new ShiftTabs() {
                            protected int getStartIndex() {
                                return sPane.getVisibleStartIndex() + 1;
                            }
                        };
            } else if (str.equals(SingleRowTabbedPane.LAST)) {
                buttonListeners[i] = new ShiftTabs() {
                            protected int getStartIndex() {
                                return getStartIndex(sPane.getTabCount() - 1);
                            }
                        };
            }

            buttons[i].addActionListener(buttonListeners[i]);
        }
    }

    protected void uninstallListeners() {
        super.uninstallListeners();

        JButton[] buttons = ((SingleRowTabbedPane) tabPane).getButtons();

        for (int i = 0; i < buttons.length; i++) {
            buttons[i].removeActionListener(buttonListeners[i]);
        }
    }

    public int tabForCoordinate(JTabbedPane pane, int x, int y) {
        int tabCount = tabPane.getTabCount();
        SingleRowTabbedPane stabPane = (SingleRowTabbedPane) tabPane;
        int visibleCount = stabPane.getVisibleCount();
        int visibleStartIndex = stabPane.getVisibleStartIndex();

        for (int i = 0, index = visibleStartIndex; i < visibleCount;
                i++, index++) {
            if (rects[index].contains(x, y)) {
                return index;
            }
        }

        return -1;
    }

    public void paint(Graphics g, JComponent c) {
        int selectedIndex = tabPane.getSelectedIndex();
        int tabPlacement = tabPane.getTabPlacement();
        int tabCount = tabPane.getTabCount();

        ensureCurrentLayout();

        SingleRowTabbedPane stabPane = (SingleRowTabbedPane) tabPane;
        int visibleCount = stabPane.getVisibleCount();
        int visibleStartIndex = stabPane.getVisibleStartIndex();

        Rectangle iconRect = new Rectangle();
        Rectangle textRect = new Rectangle();
        Rectangle clipRect = g.getClipBounds();
        Insets insets = tabPane.getInsets();

        tabRuns[0] = visibleStartIndex;

        for (int i = 0, index = visibleStartIndex; i < visibleCount;
                i++, index++) {
            if (rects[index].intersects(clipRect)) {
                paintTab(g, tabPlacement, rects, index, iconRect, textRect);
            }
        }

        if (stabPane.isVisibleTab(selectedIndex)) {
            if (rects[selectedIndex].intersects(clipRect)) {
                paintTab(g, tabPlacement, rects, selectedIndex, iconRect,
                    textRect);
            }
        }

        paintContentBorder(g, tabPlacement, selectedIndex);
    }

    protected void paintContentBorderTopEdge(Graphics g, int tabPlacement,
        int selectedIndex, int x, int y, int w, int h) {
        g.setColor(selectHighlight);

        if ((tabPlacement != TOP) || (selectedIndex < 0) ||
                ((rects[selectedIndex].y + rects[selectedIndex].height + 1) < y) ||
                !((SingleRowTabbedPane) tabPane).isVisibleTab(selectedIndex)) {
            g.drawLine(x, y, (x + w) - 2, y);
        } else {
            Rectangle selRect = rects[selectedIndex];
            g.drawLine(x, y, selRect.x + 1, y);

            if ((selRect.x + selRect.width) < ((x + w) - 2)) {
                g.drawLine(selRect.x + selRect.width, y, (x + w) - 2, y);
            } else {
                g.setColor(shadow);
                g.drawLine((x + w) - 2, y, (x + w) - 2, y);
            }
        }
    }

    protected void paintContentBorderBottomEdge(Graphics g, int tabPlacement,
        int selectedIndex, int x, int y, int w, int h) {
        g.setColor(darkShadow);

        if ((tabPlacement != BOTTOM) || (selectedIndex < 0) ||
                ((rects[selectedIndex].y - 1) > h) ||
                !((SingleRowTabbedPane) tabPane).isVisibleTab(selectedIndex)) {
            g.drawLine(x, (y + h) - 1, (x + w) - 1, (y + h) - 1);
        } else {
            Rectangle selRect = rects[selectedIndex];
            g.drawLine(x, (y + h) - 1, selRect.x, (y + h) - 1);

            if ((selRect.x + selRect.width) < ((x + w) - 2)) {
                g.drawLine(selRect.x + selRect.width, (y + h) - 1, (x + w) - 1,
                    (y + h) - 1);
            }
        }
    }

    protected Insets getTabAreaInsets(int tabPlacement) {
        SingleRowTabbedPane stabPane = (SingleRowTabbedPane) tabPane;
        Dimension d = stabPane.getButtonPreferredSize();
        int n = stabPane.getButtonCount();
        int buttonPlacement = stabPane.getButtonPlacement();

        Insets currentInsets = new Insets(0, 0, 0, 0);

        if (tabPlacement == TOP) {
            currentInsets.top = tabAreaInsets.top;
            currentInsets.bottom = tabAreaInsets.bottom;
        } else {
            currentInsets.top = tabAreaInsets.bottom;
            currentInsets.bottom = tabAreaInsets.top;
        }

        if (buttonPlacement == RIGHT) {
            currentInsets.left = tabAreaInsets.left;
            currentInsets.right = tabAreaInsets.right + (n * d.width);
        } else {
            currentInsets.left = tabAreaInsets.left + (n * d.width);
            currentInsets.right = tabAreaInsets.right;
        }

        return currentInsets;
    }

    protected int lastTabInRun(int tabCount, int run) {
        SingleRowTabbedPane stabPane = (SingleRowTabbedPane) tabPane;

        return (stabPane.getVisibleStartIndex() + stabPane.getVisibleCount()) -
        1;
    }

    protected void ensureCurrentLayout() {
        SingleRowTabbedLayout layout = (SingleRowTabbedLayout) tabPane.getLayout();
        layout.calculateLayoutInfo();
        setButtonsEnabled();
    }

    protected void setButtonsEnabled() {
        SingleRowTabbedPane stabPane = (SingleRowTabbedPane) tabPane;
        int visibleCount = stabPane.getVisibleCount();
        int visibleStartIndex = stabPane.getVisibleStartIndex();
        JButton[] buttons = stabPane.getButtons();
        boolean lEnable = 0 < visibleStartIndex;
        boolean rEnable = (visibleStartIndex + visibleCount) < tabPane.getTabCount();

        for (int i = 0; i < buttons.length; i++) {
            boolean enable = false;
            String str = buttons[i].getActionCommand();

            if (str.equals(SingleRowTabbedPane.ROTATE)) {
                enable = lEnable || rEnable;
            } else if (str.equals(SingleRowTabbedPane.PREVIOUS)) {
                enable = lEnable;
            } else if (str.equals(SingleRowTabbedPane.NEXT)) {
                enable = rEnable;
            } else if (str.equals(SingleRowTabbedPane.FIRST)) {
                enable = lEnable;
            } else if (str.equals(SingleRowTabbedPane.LEFT_SHIFT)) {
                enable = lEnable;
            } else if (str.equals(SingleRowTabbedPane.RIGHT_SHIFT)) {
                enable = rEnable;
            } else if (str.equals(SingleRowTabbedPane.LAST)) {
                enable = rEnable;
            }

            buttons[i].setEnabled(enable);
        }
    }

    //
    // Tab Navigation by Key
    // (Not yet done)
    //
    protected void ensureVisibleTabAt(int index) {
        SingleRowTabbedPane stabPane = (SingleRowTabbedPane) tabPane;
        int visibleCount = stabPane.getVisibleCount();
        int visibleStartIndex = stabPane.getVisibleStartIndex();
        int visibleEndIndex = (visibleStartIndex + visibleCount) - 1;

        if ((visibleStartIndex < index) && (index < visibleEndIndex)) {
            return;
        }

        int selectedIndex = tabPane.getSelectedIndex();
        boolean directionIsRight = (0 < (index - selectedIndex)) ? true : false;

        //if (directionIsRight) {
        if (index <= visibleStartIndex) {
            if (visibleStartIndex == 0) {
                return;
            }

            stabPane.setVisibleStartIndex(--visibleStartIndex);
            ((SingleRowTabbedLayout) tabPane.getLayout()).calculateLayoutInfo();

            int count = stabPane.getVisibleCount();
            int startIndex = stabPane.getVisibleStartIndex();

            if ((startIndex <= index) && (index <= ((startIndex + count) - 1))) {
            } else {
                stabPane.setVisibleStartIndex(++visibleStartIndex);
            }
        }

        //} else {
        if (visibleEndIndex <= index) {
            if (visibleStartIndex == (visibleCount + 1)) {
                return;
            }

            stabPane.setVisibleStartIndex(++visibleStartIndex);
            ((SingleRowTabbedLayout) tabPane.getLayout()).calculateLayoutInfo();

            int count = stabPane.getVisibleCount();
            int startIndex = stabPane.getVisibleStartIndex();

            if ((startIndex <= index) && (index <= ((startIndex + count) - 1))) {
            } else {
                stabPane.setVisibleStartIndex(--visibleStartIndex);
            }
        }

        //}
        int c = stabPane.getVisibleCount();
        int s = stabPane.getVisibleStartIndex();
    }

    protected void selectNextTab(int current) {
        for (int i = current + 1; i < tabPane.getTabCount(); i++) {
            if (tabPane.isEnabledAt(i)) {
                ensureVisibleTabAt(i);
                tabPane.setSelectedIndex(i);

                break;
            }
        }
    }

    protected void selectPreviousTab(int current) {
        for (int i = current - 1; 0 <= i; i--) {
            if (tabPane.isEnabledAt(i)) {
                ensureVisibleTabAt(i);
                tabPane.setSelectedIndex(i);

                break;
            }
        }
    }

    //
    // these methods exist for innerclass
    //
    void setMaxTabHeight(int maxTabHeight) {
        this.maxTabHeight = maxTabHeight;
    }

    int getMaxTabHeight() {
        return maxTabHeight;
    }

    Rectangle[] getRects() {
        return rects;
    }

    SingleRowTabbedPane getTabbedPane() {
        return (SingleRowTabbedPane) tabPane;
    }

    /*DEPRECATED
    protected FontMetrics getFontMetrics() {
      Font font = tabPane.getFont();
      //return Toolkit.getDefaultToolkit().getFontMetrics(font);
      return null;
    }
    */
    protected int calculateMaxTabHeight(int tabPlacement) {
        return super.calculateMaxTabHeight(tabPlacement);
    }

    protected int calculateTabWidth(int tabPlacement, int tabIndex,
        FontMetrics metrics) {
        return super.calculateTabWidth(tabPlacement, tabIndex, metrics);
    }

    protected void assureRectsCreated(int tabCount) {
        super.assureRectsCreated(tabCount);
    }

    //
    // Layout
    //
    class SingleRowTabbedLayout extends BasicTabbedPaneUI.TabbedPaneLayout {
        JTabbedPane tabPane;

        SingleRowTabbedLayout(JTabbedPane tabPane) {
            this.tabPane = tabPane;
        }

        public void layoutContainer(Container parent) {
            super.layoutContainer(parent);

            if (tabPane.getComponentCount() < 1) {
                return;
            }

            int tabPlacement = tabPane.getTabPlacement();
            int maxTabHeight = calculateMaxTabHeight(tabPlacement);
            Insets tabAreaInsets = getTabAreaInsets(tabPlacement);
            Insets insets = tabPane.getInsets();
            Rectangle bounds = tabPane.getBounds();

            SingleRowTabbedPane stabPane = (SingleRowTabbedPane) tabPane;
            Dimension d = stabPane.getButtonPreferredSize();
            JButton[] buttons = stabPane.getButtons();
            int buttonPlacement = stabPane.getButtonPlacement();

            int x;
            int y;

            if (tabPlacement == TOP) { // TOP
                y = bounds.y + insets.top + tabAreaInsets.top;
            } else { // BOTTOM
                y = (bounds.y + bounds.height) - insets.bottom -
                    tabAreaInsets.bottom - maxTabHeight;
            }

            if (buttonPlacement == RIGHT) { // RIGHT
                x = (bounds.x + bounds.width) - insets.right;

                for (int i = buttons.length - 1; 0 <= i; i--) {
                    x -= d.width;
                    buttons[i].setBounds(x, y, d.width, d.height);
                }
            } else { // LEFT
                x = bounds.x + insets.left;

                for (int i = 0; i < buttons.length; i++) {
                    buttons[i].setBounds(x, y, d.width, d.height);
                    x += d.width;
                }
            }
        }

        public void calculateLayoutInfo() {
            int tabCount = tabPane.getTabCount();
            assureRectsCreated(tabCount);
            calculateTabWidths(tabPane.getTabPlacement(), tabCount);
            calculateTabRects(tabPane.getTabPlacement(), tabCount);
        }

        protected void calculateTabWidths(int tabPlacement, int tabCount) {
            if (tabCount == 0) {
                return;
            }

            //FontMetrics metrics = Toolkit.getDefaultToolkit().getFontMetrics(tabPane.getFont());
            //int fontHeight = metrics.getHeight();
            //String test = "Ceci est un test possible!!";
            Font font = tabPane.getFont();
            FontMetrics metrics = tabPane.getFontMetrics(font);
            int fontHeight = metrics.getHeight();

            int maxTabHeight = calculateMaxTabHeight(tabPlacement);
            setMaxTabHeight(maxTabHeight);

            Rectangle[] rects = getRects();

            for (int i = 0; i < tabCount; i++) {
                rects[i].width = calculateTabWidth(tabPlacement, i, metrics);
                rects[i].height = maxTabHeight;
            }
        }

        protected void calculateTabRects(int tabPlacement, int tabCount) {
            if (tabCount == 0) {
                return;
            }

            SingleRowTabbedPane stabPane = (SingleRowTabbedPane) tabPane;
            Dimension size = tabPane.getSize();
            Insets insets = tabPane.getInsets();
            Insets tabAreaInsets = getTabAreaInsets(tabPlacement);
            int selectedIndex = tabPane.getSelectedIndex();

            int maxTabHeight = getMaxTabHeight();
            int x = insets.left + tabAreaInsets.left;
            int y;

            if (tabPlacement == TOP) {
                y = insets.top + tabAreaInsets.top;
            } else { // BOTTOM
                y = size.height - insets.bottom - tabAreaInsets.bottom -
                    maxTabHeight;
            }

            int returnAt = size.width - (insets.right + tabAreaInsets.right);
            Rectangle[] rects = getRects();
            int visibleStartIndex = stabPane.getVisibleStartIndex();
            int visibleCount = 0;

            for (int i = visibleStartIndex; i < tabCount; i++) {
                Rectangle rect = rects[i];

                if (visibleStartIndex < i) {
                    rect.x = rects[i - 1].x + rects[i - 1].width;
                } else {
                    rect.x = x;
                }

                if ((rect.x + rect.width) > returnAt) {
                    break;
                } else {
                    visibleCount++;
                    rect.y = y;
                }
            }

            stabPane.setVisibleCount(visibleCount);
            stabPane.setVisibleStartIndex(visibleStartIndex);
        }
    }

    //
    // Listener
    //
    protected class ShiftTabs implements ActionListener {
        SingleRowTabbedPane sPane;

        public void actionPerformed(ActionEvent e) {
            sPane = getTabbedPane();

            int index = getStartIndex();
            sPane.setVisibleStartIndex(index);
            sPane.repaint();
        }

        //public abstract int getStartIndex();
        protected int getStartIndex() {
            return 0; // first tab
        }

        protected int getStartIndex(int lastIndex) {
            Insets insets = sPane.getInsets();
            Insets tabAreaInsets = getTabAreaInsets(sPane.getTabPlacement());
            int width = sPane.getSize().width - (insets.left + insets.right) -
                (tabAreaInsets.left + tabAreaInsets.right);
            int index;
            Rectangle[] rects = getRects();

            for (index = lastIndex; 0 <= index; index--) {
                width -= rects[index].width;

                if (width < 0) {
                    break;
                }
            }

            return ++index;
        }
    }
}
