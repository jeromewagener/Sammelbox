package org.sammelbox.android.model;

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
			return StarRating.ZERO_STARS;
		// Default value is never reached, it would result in a null pointer exception.
		// However method must return something.
		default:
			return "";
		}
	}
}
