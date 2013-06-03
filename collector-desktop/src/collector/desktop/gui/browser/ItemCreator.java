package collector.desktop.gui.browser;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import collector.desktop.database.AlbumItem;
import collector.desktop.database.FieldType;
import collector.desktop.database.ItemField;
import collector.desktop.database.OptionType;
import collector.desktop.filesystem.FileSystemAccessWrapper;
import collector.desktop.internationalization.DictKeys;
import collector.desktop.internationalization.Translator;
import collector.desktop.settings.ApplicationSettingsManager;

public class ItemCreator {
	static String getAlbumItemTableRowHtml(AlbumItem albumItem) {
		return getAlbumItemTableRowHtml(albumItem, true);
	}
	
	static String getAlbumItemTableRowHtml(AlbumItem albumItem, boolean showAddAndUpdateButtons) {
		StringBuilder htmlDataColumnContent = new StringBuilder();
		StringBuilder htmlPictureColumnContent = new StringBuilder();
		StringBuilder albumItemTableRowHtml = new StringBuilder();

		addAlbumItemTableRow(albumItem, htmlDataColumnContent, htmlPictureColumnContent, albumItemTableRowHtml, showAddAndUpdateButtons);

		return albumItemTableRowHtml.toString();
	}

	static String getAlbumItemDivContainerHtml(AlbumItem albumItem) {
		StringBuilder htmlBuilder = new StringBuilder();

		addAlbumItemDivContainer(albumItem, htmlBuilder);

		return htmlBuilder.toString();
	}

	static void addAlbumItemDivContainer(AlbumItem albumItem, StringBuilder htmlBuilder) {
		htmlBuilder.append("<div id=\"imageId" + albumItem.getItemID() + "\" class=\"pictureContainer\" " +
				"onMouseOver=\"parent.location.href=&quot;show:///details=" + albumItem.getItemID() + "&quot;\" onClick=\"parent.location.href=&quot;show:///detailsComposite=" + albumItem.getItemID() + "&quot;\">");

		htmlBuilder.append("<div class=\"innerPictureContainer\">");
		htmlBuilder.append("<img src=\"" + albumItem.getPrimaryThumbnailPicturePath() + "\">");

		htmlBuilder.append("</div>");
		htmlBuilder.append("</div>");
	}

	static void addAlbumItemTableRow(AlbumItem albumItem, StringBuilder htmlDataColumnContent, StringBuilder htmlPictureColumnContent, StringBuilder albumItemTableRowHtml) {
		addAlbumItemTableRow(albumItem, htmlDataColumnContent, htmlPictureColumnContent, albumItemTableRowHtml, true);
	}
	
	static void addAlbumItemTableRow(AlbumItem albumItem, StringBuilder htmlDataColumnContent, StringBuilder htmlPictureColumnContent, StringBuilder albumItemTableRowHtml, boolean showButtons) {
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
					htmlDataColumnContent.append("<span class=\"boldy\"> " + Utilities.escapeHtmlString(fieldItem.getName()) + "</span> : " + Translator.get(DictKeys.BROWSER_YES) + " <br>");
				} else if (fieldItem.getValue() == OptionType.No) {
					htmlDataColumnContent.append("<span class=\"boldy\"> " + Utilities.escapeHtmlString(fieldItem.getName()) + "</span> : " + Translator.get(DictKeys.BROWSER_NO) + " <br>");
				} else {
					htmlDataColumnContent.append("<span class=\"boldy\"> " + Utilities.escapeHtmlString(fieldItem.getName()) + "</span> : " + Translator.get(DictKeys.BROWSER_UNKNOWN) + " <br>");
				}
			} 
			else if (fieldItem.getType().equals(FieldType.Date)) {
				java.sql.Date sqlDate = fieldItem.getValue();

				java.util.Date utilDate = new java.util.Date(sqlDate.getTime());

				SimpleDateFormat f = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.LONG, ApplicationSettingsManager.getUserDefinedLocale());

				htmlDataColumnContent.append("<span class=\"boldy\"> " + Utilities.escapeHtmlString(fieldItem.getName()) + "</span> : " + f.format(utilDate) + "<br>");
			} else {
				htmlDataColumnContent.append("<span class=\"boldy\"> " + Utilities.escapeHtmlString(fieldItem.getName()) + "</span> : " + Utilities.escapeHtmlString(fieldItem.getValue().toString()) + "<br>");
			}
		}	

		if (showButtons) {
			htmlDataColumnContent.append("<form>");
			htmlDataColumnContent.append("<input type=\"button\" onclick=parent.location.href=\"show:///updateComposite=" + id + "\" value=\"" + Translator.get(DictKeys.BROWSER_UPDATE) + "\">");
			htmlDataColumnContent.append("<input type=\"button\" onclick=parent.location.href=\"show:///deleteComposite=" + id + "\" value=\"" + Translator.get(DictKeys.BROWSER_DELETE) + "\">");
			htmlDataColumnContent.append("</form>");
		}

		albumItemTableRowHtml.append("<tr id=\"albumId" + id + "\"><td>" + htmlPictureColumnContent + "</td><td width=90% bgcolor=" + Utilities.getBackgroundColorOfWidgetInHex() + ">" + htmlDataColumnContent + "</td></tr><tr><td height=\"20\" colspan=\"2\"></td></tr>");		
	}
}
