package collector.desktop.database;

public class DatabaseStringUtilities {

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
	 * @param regularName The usual name with possibly enclosing singhle quotes.
	 * @return The proper string with one layer of single quotes removed if present.
	 */
	static String removeEnclosingNameWithQuotes(String regularName) {
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
