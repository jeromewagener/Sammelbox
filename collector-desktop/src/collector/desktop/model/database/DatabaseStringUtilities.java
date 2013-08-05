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

package collector.desktop.model.database;

import collector.desktop.model.database.operations.DatabaseConstants;

public class DatabaseStringUtilities {

	/** Creates a database safe album table name for a given album name. The according suffix is used! */
	public static String generateTableName(String albumName) {
		return (albumName.toLowerCase()).replace(" ", "_");
	}
	
	/** Creates a database safe picture table name for a given album name The according suffix is used! */
	public static String generatePictureTableName(String albumName) {
		return (albumName.toLowerCase()).replace(" ", "_") + DatabaseConstants.PICTURE_TABLE_SUFFIX;
	}
	
	/** Creates a database type info table name for a given album name The according suffix is used! */
	public static String generateTypeInfoTableName(String albumName) {
		return (albumName.toLowerCase()).replace(" ", "_") + DatabaseConstants.TYPE_INFO_SUFFIX;
	}
	
	/** Creates a database safe index table name for a given album name The according suffix is used! */
	public static String generateIndexTableName(String albumName) {
		return (albumName.toLowerCase()).replace(" ", "_") + DatabaseConstants.INDEX_NAME_SUFFIX;
	}
	
	/** Creates a database safe temporary table name for a given album name The according suffix is used! */
	public static String generateTempTableName(String albumName) {
		return (albumName.toLowerCase()).replace(" ", "_") + DatabaseConstants.TEMP_TABLE_SUFFIX;
	}
	
	/**
	 * Transforms a value of an album Item and escapes single quotes.
	 * @param value The value which must not be enclosed in single quotes.
	 * @return A string with the specified value which has the quotes escaped for further processing. Typically used in a 
	 * raw SELECT statement.
	 */
	public static String sanitizeSingleQuotesInAlbumItemValues(String value) {
		int lastIndex = 0;
		int singleQuoteIndex = value.indexOf('\'',0);
		StringBuilder sb = new StringBuilder();
		while (singleQuoteIndex != -1) {
			sb.append(value.substring(lastIndex, singleQuoteIndex));
			sb.append("''");
			lastIndex = singleQuoteIndex+1;
			singleQuoteIndex =value.indexOf('\'',singleQuoteIndex+1);
		}
		if (lastIndex>-1) {
			sb.append(value.substring(lastIndex));
		}
		return sb.toString();
	}

	/**
	 * Removes one layer of any single quotes enclosing the name. Quotes are unnecessary if setString is used to
	 * add the name to a query.    
	 * @param regularName The usual name with possibly enclosing single quotes.
	 * @return The proper string with one layer of single quotes removed if present.
	 */
	public static String removeQuotesEnclosingName(String regularName) {
		if (regularName.startsWith("'") && regularName.endsWith("'")) {
			return regularName.substring(1, regularName.length()-1);
		}
		return regularName ;
	}

	/**
	 * Encloses a given album or field name with single quotes such that db accepts it, except for columnNames in a select query.
	 * Use {@link DatabaseStringUtilities#transformColumnNameToSelectQueryName(String)} instead for columnName in select queries.
	 * Use quote marks to enclose columnnames or album names with spaces for example.    
	 * @param regularName The usual name without special markup for low level db interaction.
	 * @return The proper string for the database interaction.
	 */
	public static String encloseNameWithQuotes(String regularName) {
		if (regularName.startsWith("'") && regularName.endsWith("'")) {
			return regularName;
		}
		return "'" + regularName + "'";
	}

	/**
	 * Transforms a given fieldName into a columnName format, the db accepts. Use squared brackets to enclose columnnames with spaces for example.    
	 * @param fieldName The name of a field to be transformed.
	 * @return The proper string for low level query interaction with the database.
	 */
	public static String transformColumnNameToSelectQueryName(String fieldName) {
		if (fieldName.startsWith("[") && fieldName.endsWith("]")) {
			return fieldName;
		}
		return "[" + fieldName + "]";
	}

}
