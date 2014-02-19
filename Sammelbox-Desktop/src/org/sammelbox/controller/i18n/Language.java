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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum Language {
	// if you add a language, please add the language to valuesWithoutUnknown()
	UNKNOWN,
	ENGLISH,
	DEUTSCH;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Language.class);
	
	public static String getDictionaryBundle(Language language) {
		switch (language) {
		case ENGLISH:
			return "internationalization/dict_en";

		case DEUTSCH:
			return "internationalization/dict_de";
		
		default:
			return "internationalization/dict_en";
		}
	}
	
	public static String getHelpPage(Language language) {
		switch (language) {
		case ENGLISH:
			return "internationalization/help_en.html";

		case DEUTSCH:
			return "internationalization/help_de.html";
		
		default:
			return "internationalization/help_en.html";
		}
	}
	
	public static Language[] valuesWithoutUnknown() {
		return new Language[] {
				Language.ENGLISH,
				Language.DEUTSCH
		};
	}
	
    public static String getTranslation(Language language) {
    	if (ENGLISH.equals(language)) {
    		return Translator.toBeTranslated("English");
    	} else if (DEUTSCH.equals(language)) {
    		return Translator.toBeTranslated("Deutsch");
    	}
    	
    	LOGGER.error("A translation for an unknown language was requested");
    	return "";
    }
    
    public static Language byTranslation(String language) {
    	if ((Translator.toBeTranslated("English").equals(language))) {
    		return ENGLISH;
    	} else if ((Translator.toBeTranslated("Deutsch").equals(language))) {
    		return DEUTSCH;
    	}
    	
    	LOGGER.error("A language for an unknown translation was requested");
    	return null;
    }
}
