package collector.desktop.view.browser;

import java.util.LinkedList;

import org.eclipse.swt.browser.Browser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import collector.desktop.model.album.AlbumItem;
import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException;
import collector.desktop.model.database.exceptions.ExceptionHelper;
import collector.desktop.model.database.operations.DatabaseOperations;
import collector.desktop.view.ApplicationUI;
import collector.desktop.view.internationalization.DictKeys;
import collector.desktop.view.internationalization.Translator;
import collector.desktop.view.various.Constants;

public class FeedbackCreator {
	private final static Logger LOGGER = LoggerFactory.getLogger(FeedbackCreator.class);
	
	/** A list of alterations that have already been performed via the alter album functionality */
	private static LinkedList<String> alterations = new LinkedList<String>();
	
	static void generatAlbumItemUpdatedPage(long albumItemId) {
		AlbumItem updatedAlbumItem;
		try {
			updatedAlbumItem = DatabaseOperations.getAlbumItem(ApplicationUI.getSelectedAlbum(), albumItemId);


			if (updatedAlbumItem != null) {
				ApplicationUI.getAlbumItemSWTBrowser().setText(
						generateItemAddedOrUpdatedFeedbackConstruct(
								Translator.get(DictKeys.BROWSER_ITEM_UPDATED),
								ItemCreator.getAlbumItemTableRowHtml(updatedAlbumItem)));
			}
		} catch (DatabaseWrapperOperationException ex) {
			LOGGER.error("An error occured while fetching the album item #" + albumItemId + " in the album: " + 
							ApplicationUI.getSelectedAlbum() + " \n Stacktrace:" + ExceptionHelper.toString(ex));
		}
	}

	static void generateAlbumItemAddedPage(long albumItemId) {
		AlbumItem addedAlbumItem;
		try {
			addedAlbumItem = DatabaseOperations.getAlbumItem(ApplicationUI.getSelectedAlbum(), albumItemId);

			if (addedAlbumItem != null) {
				ApplicationUI.getAlbumItemSWTBrowser().setText(
						generateItemAddedOrUpdatedFeedbackConstruct(
								Translator.get(DictKeys.BROWSER_ITEM_ADDED),
								ItemCreator.getAlbumItemTableRowHtml(addedAlbumItem)));
			}
		} catch (DatabaseWrapperOperationException ex) {
			LOGGER.error("An error occured while fetching the album item #" + albumItemId + " in the album: " + 
					ApplicationUI.getSelectedAlbum() + " \n Stacktrace:" + ExceptionHelper.toString(ex));
		}
	}
	
	private static String generateItemAddedOrUpdatedFeedbackConstruct(String title, String addedOrUpdatedItemAsHtmlTableRowItem) {
		return 	"<!DOCTYPE HTML>" +
				"  <html>" +
				"    <head>" +
				"      <meta " + Constants.META_PARAMS + ">" +
				"      <link rel=stylesheet href=\"" + Constants.STYLE_CSS + "\" />" +
				"      <script src=\"" + Constants.EFFECTS_JS + "\"></script>" +
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
				Translator.get(DictKeys.BROWSER_CREATING_NEW_ALBUM),
				Translator.get(DictKeys.BROWSER_ALBUM_WILL_HANDLE_FOLLOWING_FORMAT)));
	}
	
	static void showCreateAlterAlbumPage(Browser browser, AlbumItem albumItem) {		
		browser.setText(generateAlbumAddedOrUpdatedFeedbackConstruct(
				albumItem,
				Translator.get(DictKeys.BROWSER_MODIFYING_ALBUM, ApplicationUI.getSelectedAlbum()),
				Translator.get(DictKeys.BROWSER_MODIFY_WARNING)));
	}
	
	private static String generateAlbumAddedOrUpdatedFeedbackConstruct(AlbumItem albumItem, String title, String subTitle) {
		return "<!DOCTYPE HTML>" +
			   "  <html>" +
			   "    <head>" +
			   "      <meta " + Constants.META_PARAMS + ">" + 
			   "      <link rel=stylesheet href=\"" + Constants.STYLE_CSS + "\" />" +
			   "      <script src=\"" + Constants.EFFECTS_JS + "\"></script>" +
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
