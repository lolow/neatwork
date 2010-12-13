package neatwork;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
    private static final String BUNDLE_NAME = "neatwork.neatwork"; 
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
