package collector.desktop.internationalization;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Translator {
	private ResourceBundle resourceBundle;
	
	public Translator(Language language) {
		changeLanguage(language);
	}
	
	public void changeLanguage(Language language) {		
		try {
			this.resourceBundle = ResourceBundle.getBundle(Language.getDictionaryBundle(language));
		} catch (MissingResourceException mre) {
			System.err.println("properties not found"); // TODO log me
		}
	}
	
	public String get(String key) {
		return resourceBundle.getString(key);
	}
	
	public String get(String key, Object... parameters) {
		return MessageFormat.format(resourceBundle.getString(key), parameters);
	}
}
