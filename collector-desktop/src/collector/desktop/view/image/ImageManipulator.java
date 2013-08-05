/** -----------------------------------------------------------------
 *    Sammelbox: Collection Manager - A free and open-source collection manager for Windows & Linux
 *    Copyright (C) 2011 Jérôme Wagener & Paul Bicheler
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ** ----------------------------------------------------------------- */

package collector.desktop.view.image;

import java.io.File;
import java.util.UUID;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import collector.desktop.controller.filesystem.FileSystemAccessWrapper;
import collector.desktop.model.album.AlbumItemPicture;
import collector.desktop.view.ApplicationUI;

public class ImageManipulator {
	private final static Logger LOGGER = LoggerFactory.getLogger(ApplicationUI.class);
	
	/** The maximum height of a thumb nail in pixels. Only originals with a higher resolution will be resized */
	private final static int MAX_HEIGHT_IN_PIXELS = 200;
	/** The maximum width of a thumb nail in pixels. Only originals with a higher resolution will be resized */
	private final static int MAX_WIDTH_IN_PIXELS = 200;
	
	/** This method is used to copy originals, and create thumb nails, within the picture folder
	 * @param pictureFile the original image
	 * @param album the album to which the image should be assigned 
	 * @return a picture pointing to the location of the original file and thumb nail within the album */	
	public static AlbumItemPicture adaptAndStoreImageForCollector(File pictureFile, String album) {
		try {
			Image thumbnailImage = new Image(Display.getCurrent(), pictureFile.getCanonicalPath());
			
			double imageWidth = (double) thumbnailImage.getImageData().width;
			double imageHeight = (double) thumbnailImage.getImageData().height;
			
			String identifierForOriginal = UUID.randomUUID().toString();
			String identifierForThumbnail = UUID.randomUUID().toString();
			
			String newFileNameForOriginal = identifierForOriginal + "." + FileSystemAccessWrapper.getFileExtension(pictureFile.getName());
			String newFileNameForThumbnail = identifierForThumbnail + ".png";
			
			String newFileLocationForOriginal = FileSystemAccessWrapper.getFilePathForAlbum(album) + File.separatorChar + newFileNameForOriginal;
			String newFileLocationForThumbnail = FileSystemAccessWrapper.COLLECTOR_HOME_THUMBNAILS_FOLDER + File.separatorChar + newFileNameForThumbnail;
			
			
			if (imageWidth > MAX_WIDTH_IN_PIXELS || imageHeight > MAX_HEIGHT_IN_PIXELS) {
				int newWidth = 0, newHeight = 0;
				double imageRatio = 0.0;

				if (imageWidth >= imageHeight) {
					imageRatio = imageWidth / imageHeight;
					newWidth = MAX_WIDTH_IN_PIXELS;
					newHeight = (int) (MAX_WIDTH_IN_PIXELS / imageRatio);
				} else {
					imageRatio = imageHeight / imageWidth;
					newHeight = MAX_HEIGHT_IN_PIXELS;
					newWidth = (int) (MAX_HEIGHT_IN_PIXELS / imageRatio);
				}

				thumbnailImage = new Image(Display.getCurrent(), thumbnailImage.getImageData().scaledTo(newWidth, newHeight));			
			}

			ImageLoader imageLoader = new ImageLoader();
			imageLoader.data = new ImageData[] { thumbnailImage.getImageData() };
			imageLoader.save(newFileLocationForThumbnail, SWT.IMAGE_PNG);

			FileSystemAccessWrapper.copyFile(new File(pictureFile.getPath()), new File(newFileLocationForOriginal));
						
			return new AlbumItemPicture(newFileNameForThumbnail, newFileNameForOriginal, album, AlbumItemPicture.PICTURE_ID_UNDEFINED);
			
		} catch (Exception ex) {
			LOGGER.error("An error occured while manipulating an image", ex);
		}
		
		return null;
	}
}
