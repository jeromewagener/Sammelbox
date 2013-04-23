package collector.desktop.gui;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Widget;

import collector.desktop.Collector;

public class ImageDropAndManagementComposite extends Composite implements DropTargetListener{
	/** A list of image URIs pointing to already renamed and resized pictures in the corresponding album */
	private LinkedList<URI> imageURIs = new LinkedList<URI>();
	/** An inner composite presenting the pictures */
	private Composite imageComposite;
	/** An inner scrollable composite wrapping the imageComposite */ 
	private ScrolledComposite imageScrolledComposite;

	/** Creates a new ImageDropAndManagementComposite as a child of the provided parent composite 
	 * @param parentComposite the parent composite of the ImageDropAndManagementComposite */
	public ImageDropAndManagementComposite(Composite parentComposite) {
		super(parentComposite, SWT.NONE);
		this.initialize();
	}

	/** Creates a new ImageDropAndManagementComposite as a child of the provided parent composite. 
	 * Images can be provided for inclusion upon creation.
	 * @param parentComposite the parent composite of the ImageDropAndManagementComposite 
	 * @param imageURIs a list of URIs pointing to images that should be included within the imageComposite */
	public ImageDropAndManagementComposite(Composite parentComposite, ArrayList<URI> imageURIs) {	
		super(parentComposite, SWT.NONE);
		this.initialize();

		for (URI uri : imageURIs) {
			File file = new File(uri);
			this.imageURIs.addLast(file.toURI());
		}

		this.refreshImageComposite();
	}

	/** Initializes the ImageDropAndManagementComposite by setting default sizes, labels etc..*/
	private void initialize() {
		this.setLayout(new GridLayout(1, false));
		this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label dropTextLabel = new Label(this, SWT.BORDER | SWT.CENTER | SWT.VERTICAL);
		dropTextLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		dropTextLabel.setText("Drop Image Here");
		addDropSupport(dropTextLabel);

		imageScrolledComposite = new ScrolledComposite(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL );
		imageScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		imageComposite = new Composite(imageScrolledComposite, SWT.NONE);
		imageComposite.setLayout(new GridLayout(4, false));
		imageComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		imageScrolledComposite.setContent(imageComposite);

		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.heightHint = 130;

		imageScrolledComposite.setLayoutData(gridData);

		addDropSupport(imageScrolledComposite);
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
	 * has been added to the imageURIs list */
	public void refreshImageComposite() {
		for (Control control : imageComposite.getChildren()) {
			control.dispose();
		}

		for (final URI fileURI : imageURIs) {			
			Image originalImage = new Image(Display.getCurrent(), fileURI.getPath());	
			Image scaledImage = new Image(Display.getCurrent(), originalImage.getImageData().scaledTo(100, 100));

			Label pictureLabel = new Label(imageComposite, SWT.NONE);
			pictureLabel.setImage(scaledImage);
			Button deleteButton = new Button(imageComposite, SWT.NONE);
			deleteButton.setText("Remove");
			deleteButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					imageURIs.remove(fileURI);

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
					int index = imageURIs.indexOf(fileURI);

					if (index > 0) {					
						URI tmpURI = fileURI;

						imageURIs.remove(fileURI);
						imageURIs.add(index - 1, tmpURI);

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
					int index = imageURIs.indexOf(fileURI);

					if (index < imageURIs.size() - 1) {
						URI tmpURI = fileURI;

						imageURIs.remove(fileURI);
						imageURIs.add(index + 1, tmpURI);

						refreshImageComposite();
					}
				}
			});
		}

		imageComposite.pack();
		imageComposite.layout();
	}

	/** Return all image URIs 
	 * @return the imageURIs list */
	public List<URI> getAllImageURIs() {		
		return imageURIs;
	}

	@Override
	public void dragEnter(DropTargetEvent event) {
		System.out.println("drag enter");//TODO: log instead of syso
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
		if (event.data instanceof String[]) {
			String[] filenames = (String[]) event.data;
			if (filenames.length > 0){
				for (String filename : filenames) {
					URI pictureLocationInAlbum = ImageManipulator.adaptAndStoreImageForCollector(
							new File(filename).toURI(), Collector.getSelectedAlbum());
					if (pictureLocationInAlbum == null) {
						  showDroppedUnsupportedFileMessageBox(filename);
					}else {
						imageURIs.addLast(pictureLocationInAlbum);
					}
				}
				refreshImageComposite();
			}
		}
	}


	@Override
	public void dropAccept(DropTargetEvent arg0) {	
	}
	/** This method displays a message box informing the user of trying to drop the unsupported file.*/
	public void showDroppedUnsupportedFileMessageBox(String filePathToUnsupportedFilegeBox){
		MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_ERROR);
		messageBox.setText("Invalid File");
	    messageBox.setMessage(filePathToUnsupportedFilegeBox +" is not valid a file or of supported type. The only supported file types are: BMP, ICO, JPEG, GIF, PNG and TIFF.");
	    messageBox.open();
	}
}
