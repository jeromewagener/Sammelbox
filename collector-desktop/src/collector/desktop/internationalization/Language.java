package collector.desktop.internationalization;


public enum Language {
	Unknown,
	English,
	Deutsch;
	
	public static String getDictionaryBundle(Language language) {
		switch (language) {
		case English:
			return "internationalization/dict_en";

		case Deutsch:
			return "internationalization/dict_de";
		
		default:
			return "internationalization/dict_en";
		}
	}
	
	public static String[] allLanguages() {
		String[] allLanguages = new String[values().length - 1];
		
		int i=0;
		for (Language language : values()) {
			if (language != Unknown) {
				allLanguages[i++] = language.toString();
			}
		}
		
		return allLanguages;
	}
}
