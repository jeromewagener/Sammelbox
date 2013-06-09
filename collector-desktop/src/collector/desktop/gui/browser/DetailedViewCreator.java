package collector.desktop.gui.browser;

import org.eclipse.swt.browser.Browser;

import collector.desktop.Collector;
import collector.desktop.album.AlbumItem;
import collector.desktop.database.AlbumItemStore;
import collector.desktop.gui.various.ComponentFactory;
import collector.desktop.internationalization.DictKeys;
import collector.desktop.internationalization.Translator;

public class DetailedViewCreator {	
	static void showDetailedAlbum(Browser browser) {
		// Exit if no album is selected
		if (!Collector.hasSelectedAlbum()) {
			ComponentFactory.showErrorDialog(
					Collector.getShell(), 
					Translator.get(DictKeys.DIALOG_TITLE_NO_ALBUM_SELECTED), 
					Translator.get(DictKeys.DIALOG_CONTENT_NO_ALBUM_SELECTED));
			return;
		}
		
		// Builders for efficient html creation
		StringBuilder albumItemTableRowHtml = new StringBuilder();
		StringBuilder htmlDataColumnContent = new StringBuilder();
		StringBuilder htmlPictureColumnContent = new StringBuilder();

		// Add all available album items to a html table
		for (AlbumItem albumItem : AlbumItemStore.getAlbumItems(AlbumItemStore.getStopIndex())) {
			htmlDataColumnContent.delete(0, htmlDataColumnContent.length());
			htmlPictureColumnContent.delete(0, htmlPictureColumnContent.length());

			ItemCreator.addAlbumItemTableRow(albumItem, htmlDataColumnContent, htmlPictureColumnContent, albumItemTableRowHtml);
		}

		// If no album items have been found
		if (htmlDataColumnContent.length() == 0 && htmlPictureColumnContent.length() == 0) {
			albumItemTableRowHtml.delete(0, albumItemTableRowHtml.length());
			albumItemTableRowHtml.append("<tr><td>" + BrowserConstants.NO_ITEMS_FOUND + "</tr></td>");
		}

		// Create final page html
		String finalPageAsHtml = 
				"<!DOCTYPE HTML>" +
				"  <html>" +
				"    <head>" +
				"      <meta " + BrowserConstants.META_PARAMS + ">" + 
				"      <link rel=stylesheet href=\"" + BrowserConstants.STYLE_CSS + "\" />" +
				"      <script src=\"" + BrowserConstants.EFFECTS_JS + "\"></script>" +
				"    </head>" +
				"    <body bgcolor=white>" +
				"      <font face=\"" + Utilities.getDefaultSystemFont() + "\">" + 
				"        <table id=\"albumItems\" border=0>" + albumItemTableRowHtml + "</table>" +
				"	   </font>" +
				"    </body>" +
				"  </html>";
		
		browser.setText(finalPageAsHtml);
		Utilities.setLastPageAsHtml(finalPageAsHtml);		
	}
}
