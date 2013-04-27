package collector.desktop.gui;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;

import collector.desktop.Collector;
import collector.desktop.database.AlbumItem;
import collector.desktop.database.DatabaseWrapper;
import collector.desktop.database.FieldType;
import collector.desktop.database.ItemField;

public class BrowserListener implements LocationListener, ProgressListener, MenuDetectListener {
	/** The parent composite to which this listener belongs to */
	private Composite parentComposite;

	/** Creates a new browser listener that can be attached to an SWT browser */
	public BrowserListener(Composite parentComposite) {
		this.parentComposite = parentComposite;
	}

	/** This method is required since under some circumstances, an question-mark is added to the end of the URL (event.location). If
	 * a question mark is present at the end, it will be deleted. Otherwise, the unmodified string is returned 
	 * @return a string without a question mark at the end*/
	private String removeQuestionMarkAtTheEndIfPresent(String string) {
		if (string.charAt(string.length() - 1) == '?') {
			string = string.substring(0, string.length() - 1);
		}

		return string;
	}

	@Override
	public void changed(LocationEvent event) {}

	@Override
	/** If the current browser location is changing, it must be checked what the new location will be. Because the location may contain
	 * program specific commands such as "show:///updateComposite=2". In this case, the change is not performed, but instead an operation
	 * is executed. (E.g. opening a new composite) 
	 * @param event the location event used to identify the new location */
	public void changing(LocationEvent event) {		
		// TODO why not constants?
		String showUpdateComposite = "show:///updateComposite=";
		String showDeleteComposite = "show:///deleteComposite=";
		String showBigPicture = "show:///bigPicture=";
		String showLastPage = "show:///lastPage";
		String showDetails = "show:///details=";
		String showDetailsComposite = "show:///detailsComposite=";
		String addAdditionalAlbumItems = "show:///addAdditionalAlbumItems";
		
		
		if (event.location.startsWith(showUpdateComposite)) {
			String id = event.location.substring(showUpdateComposite.length());
			removeQuestionMarkAtTheEndIfPresent(id);

			Collector.changeRightCompositeTo(PanelType.UpdateEntry,
					CompositeFactory.getUpdateAlbumItemComposite(parentComposite, Collector.getSelectedAlbum(), Long.parseLong(id)));
			BrowserContent.jumpToAnchor(BrowserContent.getAnchorForAlbumItemId(Integer.parseInt(id)));

			// Do not change the page
			event.doit = false;

		} else if (event.location.startsWith(showDeleteComposite)) {
			String id = event.location.substring(showDeleteComposite.length());
			removeQuestionMarkAtTheEndIfPresent(id);

			MessageBox messageBox = new MessageBox(parentComposite.getShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
			messageBox.setMessage("Do you really want to delete this ablum item? This record will be permanently lost!");
			messageBox.setText("Delete");
			if (messageBox.open() == SWT.YES) {
				DatabaseWrapper.deleteAlbumItem(Collector.getSelectedAlbum(), Long.parseLong(id));
				BrowserContent.performBrowserQueryAndShow(
						Collector.getAlbumItemSWTBrowser(), DatabaseWrapper.createSelectStarQuery(Collector.getSelectedAlbum()));
			}
			
			// Do not change the page
			event.doit = false;
		} else if (event.location.startsWith(showBigPicture)) {
			String pathAndIdString = event.location.substring(showBigPicture.length());
			
			removeQuestionMarkAtTheEndIfPresent(pathAndIdString);

			String[] pathAndIdArray = pathAndIdString.split("\\?");

			BrowserContent.showPicture(pathAndIdArray[0], Long.parseLong(pathAndIdArray[1].replace("imageId", "")));
			BrowserContent.setFutureJumpAnchor(pathAndIdArray[1]);

			// Do not change the page
			event.doit = false;
		} else if (event.location.startsWith(showLastPage)) {
			BrowserContent.goBackToLastPage();

			// Do not change the page
			event.doit = false;
		} else if (event.location.startsWith(showDetails)) {
			String id = event.location.substring(showDetails.length());
			removeQuestionMarkAtTheEndIfPresent(id);
			
			AlbumItem albumItem = DatabaseWrapper.fetchAlbumItem(Collector.getSelectedAlbum(), Long.parseLong(id));
			
			List<ItemField> itemFields = albumItem.getFields();
			
			StringBuilder sb = new StringBuilder();
			for (ItemField itemField : itemFields) {
				if (albumItem.getField(itemField.getName()).getType() != FieldType.ID
						&& albumItem.getField(itemField.getName()).getType() != FieldType.UUID
						&& albumItem.getField(itemField.getName()).getType() != FieldType.Picture ) {
					sb.append(albumItem.getField(itemField.getName()).getName() + ": " + albumItem.getField(itemField.getName()).getValue() + ", ");
				}
			}
			sb.append("...");
			
			StatusBarComposite.getInstance(Collector.getShell()).writeStatus(sb.toString());
			
			// Do not change the page
			event.doit = false;
		} else if (event.location.startsWith(showDetailsComposite)) {
			String id = event.location.substring(showDetailsComposite.length());
				removeQuestionMarkAtTheEndIfPresent(id);

				Collector.changeRightCompositeTo(PanelType.UpdateEntry,
						CompositeFactory.getUpdateAlbumItemComposite(parentComposite, Collector.getSelectedAlbum(), Long.parseLong(id)));
				
				BrowserContent.jumpToAnchor(BrowserContent.getAnchorForAlbumItemId(Long.parseLong(id)));
				
				// Do not change the page
				event.doit = false;
		} else if (event.location.startsWith("file:///")) {
			if (event.location.contains(".collector/app-data/loading.html")) {
				event.doit = true;
			} else {
				event.doit = false;
			}
		} else if (event.location.equals(addAdditionalAlbumItems)) {
			BrowserContent.addAdditionalAlbumItems();
			
			// Do not change the page
			event.doit = false;
		}
	}

	@Override
	public void changed(ProgressEvent event) {
	}

	@Override
	/** As soon as a page is completely loaded, it is possible to jump to a previously defined anchor */
	public void completed(ProgressEvent event) {
		BrowserContent.jumpToAnchor(BrowserContent.getFutureJumpAnchor());
	}

	@Override
	/** Don't show the usual browser menu */
	public void menuDetected(MenuDetectEvent e) {
		e.doit = false;
	}
}
