package collector.desktop.view;

import java.io.File;

import collector.desktop.controller.filesystem.FileSystemAccessWrapper;

public class UIConstants {
	// ------------------ GUI Constants ------------------
	public static final int SCROLL_SPEED_MULTIPLICATOR = 3;
	
	public static final int RIGHT_PANEL_LARGE_WIDTH = 320;
	public static final int RIGHT_PANEL_MEDIUM_WIDTH = 225;
	public static final int RIGHT_PANEL_SMALL_WIDTH = 150;
	public static final int RIGHT_PANEL_NO_WIDTH = 0;

	/** The minimum width of the shell in pixels. The shell can never have a smaller width than this. */
	public static final int MIN_SHELL_WIDTH = 1110;
	/** The minimum height of the shell in pixels. The shell can never have a smaller height than this. */
	public static final int MIN_SHELL_HEIGHT = 700;
	
	// ------------------ Browser Constants ------------------
	public static final String STYLE_CSS = "file://"+ FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA + File.separatorChar + "style.css";
	public static final String EFFECTS_JS = "file://" + FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA + File.separatorChar + "effects.js";
	public static final String META_PARAMS = "http-equiv=\"X-UA-Compatible\" content=\"IE=9\" charset=\"utf-8\"";
	
	// ------------------ Browser Listener Constants ------------------
	public static final String SHOW_UPDATE_COMPOSITE = "show:///updateComposite=";
	public static final String SHOW_DELETE_COMPOSITE = "show:///deleteComposite=";
	public static final String SHOW_BIG_PICTURE = "show:///bigPicture=";
	public static final String SHOW_LAST_PAGE = "show:///lastPage";
	public static final String SHOW_DETAILS = "show:///details=";
	public static final String SHOW_DETAILS_COMPOSITE = "show:///detailsComposite=";
	public static final String ADD_ADDITIONAL_ALBUM_ITEMS = "show:///addAdditionalAlbumItems";
	public static final String SHOW_DETAILS_VIEW_OF_ALBUM = "show:///showDetailsViewOfAlbum";
}
