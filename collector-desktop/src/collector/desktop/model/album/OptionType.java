package collector.desktop.model.album;

import collector.desktop.view.internationalization.DictKeys;

public enum OptionType {
	YES,
	NO,
	UNKNOWN;

	/**
	 * Retrieves the option type string value which is stored in the database, based on a given dictionary key
	 * @param dictKey the dictionary key for which the string database value should be retrieved
	 * @return the string database value for a given dictionary option type key
	 * */
	public static String getDatabaseOptionValue(String dictKey) {
		switch (dictKey) {
		case DictKeys.BROWSER_YES:
			return OptionType.YES.toString();

		case DictKeys.BROWSER_NO:
			return OptionType.NO.toString();

		case DictKeys.BROWSER_UNKNOWN:
		default:
			return OptionType.UNKNOWN.toString();
		}
	} 
}
