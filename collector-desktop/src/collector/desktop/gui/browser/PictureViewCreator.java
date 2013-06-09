package collector.desktop.gui.browser;

import java.io.File;
import java.net.URI;
import java.util.List;

import collector.desktop.Collector;
import collector.desktop.album.AlbumItem;
import collector.desktop.album.FieldType;
import collector.desktop.album.ItemField;
import collector.desktop.database.DatabaseWrapper;
import collector.desktop.gui.composites.StatusBarComposite;
import collector.desktop.internationalization.DictKeys;
import collector.desktop.internationalization.Translator;

public class PictureViewCreator {
	static void showPicture(String pathToPicture, long albumItemId) {
		StatusBarComposite.getInstance(Collector.getShell()).writeStatus(Translator.get(DictKeys.STATUSBAR_CLICK_TO_RETURN));

		StringBuilder picturePage = new StringBuilder();
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
				if (uri.toString().contains("original")) { // TODO use new picture table ASAP
					if (uri.toString().contains(imageId)) {
						originalPathToPicture = uri.toString();
					}

					smallPictures.append(
							"<a onMouseover='change(\"bigimg\", \"" + uri.toString() + "\")'>" + 
					        "  <img border=\"1\" " +
					        "       onMouseOver='this.style.cursor=\"pointer\"' " +
					        "       id=\"smallimage" + counter + "\" " +
					        "		style=\"width:120px; margin-top:10px;\"" +
					        "       src=\"" + uri.toString() + "\">" +
					        "</a>" +
					        "</br>");

					counter++;
				}
			}

			smallPictures.append("<br>" +
					             "<form>" +
					             "  <input type='button' " +
					             "         onclick=\"parent.location.href='show:///lastPage'\" " +
					             "         value='Go Back'>" +
					             "</form>");
		}

		picturePage.append(
				"<html>" +
                "  <head>" +
				"      <meta" + BrowserConstants.IE_META_PARAMS + ">" + 
				"      <link rel=stylesheet href=\"" + BrowserConstants.STYLE_CSS + "\"></link>" +
				"      <script src=\"" + BrowserConstants.EFFECTS_JS + "\"></script>" +
		        "  </head>" +
		        "  <body>" +
		        "    <table>" +
		        "      <tr>" +
		        "        <td align=\"center\" valign=\"top\">" +
		                   smallPictures.toString() +		
		        "        </td>" +
		        "        <td align=\"left\" valign=\"top\">" +
		        "          <img style=\"max-width: 100%; max-height: 100%;\" " +
		        "               id=\"bigimg\" src=\"" + originalPathToPicture + "\" " +
		        "               onMouseOver=\"changeCursorToHand('bigimg')\" " +
		        "               onclick=\"parent.location.href='show:///lastPage'\">" +
		        "        </td>" +
		        "      </tr>" +
		        "    </table>" +
		        "  </body>" +
		        "</html>");

		Collector.getAlbumItemSWTBrowser().setText(picturePage.toString());
	}
}
