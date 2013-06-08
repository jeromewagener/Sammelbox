package collector.desktop.gui.browser;

import java.io.File;
import java.net.URI;
import java.util.List;

import collector.desktop.Collector;
import collector.desktop.album.AlbumItem;
import collector.desktop.album.FieldType;
import collector.desktop.album.ItemField;
import collector.desktop.database.DatabaseWrapper;
import collector.desktop.filesystem.FileSystemAccessWrapper;
import collector.desktop.gui.composites.StatusBarComposite;
import collector.desktop.gui.various.ComponentFactory;
import collector.desktop.internationalization.DictKeys;
import collector.desktop.internationalization.Translator;

public class PictureViewCreator {
	static void showPicture(String pathToPicture, long albumItemId) {
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
}
