package collector.desktop.controller.settings;

import java.util.Locale;

import collector.desktop.controller.filesystem.FileSystemAccessWrapper;
import collector.desktop.controller.i18n.Language;

public class ApplicationSettingsManager {
	private static ApplicationSettings applicationSettings = new ApplicationSettings();
	
	public static Language getUserDefinedLanguage() {
		return applicationSettings.getUserDefinedLanguage();
	}
	
	public static void initializeFromSettingsFile() {
		applicationSettings = FileSystemAccessWrapper.loadSettings();
	}
	
	public static void storeToSettingsFile() {
		FileSystemAccessWrapper.storeSettings(applicationSettings);
	}
	
	public static Locale getUserDefinedLocale() {
		switch (applicationSettings.getUserDefinedLanguage()) {
		case Deutsch:
			return Locale.GERMAN;

		default:
			return Locale.ENGLISH;
		}
	}
	
	public static void setUserDefinedLanguage(Language language) {
		applicationSettings.setUserDefinedLanguage(language);
	}
	
	public static class ApplicationSettings {
		private Language userDefinedLanguage = Language.Unknown;

		public ApplicationSettings() {}
		
		public Language getUserDefinedLanguage() {
			return userDefinedLanguage;
		}

		public void setUserDefinedLanguage(Language userDefinedLanguage) {
			this.userDefinedLanguage = userDefinedLanguage;
		}
	}
}
