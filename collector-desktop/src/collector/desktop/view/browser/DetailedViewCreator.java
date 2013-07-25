package collector.desktop.view.browser;

import org.eclipse.swt.browser.Browser;

import collector.desktop.controller.GuiController;
import collector.desktop.model.album.AlbumItem;
import collector.desktop.model.album.AlbumItemStore;
import collector.desktop.view.ApplicationUI;
import collector.desktop.view.internationalization.DictKeys;
import collector.desktop.view.internationalization.Translator;
import collector.desktop.view.various.ComponentFactory;
import collector.desktop.view.various.Constants;

public class DetailedViewCreator {	
	static void showDetailedAlbum(Browser browser) {
		// Exit if no album is selected
		if (!ApplicationUI.hasSelectedAlbum()) {
			ComponentFactory.showErrorDialog(
					ApplicationUI.getShell(), 
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
			albumItemTableRowHtml.append(
	          "<tr><td><div>" + 
	            "<h3>" + 
	              Translator.get(DictKeys.BROWSER_NO_ITEMS_FOUND, GuiController.getGuiState().getSelectedAlbum()) + 
	            "</h3>" + 
	             Translator.get(DictKeys.BROWSER_NO_ITEMS_FOUND_EXPLANATION) + 
	          "</div></td></tr>"); 
		}

		// Create final page html
		String finalPageAsHtml = 
				"<!DOCTYPE HTML>" +
				"  <html>" +
				"    <head>" +
				"      <title>sammelbox.org</title>" +
				"      <meta " + Constants.META_PARAMS + ">" + 
				"      <link rel=stylesheet href=\"" + Constants.STYLE_CSS + "\" />" +
				"      <script src=\"" + Constants.EFFECTS_JS + "\"></script>" +
				"    </head>" +
				"    <body style=\"background-color:#ffffff;font-family:" +  Utilities.getDefaultSystemFont() + "\">" +
				"      <table id=\"albumItems\" border=0>" + albumItemTableRowHtml + "</table>" +
				"    </body>" +
				"  </html>";
		
		browser.setText(finalPageAsHtml);
		Utilities.setLastPageAsHtml(finalPageAsHtml);		
	}
}
