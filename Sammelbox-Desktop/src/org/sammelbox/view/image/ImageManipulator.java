/** -----------------------------------------------------------------
 *    Sammelbox: Collection Manager - A free and open-source collection manager for Windows & Linux
 *    Copyright (C) 2011 Jerome Wagener & Paul Bicheler
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

package org.sammelbox.view.image;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.imaging.ImageFormat;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.sammelbox.controller.filesystem.FileSystemAccessWrapper;
import org.sammelbox.controller.filesystem.FileSystemLocations;
import org.sammelbox.model.album.AlbumItemPicture;
import org.sammelbox.view.ApplicationUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ImageManipulator {
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationUI.class);
	
	/** The maximum height of a thumb nail in pixels. Only originals with a higher resolution will be resized */
	private static final int MAX_HEIGHT_IN_PIXELS = 200;
	/** The maximum width of a thumb nail in pixels. Only originals with a higher resolution will be resized */
	private static final int MAX_WIDTH_IN_PIXELS = 200;
	
	public ImageManipulator() {
	}

	
	/**This method is used to copy originals, and create thumb nails, within the picture folder. 
	 * It uses Apache imaging instead of SWT which seems to leak memory.
	 * @param pictureFile the original image
	 * @param album the album to which the image should be assigned 
	 * @return a picture pointing to the location of the original file and thumb nail within the album */	
	public static AlbumItemPicture adaptAndStoreImageForCollectorUsingApacheImaging(File pictureFile, String album) {
		try {
			BufferedImage thumbnailImage = Imaging.getBufferedImage(pictureFile);
			
			int imageWidth = thumbnailImage.getWidth();
			int imageHeight	= thumbnailImage.getHeight();
			
			String identifierForOriginal = UUID.randomUUID().toString();
			String identifierForThumbnail = UUID.randomUUID().toString();
			
			String newFileNameForOriginal = identifierForOriginal + "." + FileSystemAccessWrapper.getFileExtension(pictureFile.getName());
			String newFileNameForThumbnail = identifierForThumbnail + ".png";
			
			String newFileLocationForOriginal = FileSystemAccessWrapper.getFilePathForAlbum(album) + File.separatorChar + newFileNameForOriginal;
			String newFileLocationForThumbnail = FileSystemLocations.getThumbnailsDir() + File.separatorChar + newFileNameForThumbnail;
			
			
			if (imageWidth > MAX_WIDTH_IN_PIXELS || imageHeight > MAX_HEIGHT_IN_PIXELS) {
				int newWidth = 0, newHeight = 0;
				double imageRatio = 0.0;

				if (imageWidth >= imageHeight) {
					imageRatio = ((double) imageWidth / (double) imageHeight);
					newWidth = MAX_WIDTH_IN_PIXELS;
					newHeight = (int) (MAX_WIDTH_IN_PIXELS / imageRatio);
				} else {
					imageRatio = ((double) imageHeight / (double) imageWidth);
					newHeight = MAX_HEIGHT_IN_PIXELS;
					newWidth = (int) (MAX_HEIGHT_IN_PIXELS / imageRatio);
				}
				Image resizedImage = thumbnailImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
				thumbnailImage = new BufferedImage(newWidth, newHeight, thumbnailImage.getType());
				thumbnailImage.getGraphics().drawImage(resizedImage, 0, 0 , null);
			}

			final ImageFormat format = ImageFormat.IMAGE_FORMAT_PNG;
            final Map<String,Object> optionalParams = new HashMap<String,Object>();
            File thumbnailDestination = new File(newFileLocationForThumbnail); 
            Imaging.writeImage(thumbnailImage, thumbnailDestination, format, optionalParams);
			
			FileSystemAccessWrapper.copyFile(new File(pictureFile.getPath()), new File(newFileLocationForOriginal));
						
			return new AlbumItemPicture(newFileNameForThumbnail, newFileNameForOriginal, album, AlbumItemPicture.PICTURE_ID_UNDEFINED);
			
		} catch (IOException | ImageReadException | ImageWriteException ex) {
			LOGGER.error("An error occured while manipulating an image", ex);
		}
		
		return null;
	}
}
