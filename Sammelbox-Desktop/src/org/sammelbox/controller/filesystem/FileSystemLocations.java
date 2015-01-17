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

package org.sammelbox.controller.filesystem;

import java.io.File;

public final class FileSystemLocations {
	/** PLEASE NOTE: this folder does not necessarily contain the collection data as the user can specify a redirect. In this case, only 
	 * the redirect file will be kept within this default folder. However, if the user doesn't use a redirect, all collection data will 
	 * be stored here. Please use {@link #getActiveHomeDir()} to identify the real home directory! */
	public static final String DEFAULT_SAMMELBOX_HOME = System.getProperty("user.home") + File.separatorChar + "Sammelbox";
	
	/** This directory is used to run unit tests without destroying any real collections on your development machine. */
	public static final String DEFAULT_SAMMELBOX_TEST_HOME = System.getProperty("user.home") + File.separatorChar + ".sammelbox.tests";

	/** This file only exists if the user specified a redirect of his home folder. In this case, the corresponding file only contains
	 * the path of the real home directory */
	public static final String HOME_REDIRECTION_FILE = DEFAULT_SAMMELBOX_HOME  + File.separatorChar + "home-redirect.txt";
	
	public static final String DATABASE_NAME = "sammelbox.db";
	public static final String DATABASE_TO_RESTORE_NAME = "sammelbox.restore.db";
	public static final String THUMBNAILS_DIR_NAME = "thumbnails";
	public static final String BACKUPS_DIR_NAME = "backups";
	public static final String APP_DATA_DIR_NAME = "app-data";
	public static final String CSS_JS_DIR_NAME = "css-js";
	public static final String ALBUM_PICTURES_DIR_NAME = "album-pictures";

	public static final String TEMP_DIR = System.getProperty("java.io.tmpdir") + File.separatorChar;
	public static final String USER_HOME = System.getProperty("user.home");
	public static final String WORKING_DIR = System.getProperty("user.dir");

	private FileSystemLocations() {}
	
	/** The path to the currently active home directory */
	private static String activeHomeDir = null;
	
	/** Sets the path to the currently active home directory 
	 * This method must be called during initialization of the program. */
	public static void setActiveHomeDir(String activeHomeDir) {
		FileSystemLocations.activeHomeDir = activeHomeDir;
	}
	
	public static String getActiveHomeDir()           { return activeHomeDir; }
	public static String getAppDataDir()              { return activeHomeDir + File.separatorChar + APP_DATA_DIR_NAME; }
	public static String getAppDataGraphicsDir()      { return activeHomeDir + File.separatorChar + APP_DATA_DIR_NAME + File.separatorChar + "graphics"; }
	public static String getThumbnailsDir()           { return activeHomeDir + File.separatorChar + THUMBNAILS_DIR_NAME; }
	public static String getBackupDir()               { return activeHomeDir + File.separatorChar + BACKUPS_DIR_NAME; }
	public static String getAlbumPicturesDir()        { return activeHomeDir + File.separatorChar + ALBUM_PICTURES_DIR_NAME; }
	public static String getDatabaseFile()            { return activeHomeDir + File.separatorChar + DATABASE_NAME; }
	public static String getDatabaseRestoreFile()     { return activeHomeDir + File.separatorChar + DATABASE_TO_RESTORE_NAME; }
	public static String getCssJsDir()                { return activeHomeDir + File.separatorChar + APP_DATA_DIR_NAME + File.separatorChar + CSS_JS_DIR_NAME; }
	
	public static String getPlaceholderPNG()          { return getAppDataGraphicsDir() + File.separatorChar + "placeholder.png"; }
	public static String getPlaceholder2PNG()         { return getAppDataGraphicsDir() + File.separatorChar + "placeholder2.png"; }
	public static String getPlaceholder3PNG()         { return getAppDataGraphicsDir() + File.separatorChar + "placeholder3.png"; }
	public static String getZeroStarsPNG()            { return getAppDataGraphicsDir() + File.separatorChar + "zerostars.png"; }
	public static String getOneStarPNG()              { return getAppDataGraphicsDir() + File.separatorChar + "onestar.png"; }
	public static String getTwoStarsPNG()             { return getAppDataGraphicsDir() + File.separatorChar + "twostars.png"; }
	public static String getThreeStarsPNG()           { return getAppDataGraphicsDir() + File.separatorChar + "threestars.png"; }
	public static String getFourStarsPNG()            { return getAppDataGraphicsDir() + File.separatorChar + "fourstars.png"; }
	public static String getFiveStarsPNG()            { return getAppDataGraphicsDir() + File.separatorChar + "fivestars.png"; }
	public static String getMinimizeArrowPNG()        { return getAppDataGraphicsDir() + File.separatorChar + "minimize-arrow.png"; }
	public static String getMaximizeArrowPNG()        { return getAppDataGraphicsDir() + File.separatorChar + "maximize-arrow.png"; }
	public static String getResizeSpreadsheetPNG()    { return getAppDataGraphicsDir() + File.separatorChar + "resize-spreadsheet.png"; }
	public static String getLogoPNG()                 { return getAppDataGraphicsDir() + File.separatorChar + "logo.png"; }
	public static String getLogoSmallPNG()            { return getAppDataGraphicsDir() + File.separatorChar + "logo-small.png"; }
	
	public static String getWelcomeXML()              { return getAppDataDir() + File.separatorChar + "welcome.xml"; }
	public static String getSavedSearchesXML()        { return getAppDataDir() + File.separatorChar + "saved-searches.xml"; }
	public static String getAlbumsXML()               { return getAppDataDir() + File.separatorChar + "albums.xml"; }
	public static String getSettingsXML()             { return getAppDataDir() + File.separatorChar + "settings.xml"; }
	public static String getLockFile()                { return getAppDataDir() + File.separatorChar + ".lock"; }
	
	public static String getEffectsJS()               { return getAppDataDir() + File.separatorChar + CSS_JS_DIR_NAME + File.separatorChar + "effects.js"; }
	public static String getSpreadsheetScripts()      { return getAppDataDir() + File.separatorChar + CSS_JS_DIR_NAME + File.separatorChar + "spreadsheet-scripts.js"; }
	public static String getStyleCSS()                { return getAppDataDir() + File.separatorChar + CSS_JS_DIR_NAME + File.separatorChar + "style.css"; }
	public static String getStyleCSSSpreadsheet()     { return getAppDataDir() + File.separatorChar + CSS_JS_DIR_NAME + File.separatorChar + "style-spreadsheet.css"; }
}
