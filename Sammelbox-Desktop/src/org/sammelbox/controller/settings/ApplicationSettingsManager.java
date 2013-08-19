/** -----------------------------------------------------------------
 *    Sammelbox: Collection Manager - A free and open-source collection manager for Windows & Linux
 *    Copyright (C) 2011 Jérôme Wagener & Paul Bicheler
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ** ----------------------------------------------------------------- */

package org.sammelbox.controller.settings;

import java.util.Locale;

import org.sammelbox.controller.filesystem.XMLStorageWrapper;
import org.sammelbox.controller.i18n.Language;

public class ApplicationSettingsManager {
	private static ApplicationSettings applicationSettings = new ApplicationSettings();
	
	public static Language getUserDefinedLanguage() {
		return applicationSettings.getUserDefinedLanguage();
	}
	
	public static void initializeFromSettingsFile() {
		applicationSettings = XMLStorageWrapper.retrieveSettings();
	}
	
	public static void storeToSettingsFile() {
		XMLStorageWrapper.storeSettings(applicationSettings);
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
