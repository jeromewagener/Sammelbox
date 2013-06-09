package collector.desktop.gui.browser;

import java.io.File;

import collector.desktop.Collector;
import collector.desktop.filesystem.FileSystemAccessWrapper;
import collector.desktop.internationalization.DictKeys;
import collector.desktop.internationalization.Translator;

public class BrowserConstants {
	public static final String STYLE_CSS = 
			"file://"+ FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA + File.separatorChar + "style.css";
	public static final String EFFECTS_JS = 
			"file://" + FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA + File.separatorChar + "effects.js";
	public static final String IE_META_PARAMS = 
			"http-equiv=\"X-UA-Compatible\" content=\"IE=9\"";
	public static final String NO_ITEMS_FOUND = 
			"<div>" +
			"  <h3>" + Translator.get(DictKeys.BROWSER_NO_ITEMS_FOUND, Collector.getSelectedAlbum()) + "</h3>" + 
			   Translator.get(DictKeys.BROWSER_NO_ITEMS_FOUND_EXPLANATION) + 
			"</div>";
}
