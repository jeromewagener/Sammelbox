/** -----------------------------------------------------------------
 *    Sammelbox: Collection Manager - A free and open-source collection manager for Windows & Linux
 *    Copyright (C) 2011 Jérôme Wagener & Paul Bicheler
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

	/** 
	 * The Picture type and ID are not included since they should not be directly be edited by the user. 
	 * Picture is also not included in the returned string since this method is only used to fill ComboBoxes 
	 * listing possible FieldTypes for the create new Album dialog. GUI specific method.
	 */
	public static String[] toUserTypeStringArray() {
		return new String[] { 	
				FieldType.TEXT.toString(), 
				FieldType.INTEGER.toString(),
				FieldType.DECIMAL.toString(),
				FieldType.STAR_RATING.toString(),
				FieldType.OPTION.toString(),
				FieldType.DATE.toString(),
				FieldType.URL.toString()
				//FieldType.Time.toString(),//TODO In the current iteration the time is not needed as explicit field type. 
		};
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