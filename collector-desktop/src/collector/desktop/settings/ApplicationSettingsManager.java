package collector.desktop.settings;

import java.util.Locale;

import collector.desktop.filesystem.FileSystemAccessWrapper;
import collector.desktop.internationalization.Language;

public class ApplicationSettingsManager {
	private static ApplicationSettings applicationSettings = new ApplicationSettings();
	
	public static Language getUserDefinedLanguage() {
		return applicationSettings.getUserDefinedLanguage();
	}
	
	public static void loadFromSettingsFile() {
		applicationSettings = FileSystemAccessWrapper.loadSettings();
	}
	
	public static void storeToSettingsFile() {
		FileSystemAccessWrapper.storeSettings(applicationSettings);
	}
	
	public static Locale getUserDefinedLocale() {
		// TODO allow user to define locale
		switch (applicationSettings.getUserDefinedLanguage()) {
		case DE:
			return Locale.GERMAN;

		default:
			return Locale.ENGLISH;
		}
	}
	
	public static class ApplicationSettings {
		private Language userDefinedLanguage = Language.UNKNOWN;

		public ApplicationSettings() {}
		
		public Language getUserDefinedLanguage() {
			return userDefinedLanguage;
		}

		public void setUserDefinedLanguage(Language userDefinedLanguage) {
			this.userDefinedLanguage = userDefinedLanguage;
		}
	}
}
