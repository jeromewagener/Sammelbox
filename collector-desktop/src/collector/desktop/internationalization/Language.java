package collector.desktop.internationalization;

public enum Language {
	EN,
	DE;

	public static String getDictionaryBundle(Language language) {
		switch (language) {
		case EN:
			return "i18n/dict_en";

		case DE:
			return "i18n/dict_de";
		
		default:
			return "i18n/dict_en";
		}
	}
}
