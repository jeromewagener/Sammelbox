package collector.desktop.gui;

import java.io.File;

import collector.desktop.Collector;
import collector.desktop.filesystem.FileSystemAccessWrapper;
import collector.desktop.internationalization.DictKeys;
import collector.desktop.internationalization.Translator;

public class GuiConstants {
	// ------------------ Browser Constants ------------------
	public static final String STYLE_CSS = "file://"+ FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA + File.separatorChar + "style.css";
	public static final String EFFECTS_JS = "file://" + FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA + File.separatorChar + "effects.js";
	public static final String META_PARAMS = "http-equiv=\"X-UA-Compatible\" content=\"IE=9\" charset=\"utf-8\"";
	public static final String NO_ITEMS_FOUND = 
			"<div>" +
			"  <h3>" + Translator.get(DictKeys.BROWSER_NO_ITEMS_FOUND, Collector.getSelectedAlbum()) + "</h3>" + 
			   Translator.get(DictKeys.BROWSER_NO_ITEMS_FOUND_EXPLANATION) + 
			"</div>";
	
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
