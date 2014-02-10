package org.sammelbox.controller.menu;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.MessageBox;
import org.sammelbox.controller.GuiController;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.controller.managers.SavedSearchManager;
import org.sammelbox.model.GuiState;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseOperations;
import org.sammelbox.view.ApplicationUI;
import org.sammelbox.view.browser.BrowserFacade;
import org.sammelbox.view.sidepanes.AdvancedSearchSidepane;
import org.sammelbox.view.sidepanes.AlterAlbumSidepane;
import org.sammelbox.view.sidepanes.CreateAlbumSidepane;
import org.sammelbox.view.various.ComponentFactory;
import org.sammelbox.view.various.PanelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AlbumMenuItemListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(AlbumMenuItemListener.class);
	
	private AlbumMenuItemListener() {
		// not needed
	}
	
	static SelectionAdapter getAdvancedSearchListener() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (ApplicationUI.isAlbumSelectedAndShowMessageIfNot()) {
					ApplicationUI.changeRightCompositeTo(PanelType.ADVANCED_SEARCH, AdvancedSearchSidepane.build(
							ApplicationUI.getThreePanelComposite(), ApplicationUI.getSelectedAlbum()));
				}
			}
		};
	}
	
	static SelectionAdapter getCreateNewAlbumListener() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				BrowserFacade.clearAlterationList();
				ApplicationUI.changeRightCompositeTo(PanelType.ADD_ALBUM, CreateAlbumSidepane.build(ApplicationUI.getThreePanelComposite()));
			}
		};
	}
	
	static SelectionAdapter getAlterAlbumListener() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (ApplicationUI.isAlbumSelectedAndShowMessageIfNot()) {
					ApplicationUI.changeRightCompositeTo(PanelType.ALTER_ALBUM, AlterAlbumSidepane.build(
							ApplicationUI.getThreePanelComposite(), ApplicationUI.getSelectedAlbum()));
				}
			}
		};
	}
	
	static SelectionAdapter getDeleteAlbumListener() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (ApplicationUI.isAlbumSelectedAndShowMessageIfNot()) {					
					MessageBox messageBox = ComponentFactory.getMessageBox(
							Translator.get(DictKeys.DIALOG_TITLE_DELETE_ALBUM), 
							Translator.get(DictKeys.DIALOG_CONTENT_DELETE_ALBUM, ApplicationUI.getSelectedAlbum()), 
							SWT.ICON_WARNING | SWT.YES | SWT.NO);
					
					if (messageBox.open() == SWT.YES) {
						try {
							SavedSearchManager.removeSavedSearchesFromAlbum(ApplicationUI.getSelectedAlbum());
							DatabaseOperations.removeAlbumAndAlbumPictures(ApplicationUI.getSelectedAlbum());
							BrowserFacade.showAlbumDeletedPage(ApplicationUI.getSelectedAlbum());
							GuiController.getGuiState().setSelectedAlbum(GuiState.NO_ALBUM_SELECTED);
							ApplicationUI.refreshAlbumList();
						} catch (DatabaseWrapperOperationException ex) {
							LOGGER.error("A database error occured while removing the following album: '" + 
												ApplicationUI.getSelectedAlbum() + "'", ex);
						}
					}
				}
			}
		};
	}
}
