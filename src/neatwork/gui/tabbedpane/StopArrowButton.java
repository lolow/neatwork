package neatwork.gui.tabbedpane;

import java.awt.*;

import javax.swing.*;
import javax.swing.plaf.basic.*;


/**
 * Title:        Neatwork2
 * Description:  Water distribution network designer
 * Copyright:    Copyright (c) 2002
 * Company:      LOGILAB - University of Geneva
 * @author L. DROUET
 * @version 1.0
 */
public class StopArrowButton extends BasicArrowButton {
    public StopArrowButton(int direction) {
        super(direction);
    }

    public void paintTriangle(Graphics g, int x, int y, int size,
        int direction, boolean isEnabled) {
        super.paintTriangle(g, x, y, size, direction, isEnabled);

        Color c = g.getColor();

        if (isEnabled) {
            g.setColor(UIManager.getColor("controlDkShadow")); 
        } else {
            g.setColor(UIManager.getColor("controlShadow")); 
        }

        g.translate(x, y);
        size = Math.max(size, 2);

        int mid = size / 2;
        int h = size - 1;

        if (direction == WEST) {
            g.drawLine(-1, mid - h, -1, mid + h);

            if (!isEnabled) {
                g.setColor(UIManager.getColor("controlLtHighlight")); 
                g.drawLine(0, mid - h + 1, 0, mid - 1);
                g.drawLine(0, mid + 2, 0, mid + h + 1);
            }
        } else { // EAST
            g.drawLine(size, mid - h, size, mid + h);

            if (!isEnabled) {
                g.setColor(UIManager.getColor("controlLtHighlight")); 
                g.drawLine(size + 1, mid - h + 1, size + 1, mid + h + 1);
            }
        }

        g.setColor(c);
    }
}
