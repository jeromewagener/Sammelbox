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

import java.io.File;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.ImageTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Widget;
import org.sammelbox.controller.GuiController;
import org.sammelbox.controller.events.EventObservable;
import org.sammelbox.controller.events.SammelboxEvent;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.model.album.AlbumItemPicture;
import org.sammelbox.view.ApplicationUI;
import org.sammelbox.view.various.ComponentFactory;

public class ImageDropAndManagementComposite extends Composite implements DropTargetListener {
	private static final String[] ALLOWED_EXTENSIONS = new String[] { ".jpeg", ".jpg", ".png", ".gif", ".bmp", ".ico", ".tiff" };

	private static final double PERCENTAGE_DIVISOR = 100.0;
	private static final int MAX_HEIGHT_OR_WIDTH_IN_PIXELS = 100;
	private static final int IMAGE_LIST_HEIGHT_IN_PIXELS = 130;
	private static final int NUMBER_OF_IMAGE_LIST_COLUMNS = 4;
	
	private Label dropTextLabel;
	
	/** A list of images pointing to copies of the original files, located within the corresponding album folder */
	private LinkedList<AlbumItemPicture> pictures = new LinkedList<AlbumItemPicture>();
	/** An inner composite presenting the pictures */
	private Composite imageComposite;

	/** Creates a new ImageDropAndManagementComposite as a child of the provided parent composite 
	 * @param parentComposite the parent composite of the ImageDropAndManagementComposite */
	public ImageDropAndManagementComposite(Composite parentComposite) {
		super(parentComposite, SWT.NONE);
		this.initialize();
	}

	/** Creates a new ImageDropAndManagementComposite as a child of the provided parent composite. 
	 * Images can be provided for inclusion upon creation.
	 * @param parentComposite the parent composite of the ImageDropAndManagementComposite 
	 * @param pictures a list of pictures pointing to images that should be included within the imageComposite */
	public ImageDropAndManagementComposite(Composite parentComposite, List<AlbumItemPicture> pictures) {	
		super(parentComposite, SWT.NONE);
		this.initialize();		

		for (AlbumItemPicture picture : pictures) {
			this.pictures.addLast(picture);
		}

		this.refreshImageComposite();
	}

	/** Initializes the ImageDropAndManagementComposite by setting default sizes, labels etc..*/
	private void initialize() {
		this.setLayout(new GridLayout(1, false));
		this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		dropTextLabel = new Label(this, SWT.BORDER | SWT.CENTER | SWT.VERTICAL);
		dropTextLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		dropTextLabel.setText(Translator.get(DictKeys.LABEL_DROP_IMAGE_HERE));
		dropTextLabel.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent mouseEvent) {
				// cannot be used since this could be a drag event
			}
			
			@Override
			public void mouseDown(MouseEvent arg0) {
				FileDialog openFileDialog = new FileDialog(ApplicationUI.getShell(), SWT.MULTI);
				openFileDialog.setText(Translator.get(DictKeys.DIALOG_SELECT_IMAGES));
				openFileDialog.setFilterPath(System.getProperty("user.home"));
				String[] filterExt = { "*.JPEG;*.jpeg;*.JPG;*.jpg;*.PNG;*.png;*.GIF;*.gif;*.BMP;*.bmp;*.ICO;*.ico;*.TIFF;*.tiff" };
				openFileDialog.setFilterExtensions(filterExt);

				String firstFile = openFileDialog.open();
				if (firstFile != null) {
					String selectionFolder = new File(firstFile).getParent();
					String[] filenames = openFileDialog.getFileNames();
					String[] filepaths = new String[filenames.length];

					for (int i=0; i<filenames.length; i++) {
						filepaths[i] = selectionFolder + File.separatorChar + filenames[i];
					}
					
					ImageProcessingThread imageProcessingThread = new ImageProcessingThread(filepaths);
					imageProcessingThread.start();
				}
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent arg0) {}
		});
		addDropSupport(dropTextLabel);

		ScrolledComposite imageScrolledComposite = new ScrolledComposite(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL );
		imageScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		imageComposite = new Composite(imageScrolledComposite, SWT.NONE);
		imageComposite.setLayout(new GridLayout(NUMBER_OF_IMAGE_LIST_COLUMNS, false));
		imageComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		imageScrolledComposite.setContent(imageComposite);

		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.heightHint = IMAGE_LIST_HEIGHT_IN_PIXELS;

		imageScrolledComposite.setLayoutData(gridData);

		addDropSupport(imageScrolledComposite);
		
		addDisposeListener(new DisposeListener() {			
			@Override
			public void widgetDisposed(DisposeEvent arg0) {
				disposeAllChildren();				
			}
		});
	}

	/** This method adds drop (drag & drop) support to a given SWT widget 
	 * @param widget an SWT widget such as a label or a composite */
	private void addDropSupport(final Widget widget) {
		int ops = DND.DROP_COPY | DND.DROP_LINK | DND.DROP_DEFAULT;
		final FileTransfer fTransfer = FileTransfer.getInstance();
		final ImageTransfer iTransfer = ImageTransfer.getInstance();
		Transfer[] transfers = new Transfer[] { fTransfer, iTransfer };
		

		DropTarget target = new DropTarget((Control) widget, ops);
		target.setTransfer(transfers);
		target.addDropListener(this);
	}

	/** This method refreshes the ImageDropAndManagementComposite in a sense that the image composite is completely rebuild. This method
	 * should be called after the creation of a ImageDropAndManagementComposite when pictures are provided, or in case a new picture 
	 * has been added to the picture list */
	public final void refreshImageComposite() {
		disposeAllChildren();

		for (final AlbumItemPicture picture : pictures) {			
			Image originalImage = new Image(Display.getCurrent(), picture.getOriginalPicturePath());
			Image scaledImage;
			
			Rectangle originalImageBounds =  originalImage.getBounds();

			int originalWidth = originalImageBounds.width;
			int originalHeight = originalImageBounds.height;
			
			if (originalWidth < originalHeight) {
				scaledImage = ImageDropAndManagementComposite.resize(originalImage,
						(int) Math.round(originalWidth / ((double)(originalHeight / PERCENTAGE_DIVISOR))), 
						MAX_HEIGHT_OR_WIDTH_IN_PIXELS);
			} else {
				scaledImage = ImageDropAndManagementComposite.resize(originalImage,
						MAX_HEIGHT_OR_WIDTH_IN_PIXELS, 
						(int) Math.round(originalHeight / ((double)(originalWidth/ PERCENTAGE_DIVISOR))));
			}
			
			originalImage.dispose();
			final Label pictureLabel = new Label(imageComposite, SWT.NONE);
			pictureLabel.setImage(scaledImage);
			pictureLabel.addDisposeListener(new DisposeListener() {				
				@Override
				public void widgetDisposed(DisposeEvent disposeEvent) {
					pictureLabel.getImage().dispose();
				}
			});
			

			Button deleteButton = new Button(imageComposite, SWT.NONE);
			deleteButton.setText(Translator.get(DictKeys.BUTTON_REMOVE));
			deleteButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent selectionEvent) {
					pictures.remove(picture);
					refreshImageComposite();
				}
			});					
			
			InputStream istream = this.getClass().getClassLoader().getResourceAsStream("graphics/arrow-up.png");
			Image arrowUp = new Image(Display.getCurrent(),istream);

			Button upButton = new Button(imageComposite, SWT.NONE);
			upButton.setImage(arrowUp);
			upButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					int index = pictures.indexOf(picture);

					if (index > 0) {					
						AlbumItemPicture tmpPicture = picture;

						pictures.remove(picture);
						pictures.add(index - 1, tmpPicture);

						refreshImageComposite();
					}
				}
			});
			
			istream = this.getClass().getClassLoader().getResourceAsStream("graphics/arrow-down.png");
			Image arrowDown = new Image(Display.getCurrent(),istream);
			
			Button downButton = new Button(imageComposite, SWT.NONE);
			downButton.setImage(arrowDown);
			downButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					int index = pictures.indexOf(picture);

					if (index < pictures.size() - 1) {
						AlbumItemPicture tmpPicture = picture;

						pictures.remove(picture);
						pictures.add(index + 1, tmpPicture);

						refreshImageComposite();
					}
				}
			});
		}

		imageComposite.pack();
		imageComposite.layout();
	}

	/** Return all images 
	 * @return the images */
	public List<AlbumItemPicture> getAllPictures() {		
		return pictures;
	}

	@Override
	public void dragEnter(DropTargetEvent event) {
		// Changing the event detail to drop_copy enables the drop. 
		event.detail = DND.DROP_COPY;
	}
	@Override
	public void dragLeave(DropTargetEvent arg0) {	
	}

	@Override
	public void dragOperationChanged(DropTargetEvent arg0) {		
	}

	@Override
	public void dragOver(DropTargetEvent arg0) {
	}

	@Override
	public void drop(DropTargetEvent event) {				
		if (!ApplicationUI.isAlbumSelectedAndShowMessageIfNot()) {
			return;
		}
		
		if (event.data instanceof String[]) {
			String[] filepaths = (String[]) event.data;
			
			ImageProcessingThread imageProcessingThread = new ImageProcessingThread(filepaths);
			imageProcessingThread.start();
		}
	}

	@Override
	public void dropAccept(DropTargetEvent arg0) {	
	}
	
	/** This method displays a message box informing the user of trying to drop the unsupported file */
	public void showDroppedUnsupportedFileMessageBox(String filePathToUnsupportedFile) {
	    ComponentFactory.getMessageBox(Translator.get(DictKeys.DIALOG_TITLE_INVALID_IMAGE_FILE_FORMAT), 
	    		Translator.get(DictKeys.DIALOG_CONTENT_INVALID_IMAGE_FILE_FORMAT, new File(filePathToUnsupportedFile).getName()), 
	    		SWT.ICON_ERROR).open();
	}
	
	public void disposeAllChildren() {
		for (Control control : imageComposite.getChildren()) {
			control.dispose();			
		}
	}
	
	public static Image resize(Image image, int width, int height) {
		Image scaled = new Image(Display.getDefault(), width, height);
		GC gc = new GC(scaled);
		gc.setAntialias(SWT.ON);
		gc.setInterpolation(SWT.HIGH);
		gc.drawImage(image, 0, 0, image.getBounds().width, image.getBounds().height, 0, 0, width, height);
		gc.dispose();
		return scaled;
	}
	
	private class ImageProcessingThread extends Thread {
		private final String[] filepaths;
		private final Font originalFont;
		private final Font boldFont;
		
		public ImageProcessingThread(String[] filepaths) {
			this.filepaths = filepaths;
			
			FontData fontData = dropTextLabel.getFont().getFontData()[0];
			originalFont = dropTextLabel.getFont();
			boldFont = new Font(ApplicationUI.getShell().getDisplay(), new FontData(fontData.getName(), fontData.getHeight(), SWT.BOLD));
		}
		
		@Override
		public void run() {
			ApplicationUI.getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					EventObservable.addEventToQueue(SammelboxEvent.DISABLE_SAMMELBOX);
				}
			});
					
			if (filepaths.length > 0) {
				for (int i = 0; i < filepaths.length; i++) {
					final int current = i + 1;
					ApplicationUI.getShell().getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							dropTextLabel.setText(Translator.get(DictKeys.LABEL_PROCESSING_IMG_X_OF_Y, current, filepaths.length));
							dropTextLabel.setFont(boldFont);
						}
					});
					
					final String fileName = filepaths[i];

					boolean processFile = false;
					for (String allowedExtension : ALLOWED_EXTENSIONS) {
						if (fileName.toLowerCase().endsWith(allowedExtension)) {
							processFile = true;
							break;
						}
					}
					
					if (processFile) {
						AlbumItemPicture picture = ImageManipulator.adaptAndStoreImageForCollector(
								new File(fileName), GuiController.getGuiState().getSelectedAlbum());
						if (picture == null) {
							ApplicationUI.getShell().getDisplay().asyncExec(new Runnable() {
								@Override
								public void run() {
									showDroppedUnsupportedFileMessageBox(fileName);
								}
							});
						} else {
							pictures.add(picture);
						}
					} else {
						ApplicationUI.getShell().getDisplay().asyncExec(new Runnable() {
							@Override
							public void run() {
								showDroppedUnsupportedFileMessageBox(fileName);
							}
						});
					}
				}
				
				ApplicationUI.getShell().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						refreshImageComposite();
					}
				});
			}
			
			ApplicationUI.getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					dropTextLabel.setText(Translator.get(DictKeys.LABEL_DROP_IMAGE_HERE));
					dropTextLabel.setFont(originalFont);
					
					EventObservable.addEventToQueue(SammelboxEvent.ENABLE_SAMMELBOX);
				}
			});
		}
	}
}
