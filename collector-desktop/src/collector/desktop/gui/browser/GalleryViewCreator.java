package collector.desktop.gui.browser;

import java.net.URI;
import java.util.List;

import org.eclipse.swt.browser.Browser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import collector.desktop.album.AlbumItem;
import collector.desktop.album.FieldType;
import collector.desktop.album.ItemField;
import collector.desktop.database.AlbumItemStore;
import collector.desktop.filesystem.FileSystemAccessWrapper;

public class GalleryViewCreator {
	private final static Logger LOGGER = LoggerFactory.getLogger(GalleryViewCreator.class);
	
	static void showOverviewAlbum(Browser browser) {
		StringBuilder galleryItemHtmlBuilder = new StringBuilder();
		
		for (AlbumItem albumItem : AlbumItemStore.getAlbumItems(AlbumItemStore.getStopIndex())) {
			String picturePath = "";
			long id = 0;

			for (ItemField fieldItem : albumItem.getFields()) {				
				if (fieldItem.getType().equals(FieldType.UUID)) {
					// schema or content version UUID --> ignore
				} else if (fieldItem.getType().equals(FieldType.ID)) {
					if (!fieldItem.getName().equals("typeinfo")) {
						// do not show, but store id
						id = fieldItem.getValue();
					} else {
						LOGGER.warn("Found a field type that wasn't expected: " + fieldItem.getName());
					}
				} else if (fieldItem.getType().equals(FieldType.Picture)) {
					List<URI> uris = fieldItem.getValue();
					picturePath = FileSystemAccessWrapper.PLACEHOLDERIMAGE;

					for (URI uri : uris) {
						// find and return first thumb nail
						if (!uri.toString().contains("original")) { // TODO use new picture table system ASAP
							picturePath = uri.toString();
							break;
						}
					}
				}
			}		

			galleryItemHtmlBuilder.append("<div id=\"imageId" + id + "\" " +
					                      "     class=\"pictureContainer\" " +
					                      "     onMouseOver=\"parent.location.href=&quot;show:///details=" + id + "&quot;\" " +
					                      "		onClick=\"parent.location.href=&quot;show:///detailsComposite=" + id + "&quot;\">");
			galleryItemHtmlBuilder.append("  <div class=\"innerPictureContainer\">");
			galleryItemHtmlBuilder.append("    <img src=\"" + picturePath + "\">");
			galleryItemHtmlBuilder.append("  </div>");
			galleryItemHtmlBuilder.append("</div>");
		}

		String finalPageAsHtml = "<!DOCTYPE HTML>" +
								 "  <html>" +
								 "    <head>" +
								 "      <meta " + BrowserConstants.META_PARAMS + ">" + 
								 "      <link rel=stylesheet href=\"" + BrowserConstants.STYLE_CSS + "\" />" +
								 "      <script src=\"" + BrowserConstants.EFFECTS_JS + "\"></script>" +
								 "    </head>" +
								 "    <body>" +
								 "      <font face=\"" + Utilities.getDefaultSystemFont() + "\"><div id=\"albumItems\">" +
								          galleryItemHtmlBuilder.toString() +
								 "      </font>" +
								 "    </body>";

		browser.setText(finalPageAsHtml);
		Utilities.setLastPageAsHtml(finalPageAsHtml);
	}
}
