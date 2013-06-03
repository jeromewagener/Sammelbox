package collector.desktop.gui.browser;

import java.io.File;
import java.net.URI;
import java.util.List;

import org.eclipse.swt.browser.Browser;

import collector.desktop.database.AlbumItem;
import collector.desktop.database.AlbumItemStore;
import collector.desktop.database.FieldType;
import collector.desktop.database.ItemField;
import collector.desktop.filesystem.FileSystemAccessWrapper;

public class GalleryViewCreator {
	static void showOverviewAlbum(Browser browser) {
		StringBuilder htmlBuilder = new StringBuilder();

		htmlBuilder.append("<!DOCTYPE HTML>");

		String javaScript = "<script src=\"file://" + FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA + File.separatorChar + "effects.js" + "\"></script>";
		String styleCSS = "<link rel=stylesheet href=\"file://"+ FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA + File.separatorChar + "style.css" + "\"></link>";

		htmlBuilder.append("<html><head><meta http-equiv=\"X-UA-Compatible\" content=\"IE=9\">" + styleCSS + " " + javaScript + "</head><body><font face=\"" + Utilities.getDefaultSystemFont() + "\"><div id=\"albumItems\">");

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
		Utilities.setLastPageAsHtml(finalPageAsHtml);
	}
}
