/** -----------------------------------------------------------------
 *    Sammelbox: Collection Manager - A free and open-source collection manager for Windows & Linux
 *    Copyright (C) 2011 Jerome Wagener & Paul Bicheler
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ** ----------------------------------------------------------------- */

package org.sammelbox.controller.i18n;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.sammelbox.controller.settings.SettingsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Translator {
	private static final Logger LOGGER = LoggerFactory.getLogger(Translator.class);
	
	private static Language language = null;
	private static ResourceBundle languageBundle = null;
	private Translator() {}
	
	/**
	 * Automatically defines the language to be used by the translator by looking at the system language as well as user preferences.
	 * The user preference has priority over the system language.
	 * */
	public static void setLanguageFromSettingsOrSystem() {
		Language userDefinedLanguage = SettingsManager.getUserDefinedLanguage();
		
		if (userDefinedLanguage != Language.UNKNOWN) {
			setLanguageManually(userDefinedLanguage);
		} else {
			switch (System.getProperty("user.language")) {
			case "de":
				setLanguageManually(Language.DEUTSCH);
				break;
			default:
				setLanguageManually(Language.ENGLISH);
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
			LOGGER.error("The properties file for the selected language (" + language + ") could not be found", mre);
		}
	}
	
	
	/**
	 * Retrieve the language that is currently used for translations. Defaults to English if the language is Unknown
	 * @return the used language
	 * */
	public static Language getUsedLanguage() {
		if (language == Language.UNKNOWN) {
			return Language.ENGLISH;
		}
		
		return language;
	}
	
	/**
	 * Use this method to quickly add a string without the need to immediately translating it, or adding a key to the dictionary
	 * This method should be unused if a release build is produced
	 * @param stringToBeTranslated the string that needs to be translated
	 * @return the string entered as parameter, with a warning prefix
	 * */
	public static String toBeTranslated(String stringToBeTranslated) {
		LOGGER.warn("The following string needs to be translated: " + stringToBeTranslated);
		return get(DictKeys.TO_BE_TRANSLATED, stringToBeTranslated);
	}
	
	/**
	 * Retrieve the translation for the specified key. The translation depends on the selected language
	 * @param key the generic key for the internationalized string
	 * @param parameters an arbitrary number of arguments passed to the translation string
	 * @return a language dependent string which matches the given key
	 * */
	public static String get(String key, Object... parameters) {
		if (languageBundle == null) {
			setLanguageFromSettingsOrSystem();
		}
		
		try {
			if (parameters.length == 0) {
				return languageBundle.getString(key);
			} else {
				return MessageFormat.format(languageBundle.getString(key), parameters);
			}
		} catch (MissingResourceException mre) {
			LOGGER.error("It seems that the following key (" + key + ") is not yet translated for the chosen language (" + language + ")", mre);
			
			return "";			
		}
	}
}
