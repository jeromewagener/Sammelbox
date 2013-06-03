package collector.desktop.gui.browser;

import java.io.File;
import java.util.LinkedList;

import org.eclipse.swt.browser.Browser;

import collector.desktop.Collector;
import collector.desktop.database.AlbumItem;
import collector.desktop.database.DatabaseWrapper;
import collector.desktop.filesystem.FileSystemAccessWrapper;
import collector.desktop.internationalization.DictKeys;
import collector.desktop.internationalization.Translator;

public class FeedbackCreator {
	/** A list of alterations, already performed via the alter album functionality */
	private static LinkedList<String> alterations = new LinkedList<String>();
	
	static void generatAlbumItemUpdatedPage(long albumItemId) {
		String javaScript = "<script src=\"file://" + FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA + File.separatorChar + "effects.js" + "\"></script>";
		String styleCSS = "<link rel=stylesheet href=\"file://"+ FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA + File.separatorChar + "style.css" + "\"></link>";

		AlbumItem addedAlbumItem = DatabaseWrapper.fetchAlbumItem(Collector.getSelectedAlbum(), albumItemId);

		if (addedAlbumItem != null) {
			String addedItemHtml = ItemCreator.getAlbumItemTableRowHtml(addedAlbumItem);
		
			String finalPageAsHtml = "<!DOCTYPE HTML><html><head><meta http-equiv=\"X-UA-Compatible\" content=\"IE=9\" >" 
										+ styleCSS + " " + javaScript + "</head><body bgcolor=white><font face=\"" + Utilities.getDefaultSystemFont() 
										+ "\"><h1>" + Translator.get(DictKeys.BROWSER_ITEM_UPDATED) + "</h1><table id=\"albumItems\" border=0>" + addedItemHtml + "</table></font>" 
										+ "<br><form><input type=\"button\" onclick=parent.location.href=\"show:///showDetailsViewOfAlbum\" value=\"" 
										+ Translator.get(DictKeys.BROWSER_BACK_TO_ALBUM) + "\"></form></body></html>";
	
			Collector.getAlbumItemSWTBrowser().setText(finalPageAsHtml);
		}
	}

	static void generateAlbumItemAddedPage(long albumItemId) {
		String javaScript = "<script src=\"file://" + FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA + File.separatorChar + "effects.js" + "\"></script>";
		String styleCSS = "<link rel=stylesheet href=\"file://"+ FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA + File.separatorChar + "style.css" + "\"></link>";

		AlbumItem addedAlbumItem = DatabaseWrapper.fetchAlbumItem(Collector.getSelectedAlbum(), albumItemId);

		if (addedAlbumItem != null) {
			String addedItemHtml = ItemCreator.getAlbumItemTableRowHtml(addedAlbumItem);
		
			String finalPageAsHtml = "<!DOCTYPE HTML><html><head><meta http-equiv=\"X-UA-Compatible\" content=\"IE=9\" >" 
										+ styleCSS + " " + javaScript + "</head><body bgcolor=white><font face=\"" + Utilities.getDefaultSystemFont() 
										+ "\"><h1>" + Translator.get(DictKeys.BROWSER_ITEM_ADDED) + "</h1><table id=\"albumItems\" border=0>" + addedItemHtml + "</table></font>" 
										+ "<br><form><input type=\"button\" onclick=parent.location.href=\"show:///showDetailsViewOfAlbum\" value=\"" 
										+ Translator.get(DictKeys.BROWSER_BACK_TO_ALBUM) + "\"></form></body></html>";
	
			Collector.getAlbumItemSWTBrowser().setText(finalPageAsHtml);
		}
	}
	
	static void showCreateNewAlbumPage(Browser browser, AlbumItem albumItem) {
		StringBuilder htmlBuilder = new StringBuilder();
		String styleCSS = "<link rel=stylesheet href=\"file://"+ FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA + File.separatorChar + "style.css" + "\"></link>";
		String javaScript = "<script src=\"file://" + FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA + File.separatorChar + "effects.js" + "\"></script>";
		
		htmlBuilder.append("<html><head>" + styleCSS + javaScript + "</head><body>");
		htmlBuilder.append("<h1>" + Translator.toBeTranslated("Creating a new Album") + "</h1>");
		htmlBuilder.append("<h4>" + Translator.toBeTranslated("Your Album will be able to store items in the following format:") + "</h4>");
		htmlBuilder.append("<hr noshade size=\"1\">");
		htmlBuilder.append("<table>" + ItemCreator.getAlbumItemTableRowHtml(albumItem, false) + "</table>");
		htmlBuilder.append("<hr noshade size=\"1\">");
		htmlBuilder.append("</body></html>");
		
		browser.setText(htmlBuilder.toString());
	}
	
	static void showCreateAlterAlbumPage(Browser browser, AlbumItem albumItem) {
		StringBuilder htmlBuilder = new StringBuilder();
		String styleCSS = "<link rel=stylesheet href=\"file://"+ FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA + File.separatorChar + "style.css" + "\"></link>";
		String javaScript = "<script src=\"file://" + FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA + File.separatorChar + "effects.js" + "\"></script>";
		
		htmlBuilder.append("<html><head>" + styleCSS + javaScript + "</head><body>");
		htmlBuilder.append("<h1>" + Translator.toBeTranslated("Modifying ") + Collector.getSelectedAlbum() + "</h1>");
		htmlBuilder.append("<h4>" + Translator.toBeTranslated("<u>Attention:</u> All changes will have <font color=red>imediate</font> effects!<br>" +
				"Your Album is currently able to store items in the following format:") + "</h4>");
		htmlBuilder.append("<hr noshade size=\"1\">");
		htmlBuilder.append("<table>" + ItemCreator.getAlbumItemTableRowHtml(albumItem, false) + "</table>");
		htmlBuilder.append("<hr noshade size=\"1\">");
		htmlBuilder.append("<ul>" + getAlterationsAsListItems() + "</ul>");
		htmlBuilder.append("</body></html>");
		
		System.out.println(htmlBuilder.toString());
		
		browser.setText(htmlBuilder.toString());
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
