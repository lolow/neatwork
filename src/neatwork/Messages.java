/*
 * Created on 2 juil. 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package neatwork;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * Classe de selection des ressources localisees
 * @author L. DROUET
 *
 */
public class Messages {
    private static final String BUNDLE_NAME = "neatwork.neatwork"; //$NON-NLS-1$
    private static ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME,
            Locale.getDefault());

    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }

    public static void setLocale(Locale locale) {
        RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, locale);
    }
}
