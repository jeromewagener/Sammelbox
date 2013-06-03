package collector.desktop.gui.browser;

import collector.desktop.Collector;
import collector.desktop.filesystem.FileSystemAccessWrapper;
import collector.desktop.gui.managers.AlbumManager;
import collector.desktop.gui.managers.WelcomePageManager;
import collector.desktop.internationalization.DictKeys;
import collector.desktop.internationalization.Translator;

public class WelcomePageCreator {
	static void loadWelcomePage() {
		String welcomePage = "";

		welcomePage += "<html width=100% height=80%>";
		welcomePage += "<body width=100% height=80%>";
		welcomePage += "	<font face=\"" + Utilities.getDefaultSystemFont() + "\">";
		welcomePage += "	<br>";
		welcomePage += "	<table width=100% height=80%>";
		welcomePage += "		<tr width=100% height=80%>";
		welcomePage += "			<td width=50% align=\"center\">";
		welcomePage += "				<h1>Sammelbox</h1>";
		welcomePage += "				<img height=\"350px\" src=\" " + FileSystemAccessWrapper.LOGO + " \">";
		welcomePage += "				<h2>Collection Manager</h2>";
		welcomePage += "			</td>";
		welcomePage += "			<td width=50%>";
		welcomePage += "				<div style=\"padding:10px; background-color:#" + Utilities.getBackgroundColorOfWidgetInHex() + "\">";

		welcomePage += generateAlbumInformation();
		welcomePage += generateFavorites();

		welcomePage += "				</div>";
		welcomePage += "			</td>";
		welcomePage += "		</tr>";
		welcomePage += "	</table>";
		welcomePage += "	</font>";
		welcomePage += "</body>";
		welcomePage += "</html>";

		Collector.getAlbumItemSWTBrowser().setText(welcomePage);
	}
	
	private static String generateAlbumInformation() {
		String welcomePage = "<h4>" + Translator.get(DictKeys.BROWSER_ALBUM_INFORMATION) + "</h4>";
		welcomePage += "<ul>";

		boolean empty = true;
		for (String album : AlbumManager.getInstance().getAlbums()) {
			welcomePage += "<li>Album <b>" + album + "</b> <br> <font size=-1><i>(" + 
					Translator.get(DictKeys.BROWSER_NUMBER_OF_ITEMS_AND_LAST_UPDATED, 
							WelcomePageManager.getInstance().getNumberOfItemsInAlbum(album),
							WelcomePageManager.getInstance().getLastModifiedDate(album))
							+ "</i></font></li>";

			empty = false;
		}

		if (empty) {
			welcomePage += "<li>" + Translator.get(DictKeys.BROWSER_NO_INFORMATION_AVAILABLE) + "</li>";
		}

		welcomePage += "</ul>";

		return welcomePage;
	}

	private static String generateFavorites() {
		String welcomePage = "<h4>" + Translator.get(DictKeys.BROWSER_FAVORITE_ALBUMS_AND_VIEWS) + "</h4>";
		welcomePage += "<ol>";

		int favCounter = 0;
		boolean empty = true;
		for (String albumOrViewName : WelcomePageManager.getInstance().getAlbumAndViewsSortedByClicks().keySet()) {
			welcomePage += "<li>" + albumOrViewName + "<font size=-1><i> " +
					Translator.get(DictKeys.BROWSER_CLICKS_FOR_ALBUM, WelcomePageManager.getInstance().getAlbumAndViewsSortedByClicks().get(albumOrViewName)) + "</i></font></li>";

			if (++favCounter == 5) {
				break;
			}

			empty = false;
		}

		if (empty) {
			welcomePage += "<li>" + Translator.get(DictKeys.BROWSER_NO_INFORMATION_AVAILABLE) + "</li>";
		}

		welcomePage += "</ol>";

		return welcomePage;		
	}
}
