package org.sammelbox.view.sidepanes;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.model.album.AlbumItemPicture;
import org.sammelbox.view.image.ImageDropAndManagementComposite;
import org.sammelbox.view.various.ComponentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public final class ImageViewerSidepane extends Composite {
	private static final Logger LOGGER = LoggerFactory.getLogger(ImageViewerSidepane.class);
	
	private static final int DEFAULT_SEPARATOR_HEIGHT_IN_PIXELS = 10;
	private static final int NUMBER_OF_IMAGE_LIST_COLUMNS = 1;
	private static final double PERCENTAGE_DIVISOR = 180.0;
	private static final int MAX_HEIGHT_OR_WIDTH_IN_PIXELS = 180;
	
	private List<AlbumItemPicture> pictures;
	
	public ImageViewerSidepane(Composite parentComposite, List<AlbumItemPicture> pictures) {
		super(parentComposite, SWT.NONE);
		
		this.pictures = pictures;
		this.initialize();
	}
	
	private void initialize() {
		this.setLayout(new GridLayout(1, false));
		this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		ComponentFactory.getPanelHeaderComposite(this, Translator.get(DictKeys.LABEL_IMAGES));
		
		GridData showImageBrowserButtonGridData = new GridData();
		showImageBrowserButtonGridData.grabExcessHorizontalSpace = true;
		
		ScrolledComposite imageScrolledComposite = new ScrolledComposite(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL );
		imageScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		// min height griddata
		GridData minHeightGridData = new GridData(GridData.FILL_BOTH);
		minHeightGridData.minimumHeight = DEFAULT_SEPARATOR_HEIGHT_IN_PIXELS;

		// separator
		new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(minHeightGridData);
		
		Button showImageBrowser = new Button(this, SWT.NONE);
		showImageBrowser.setText(Translator.get(DictKeys.BUTTON_SHOW_IMAGES_EXTERNALLY));
		showImageBrowser.setLayoutData(showImageBrowserButtonGridData);
		showImageBrowser.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				for (int i=0; i<pictures.size(); i++) {
					File imageFile = new File(pictures.get(i).getOriginalPicturePath());
				    Desktop desktop = Desktop.getDesktop();
				    try {
						desktop.open(imageFile);
					} catch (IOException e) {
						LOGGER.error("An error occurred while opening the images in an external image viewer", e);
					}
				}
			}
		});
		
		Composite imageComposite = new Composite(imageScrolledComposite, SWT.NONE);
		imageComposite.setLayout(new GridLayout(NUMBER_OF_IMAGE_LIST_COLUMNS, false));
		imageComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		imageScrolledComposite.setContent(imageComposite);

		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);

		imageScrolledComposite.setLayoutData(gridData);
		
		for (final AlbumItemPicture picture : pictures) {			
			Image originalImage = new Image(Display.getCurrent(), picture.getOriginalPicturePath());
			Image scaledImage;
			
			Rectangle originalImageBounds = originalImage.getBounds();

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
		}
		
		imageComposite.pack();
		imageComposite.layout();
	}
}
