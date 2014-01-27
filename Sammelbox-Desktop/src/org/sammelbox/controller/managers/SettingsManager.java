/** -----------------------------------------------------------------
 *    Sammelbox: Collection Manager - A free and open-source collection manager for Windows & Linux
 *    Copyright (C) 2011 Jerome Wagener & Paul Bicheler
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

package org.sammelbox.controller.managers;

import java.util.Locale;

import org.sammelbox.controller.filesystem.xml.XmlStorageWrapper;
import org.sammelbox.controller.i18n.Language;
import org.sammelbox.model.settings.ApplicationSettings;

public final class SettingsManager {	
	private static ApplicationSettings applicationSettings = new ApplicationSettings();
	
	private SettingsManager() {
	}
	
	public static Language getUserDefinedLanguage() {
		return applicationSettings.getUserDefinedLanguage();
	}
	
	public static void initializeFromSettingsFile() {
		applicationSettings = XmlStorageWrapper.retrieveSettings();
	}
	
	public static void storeToSettingsFile() {
		XmlStorageWrapper.storeSettings(applicationSettings);
	}
	
	public static Locale getUserDefinedLocale() {
		switch (applicationSettings.getUserDefinedLanguage()) {
		case DEUTSCH:
			return Locale.GERMAN;

		default:
			return Locale.ENGLISH;
		}
	}
	
	public static void setApplicationSettings(ApplicationSettings applicationSettings) {
		SettingsManager.applicationSettings = applicationSettings;
		storeToSettingsFile();
	}
	
	public static ApplicationSettings getSettings() {
		return applicationSettings;
	}

	public static boolean showDebugMenu() {
		return applicationSettings.showDebugMenu();
	}
}
