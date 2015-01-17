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
	GERMAN,
	FRENCH;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Language.class);
	
	public static String getDictionaryBundle(Language language) {
		switch (language) {
		case ENGLISH:
			return "internationalization/dict_en";

		case GERMAN:
			return "internationalization/dict_de";
		
		case FRENCH:
			return "internationalization/dict_fr";
			
		default:
			return "internationalization/dict_en";
		}
	}
	
	public static String getHtmlPage(Language language, String pageName) {
		switch (language) {
		case ENGLISH:
			return "internationalization/html/" + pageName + "_en.html";

		case GERMAN:
			return "internationalization/html/" + pageName + "_de.html";
		
		case FRENCH:
			return "internationalization/html/" + pageName + "_fr.html";
			
		default:
			return "internationalization/html/" + pageName + "_en.html";
		}
	}
	
	public static Language[] valuesWithoutUnknown() {
		return new Language[] {
				Language.ENGLISH,
				Language.GERMAN,
				Language.FRENCH
		};
	}
	
    public static String getTranslation(Language language) {
    	if (ENGLISH.equals(language)) {
    		return Translator.get(DictKeys.ENGLISH);
    	} else if (GERMAN.equals(language)) {
    		return Translator.get(DictKeys.GERMAN);
    	} else if (FRENCH.equals(language)) {
    		return Translator.get(DictKeys.FRENCH);
    	}
    	
    	LOGGER.error("A translation for an unknown language was requested");
    	return "";
    }
    
    public static Language byTranslation(String language) {
    	if ((Translator.get(DictKeys.ENGLISH).equals(language))) {
    		return ENGLISH;
    	} else if ((Translator.get(DictKeys.GERMAN).equals(language))) {
    		return GERMAN;
    	} else if ((Translator.get(DictKeys.FRENCH).equals(language))) {
    		return FRENCH;
    	}
    	
    	LOGGER.error("A language for an unknown translation was requested");
    	return null;
    }
}
