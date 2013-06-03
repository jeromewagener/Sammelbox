package collector.desktop.gui.browser;

import java.io.File;

import org.eclipse.swt.browser.Browser;

import collector.desktop.Collector;
import collector.desktop.database.AlbumItem;
import collector.desktop.database.AlbumItemStore;
import collector.desktop.filesystem.FileSystemAccessWrapper;
import collector.desktop.gui.various.ComponentFactory;
import collector.desktop.internationalization.DictKeys;
import collector.desktop.internationalization.Translator;

public class DetailedViewCreator {
	static void showDetailedAlbum(Browser browser) {
		if (!Collector.hasSelectedAlbum()) {
			ComponentFactory.showErrorDialog(
					Collector.getShell(), 
					Translator.get(DictKeys.DIALOG_TITLE_NO_ALBUM_SELECTED), 
					Translator.get(DictKeys.DIALOG_CONTENT_NO_ALBUM_SELECTED));
			return;
		}
		StringBuilder albumItemTableRowHtml = new StringBuilder();

		String javaScript = "<script src=\"file://" + FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA + File.separatorChar + "effects.js" + "\"></script>";
		String styleCSS = "<link rel=stylesheet href=\"file://"+ FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA + File.separatorChar + "style.css" + "\"></link>";

		StringBuilder htmlDataColumnContent = new StringBuilder();
		StringBuilder htmlPictureColumnContent = new StringBuilder();

		for (AlbumItem albumItem : AlbumItemStore.getAlbumItems(AlbumItemStore.getStopIndex())) {
			htmlDataColumnContent.delete(0, htmlDataColumnContent.length());
			htmlPictureColumnContent.delete(0, htmlPictureColumnContent.length());

			ItemCreator.addAlbumItemTableRow(albumItem, htmlDataColumnContent, htmlPictureColumnContent, albumItemTableRowHtml);
		}

		if (htmlDataColumnContent.length() == 0 && htmlPictureColumnContent.length() == 0) {
			albumItemTableRowHtml.delete(0, albumItemTableRowHtml.length());
			albumItemTableRowHtml.append("<tr><td><h3>" + Translator.get(DictKeys.BROWSER_NO_ITEMS_FOUND, Collector.getSelectedAlbum()) + "</h3>" + Translator.get(DictKeys.BROWSER_NO_ITEMS_FOUND_EXPLANATION) + "</td></tr>");
		}

		String finalPageAsHtml = "<!DOCTYPE HTML>" +
				"<html><head><meta http-equiv=\"X-UA-Compatible\" content=\"IE=9\" >" + styleCSS + " " + javaScript + "</head><body bgcolor=white><font face=\"" + Utilities.getDefaultSystemFont() + "\"><table id=\"albumItems\" border=0>" + albumItemTableRowHtml + "</table></font></body></html>";

		browser.setText(finalPageAsHtml);

		Utilities.setLastPageAsHtml(finalPageAsHtml);		
	}
}
