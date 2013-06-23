package collector.desktop.gui.browser;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import collector.desktop.Collector;
import collector.desktop.album.AlbumItem.AlbumItemPicture;
import collector.desktop.database.DatabaseWrapper;
import collector.desktop.database.exceptions.DatabaseWrapperOperationException;
import collector.desktop.database.exceptions.ExceptionHelper;
import collector.desktop.gui.GuiConstants;
import collector.desktop.gui.composites.StatusBarComposite;
import collector.desktop.internationalization.DictKeys;
import collector.desktop.internationalization.Translator;

public class PictureViewCreator {
	private static final Logger LOGGER = LoggerFactory.getLogger(PictureViewCreator.class);
	
	static void showPicture(long albumItemId) {
		StatusBarComposite.getInstance(Collector.getShell()).writeStatus(Translator.get(DictKeys.STATUSBAR_CLICK_TO_RETURN));

		StringBuilder picturePage = new StringBuilder();
		try {
			List<AlbumItemPicture> pictures = DatabaseWrapper.getAlbumItemPictures(Collector.getSelectedAlbum(), albumItemId);
			
			String originalPathToPicture = "";
			if (pictures.size() >= 1) {
				originalPathToPicture = pictures.get(0).getOriginalPicturePath();
			}
	
			StringBuilder smallPictures = new StringBuilder();
			if (pictures.size() >= 2) {
				int counter = 1;
	
				for (AlbumItemPicture picture : pictures) {
					smallPictures.append(
							"<a onMouseover='change(\"bigimg\", \"" + picture.getOriginalPicturePath() + "\")'>" + 
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
					"      <meta " + GuiConstants.META_PARAMS + ">" + 
					"      <link rel=stylesheet href=\"" + GuiConstants.STYLE_CSS + "\" />" +
					"      <script src=\"" + GuiConstants.EFFECTS_JS + "\"></script>" +
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
		} catch (DatabaseWrapperOperationException ex) {
			LOGGER.error("An error occured while fetching the album item #" + albumItemId + " in the album: " + 
					Collector.getSelectedAlbum() + " \n Stacktrace:" + ExceptionHelper.toString(ex));
		}
	}
}
