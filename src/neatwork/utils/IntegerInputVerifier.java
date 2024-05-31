package neatwork.utils;

import java.awt.*;

import javax.swing.*;


/**
 * verifie que l'entee est un entier
 * @author L. DROUET
 * @version 1.0
 */
public class IntegerInputVerifier extends InputVerifier {
    public boolean verify(JComponent comp) {
        boolean returnValue = true;
        JTextField textField = (JTextField) comp;
        String content = textField.getText();

        if (content.length() != 0) {
            try {
                int z = Integer.parseInt(content);

                if (z < 0) {
                    returnValue = false;
                }
            } catch (NumberFormatException e) {
                returnValue = false;
            }
        }

        return returnValue;
    }

    public boolean shouldYieldFocus(JComponent input) {
        boolean valid = super.shouldYieldFocus(input);

        if (!valid) {
            Toolkit.getDefaultToolkit().beep();
        }

        return valid;
    }
}
