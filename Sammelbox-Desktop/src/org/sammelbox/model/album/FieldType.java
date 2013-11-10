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

package org.sammelbox.model.album;

import java.sql.Date;
import java.sql.Time;

import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum FieldType {
	ID,
	TEXT,
	DECIMAL, 
	DATE, 
	TIME,
	UUID,
	STAR_RATING,
	URL,
	INTEGER,
	OPTION; 

	private static final Logger LOGGER = LoggerFactory.getLogger(FieldType.class);
	
	/** Returns a collection of translated field types. Special FieldTypes such as ID and UUID won't be included */
	public static String[] getTranslatedFieldTypes() {
		return new String[] {
				Translator.get(DictKeys.FIELD_TYPE_TEXT),
				Translator.get(DictKeys.FIELD_TYPE_DECIMAL),
				Translator.get(DictKeys.FIELD_TYPE_DATE),
				Translator.get(DictKeys.FIELD_TYPE_STAR_RATING),
				Translator.get(DictKeys.FIELD_TYPE_URL),
				Translator.get(DictKeys.FIELD_TYPE_INTEGER),
				Translator.get(DictKeys.FIELD_TYPE_OPTION)
		};
	}
	
	public static FieldType valueOfTranslatedFieldType(String translatedFieldType) {
		if (translatedFieldType.equals(Translator.get(DictKeys.FIELD_TYPE_TEXT))) {
			return FieldType.TEXT;
		} else if (translatedFieldType.equals(Translator.get(DictKeys.FIELD_TYPE_DECIMAL))) {
			return FieldType.DECIMAL;
		} else if (translatedFieldType.equals(Translator.get(DictKeys.FIELD_TYPE_DATE))) {
			return FieldType.DATE;
		} else if (translatedFieldType.equals(Translator.get(DictKeys.FIELD_TYPE_STAR_RATING))) {
			return FieldType.STAR_RATING;
		} else if (translatedFieldType.equals(Translator.get(DictKeys.FIELD_TYPE_URL))) {
			return FieldType.URL;
		} else if (translatedFieldType.equals(Translator.get(DictKeys.FIELD_TYPE_INTEGER))) {
			return FieldType.INTEGER;
		} else if (translatedFieldType.equals(Translator.get(DictKeys.FIELD_TYPE_OPTION))) {
			return FieldType.OPTION;
		}
		
		LOGGER.error("We should never return null at this point. "
				+ "However, if null is returned, this means that a new fieldtype has not been translated");
		
		return null;
	}
	
	public static String translateFieldType(FieldType fieldType) {
		if (fieldType.equals(FieldType.TEXT)) {
			return Translator.get(DictKeys.FIELD_TYPE_TEXT);
		} else if (fieldType.equals(FieldType.DECIMAL)) {
			return Translator.get(DictKeys.FIELD_TYPE_DECIMAL);
		} else if (fieldType.equals(FieldType.DATE)) {
			return Translator.get(DictKeys.FIELD_TYPE_DATE);
		} else if (fieldType.equals(FieldType.STAR_RATING)) {
			return Translator.get(DictKeys.FIELD_TYPE_STAR_RATING);
		} else if (fieldType.equals(FieldType.URL)) {
			return Translator.get(DictKeys.FIELD_TYPE_URL);
		} else if (fieldType.equals(FieldType.INTEGER)) {
			return Translator.get(DictKeys.FIELD_TYPE_INTEGER);
		} else if (fieldType.equals(FieldType.OPTION)) {
			return Translator.get(DictKeys.FIELD_TYPE_OPTION);
		}
		
		LOGGER.error("We should never return null at this point. "
				+ "However, if null is returned, this means that a new fieldtype has not been translated");
		
		return null;
	}

	/**
	 * Transforms a fieldtype into its corresponding sqlite db equivalent.
	 * @return
	 */
	public String toDatabaseTypeString() {
		String res = "";
		switch (this) {
		case ID :
			res = "INTEGER";
			break;
		case TEXT:
			res = "TEXT";
			break;
		case DECIMAL:
			res = "REAL";
			break;
		case DATE:
			res = "DATE";
			break;
		case TIME:
			res = "TIME";
			break;
		case UUID:
			res= "TEXT";
			break;
		case STAR_RATING:
			res = "INTEGER";
			break;
		case URL:
			res = "TEXT";
			break;
		case INTEGER:
			res = "INTEGER";
			break;
		// Default value is never reached as it would result in a null pointer exception.
		// However method must return something and the switch should have a default case.
		default: 
			res = "TEXT";
			break;
		}
		return res;
	}

	/**
	 * Gets the default value for the given type in their correct database type according to toDatabaseTypeString().
	 * @return The default value of the given field type. Empty string if no field type could be determined, which case is
	 * never reachable under normal circumstances.
	 */
	public Object getDefaultValue() {
		switch (this){
		case TEXT: 
			return  "";
		case DECIMAL: 
			return 0.0d;
		case INTEGER: 
			return 0;
		case DATE: 
			return new Date(System.currentTimeMillis());
		case TIME: 
			return new Time(System.currentTimeMillis());
		case URL:
			return "";
		case OPTION:
			return OptionType.UNKNOWN;
		case STAR_RATING :
			return org.sammelbox.model.album.StarRating.ZERO_STARS;
		// Default value is never reached, it would result in a null pointer exception.
		// However method must return something.
		default:
			return "";
		}
	}
}