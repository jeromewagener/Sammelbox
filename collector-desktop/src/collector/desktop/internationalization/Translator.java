package collector.desktop.internationalization;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Translator {
	private static Language language = null;
	private static ResourceBundle languageBundle = null;
	
	private Translator() {}
	
	public static void setLanguageFromSettingsOrSystem() {
		switch (System.getProperty("user.language")) {
		case "de":
			setLanguage(Language.DE);
			break;
		default:
			setLanguage(Language.EN);
		}
	}
	
	public static void setLanguage(Language language) {		
		try {
			Translator.language = language;
			languageBundle = ResourceBundle.getBundle(Language.getDictionaryBundle(language));
		} catch (MissingResourceException mre) {
			System.err.println("properties not found"); // TODO log me
		}
	}
	/*
	public static String get(String key) {
		return get(key, new Object[]{});
	}*/
	
	public static String get(String key, Object... parameters) {
		if (languageBundle == null) {
			throw new RuntimeException("In order to use the translator, a language must be set first!");
		}
		
		try {
			if (parameters.length == 0) {
				return languageBundle.getString(key);
			} else {
				return MessageFormat.format(languageBundle.getString(key), parameters);
			}
		} catch (MissingResourceException mre) {
			throw new RuntimeException("It seems that the following key (" + key + ") is not yet translated for the chosen language (" + language + ")");
		}
	}
}
