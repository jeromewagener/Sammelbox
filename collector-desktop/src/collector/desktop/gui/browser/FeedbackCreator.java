package collector.desktop.gui.browser;

import java.util.LinkedList;

import org.eclipse.swt.browser.Browser;

import collector.desktop.Collector;
import collector.desktop.album.AlbumItem;
import collector.desktop.database.DatabaseWrapper;
import collector.desktop.internationalization.DictKeys;
import collector.desktop.internationalization.Translator;

public class FeedbackCreator {
	/** A list of alterations that have already been performed via the alter album functionality */
	private static LinkedList<String> alterations = new LinkedList<String>();
	
	static void generatAlbumItemUpdatedPage(long albumItemId) {
		AlbumItem updatedAlbumItem = DatabaseWrapper.fetchAlbumItem(Collector.getSelectedAlbum(), albumItemId);

		if (updatedAlbumItem != null) {
			Collector.getAlbumItemSWTBrowser().setText(
					generateItemAddedOrUpdatedFeedbackConstruct(
							Translator.get(DictKeys.BROWSER_ITEM_UPDATED),
							ItemCreator.getAlbumItemTableRowHtml(updatedAlbumItem)));
		}
	}

	static void generateAlbumItemAddedPage(long albumItemId) {
		AlbumItem addedAlbumItem = DatabaseWrapper.fetchAlbumItem(Collector.getSelectedAlbum(), albumItemId);

		if (addedAlbumItem != null) {
			Collector.getAlbumItemSWTBrowser().setText(
					generateItemAddedOrUpdatedFeedbackConstruct(
							Translator.get(DictKeys.BROWSER_ITEM_ADDED),
							ItemCreator.getAlbumItemTableRowHtml(addedAlbumItem)));
		}
	}
	
	private static String generateItemAddedOrUpdatedFeedbackConstruct(String title, String addedOrUpdatedItemAsHtmlTableRowItem) {
		return 	"<!DOCTYPE HTML>" +
				"  <html>" +
				"    <head>" +
				"      <meta " + BrowserConstants.IE_META_PARAMS + ">" +
				"      <link rel=stylesheet href=\"" + BrowserConstants.STYLE_CSS + "\"></link>" +
				"      <script src=\"" + BrowserConstants.EFFECTS_JS + "\"></script>" +
				"    </head>" +
				"    <body bgcolor=white>" +
				"      <font face=\"" + Utilities.getDefaultSystemFont() + "\">" +
				"        <h1>" + title + "</h1>" +
				"          <table id=\"albumItems\" border=0>" + addedOrUpdatedItemAsHtmlTableRowItem + "</table>" +
				"      </font>" +
				"      <br>" +
				"      <form>" +
				"        <input type=\"button\" " +
				"               onclick=parent.location.href=\"show:///showDetailsViewOfAlbum\" " +
				"               value=\"" + Translator.get(DictKeys.BROWSER_BACK_TO_ALBUM) + "\">" +
				"      </form>" +
				"    </body>" +
				"  </html>";
	}
	
	static void showCreateNewAlbumPage(Browser browser, AlbumItem albumItem) {
		browser.setText(generateAlbumAddedOrUpdatedFeedbackConstruct(
				albumItem,
				Translator.toBeTranslated("Creating a new Album"),
				Translator.toBeTranslated("Your Album will be able to store items in the following format:")));
	}
	
	static void showCreateAlterAlbumPage(Browser browser, AlbumItem albumItem) {		
		browser.setText(generateAlbumAddedOrUpdatedFeedbackConstruct(
				albumItem,
				Translator.toBeTranslated("Modifying ") + Collector.getSelectedAlbum(),
				Translator.toBeTranslated("<u>Attention:</u> All changes will have <font color=red>imediate</font> effects!<br>Your Album is currently able to store items in the following format:")));
	}
	
	private static String generateAlbumAddedOrUpdatedFeedbackConstruct(AlbumItem albumItem, String title, String subTitle) {
		return "<!DOCTYPE HTML>" +
			   "  <html>" +
			   "    <head>" +
			   "      <meta" + BrowserConstants.IE_META_PARAMS + ">" + 
			   "      <link rel=stylesheet href=\"" + BrowserConstants.STYLE_CSS + "\"></link>" +
			   "      <script src=\"" + BrowserConstants.EFFECTS_JS + "\"></script>" +
			   "    </head>" +
			   "    <body>" +
		       "      <h1>" + title + "</h1>" +
		       "      <h4>" + subTitle + "</h4>" +
		       "      <hr noshade size=\"1\">" +
		       "      <table>" + ItemCreator.getAlbumItemTableRowHtml(albumItem, false) + "</table>" +
		       "      <hr noshade size=\"1\">" +
		       "      <ul>" + getAlterationsAsListItems() + "</ul>" +
		       "    </body>" +
		       "  </html>";
	}
	
	static String getAlterationsAsListItems() {
		StringBuilder listItems = new StringBuilder();
		
		for (String alteration : alterations) {
			listItems.append("<li>" + alteration + "</li>");
		}
		
		return listItems.toString();
	}
	
	static void clearAlterationList() {
		alterations.clear();
	}
	
	static void addModificationToAlterationList(String modification) {
		alterations.addFirst(modification);
	}
}
