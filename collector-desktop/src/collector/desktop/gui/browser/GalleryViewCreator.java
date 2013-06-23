package collector.desktop.gui.browser;

import java.util.List;

import org.eclipse.swt.browser.Browser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import collector.desktop.Collector;
import collector.desktop.album.AlbumItem;
import collector.desktop.album.AlbumItem.AlbumItemPicture;
import collector.desktop.album.FieldType;
import collector.desktop.album.ItemField;
import collector.desktop.database.AlbumItemStore;
import collector.desktop.database.DatabaseWrapper;
import collector.desktop.database.exceptions.DatabaseWrapperOperationException;
import collector.desktop.database.exceptions.ExceptionHelper;
import collector.desktop.filesystem.FileSystemAccessWrapper;
import collector.desktop.gui.GuiConstants;

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
					}
				}
			}		

			List<AlbumItemPicture> pictures = null;
			try {
				pictures = DatabaseWrapper.getAlbumItemPictures(Collector.getSelectedAlbum(), id);
			} catch (DatabaseWrapperOperationException ex) {
				LOGGER.error("An error occured while retrieving the pictures associated with the album item #'" + 
					id + "' from the album '" + Collector.getSelectedAlbum() + "' \n Stacktrace: " + ExceptionHelper.toString(ex));
			}
			
			if (pictures == null || pictures.isEmpty()) {
				picturePath = FileSystemAccessWrapper.PLACEHOLDERIMAGE;
			} else {
				picturePath = pictures.get(0).getThumbnailPicturePath();
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
								 "      <meta " + GuiConstants.META_PARAMS + ">" + 
								 "      <link rel=stylesheet href=\"" + GuiConstants.STYLE_CSS + "\" />" +
								 "      <script src=\"" + GuiConstants.EFFECTS_JS + "\"></script>" +
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
