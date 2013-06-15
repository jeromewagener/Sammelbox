package collector.desktop.gui.browser;

import java.util.List;

import collector.desktop.Collector;
import collector.desktop.album.AlbumItem;
import collector.desktop.album.AlbumItem.AlbumItemPicture;
import collector.desktop.album.FieldType;
import collector.desktop.album.ItemField;
import collector.desktop.database.DatabaseWrapper;
import collector.desktop.database.exceptions.FailedDatabaseWrapperOperationException;
import collector.desktop.gui.composites.StatusBarComposite;
import collector.desktop.internationalization.DictKeys;
import collector.desktop.internationalization.Translator;

public class PictureViewCreator {
	static void showPicture(String pathToPicture, long albumItemId) {
		StatusBarComposite.getInstance(Collector.getShell()).writeStatus(Translator.get(DictKeys.STATUSBAR_CLICK_TO_RETURN));

		StringBuilder picturePage = new StringBuilder();
		AlbumItem albumItem;
		try {
			albumItem = DatabaseWrapper.fetchAlbumItem(Collector.getSelectedAlbum(), albumItemId);
				
			List<AlbumItemPicture> pictures = null;
			for (ItemField itemField : albumItem.getFields()) {
				if (itemField.getType().equals(FieldType.Picture)) {
					pictures = itemField.getValue();			
					break;
				}
			}
	
			String originalPathToPicture = "";
	
			StringBuilder smallPictures = new StringBuilder();
			if (pictures.size() >= 2) {
				int counter = 1;
	
				for (AlbumItemPicture picture : pictures) {
					originalPathToPicture = picture.getOriginalPicturePath();

					smallPictures.append(
							"<a onMouseover='change(\"bigimg\", \"" + picture.getThumbnailPicturePath() + "\")'>" + 
									"  <img border=\"1\" " +
									"       onMouseOver='this.style.cursor=\"pointer\"' " +
									"       id=\"smallimage" + counter + "\" " +
									"		style=\"width:120px; margin-top:10px;\"" +
									"       src=\"" + picture.getThumbnailPicturePath() + "\">" +
									"</a>" +
							"</br>");

					counter++;
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
					"      <meta " + BrowserConstants.META_PARAMS + ">" + 
					"      <link rel=stylesheet href=\"" + BrowserConstants.STYLE_CSS + "\" />" +
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
		} catch (FailedDatabaseWrapperOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
