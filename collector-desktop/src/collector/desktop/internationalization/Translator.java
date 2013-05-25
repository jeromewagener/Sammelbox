package collector.desktop.internationalization;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import collector.desktop.settings.ApplicationSettingsManager;

public class Translator {
	private static Language language = null;
	private static ResourceBundle languageBundle = null;
	private Translator() {}
	
	/**
	 * Automatically defines the language to be used by the translator by looking at the system language as well as user preferences.
	 * The user preference has priority over the system language.
	 * */
	public static void setLanguageFromSettingsOrSystem() {
		Language language = ApplicationSettingsManager.getUserDefinedLanguage();
		
		if (language != Language.Unknown) {
			setLanguageManually(language);
		} else {
			switch (System.getProperty("user.language")) {
			case "de":
				setLanguageManually(Language.Deutsch);
				break;
			default:
				setLanguageManually(Language.English);
			}
		}
	}
	
	/**
	 * Manually defines the language to be used by the translator
	 * @param the language to be used to translate dictionary keys into human readable strings
	 * */	
	public static void setLanguageManually(Language language) {		
		try {
			Translator.language = language;
			languageBundle = ResourceBundle.getBundle(Language.getDictionaryBundle(language));
		} catch (MissingResourceException mre) {
			System.err.println("properties not found"); // TODO log me
		}
	}
	
	
	/**
	 * Retrieve the language that is currently used for translations. Defaults to English if the language is Unknown
	 * @return the used language
	 * */
	public static Language getUsedLanguage() {
		if (language == Language.Unknown) {
			return Language.English;
		}
		
		return language;
	}
	
	/**
	 * Use this method to quickly add a string without the need to immediately translating it, or adding a key to the dictionary
	 * If this method is used during the build of a release version, the build will fail
	 * @param stringToBeTranslated the string that needs to be translated
	 * @return the string entered as parameter, enclosed with a "translation missing" warning
	 * */
	public static String toBeTranslated(String stringToBeTranslated) {
		// TODO log that an untranslated string was used
		// TODO if the release flag is set, and the method is still in use, fail the build!
	
		return get(DictKeys.TO_BE_TRANSLATED, stringToBeTranslated);
	}
	
	/**
	 * Retrieve the translation for the specified key. The translation depends on the selected language
	 * @param parameters an arbitrary number of arguments passed to the translation string
	 * @return a language dependent string which matches the given key
	 * */
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
