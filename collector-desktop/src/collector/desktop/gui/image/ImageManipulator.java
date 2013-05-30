package collector.desktop.gui.image;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

import collector.desktop.filesystem.FileSystemAccessWrapper;

public class ImageManipulator {
	/** The maximum height of a picture in pixels. Pictures with a higher resolution will be resized */
	private final static int MAX_HEIGHT_IN_PIXELS = 200;
	/** The maximum width of a picture in pixels. Pictures with a higher resolution will be resized */
	private final static int MAX_WIDTH_IN_PIXELS = 200;

	/** This method is used to resize a given image (if larger than MAX_HEIGHT or MAX_WIDTH) and to store this image using a unique name 
	 * within the provided album.
	 * @param originalImageURI the URI of the original image 
	 * @param album the album to which the image should be assigned 
	 * @return a URI pointing to the location of the picture within the album */	
	public static List<URI> adaptAndStoreImageForCollector(URI originalImageURI, String album) {
		try {
			Image image = new Image(Display.getCurrent(), originalImageURI.getPath());
			
			double imageWidth = (double) image.getImageData().width;
			double imageHeight = (double) image.getImageData().height;
			
			String identifier = UUID.randomUUID().toString() + "_" + Long.toString(System.currentTimeMillis());
			String newFileName = identifier + ".png";
			String newFileNameOriginal = identifier + "_original." +  FileSystemAccessWrapper.getFileExtension(originalImageURI.getPath());
			String newLocation = FileSystemAccessWrapper.getFilePathForAlbum(album) + File.separatorChar + newFileName;
			String newLocationOriginal = FileSystemAccessWrapper.getFilePathForAlbum(album) + File.separatorChar + newFileNameOriginal;
			
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

				image = new Image(Display.getCurrent(), image.getImageData().scaledTo(newWidth, newHeight));			
			}

			ImageLoader imageLoader = new ImageLoader();
			imageLoader.data = new ImageData[] { image.getImageData() };
			imageLoader.save(newLocation, SWT.IMAGE_PNG);

			try {
				FileSystemAccessWrapper.copyFile(new File(originalImageURI.getPath()), new File(newLocationOriginal));
			} catch (IOException e) {
				// TODO Log the exception message into the log.
				e.printStackTrace();
			}
			
			List<URI> newAndOriginalImage = new ArrayList<URI>(2);
			newAndOriginalImage.add(new File(newLocation).toURI());
			newAndOriginalImage.add(new File(newLocationOriginal).toURI());
			
			return newAndOriginalImage;
		} catch (SWTException swte) {
			// TODO do something if format not supported is.. (YODA) Log the exception message into the log.
			System.err.println("ImageManipulator.adaptAndStoreImageForCollector() - "+swte.getMessage());
			return null;
		}
	}
}
