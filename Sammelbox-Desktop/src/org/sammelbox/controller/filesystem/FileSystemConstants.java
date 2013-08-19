package org.sammelbox.controller.filesystem;

import java.io.File;

public class FileSystemConstants {
	public static final String DATABASE_NAME					= "collector.db";
	public static final String DATABASE_TO_RESTORE_NAME			= "collector.restore.db";

	public static final String TEMP_DIR 						= System.getProperty("java.io.tmpdir");
	public static final String USER_HOME 						= System.getProperty("user.home");
	public static final String COLLECTOR_HOME 					= System.getProperty("user.home") + File.separatorChar + ".collector";
	
	public static final String COLLECTOR_HOME_APPDATA 			= COLLECTOR_HOME + File.separatorChar + "app-data";
	public static final String COLLECTOR_HOME_THUMBNAILS_FOLDER = COLLECTOR_HOME + File.separatorChar + "thumbnails";
	public static final String COLLECTOR_HOME_BACKUPS			= COLLECTOR_HOME + File.separatorChar + "backups"; 
	public static final String COLLECTOR_HOME_ALBUM_PICTURES 	= COLLECTOR_HOME + File.separatorChar + "album-pictures";
	public static final String DATABASE 						= COLLECTOR_HOME + File.separatorChar + DATABASE_NAME;
	public static final String DATABASE_TO_RESTORE				= COLLECTOR_HOME + File.separatorChar + DATABASE_TO_RESTORE_NAME;
	
	public static final String PLACEHOLDERIMAGE 				= COLLECTOR_HOME_APPDATA + File.separatorChar + "placeholder.png";
	public static final String PLACEHOLDERIMAGE2 				= COLLECTOR_HOME_APPDATA + File.separatorChar + "placeholder2.png";
	public static final String PLACEHOLDERIMAGE3 				= COLLECTOR_HOME_APPDATA + File.separatorChar + "placeholder3.png";
	public static final String ZERO_STARS_IMAGE 				= COLLECTOR_HOME_APPDATA + File.separatorChar + "zerostars.png";
	public static final String ONE_STAR_IMAGE 					= COLLECTOR_HOME_APPDATA + File.separatorChar + "onestar.png";
	public static final String TWO_STARS_IMAGE 					= COLLECTOR_HOME_APPDATA + File.separatorChar + "twostars.png";
	public static final String THREE_STARS_IMAGE 				= COLLECTOR_HOME_APPDATA + File.separatorChar + "threestars.png";
	public static final String FOUR_STARS_IMAGE 				= COLLECTOR_HOME_APPDATA + File.separatorChar + "fourstars.png";
	public static final String FIVE_STARS_IMAGE 				= COLLECTOR_HOME_APPDATA + File.separatorChar + "fivestars.png";
	public static final String LOGO 							= COLLECTOR_HOME_APPDATA + File.separatorChar + "logo.png";
	public static final String LOGO_SMALL 						= COLLECTOR_HOME_APPDATA + File.separatorChar + "logo-small.png";
	public static final String VIEW_FILE						= COLLECTOR_HOME_APPDATA + File.separatorChar + "views.xml";
	public static final String ALBUM_FILE            			= COLLECTOR_HOME_APPDATA + File.separatorChar + "albums.xml";
	public static final String SETTINGS_FILE					= COLLECTOR_HOME_APPDATA + File.separatorChar + "settings.xml";
	public static final String WELCOME_PAGE_FILE				= COLLECTOR_HOME_APPDATA + File.separatorChar + "welcome.xml";
	public static final String LOCK_FILE						= COLLECTOR_HOME_APPDATA + File.separatorChar + ".lock";
	public static final String EFFECTS_JS						= COLLECTOR_HOME_APPDATA + File.separatorChar + "effects.js";
	public static final String STYLE_CSS						= COLLECTOR_HOME_APPDATA + File.separatorChar + "style.css";
}
