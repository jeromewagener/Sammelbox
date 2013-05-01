package collector.desktop.gui;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Display;

import collector.desktop.Collector;
import collector.desktop.database.AlbumItem;
import collector.desktop.database.AlbumItemResultSet;
import collector.desktop.database.AlbumItemStore;
import collector.desktop.database.DatabaseWrapper;
import collector.desktop.database.FieldType;
import collector.desktop.database.ItemField;
import collector.desktop.database.OptionType;
import collector.desktop.filesystem.FileSystemAccessWrapper;

public class BrowserContent {
	/** The anchor to which a jump is performed as soon as the page is fully loaded. 
	 * This field is used via the set and get methods by the browser progress listener */
	private static String futureJumpAnchor = null;
	
	public static String getAnchorForAlbumItemId(long albumItemId) {
		return "albumId" + albumItemId;
	}

	/** To avoid rebuilding the exact same HTML output when returning from the picture viewer 
	 * to the album view, the last generated HTML output is saved  */
	private static String lastPageAsHtml;

	public static void performBrowserQueryAndShow(Browser browser, String sqlQuery) {				
		AlbumItemStore.reinitializeStore(DatabaseWrapper.executeSQLQuery(sqlQuery));
		showAlbum(browser);
	}

	public static void performLastQuery(Browser browser) {
		showAlbum(browser);
	}

	public static void showResultSet(Browser browser, AlbumItemResultSet albumItemResultSet) {
		AlbumItemStore.reinitializeStore(albumItemResultSet);
		showAlbum(browser);
	}

	/** Use this method to set a "future-jump-anchor" to a specific item
	 * This id will be used to jump to an item after the content
	 * of the HTML document changed and is completely loaded */
	public static void setFutureJumpAnchor(String futureJumpAnchor) {
		BrowserContent.futureJumpAnchor = futureJumpAnchor;
	}

	/** The browser progress listener uses this method to jump to a previously
	 * provided anchor, as soon as the HTML document is completely loaded */
	public static String getFutureJumpAnchor() {
		return BrowserContent.futureJumpAnchor;
	}

	/** Attention: This method must only be used to jump to an anchor (id) if the currently shown
	 * HTML document is fully loaded. (Due to asynchronous creation) If it is necessary 
	 * to jump to a specific item after creating a new HTML document, the setFutureJumpAnchor 
	 * must be used. By using this method the browser progress listener will execute the
	 * jump as soon as the document is completely loaded */
	public static void jumpToAnchor(String anchor) {
		Collector.getAlbumItemSWTBrowser().execute("window.location.hash=\"" + anchor + "\"");
	}

	public static void goBackToLastPage() {
		if (lastPageAsHtml != null) {
			Collector.getAlbumItemSWTBrowser().setText(lastPageAsHtml);
		}
	}

	public static void showPicture(String pathToPicture, long albumItemId) {
		if (!Collector.hasSelectedAlbum()) {
			Collector.showErrorDialog("No album has been selected", "Please select an album from the list or create one first.");
			return;
		}
		StringBuilder smallPage = new StringBuilder();

		StatusBarComposite.getInstance(
				Collector.getShell()).writeStatus("Please click on the picture to return to your previous view!");

		AlbumItem albumItem = DatabaseWrapper.fetchAlbumItem(Collector.getSelectedAlbum(), albumItemId);
		List<URI> uris = null;
		for (ItemField itemField : albumItem.getFields()) {
			if (itemField.getType().equals(FieldType.Picture)) {
				uris = itemField.getValue();			
				break;
			}
		}

		StringBuilder smallPictures = new StringBuilder();
		if (uris.size() >= 2) {
			int counter = 1;
			for (URI uri : uris) {					
				smallPictures.append("<a onMouseover='change(\"bigimg\", \"" + uri.toString() + "\")'>");  
				smallPictures.append("<img border=\"1\" onMouseOver='this.style.cursor=\"pointer\"' id=\"smallimage" + counter + "\" style=\"width:120px;\" src=\"" + uri.toString() + "\">");
				smallPictures.append("</a>");
				smallPictures.append("</br>");

				counter++;
			}

			smallPictures.append("<br>");
			smallPictures.append("<form><input type='button' onclick=\"parent.location.href='show:///lastPage'\" value='Go Back'></form>");
		}

		smallPage.append("<html width=\"80%\" height=80%>");
		smallPage.append("<head>");
		smallPage.append("<script src=\"file://" + FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA + File.separatorChar + "effects.js" + "\"></script>");
		smallPage.append("<link rel=stylesheet href=\"file://" + FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA + File.separatorChar + "style.css" + "\"></link>");
		smallPage.append("</head>");
		smallPage.append("<body width=\"90%\" height=80%>");
		smallPage.append("<table width=\"90%\" height=80%>");
		smallPage.append("<tr width=\"90%\"  height=80%>");
		smallPage.append("<td align=\"center\">");
		smallPage.append("<img id=\"bigimg\" height=80% src=\"" + pathToPicture + "\" onMouseOver=\"changeCursorToHand('bigimg')\" onclick=\"parent.location.href='show:///lastPage'\">");		
		smallPage.append("</td>");
		smallPage.append("<td align=\"center\">");
		smallPage.append(smallPictures.toString());		
		smallPage.append("</td>");
		smallPage.append("</tr>");
		smallPage.append("</table>");
		smallPage.append("</body>");
		smallPage.append("</html>");

		Collector.getAlbumItemSWTBrowser().setText(smallPage.toString());
	}

	private static void showAlbum(Browser browser) {
		if (Collector.isViewDetailed()) {
			showDetailedAlbum(browser);
		} else {
			showOverviewAlbum(browser);
		}
	}

	private static void showOverviewAlbum(Browser browser) {
		StringBuilder htmlBuilder = new StringBuilder();

		htmlBuilder.append("<!DOCTYPE HTML>");
		
		String javaScript = "<script src=\"file://" + FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA + File.separatorChar + "effects.js" + "\"></script>";
		String styleCSS = "<link rel=stylesheet href=\"file://"+ FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA + File.separatorChar + "style.css" + "\"></link>";
		
		htmlBuilder.append("<html><head><meta http-equiv=\"X-UA-Compatible\" content=\"IE=9\">" + styleCSS + " " + javaScript + "</head><body><div id=\"albumItems\">");

		for (AlbumItem albumItem : AlbumItemStore.getAlbumItems(AlbumItemStore.getStopIndex())) {
			String picturePath = "";

			long id = 0;

			for (ItemField fieldItem : albumItem.getFields()) {				
				if (fieldItem.getType().equals(FieldType.UUID)) {
					// schema or content version UUID --> ignore
				}
				else if (fieldItem.getType().equals(FieldType.ID)) {
					if (!fieldItem.getName().equals("typeinfo")) {
						// do not show, but store id
						id = fieldItem.getValue();
					} else {
						// its a trap :-) (Probably just the typeinfo foreign key..)
					}
				}
				else if (fieldItem.getType().equals(FieldType.Picture)) {
					List<URI> uris = fieldItem.getValue();

					if (!uris.isEmpty()) {
						picturePath = uris.get(0).toString();
					} else {
						picturePath = FileSystemAccessWrapper.PLACEHOLDERIMAGE;
					}
				}
				else if (fieldItem.getType().equals(FieldType.Text)) {
				}
			}		

			htmlBuilder.append("<div id=\"imageId" + id + "\" class=\"pictureContainer\" " +
					"onMouseOver=\"parent.location.href=&quot;show:///details=" + id + "&quot;\" onClick=\"parent.location.href=&quot;show:///detailsComposite=" + id + "&quot;\">");

			htmlBuilder.append("<div class=\"innerPictureContainer\">");
			htmlBuilder.append("<img src=\"" + picturePath + "\">");

			htmlBuilder.append("</div>");
			htmlBuilder.append("</div>");
		}

		htmlBuilder.append("</div></body></html>");

		String finalPageAsHtml = htmlBuilder.toString();

		browser.setText(finalPageAsHtml);
		BrowserContent.lastPageAsHtml = finalPageAsHtml;
	}

	private static void showDetailedAlbum(Browser browser) {
		if (!Collector.hasSelectedAlbum()) {
			Collector.showErrorDialog("No album has been selected", "Please select an album from the list or create one first.");
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
			
			addAlbumItemTableRow(albumItem, htmlDataColumnContent, htmlPictureColumnContent, albumItemTableRowHtml);
		}

		if (htmlDataColumnContent.length() == 0 && htmlPictureColumnContent.length() == 0) {
			albumItemTableRowHtml.delete(0, albumItemTableRowHtml.length());
			albumItemTableRowHtml.append("<tr> <td><h3>No Items found in " + Collector.getSelectedAlbum() + "!</h3> Either you just created this album and it is still empty <i>(In this case, please feel free to add as many items " +
					"as you want using the \"Add a new Item to Album\" button or the corresponding file-menu-entry)</i> or you just performed a search which did not yield any results!</td></tr>");
		}

		String finalPageAsHtml = "<!DOCTYPE HTML>" +
				"<html><head><meta http-equiv=\"X-UA-Compatible\" content=\"IE=9\" >" + styleCSS + " " + javaScript + "</head><body bgcolor=white><table id=\"albumItems\" border=0>" + albumItemTableRowHtml + "</table></body></html>";

		browser.setText(finalPageAsHtml);

		BrowserContent.lastPageAsHtml = finalPageAsHtml;		
	}

	private static String getAlbumItemTableRowHtml(AlbumItem albumItem) {
		StringBuilder htmlDataColumnContent = new StringBuilder();
		StringBuilder htmlPictureColumnContent = new StringBuilder();
		StringBuilder albumItemTableRowHtml = new StringBuilder();
		
		addAlbumItemTableRow(albumItem, htmlDataColumnContent, htmlPictureColumnContent, albumItemTableRowHtml);
		
		return albumItemTableRowHtml.toString();
	}
	
	private static String getAlbumItemDivContainerHtml(AlbumItem albumItem) {
		StringBuilder htmlBuilder = new StringBuilder();
		
		addAlbumItemDivContainer(albumItem, htmlBuilder);
		
		return htmlBuilder.toString();
	}
	
	private static void addAlbumItemDivContainer(AlbumItem albumItem, StringBuilder htmlBuilder) {
		htmlBuilder.append("<div id=\"imageId" + albumItem.getItemID() + "\" class=\"pictureContainer\" " +
				"onMouseOver=\"parent.location.href=&quot;show:///details=" + albumItem.getItemID() + "&quot;\" onClick=\"parent.location.href=&quot;show:///detailsComposite=" + albumItem.getItemID() + "&quot;\">");

		htmlBuilder.append("<div class=\"innerPictureContainer\">");
		htmlBuilder.append("<img src=\"" + albumItem.getPrimaryPicturePath() + "\">");

		htmlBuilder.append("</div>");
		htmlBuilder.append("</div>");
	}
	
	private static void addAlbumItemTableRow(AlbumItem albumItem, StringBuilder htmlDataColumnContent, StringBuilder htmlPictureColumnContent, StringBuilder albumItemTableRowHtml) {
		long id = 0;
		
		for (ItemField fieldItem : albumItem.getFields()) {			
			if (fieldItem.getType().equals(FieldType.UUID)) {
				// schema or content version UUID --> ignore 
			}
			else if (fieldItem.getType().equals(FieldType.ID)) {
				if (!fieldItem.getName().equals("typeinfo")) {
					// do not show, but store id
					id = fieldItem.getValue();
				} else {
					// its a trap :-) (Probably just the typeinfo foreign key..)
				}
			}
			else if (fieldItem.getType().equals(FieldType.Picture)) {
				List<URI> uris = fieldItem.getValue();
				htmlPictureColumnContent.append("<table border=0><tr><td align=center width=200 height=200>");

				if (!uris.isEmpty()) {
					htmlPictureColumnContent.append("<img id=\"imageId" + id + "\" style=\"max-width:195px; max-height:195px;\" src=\"" + uris.get(0).toString() + "\" onMouseOver=changeCursorToHand(\"imageId" + id + "\") onClick=showBigPicture(\"imageId" + id + "\")>");
				} else {
					htmlPictureColumnContent.append("<img id=\"imageId" + id + "\" width=195 height=195 src=\"" + FileSystemAccessWrapper.PLACEHOLDERIMAGE + "\">");
				}

				htmlPictureColumnContent.append("</td></tr><tr><td>");
				htmlPictureColumnContent.append("<div style=\"max-width:200px;\">");
				if (uris.size() > 1) {
					for(URI uri : uris) {
						htmlPictureColumnContent.append("<div align=center style=\"display:inline; min-width:40px; width:auto; width:40px\">");
						htmlPictureColumnContent.append("<a onClick=showBigPicture(\"imageId" + id + "\") onMouseOver=\"change(&quot;imageId" + id + "&quot;, &quot;" + uri.toString() + "&quot;)\">");
						htmlPictureColumnContent.append("<img onMouseOver=this.style.cursor=\"pointer\" style=\"max-width:40px; max-height:40px;\" src=\"" + uri.toString() + "\">");
						htmlPictureColumnContent.append("</a>");
						htmlPictureColumnContent.append("</div>");
					}
				}
				htmlPictureColumnContent.append("</div></td></tr></table>");
			}
			else if (fieldItem.getType().equals(FieldType.Option)) {
				if (fieldItem.getValue() == OptionType.Yes) {
					htmlDataColumnContent.append("<span class=\"boldy\"> " + fieldItem.getName() + "</span> : Yes <br>");
				} else if (fieldItem.getValue() == OptionType.No) {
					htmlDataColumnContent.append("<span class=\"boldy\"> " + fieldItem.getName() + "</span> : No <br>");
				} else {
					htmlDataColumnContent.append("<span class=\"boldy\"> " + fieldItem.getName() + "</span> : Unknown <br>");
				}
			} 
			else if (fieldItem.getType().equals(FieldType.Date)) {
				java.sql.Date sqlDate = fieldItem.getValue();
				
				java.util.Date utilDate = new java.util.Date(sqlDate.getTime());
				
				// TODO set date format via preferences
				SimpleDateFormat f = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.LONG, Locale.ENGLISH);
				
				htmlDataColumnContent.append("<span class=\"boldy\"> " + fieldItem.getName() + "</span> : " + f.format(utilDate) + "<br>");
			} else {
				htmlDataColumnContent.append("<span class=\"boldy\"> " + fieldItem.getName() + "</span> : " + fieldItem.getValue() + "<br>");
			}
		}	

		htmlDataColumnContent.append("<form>");
		htmlDataColumnContent.append("<input type=\"button\" onclick=parent.location.href=\"show:///updateComposite=" + id + "\" value=\"Update\">");
		htmlDataColumnContent.append("<input type=\"button\" onclick=parent.location.href=\"show:///deleteComposite=" + id + "\" value=\"Delete\">");
		htmlDataColumnContent.append("</form>");


		albumItemTableRowHtml.append("<tr id=\"albumId" + id + "\"><td>" + htmlPictureColumnContent + "</td><td width=90% bgcolor=" + getBackgroundColorOfWidgetInHex() + ">" + htmlDataColumnContent + "</td></tr><tr><td height=\"20\" colspan=\"2\"></td></tr>");		
	}
	
	public static void loadHtmlPage(Browser browser, InputStream fileInputStream) {
		browser.setText(FileSystemAccessWrapper.readInputStreamIntoString(fileInputStream));
	}

	public static String getBackgroundColorOfWidgetInHex() {
		return  Integer.toHexString((Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND).getRed()))
				+ Integer.toHexString((Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND).getGreen()))
				+ Integer.toHexString((Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND).getBlue()));
	}

	public static void loadWelcomePage() {
		String welcomePage = "";

		welcomePage += "<html width=100% height=80%>";
		welcomePage += "<body width=100% height=80%>";
		welcomePage += "	<br>";
		welcomePage += "	<table width=100% height=80%>";
		welcomePage += "		<tr width=100% height=80%>";
		welcomePage += "			<td width=50% align=\"center\">";
		welcomePage += "				<h1>Sammelbox</h1>";
		welcomePage += "				<img height=\"350px\" src=\" " + FileSystemAccessWrapper.LOGO + " \">";
		welcomePage += "				<h2>Collection Manager</h2>";
		welcomePage += "			</td>";
		welcomePage += "			<td width=50%>";
		welcomePage += "				<div style=\"padding:10px; background-color:#" + getBackgroundColorOfWidgetInHex() + "\">";

		welcomePage += generateAlbumInformation();
		welcomePage += generateFavorites();
		
		welcomePage += "				</div>";
		welcomePage += "			</td>";
		welcomePage += "		</tr>";
		welcomePage += "	</table>";
		welcomePage += "</body>";
		welcomePage += "</html>";

		Collector.getAlbumItemSWTBrowser().setText(welcomePage);
	}

	public static String generateAlbumInformation() {
		String welcomePage = "<h4>Album Information</h4>";
		welcomePage += "<ul>";
				
		boolean empty = true;
		for (String albumName : DatabaseWrapper.listAllAlbums()) {
			welcomePage += "<li>Album <b>" + albumName + "</b> <br> <font size=-1><i>(" + WelcomePageManager.getInstance().getNumberOfItemsInAlbum(albumName) 
					+ " items - Last updated: " + WelcomePageManager.getInstance().getLastModifiedDate(albumName) + ")</i></font></li>";
			
			empty = false;
		}
		
		if (empty) {
			welcomePage += "<li>No information available</li>";
		}
		
		welcomePage += "</ul>";
		
		return welcomePage;
	}
	
	public static String generateFavorites() {
		String welcomePage = "<h4>Favorite Albums & Views</h4>";
		welcomePage += "<ol>";
		
		int favCounter = 0;
		boolean empty = true;
		for (String albumOrViewName : WelcomePageManager.getInstance().getAlbumAndViewsSortedByClicks().keySet()) {
			welcomePage += "<li>" + albumOrViewName + "<font size=-1><i> " +
					" - (Clicks: " + WelcomePageManager.getInstance().getAlbumAndViewsSortedByClicks().get(albumOrViewName) + ") </i></font></li>";
			
			if (++favCounter == 5) {
				break;
			}
			
			empty = false;
		}
		
		if (empty) {
			welcomePage += "<li>No information available</li>";
		}
		
		welcomePage += "</ol>";
		
		return welcomePage;		
	}
	
	public static void loadHelpPage() {
		loadHtmlPage(
				Collector.getAlbumItemSWTBrowser(),
				Collector.getInstance().getClass().getClassLoader().getResourceAsStream("helpfiles/index.html"));
	}
	
	public static void addAdditionalAlbumItems() {
		if (Collector.isViewDetailed()) {
			if (!AlbumItemStore.isStopIndexAtEnd()) {
				StringBuilder rows = new StringBuilder();
				
				AlbumItemStore.increaseStopIndex();
				for (AlbumItem albumItem : (AlbumItemStore.getAlbumItemsInRange(AlbumItemStore.getPreviousStopIndex() + 1, AlbumItemStore.getStopIndex()))) {
					rows.append(getAlbumItemTableRowHtml(albumItem));
				}
				
				
				String javascript = "var table = document.getElementById('albumItems'); " +
									"var tbody = table.tBodies[0]; " +
				                    "var temp = tbody.ownerDocument.createElement('div'); " +
				                    "temp.innerHTML = '<table>' + tbody.innerHTML + '" + rows + "</table>'; " +
				                    "tbody.parentNode.replaceChild(temp.firstChild.firstChild, tbody); ";
								
				Collector.getAlbumItemSWTBrowser().execute(javascript);
			}
		} else {
			if (!AlbumItemStore.isStopIndexAtEnd()) {
				StringBuilder divs = new StringBuilder();
				
				AlbumItemStore.increaseStopIndex();
				for (AlbumItem albumItem : (AlbumItemStore.getAlbumItemsInRange(AlbumItemStore.getPreviousStopIndex() + 1, AlbumItemStore.getStopIndex()))) {
					divs.append(getAlbumItemDivContainerHtml(albumItem));
				}
				
				String javascript = "var div = document.getElementById('albumItems');" +
									"div.innerHTML = div.innerHTML + '" + divs + "';";
								
				Collector.getAlbumItemSWTBrowser().execute(javascript);
			}
		}
	}
}
