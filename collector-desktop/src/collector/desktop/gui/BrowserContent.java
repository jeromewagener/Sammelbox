package collector.desktop.gui;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

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
import collector.desktop.internationalization.DictKeys;
import collector.desktop.internationalization.Translator;
import collector.desktop.settings.ApplicationSettingsManager;

public class BrowserContent {
	/** The anchor to which a jump is performed as soon as the page is fully loaded. 
	 * This field is used via the set and get methods by the browser progress listener */
	private static String futureJumpAnchor = null;

	/** A list of alterations, already performed via the alter album functionality */
	private static LinkedList<String> alterations = new LinkedList<String>();
	
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
			ComponentFactory.showErrorDialog(
					Collector.getShell(), 
					Translator.get(DictKeys.DIALOG_TITLE_NO_ALBUM_SELECTED), 
					Translator.get(DictKeys.DIALOG_CONTENT_NO_ALBUM_SELECTED));
			return;
		}
		StringBuilder smallPage = new StringBuilder();

		StatusBarComposite.getInstance(
				Collector.getShell()).writeStatus(Translator.get(DictKeys.STATUSBAR_CLICK_TO_RETURN));

		AlbumItem albumItem = DatabaseWrapper.fetchAlbumItem(Collector.getSelectedAlbum(), albumItemId);
		List<URI> uris = null;
		for (ItemField itemField : albumItem.getFields()) {
			if (itemField.getType().equals(FieldType.Picture)) {
				uris = itemField.getValue();			
				break;
			}
		}

		String originalPathToPicture = "";
		String thumbnailImageName = new File(pathToPicture).getName();
		String imageId = thumbnailImageName.substring(0, thumbnailImageName.lastIndexOf('.'));

		StringBuilder smallPictures = new StringBuilder();
		if (uris.size() >= 2) {
			int counter = 1;

			for (URI uri : uris) {
				if (uri.toString().contains("original")) {
					if (uri.toString().contains(imageId)) {
						originalPathToPicture = uri.toString();
					}

					smallPictures.append("<a onMouseover='change(\"bigimg\", \"" + uri.toString() + "\")'>");  
					smallPictures.append("<img border=\"1\" onMouseOver='this.style.cursor=\"pointer\"' id=\"smallimage" + counter + "\" style=\"width:120px; margin-top:10px;\" src=\"" + uri.toString() + "\">");
					smallPictures.append("</a>");
					smallPictures.append("</br>");

					counter++;
				}
			}

			smallPictures.append("<br>");
			smallPictures.append("<form><input type='button' onclick=\"parent.location.href='show:///lastPage'\" value='Go Back'></form>");
		}

		smallPage.append("<html>");
		smallPage.append("<head>");
		smallPage.append("<script src=\"file://" + FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA + File.separatorChar + "effects.js" + "\"></script>");
		smallPage.append("<link rel=stylesheet href=\"file://" + FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA + File.separatorChar + "style.css" + "\"></link>");
		smallPage.append("</head>");
		smallPage.append("<body>");
		smallPage.append("<table>");
		smallPage.append("<tr>");
		smallPage.append("<td align=\"center\" valign=\"top\">");
		smallPage.append(smallPictures.toString());		
		smallPage.append("</td>");
		smallPage.append("<td align=\"left\" valign=\"top\">");
		smallPage.append("<img style=\"max-width: 100%; max-height: 100%;\" id=\"bigimg\" src=\"" + originalPathToPicture + "\" onMouseOver=\"changeCursorToHand('bigimg')\" onclick=\"parent.location.href='show:///lastPage'\">");
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

	public static void showCreateNewAlbumPage(Browser browser, AlbumItem albumItem) {
		StringBuilder htmlBuilder = new StringBuilder();
		String styleCSS = "<link rel=stylesheet href=\"file://"+ FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA + File.separatorChar + "style.css" + "\"></link>";
		String javaScript = "<script src=\"file://" + FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA + File.separatorChar + "effects.js" + "\"></script>";
		
		htmlBuilder.append("<html><head>" + styleCSS + javaScript + "</head><body>");
		htmlBuilder.append("<h1>" + Translator.toBeTranslated("Creating a new Album") + "</h1>");
		htmlBuilder.append("<h4>" + Translator.toBeTranslated("Your Album will be able to store items in the following format:") + "</h4>");
		htmlBuilder.append("<hr noshade size=\"1\">");
		htmlBuilder.append("<table>" + getAlbumItemTableRowHtml(albumItem, false) + "</table>");
		htmlBuilder.append("<hr noshade size=\"1\">");
		htmlBuilder.append("</body></html>");
		
		browser.setText(htmlBuilder.toString());
	}
	
	public static void showCreateAlterAlbumPage(Browser browser, AlbumItem albumItem) {
		StringBuilder htmlBuilder = new StringBuilder();
		String styleCSS = "<link rel=stylesheet href=\"file://"+ FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA + File.separatorChar + "style.css" + "\"></link>";
		String javaScript = "<script src=\"file://" + FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA + File.separatorChar + "effects.js" + "\"></script>";
		
		htmlBuilder.append("<html><head>" + styleCSS + javaScript + "</head><body>");
		htmlBuilder.append("<h1>" + Translator.toBeTranslated("Modifying ") + Collector.getSelectedAlbum() + "</h1>");
		htmlBuilder.append("<h4>" + Translator.toBeTranslated("<u>Attention:</u> All changes will have <font color=red>imediate</font> effects!<br>" +
				"Your Album is currently able to store items in the following format:") + "</h4>");
		htmlBuilder.append("<hr noshade size=\"1\">");
		htmlBuilder.append("<table>" + getAlbumItemTableRowHtml(albumItem, false) + "</table>");
		htmlBuilder.append("<hr noshade size=\"1\">");
		htmlBuilder.append("<ul>" + getAlterationsAsListItems() + "</ul>");
		htmlBuilder.append("</body></html>");
		
		System.out.println(htmlBuilder.toString());
		
		browser.setText(htmlBuilder.toString());
	}
	
	private static String getAlterationsAsListItems() {
		StringBuilder listItems = new StringBuilder();
		
		for (String alteration : alterations) {
			listItems.append("<li>" + alteration + "</li>");
		}
		
		return listItems.toString();
	}
	
	public static void clearAlterationList() {
		alterations.clear();
	}
	
	public static void addModificationToAlterationList(String modification) {
		alterations.addFirst(modification);
	}
	
	/**
	 * Shows a page that allows to review the changes made to the album and reload the album to see the changes. 
	 * Only one change is displayed. 
	 * @param browser The browser used to display the webpage.
	 * @param oldAlbumName
	 * @param oldAlbumFields
	 * @param oldHasAlbumPictureField
	 * @param newAlbumName
	 * @param newAlbumFields
	 * @param newHasAlbumPictureField
	 */ /*
	public static void showAlteredAlbumPage(Browser browser,String oldAlbumName, List<MetaItemField> oldAlbumFields,
			String newAlbumName, List<MetaItemField> newAlbumFields) {
		StringBuilder htmlBuilder = new StringBuilder();
		String javaScript = "<script src=\"file://" + FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA + File.separatorChar + "effects.js" + "\"></script>";
		String styleCSS = "<link rel=stylesheet href=\"file://"+ FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA + File.separatorChar + "style.css" + "\"></link>";
		// TODO: implement better diff with more verbose output of the changes. Left old, right new kind of display
		htmlBuilder.append("<html><head><meta http-equiv=\"X-UA-Compatible\" content=\"IE=9\">" + styleCSS + " " + javaScript + "</head><body><div>");

		htmlBuilder.append("<form><table><tr><th>" + Translator.get(DictKeys.BROWSER_BEFORE) + "</th><th>" + Translator.get(DictKeys.BROWSER_AFTER) + "</th></tr>");	
		// Album names
		htmlBuilder.append("<tr><td>");
		htmlBuilder.append(oldAlbumName);// old album name
		htmlBuilder.append("</td>");
		if (!oldAlbumName.equals(newAlbumName)) {
			htmlBuilder.append("<td class=\"highlight\">");	
		}else{
			htmlBuilder.append("<td>");
		}
		htmlBuilder.append(newAlbumName);// new album name
		htmlBuilder.append("</td></tr>");
		// Fields
		int TypeOfAltering = newAlbumFields.size() - oldAlbumFields.size();// 0 for rename, 1 for add, -1 for remove field
		final int albumFieldDeleted = -1;
		final int albumFieldRenamed = 0;// rename or reordering.
		final int albumFieldAdded = 1;
		int indexOffset = 0;

		if (oldAlbumFields.size() == 0) {
			// skip old item
			htmlBuilder.append("<tr><td>------</td>");// old album
			if (newAlbumFields.size() == 0) {
				htmlBuilder.append("<td>------</td>");// new album
			}else {
				// print new item
				htmlBuilder.append("<td class=\"highlight\">");
				htmlBuilder.append(newAlbumFields.get(0).toString());// new album
				htmlBuilder.append("</td>");
			}
			htmlBuilder.append("</tr>");
		}else if (newAlbumFields.size() == 0) {
			// print old item and skip new item
			htmlBuilder.append("<tr><td class=\"highlight\">");
			htmlBuilder.append(oldAlbumFields.get(0).toString());// old album
			htmlBuilder.append("</td>");
			htmlBuilder.append("<td>------</td></tr>");// new album
		}else if (TypeOfAltering == albumFieldRenamed)  {
			for(int indexForFields=0;indexForFields<oldAlbumFields.size(); indexForFields++){
				if (!oldAlbumFields.get(indexForFields).equals(newAlbumFields.get(indexForFields))) {
					htmlBuilder.append("<tr>");
					// print old
					htmlBuilder.append(composeAlbumItemFieldTD(oldAlbumFields.get(indexForFields).toString(), false, oldAlbumFields.get(indexForFields).isQuickSearchable()));
					// print new with highlight class
					htmlBuilder.append(composeAlbumItemFieldTD(newAlbumFields.get(indexForFields).toString(), true, newAlbumFields.get(indexForFields).isQuickSearchable()));
					htmlBuilder.append("</tr>");				
				}else{
					// print old
					htmlBuilder.append("<tr>");
					htmlBuilder.append(composeAlbumItemFieldTD(oldAlbumFields.get(indexForFields).toString(), false, oldAlbumFields.get(indexForFields).isQuickSearchable())); 
					// print new
					htmlBuilder.append(composeAlbumItemFieldTD(newAlbumFields.get(indexForFields).toString(), false, newAlbumFields.get(indexForFields).isQuickSearchable()));
					htmlBuilder.append("</tr>");
				}
			}
		}else if (TypeOfAltering == albumFieldDeleted)  {
			for(int indexForFields=0;indexForFields<oldAlbumFields.size(); indexForFields++){
				if ( (indexForFields+indexOffset >=newAlbumFields.size()) || !oldAlbumFields.get(indexForFields).equals(newAlbumFields.get(indexForFields+indexOffset))) {
					// print old
					htmlBuilder.append("<tr>");
					htmlBuilder.append(composeAlbumItemFieldTD(oldAlbumFields.get(indexForFields).toString(), false, oldAlbumFields.get(indexForFields).isQuickSearchable()));
					// for new list use index-1 and print highlighted blank cell
					htmlBuilder.append("<td class=\"highlight\">------</td></tr>");
					indexOffset = -1;
				}else{
					// print old
					htmlBuilder.append("<tr>");
					htmlBuilder.append(composeAlbumItemFieldTD(oldAlbumFields.get(indexForFields).toString(), false, oldAlbumFields.get(indexForFields).isQuickSearchable()));
					// print new
					htmlBuilder.append(composeAlbumItemFieldTD(newAlbumFields.get(indexForFields+indexOffset).toString(), false, newAlbumFields.get(indexForFields+indexOffset).isQuickSearchable()));
					htmlBuilder.append("</tr>");
				}
			}
		}else if (TypeOfAltering == albumFieldAdded)  {
			for(int indexForFields=0;indexForFields<newAlbumFields.size(); indexForFields++){
				//				if ((indexForFields>=oldAlbumFields.size()) || !oldAlbumFields.get(indexForFields).equals(newAlbumFields.get(indexForFields))) {
				if ( (indexForFields+indexOffset >=oldAlbumFields.size())|| (!oldAlbumFields.get(indexForFields+indexOffset).equals(newAlbumFields.get(indexForFields)))) {

					// print old with blank
					htmlBuilder.append("<tr><td>------</td>");// old album
					// for new list use index-1 and print highlighted added cell
					htmlBuilder.append(composeAlbumItemFieldTD(newAlbumFields.get(indexForFields).toString(), true, newAlbumFields.get(indexForFields).isQuickSearchable()));
					htmlBuilder.append("</tr>");
					indexOffset = -1;
				}else{
					// print old
					htmlBuilder.append("<tr>");
					htmlBuilder.append(composeAlbumItemFieldTD(oldAlbumFields.get(indexForFields+indexOffset).toString(), false, oldAlbumFields.get(indexForFields+indexOffset).isQuickSearchable()));
					htmlBuilder.append("</td>");
					// print new
					htmlBuilder.append(composeAlbumItemFieldTD(newAlbumFields.get(indexForFields).toString(), false, newAlbumFields.get(indexForFields).isQuickSearchable()));
					htmlBuilder.append("</tr>");
				}
			}
		}

		htmlBuilder.append("</table><input type=\"button\" onclick=parent.location.href=\"show:///showDetailsViewOfAlbum\" value=\"" + Translator.get(DictKeys.BROWSER_BACK_TO_ALBUM) + "\"/>");
		htmlBuilder.append("</form></div></body></html>");
		String finalPageAsHtml = htmlBuilder.toString();

		browser.setText(finalPageAsHtml);
	}

	private static String composeAlbumItemFieldTD(String itemField, boolean highlightField, boolean isQuickSearchable){
		StringBuilder htmlBuilder = new StringBuilder();
		String itemValue = itemField.isEmpty() ? "------" : itemField;
		String tdStartTag = !highlightField ? "<td>" : "<td  class=\"highlight\">";
		htmlBuilder.append(tdStartTag);
		htmlBuilder.append("<input type=\"checkbox\" ");
		htmlBuilder.append(isQuickSearchable ? Translator.get(DictKeys.BROWSER_CHECKED) + "/>" : "/>");// item
		htmlBuilder.append(itemValue);
		htmlBuilder.append("<br></td>"); 
		return htmlBuilder.toString();
	}*/

	private static void showOverviewAlbum(Browser browser) {
		StringBuilder htmlBuilder = new StringBuilder();

		htmlBuilder.append("<!DOCTYPE HTML>");

		String javaScript = "<script src=\"file://" + FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA + File.separatorChar + "effects.js" + "\"></script>";
		String styleCSS = "<link rel=stylesheet href=\"file://"+ FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA + File.separatorChar + "style.css" + "\"></link>";

		htmlBuilder.append("<html><head><meta http-equiv=\"X-UA-Compatible\" content=\"IE=9\">" + styleCSS + " " + javaScript + "</head><body><font face=\"" + getDefaultSystemFont() + "\"><div id=\"albumItems\">");

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

					picturePath = FileSystemAccessWrapper.PLACEHOLDERIMAGE;

					for (URI uri : uris) {
						// find and return first thumbnail
						if (!uri.toString().contains("original")) {
							picturePath = uri.toString();
							break;
						}
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

		htmlBuilder.append("</div></font></body></html>");

		String finalPageAsHtml = htmlBuilder.toString();
		System.out.println(finalPageAsHtml);

		browser.setText(finalPageAsHtml);
		BrowserContent.lastPageAsHtml = finalPageAsHtml;
	}

	private static void showDetailedAlbum(Browser browser) {
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

			addAlbumItemTableRow(albumItem, htmlDataColumnContent, htmlPictureColumnContent, albumItemTableRowHtml);
		}

		if (htmlDataColumnContent.length() == 0 && htmlPictureColumnContent.length() == 0) {
			albumItemTableRowHtml.delete(0, albumItemTableRowHtml.length());
			albumItemTableRowHtml.append("<tr><td><h3>" + Translator.get(DictKeys.BROWSER_NO_ITEMS_FOUND, Collector.getSelectedAlbum()) + "</h3>" + Translator.get(DictKeys.BROWSER_NO_ITEMS_FOUND_EXPLANATION) + "</td></tr>");
		}

		String finalPageAsHtml = "<!DOCTYPE HTML>" +
				"<html><head><meta http-equiv=\"X-UA-Compatible\" content=\"IE=9\" >" + styleCSS + " " + javaScript + "</head><body bgcolor=white><font face=\"" + getDefaultSystemFont() + "\"><table id=\"albumItems\" border=0>" + albumItemTableRowHtml + "</table></font></body></html>";

		browser.setText(finalPageAsHtml);

		BrowserContent.lastPageAsHtml = finalPageAsHtml;		
	}

	private static String getAlbumItemTableRowHtml(AlbumItem albumItem) {
		return getAlbumItemTableRowHtml(albumItem, true);
	}
	
	private static String getAlbumItemTableRowHtml(AlbumItem albumItem, boolean showAddAndUpdateButtons) {
		StringBuilder htmlDataColumnContent = new StringBuilder();
		StringBuilder htmlPictureColumnContent = new StringBuilder();
		StringBuilder albumItemTableRowHtml = new StringBuilder();

		addAlbumItemTableRow(albumItem, htmlDataColumnContent, htmlPictureColumnContent, albumItemTableRowHtml, showAddAndUpdateButtons);

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
		htmlBuilder.append("<img src=\"" + albumItem.getPrimaryThumbnailPicturePath() + "\">");

		htmlBuilder.append("</div>");
		htmlBuilder.append("</div>");
	}

	private static void addAlbumItemTableRow(AlbumItem albumItem, StringBuilder htmlDataColumnContent, StringBuilder htmlPictureColumnContent, StringBuilder albumItemTableRowHtml) {
		addAlbumItemTableRow(albumItem, htmlDataColumnContent, htmlPictureColumnContent, albumItemTableRowHtml, true);
	}
	
	private static void addAlbumItemTableRow(AlbumItem albumItem, StringBuilder htmlDataColumnContent, StringBuilder htmlPictureColumnContent, StringBuilder albumItemTableRowHtml, boolean showButtons) {
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
					// TODO: its a trap :-) (Probably just the typeinfo foreign key..) probably not since that is caught just above ;)
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
						if (!uri.toString().contains("original")) {						
							htmlPictureColumnContent.append("<div align=center style=\"display:inline; min-width:40px; width:auto; width:40px\">");
							htmlPictureColumnContent.append("<a onClick=showBigPicture(\"imageId" + id + "\") onMouseOver=\"change(&quot;imageId" + id + "&quot;, &quot;" + uri.toString() + "&quot;)\">");
							htmlPictureColumnContent.append("<img onMouseOver=this.style.cursor=\"pointer\" style=\"max-width:40px; max-height:40px;\" src=\"" + uri.toString() + "\">");
							htmlPictureColumnContent.append("</a>");
							htmlPictureColumnContent.append("</div>");
						}
					}
				}
				htmlPictureColumnContent.append("</div></td></tr></table>");
			}
			else if (fieldItem.getType().equals(FieldType.Option)) {
				if (fieldItem.getValue() == OptionType.Yes) {
					htmlDataColumnContent.append("<span class=\"boldy\"> " + escapeHtmlString(fieldItem.getName()) + "</span> : " + Translator.get(DictKeys.BROWSER_YES) + " <br>");
				} else if (fieldItem.getValue() == OptionType.No) {
					htmlDataColumnContent.append("<span class=\"boldy\"> " + escapeHtmlString(fieldItem.getName()) + "</span> : " + Translator.get(DictKeys.BROWSER_NO) + " <br>");
				} else {
					htmlDataColumnContent.append("<span class=\"boldy\"> " + escapeHtmlString(fieldItem.getName()) + "</span> : " + Translator.get(DictKeys.BROWSER_UNKNOWN) + " <br>");
				}
			} 
			else if (fieldItem.getType().equals(FieldType.Date)) {
				java.sql.Date sqlDate = fieldItem.getValue();

				java.util.Date utilDate = new java.util.Date(sqlDate.getTime());

				SimpleDateFormat f = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.LONG, ApplicationSettingsManager.getUserDefinedLocale());

				htmlDataColumnContent.append("<span class=\"boldy\"> " + escapeHtmlString(fieldItem.getName()) + "</span> : " + f.format(utilDate) + "<br>");
			} else {
				htmlDataColumnContent.append("<span class=\"boldy\"> " + escapeHtmlString(fieldItem.getName()) + "</span> : " + escapeHtmlString(fieldItem.getValue().toString()) + "<br>");
			}
		}	

		if (showButtons) {
			htmlDataColumnContent.append("<form>");
			htmlDataColumnContent.append("<input type=\"button\" onclick=parent.location.href=\"show:///updateComposite=" + id + "\" value=\"" + Translator.get(DictKeys.BROWSER_UPDATE) + "\">");
			htmlDataColumnContent.append("<input type=\"button\" onclick=parent.location.href=\"show:///deleteComposite=" + id + "\" value=\"" + Translator.get(DictKeys.BROWSER_DELETE) + "\">");
			htmlDataColumnContent.append("</form>");
		}

		albumItemTableRowHtml.append("<tr id=\"albumId" + id + "\"><td>" + htmlPictureColumnContent + "</td><td width=90% bgcolor=" + getBackgroundColorOfWidgetInHex() + ">" + htmlDataColumnContent + "</td></tr><tr><td height=\"20\" colspan=\"2\"></td></tr>");		
	}

	public static void loadHtmlPage(Browser browser, InputStream fileInputStream) {
		browser.setText(FileSystemAccessWrapper.readInputStreamIntoString(fileInputStream));
	}

	// TODO initialize one time only
	public static String getBackgroundColorOfWidgetInHex() {
		return  Integer.toHexString((Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND).getRed()))
				+ Integer.toHexString((Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND).getGreen()))
				+ Integer.toHexString((Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND).getBlue()));
	}

	// TODO initialize one time only
	public static String getDefaultSystemFont() {
		return Display.getCurrent().getSystemFont().getFontData()[0].getName();
	}

	public static void loadWelcomePage() {
		String welcomePage = "";

		welcomePage += "<html width=100% height=80%>";
		welcomePage += "<body width=100% height=80%>";
		welcomePage += "	<font face=\"" + getDefaultSystemFont() + "\">";
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
		welcomePage += "	</font>";
		welcomePage += "</body>";
		welcomePage += "</html>";

		Collector.getAlbumItemSWTBrowser().setText(welcomePage);
	}

	public static String generateAlbumInformation() {
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

	public static String generateFavorites() {
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
	
	public static String escapeHtmlString(String htmlString) {
		String escapedString = htmlString;
		
		escapedString = escapedString.replace("&", "&amp;");
		escapedString = escapedString.replace("<", "&lt;");
		escapedString = escapedString.replace(">", "&gt;");

		return escapedString;
	}

	public static void generatAlbumItemUpdatedPage(long albumItemId) {
		String javaScript = "<script src=\"file://" + FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA + File.separatorChar + "effects.js" + "\"></script>";
		String styleCSS = "<link rel=stylesheet href=\"file://"+ FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA + File.separatorChar + "style.css" + "\"></link>";

		AlbumItem addedAlbumItem = DatabaseWrapper.fetchAlbumItem(Collector.getSelectedAlbum(), albumItemId);

		if (addedAlbumItem != null) {
			String addedItemHtml = getAlbumItemTableRowHtml(addedAlbumItem);
		
			String finalPageAsHtml = "<!DOCTYPE HTML><html><head><meta http-equiv=\"X-UA-Compatible\" content=\"IE=9\" >" 
										+ styleCSS + " " + javaScript + "</head><body bgcolor=white><font face=\"" + getDefaultSystemFont() 
										+ "\"><h1>" + Translator.get(DictKeys.BROWSER_ITEM_UPDATED) + "</h1><table id=\"albumItems\" border=0>" + addedItemHtml + "</table></font>" 
										+ "<br><form><input type=\"button\" onclick=parent.location.href=\"show:///showDetailsViewOfAlbum\" value=\"" 
										+ Translator.get(DictKeys.BROWSER_BACK_TO_ALBUM) + "\"></form></body></html>";
	
			Collector.getAlbumItemSWTBrowser().setText(finalPageAsHtml);
		}
	}

	public static void generateAlbumItemAddedPage(long albumItemId) {
		String javaScript = "<script src=\"file://" + FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA + File.separatorChar + "effects.js" + "\"></script>";
		String styleCSS = "<link rel=stylesheet href=\"file://"+ FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA + File.separatorChar + "style.css" + "\"></link>";

		AlbumItem addedAlbumItem = DatabaseWrapper.fetchAlbumItem(Collector.getSelectedAlbum(), albumItemId);

		if (addedAlbumItem != null) {
			String addedItemHtml = getAlbumItemTableRowHtml(addedAlbumItem);
		
			String finalPageAsHtml = "<!DOCTYPE HTML><html><head><meta http-equiv=\"X-UA-Compatible\" content=\"IE=9\" >" 
										+ styleCSS + " " + javaScript + "</head><body bgcolor=white><font face=\"" + getDefaultSystemFont() 
										+ "\"><h1>" + Translator.get(DictKeys.BROWSER_ITEM_ADDED) + "</h1><table id=\"albumItems\" border=0>" + addedItemHtml + "</table></font>" 
										+ "<br><form><input type=\"button\" onclick=parent.location.href=\"show:///showDetailsViewOfAlbum\" value=\"" 
										+ Translator.get(DictKeys.BROWSER_BACK_TO_ALBUM) + "\"></form></body></html>";
	
			Collector.getAlbumItemSWTBrowser().setText(finalPageAsHtml);
		}
	}
}
