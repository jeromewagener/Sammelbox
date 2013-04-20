package collector.desktop.database;

import java.sql.Date;
import java.sql.Time;

public enum FieldType {
	ID,
	Text,
	Number, 
	Date, 
	Time,
	Picture,
	UUID,
	StarRating,
	URL, 
	Integer,
	Option; 

	/** 
	 * The Picture type and ID are not included since they should not be directly be edited by the user. 
	 * Picture is also not included in the returned string since this method is only used to fill ComboBoxes 
	 * listing possible FieldTypes for the create new Album dialog. GUI specific method.
	 */
	public static String[] toUserTypeStringArray() {
		return new String[] { 	
				FieldType.Text.toString(), 
				FieldType.Integer.toString(),
				FieldType.Number.toString(),
				FieldType.StarRating.toString(),
				FieldType.Option.toString(),
				FieldType.Date.toString(),
				//FieldType.Time.toString(),
				FieldType.URL.toString()
				//FieldType.Time.toString(),// In the current iteration the time is not needed as explicit field type. 
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
		case Text:
			res = "TEXT";
			break;
		case Number:
			res = "REAL";
			break;
		case Date:
			res = "DATE";
			break;
		case Time:
			res = "TIME";
			break;
		case Picture:
			res = "TEXT";
			break;
		case UUID:
			res= "TEXT";
			break;
		case StarRating:
			res = "TEXT";
			break;
		case URL:
			res = "TEXT";
			break;
		case Integer:
			res = "INTEGER";
			break;
		// Default value is never reached, it would result in a null pointer exception.
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
	public Object getDefaultValue(){
		switch (this){
		case Text: 
			return  "";
		case Number: 
			return 0.0d;
		case Integer: 
			return 0;
		case Date: 
			return new Date(System.currentTimeMillis());
		case Time: 
			return new Time(System.currentTimeMillis());
		case Picture:
			return "";
		case URL:
			return "";
		case Option:
			return OptionType.Option;
		case StarRating :
			return collector.desktop.database.StarRating.ZeroStars.toString();
		// Default value is never reached, it would result in a null pointer exception.
		// However method must return something.
		default:
			return "";
		}
	}
}