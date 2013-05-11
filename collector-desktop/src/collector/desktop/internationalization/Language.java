package collector.desktop.internationalization;

public enum Language {
	EN,
	DE;

	public static String getDictionaryBundle(Language language) {
		switch (language) {
		case EN:
			return "internationalization/dict_en";

		case DE:
			return "internationalization/dict_de";
		
		default:
			return "internationalization/dict_en";
		}
	}
}
