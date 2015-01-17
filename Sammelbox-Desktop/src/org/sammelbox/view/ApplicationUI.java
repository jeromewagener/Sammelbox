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

package org.sammelbox.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.sammelbox.controller.GuiController;
import org.sammelbox.controller.events.EventObservable;
import org.sammelbox.controller.events.EventObserver;
import org.sammelbox.controller.events.SammelboxEvent;
import org.sammelbox.controller.filesystem.FileSystemLocations;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.controller.listeners.BrowserListener;
import org.sammelbox.controller.managers.AlbumManager;
import org.sammelbox.controller.managers.DatabaseIntegrityManager;
import org.sammelbox.controller.managers.SavedSearchManager;
import org.sammelbox.controller.managers.SavedSearchManager.SavedSearch;
import org.sammelbox.controller.menu.MenuManager;
import org.sammelbox.model.GuiState;
import org.sammelbox.model.album.Album;
import org.sammelbox.model.database.QueryBuilder;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseOperations;
import org.sammelbox.view.browser.BrowserFacade;
import org.sammelbox.view.composites.BrowserComposite;
import org.sammelbox.view.composites.StatusBarComposite;
import org.sammelbox.view.composites.ToolbarComposite;
import org.sammelbox.view.sidepanes.EmptySidepane;
import org.sammelbox.view.sidepanes.QuickControlSidepane;
import org.sammelbox.view.various.ComponentFactory;
import org.sammelbox.view.various.PanelType;
import org.sammelbox.view.various.ScreenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public final class ApplicationUI implements EventObserver {	
	private static final int NUMBER_OF_MAIN_PANEL_COMPOSITES = 3;
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationUI.class);
	
	/** A reference to the main display */
	private static final Display DISPLAY = Display.getDefault();
	/** A reference to the main shell */
	private static final Shell SHELL = new Shell(DISPLAY);
		
	/** A reference to the SWT Text representing the quickSearch field*/
	private static Text quickSearch;
	/** A reference to the SWT list containing all available albums */
	private static List albums;
	/** A reference to the SWT list containing all available saved searches */
	private static List savedSearchesListBox;
	
	/** A reference to a composite being part of the general user interface */
	private static Composite threePanelComposite = null;
	private static Composite leftComposite = null;
	private static Composite rightComposite = null;
	private static Composite centerComposite = null;
	private static ToolbarComposite toolbarComposite = null;
	/** A reference to the SWT browser in charge of presenting album items */
	private static Browser albumItemBrowser;
	/** A reference to the SWT album item browser listener*/
	private static BrowserListener albumItemBrowserListener;
	/** The panel type that is currently visible on the right of the main three panel composite */
	private static PanelType currentRightPanelType = PanelType.EMPTY;

	/** Defines the panel size for the different panel types */
	private static Map<PanelType, Integer> panelTypeToPixelSize = new HashMap<PanelType, Integer>() {
		private static final long serialVersionUID = 1L;	{
			put(PanelType.EMPTY, UIConstants.RIGHT_PANEL_NO_WIDTH);
			put(PanelType.ADD_ALBUM, UIConstants.RIGHT_PANEL_LARGE_WIDTH);
			put(PanelType.ADD_ENTRY, UIConstants.RIGHT_PANEL_LARGE_WIDTH);
			put(PanelType.ADVANCED_SEARCH, UIConstants.RIGHT_PANEL_LARGE_WIDTH);
			put(PanelType.ALTER_ALBUM, UIConstants.RIGHT_PANEL_LARGE_WIDTH);
			put(PanelType.SYNCHRONIZATION, UIConstants.RIGHT_PANEL_MEDIUM_WIDTH);
			put(PanelType.UPDATE_ENTRY, UIConstants.RIGHT_PANEL_LARGE_WIDTH);
			put(PanelType.HELP, UIConstants.RIGHT_PANEL_SMALL_WIDTH);
			put(PanelType.SETTINGS, UIConstants.RIGHT_PANEL_LARGE_WIDTH);
			put(PanelType.IMPORT, UIConstants.RIGHT_PANEL_LARGE_WIDTH);
			put(PanelType.IMAGE_VIEWER, UIConstants.RIGHT_PANEL_MEDIUM_WIDTH);
			put(PanelType.ALBUM_FUNCTION, UIConstants.RIGHT_PANEL_LARGE_WIDTH);
		}
	};
	
	private ApplicationUI() {
		EventObservable.registerObserver(this);
	}

	/** This method initializes the main user interface. This involves the creation of different sub-composites */
	public static void initialize() {
		new ApplicationUI();
		initialize(true);
	}
		
	/** This method initializes the main user interface. This involves the creation of different sub-composites
	 * @param showShell true if the shell should be displayed, false otherwise (for testing purposes only) */
	public static void initialize(boolean showShell) {				
		// set program icon
		SHELL.setImage(new Image(DISPLAY, FileSystemLocations.getLogoSmallPNG()));
		
		// setup the Layout for the shell
		GridLayout shellGridLayout = new GridLayout(1, false);
		shellGridLayout.marginHeight = 0;
		shellGridLayout.marginWidth = 0;
		SHELL.setMinimumSize(UIConstants.MIN_SHELL_WIDTH, UIConstants.MIN_SHELL_HEIGHT);

		// setup the Shell
		SHELL.setText(Translator.get(DictKeys.TITLE_MAIN_WINDOW));				
		SHELL.setLayout(shellGridLayout);
		
		// define toolbar composite layout data
		GridData gridDataForToolbarComposite = new GridData(GridData.FILL_BOTH);
		gridDataForToolbarComposite.grabExcessHorizontalSpace = true;
		gridDataForToolbarComposite.grabExcessVerticalSpace = false;
		
		// define three panel composite layout data
		GridData gridDataForThreePanelComposite = new GridData(GridData.FILL_BOTH);
		gridDataForThreePanelComposite.grabExcessHorizontalSpace = true;
		gridDataForThreePanelComposite.grabExcessVerticalSpace = true;
		gridDataForThreePanelComposite.verticalAlignment = GridData.FILL;
		gridDataForThreePanelComposite.horizontalAlignment = GridData.FILL;
		GridLayout mainGridLayout = new GridLayout(NUMBER_OF_MAIN_PANEL_COMPOSITES, false);

		// define left (upper & lower) composite layout data
		GridData gridDataForLeftComposite = new GridData(GridData.FILL_BOTH);
		gridDataForLeftComposite.grabExcessHorizontalSpace = false;
		GridData gridDataForUpperLeftComposite = new GridData(GridData.FILL_BOTH);
		gridDataForUpperLeftComposite.verticalAlignment = GridData.BEGINNING;
		GridData gridDataForLowerLeftComposite = new GridData(GridData.FILL_BOTH);
		gridDataForLowerLeftComposite.verticalAlignment = GridData.END;

		// define center composite layout data
		GridData gridDataForCenterComposite = new GridData(SWT.FILL, SWT.FILL, true, true);

		// define right composite layout data
		GridData gridDataForRightComposite = new GridData(GridData.FILL_BOTH);
		gridDataForRightComposite.grabExcessHorizontalSpace = false;
		gridDataForRightComposite.verticalAlignment = GridData.BEGINNING;

		// define statusbar composite layout data
		GridData gridDataForStatusBarComposite = new GridData(GridData.FILL_BOTH);
		gridDataForStatusBarComposite.grabExcessHorizontalSpace = true;
		gridDataForStatusBarComposite.grabExcessVerticalSpace = false;

		// Setup composites using layout definitions from before
		toolbarComposite = new ToolbarComposite(SHELL);
		GridLayout toolbarGridLayout = new GridLayout(1, false);
		toolbarGridLayout.marginHeight = 0;
		toolbarComposite.setLayout(toolbarGridLayout);
		toolbarComposite.setLayoutData(gridDataForToolbarComposite);
		
		threePanelComposite = new Composite(SHELL, SWT.NONE);
		threePanelComposite.setLayout(mainGridLayout);
		threePanelComposite.setLayoutData(gridDataForThreePanelComposite);

		leftComposite = new Composite(threePanelComposite, SWT.NONE);
		leftComposite.setLayout(new GridLayout(1, false));
		leftComposite.setLayoutData(gridDataForLeftComposite);
		Composite upperLeftSubComposite = QuickControlSidepane.build(leftComposite);
		upperLeftSubComposite.setLayoutData(gridDataForUpperLeftComposite);
		Composite lowerLeftSubComposite = EmptySidepane.build(leftComposite);
		lowerLeftSubComposite.setLayoutData(gridDataForLowerLeftComposite);
		albumItemBrowserListener = new BrowserListener(threePanelComposite);
		centerComposite = new BrowserComposite(threePanelComposite, SWT.NONE, albumItemBrowserListener);
		centerComposite.setLayout(new GridLayout(1, false));
		centerComposite.setLayoutData(gridDataForCenterComposite);
		rightComposite = EmptySidepane.build(threePanelComposite);
		rightComposite.setLayout(new GridLayout(1, false));
		rightComposite.setLayoutData(gridDataForRightComposite);

		Composite statusComposite = StatusBarComposite.getInstance(SHELL).getStatusbarComposite();
		statusComposite.setLayout(new GridLayout(1, false));
		statusComposite.setLayoutData(gridDataForStatusBarComposite);

		// Create the menu bar
		MenuManager.createAndInitializeMenuBar(SHELL);

		// center the shell to primary screen
		Rectangle primaryScreenClientArea = ScreenUtils.getPrimaryScreenClientArea(DISPLAY);
		int xCoordinateForShell = primaryScreenClientArea.width / 2 - UIConstants.MIN_SHELL_WIDTH / 2;
		int yCoordinateForShell = primaryScreenClientArea.height / 2 - UIConstants.MIN_SHELL_HEIGHT / 2;
		SHELL.setLocation(xCoordinateForShell, yCoordinateForShell);

		// Create the album manager
		AlbumManager.initialize();
		for (Album album : AlbumManager.getAlbums()) {
			albums.add(album.getAlbumName());
		}
		
		// Create the saved searches manager
		SavedSearchManager.initialize();
		
		// SWT display management
		SHELL.pack();

		Rectangle displayClientArea = DISPLAY.getPrimaryMonitor().getClientArea();
		if (maximizeShellOnStartUp(displayClientArea.width, displayClientArea.height)){
			SHELL.setMaximized(true);
		}
		
		if (showShell) {
			SHELL.open();
	
			selectDefaultAndShowWelcomePage();		
	
			while (!SHELL.isDisposed()) {
				if (!DISPLAY.readAndDispatch()) {
					DISPLAY.sleep();
				}
			}
	
			DISPLAY.dispose();
		}
		
		try {
			DatabaseIntegrityManager.backupAutoSave();
		} catch (DatabaseWrapperOperationException ex) {
			LOGGER.error("Couldn't create an auto save of the database file", ex);
		}
	}

	public static Composite getThreePanelComposite() {
		return threePanelComposite;
	}

	public static void selectDefaultAndShowWelcomePage() {
		if (albums.getItemCount() > 0) {
			albums.setSelection(-1);
		}

		BrowserFacade.loadWelcomePage();
	}
	
	/** This method exchanges the center composite with a composite provided as parameter. Hereby, the previous composite is disposed. 
	 * @param newCenterComposite the new composite for the center element of the user interface */
	public static void changeCenterCompositeTo(Composite newCenterComposite) {
		Layout layout = centerComposite.getLayout();
		GridData layoutData = (GridData) centerComposite.getLayoutData();

		centerComposite.dispose();
		newCenterComposite.setLayout(layout);
		newCenterComposite.setLayoutData(layoutData);

		centerComposite = newCenterComposite;
		centerComposite.moveBelow(leftComposite);
		centerComposite.getParent().layout();
	}

	public static void resizeRightCompositeTo(int pixels) {
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.grabExcessHorizontalSpace = false;
		layoutData.grabExcessVerticalSpace = true;
		layoutData.verticalAlignment = GridData.BEGINNING;
		layoutData.widthHint = pixels;

		rightComposite.setLayoutData(layoutData);
		rightComposite.getParent().layout();
	}

	public static void changeRightCompositeTo(PanelType panelType, Composite newRightComposite) {
		changeRightCompositeTo(panelType, newRightComposite, -1);
	}
	
	/** This method exchanges the right composite with a composite provided as parameter. Hereby, the previous composite is disposed. 
	 * @param newRightComposite the new composite for the right element of the user interface
	 * @param albumItemId to be used in case the right composite is used to show an album item */
	public static void changeRightCompositeTo(PanelType panelType, Composite newRightComposite, long albumItemId) {
		// handle the case of unsaved changes
		if (GuiController.continueWithUnsavedModifications(getShell())) {
			GuiController.getGuiState().setUnsavedAlbumItem(false);
		} else {
			newRightComposite.dispose();
			return;
		}
		
		currentRightPanelType = panelType;

		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.grabExcessHorizontalSpace = false;
		layoutData.grabExcessVerticalSpace = true;
		layoutData.verticalAlignment = GridData.BEGINNING;

		if (panelTypeToPixelSize.containsKey(panelType)) {
			if (ScrolledComposite.class.isInstance(newRightComposite)) {
				ScrolledComposite sc = (ScrolledComposite) newRightComposite;
				layoutData.widthHint = panelTypeToPixelSize.get(panelType) - sc.getVerticalBar().getSize().x;
			} else {		
				layoutData.widthHint = panelTypeToPixelSize.get(panelType);
			}
		} else {
			if (ScrolledComposite.class.isInstance(newRightComposite)) {
				ScrolledComposite sc = (ScrolledComposite) newRightComposite;
				layoutData.widthHint = UIConstants.RIGHT_PANEL_MEDIUM_WIDTH - sc.getVerticalBar().getSize().x;
			} else {		
				layoutData.widthHint = UIConstants.RIGHT_PANEL_MEDIUM_WIDTH;
			}
		}

		newRightComposite.setLayoutData(layoutData);

		rightComposite.dispose();
		rightComposite = newRightComposite;
		rightComposite.moveBelow(centerComposite);
		rightComposite.getParent().layout();

		GuiController.getGuiState().setCurrentSidepaneType(panelType);
		GuiController.getGuiState().setCurrentSidepaneComposite(newRightComposite);
		GuiController.getGuiState().setIdOfAlbumItemInSidepane(albumItemId);
		
		EventObservable.addEventToQueue(SammelboxEvent.RIGHT_SIDEPANE_CHANGED);
	}

	/** Returns the currently selected/active album or view
	 * @return the currently selected/active album or view */
	public static String getSelectedAlbum() {
		return GuiController.getGuiState().getSelectedAlbum();
	}

	public static void setQuickSearchTextField(Text quickSearchTextField) {
		ApplicationUI.quickSearch = quickSearchTextField;
	}
	
	public static Text getQuickSearchTextField() {
		return ApplicationUI.quickSearch;
	}

	/** Sets the currently selected/active album and reloads it
	 * @param albumName The name of the now selected/active album. If the albumName is null or empty then all albums are deselected.  
	 * @return True if the album is selected internally and in the SWT Album list. If all albums were successfully deselected then true is also returned. 
	 * False otherwise.*/
	public static boolean setSelectedAlbumAndReload(String albumName) {		
		// Set the album name and verify that it is in the list
		GuiController.getGuiState().setSelectedAlbum(albumName);
		if (albumName == null || albumName.isEmpty()) {
			ApplicationUI.albums.deselectAll();
			return true;
		}
		
		// Reset view
		GuiController.getGuiState().setSelectedSavedSearch(GuiState.NO_VIEW_SELECTED);
		
		int albumListItemCount = ApplicationUI.albums.getItemCount();
		boolean albumSelectionIsInSync = false;
		for (int itemIndex = 0; itemIndex<albumListItemCount; itemIndex++) {
			 if (ApplicationUI.albums.getItem(itemIndex).equals(albumName) ) {
				 ApplicationUI.albums.setSelection(itemIndex);
				 albumSelectionIsInSync = true;
				 break;
			 }
		}
		
		if (!albumSelectionIsInSync && albumName.equals(GuiState.NO_ALBUM_SELECTED)) {
			LOGGER.error("The album list does not contain the album that is supposed to be selected");
			return false;
		}
	
		if (!ApplicationUI.getQuickSearchTextField().getText().isEmpty()) {
			ApplicationUI.getQuickSearchTextField().setText("");
		}
		
		try {
			ApplicationUI.getQuickSearchTextField().setEnabled(DatabaseOperations.isAlbumQuickSearchable(albumName));
		} catch (DatabaseWrapperOperationException ex) {
			LOGGER.error("An error occurred while enabling the quick search field", ex);
		}
		
		BrowserFacade.performBrowserQueryAndShow(QueryBuilder.createOrderedSelectStarQuery(albumName));
		
		ApplicationUI.getSavedSearchesListBox().setEnabled(SavedSearchManager.hasAlbumSavedSearches(albumName));
		EventObservable.addEventToQueue(SammelboxEvent.ALBUM_SELECTED);
		toolbarComposite.enableAlbumButtons(albumName);
		
		return true;
	}
	
	/** After adding/removing albums, this method should be used to refresh the album list with the current album names thus leaving no album selected.*/
	public static void refreshAlbumList() {
		EventObservable.addEventToQueue(SammelboxEvent.ALBUM_LIST_UPDATED);
		EventObservable.addEventToQueue(SammelboxEvent.SAVED_SEARCHES_LIST_UPDATED);
		ApplicationUI.getQuickSearchTextField().setEnabled(false);
	}

	/** Sets the the list of albums
	 * @param albumList the list of albums */
	public static void setAlbumList(List albumList) {
		ApplicationUI.albums = albumList;
	}

	/** Sets the the list of saved searches
	 * @param savedSearches the list of saved searches */
	public static void setSavedSearchesList(List savedSearches) {
		ApplicationUI.savedSearchesListBox = savedSearches;
	}

	/** Returns the list of saved searches
	 * @return the list of saved searches */
	public static List getSavedSearchesListBox() {
		return savedSearchesListBox;
	}

	/** Creates or retrieves the album item browser
	 * @return the album item browser */
	public static Browser createOrRetrieveAlbumItemBrowser() {
		if (albumItemBrowser == null || albumItemBrowser.isDisposed()) {
			BrowserComposite browserComposite = new BrowserComposite(
					ApplicationUI.getThreePanelComposite(), SWT.NONE, ApplicationUI.getBrowserListener());
			
			albumItemBrowser = browserComposite.getBrowser();
			
			ApplicationUI.changeCenterCompositeTo(browserComposite);
		}
		
		return albumItemBrowser;
	}
	
	public static PanelType getCurrentRightPanelType() {
		return currentRightPanelType;
	}

	public static void setCurrentRightPanelType(PanelType currentRightPanel) {
		ApplicationUI.currentRightPanelType = currentRightPanel;
	}

	public static Shell getShell() {
		return SHELL;
	}
	
	/**
	 * Decides whether the application should start in fullscreen mode or window mode, depending on the available resolution
	 */
	public static boolean maximizeShellOnStartUp(int screenWidth, int screenHeight) {
		return UIConstants.MIN_SHELL_WIDTH >= screenWidth || UIConstants.MIN_SHELL_HEIGHT >= screenHeight;
	}
	
	@Override
	public void reactToEvent(SammelboxEvent event) {		
		if (event.equals(SammelboxEvent.ALBUM_LIST_UPDATED)) {
			albums.removeAll();
			for (Album album : AlbumManager.getAlbums()) {
				albums.add(album.getAlbumName());
			}
		
		} else if (event.equals(SammelboxEvent.ALBUM_SELECTED)) {			
			savedSearchesListBox.setItems(SavedSearchManager.getSavedSearchesNamesArray(GuiController.getGuiState().getSelectedAlbum()));
			BrowserFacade.resetFutureJumpAnchor();
		
		} else if (event.equals(SammelboxEvent.SAVED_SEARCH_SELECTED)) {
			BrowserFacade.resetFutureJumpAnchor();
		
		} else if (event.equals(SammelboxEvent.SAVED_SEARCHES_LIST_UPDATED)) {
			savedSearchesListBox.removeAll();

			for (SavedSearch albumView : SavedSearchManager.getSavedSearches(GuiController.getGuiState().getSelectedAlbum())) {
				savedSearchesListBox.add(albumView.getName());
			}
			
			if (!savedSearchesListBox.isEnabled() && savedSearchesListBox.getItemCount() != 0) {
				savedSearchesListBox.setEnabled(true);
			}
		
		} else if (event.equals(SammelboxEvent.NO_ALBUM_SELECTED)) {
			ComponentFactory.showErrorDialog(
					ApplicationUI.getShell(), 
					Translator.get(DictKeys.DIALOG_TITLE_NO_ALBUM_SELECTED), 
					Translator.get(DictKeys.DIALOG_CONTENT_NO_ALBUM_SELECTED));
		
		} else if (event.equals(SammelboxEvent.DISABLE_SAMMELBOX)) {
			quickSearch.setEnabled(false);
			albums.setEnabled(false);
			savedSearchesListBox.setEnabled(false);
			
			if (currentRightPanelType.equals(PanelType.ADD_ENTRY) || currentRightPanelType.equals(PanelType.UPDATE_ENTRY)) {
				setEnabledOnCompositeControls(rightComposite, false);		
			}

		} else if (event.equals(SammelboxEvent.ENABLE_SAMMELBOX)) {
			quickSearch.setEnabled(true);
			albums.setEnabled(true);
			savedSearchesListBox.setEnabled(true);
			
			if (currentRightPanelType.equals(PanelType.ADD_ENTRY) || currentRightPanelType.equals(PanelType.UPDATE_ENTRY)) {
				setEnabledOnCompositeControls(rightComposite, true);		
			}
		}
	}

	/** This method recursively traverses the composite (which can have sub composites etc.. ) and sets the enable state
	 * of real controls. (i.e. labels are not affected). Since this method is computationally expensive, use it WISELY! */
	private void setEnabledOnCompositeControls(Composite composite, boolean enabled) {
		for (Control control : composite.getChildren()) {
			if (control instanceof Composite) {
				setEnabledOnCompositeControls((Composite) control, enabled);
			} else if (!(control instanceof Label)) {
				control.setEnabled(enabled);
			}
		}
	}
	
	public static boolean isAlbumSelectedAndShowMessageIfNot() {
		if (!GuiController.getGuiState().isAlbumSelected()) {
			EventObservable.addEventToQueue(SammelboxEvent.NO_ALBUM_SELECTED);
			return false;
		}
		return true;
	}	
	
	public static ToolbarComposite getToolbarComposite() {
		return toolbarComposite;
	}

	public static BrowserListener getBrowserListener() {
		return albumItemBrowserListener;
	}
}
