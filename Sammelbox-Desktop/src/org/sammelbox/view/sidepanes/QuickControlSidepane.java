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

package org.sammelbox.view.sidepanes;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.sammelbox.controller.GuiController;
import org.sammelbox.controller.events.EventObservable;
import org.sammelbox.controller.events.SammelboxEvent;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.controller.listeners.QuickSearchModifyListener;
import org.sammelbox.controller.managers.AlbumManager;
import org.sammelbox.controller.managers.SavedSearchManager;
import org.sammelbox.controller.managers.SavedSearchManager.SavedSearch;
import org.sammelbox.controller.managers.WelcomePageManager;
import org.sammelbox.model.GuiState;
import org.sammelbox.model.database.QueryBuilder;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseOperations;
import org.sammelbox.view.ApplicationUI;
import org.sammelbox.view.browser.BrowserFacade;
import org.sammelbox.view.various.ComponentFactory;
import org.sammelbox.view.various.FieldSelectionDialog;
import org.sammelbox.view.various.PanelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class QuickControlSidepane {
	private static final int SAVED_SEARCHES_WIDTH = 125;
	private static final int SAVED_SEARCHES_HEIGHT = 200;
	private static final int ALBUM_LIST_WIDTH = 125;
	private static final int ALBUM_LIST_HEIGHT = 100;
	private static final int ALBUM_LIST_LABEL_FONT_SIZE = 11;
	private static final int SEPARATOR_MIN_HEIGHT = 15;
	private static final Logger LOGGER = LoggerFactory.getLogger(QuickControlSidepane.class);
	
	private QuickControlSidepane() {
		// use build method instead
	}
	
	/** Returns a quick control composite (select-album-list, quick-search) used by the GUI 
	 * @param parentComposite the parent composite
	 * @return a new quick control composite */
	public static Composite build(final Composite parentComposite) {
		// setup quick control composite
		Composite quickControlComposite = new Composite(parentComposite, SWT.NONE);
		quickControlComposite.setLayout(new GridLayout());

		// separator grid data
		GridData seperatorGridData = new GridData(GridData.FILL_BOTH);
		seperatorGridData.minimumHeight = SEPARATOR_MIN_HEIGHT;

		// quick-search label
		Label quickSearchLabel = new Label(quickControlComposite, SWT.NONE);
		quickSearchLabel.setText(Translator.get(DictKeys.LABEL_QUICKSEARCH));
		quickSearchLabel.setFont(new Font(parentComposite.getDisplay(), 
				quickSearchLabel.getFont().getFontData()[0].getName(), ALBUM_LIST_LABEL_FONT_SIZE, SWT.BOLD));

		// quick-search text-box
		final Text quickSearchText = new Text(quickControlComposite, SWT.BORDER);
		quickSearchText.setLayoutData(new GridData(GridData.FILL_BOTH));
		quickSearchText.addModifyListener(new QuickSearchModifyListener());
		ApplicationUI.setQuickSearchTextField(quickSearchText);

		// separator
		new Label(quickControlComposite, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(seperatorGridData);

		// select album label
		Label selectAlbumLabel = new Label(quickControlComposite, SWT.NONE);
		selectAlbumLabel.setText(Translator.get(DictKeys.LABEL_ALBUM_LIST));
		selectAlbumLabel.setFont(new Font(parentComposite.getDisplay(), 
				selectAlbumLabel.getFont().getFontData()[0].getName(), ALBUM_LIST_LABEL_FONT_SIZE, SWT.BOLD));

		// the list of albums (listener is added later)
		final List albumList = new List(quickControlComposite, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);

		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = ALBUM_LIST_HEIGHT;
		gridData.widthHint = ALBUM_LIST_WIDTH;
		albumList.setLayoutData(gridData);

		// Set the currently active album
		ApplicationUI.setAlbumList(albumList);

		// separator
		new Label(quickControlComposite, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(seperatorGridData);

		// select album label
		Label selectViewLabel = new Label(quickControlComposite, SWT.NONE);
		selectViewLabel.setText(Translator.get(DictKeys.LABEL_SAVED_SEARCHES));
		selectViewLabel.setFont(new Font(parentComposite.getDisplay(),
				selectAlbumLabel.getFont().getFontData()[0].getName(), ALBUM_LIST_LABEL_FONT_SIZE, SWT.BOLD));

		// the list of albums (listener is added later)
		final List savedSearchesList = new List(quickControlComposite, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);

		GridData gridData2 = new GridData(GridData.FILL_BOTH);
		gridData2.heightHint = SAVED_SEARCHES_HEIGHT;
		gridData2.widthHint = SAVED_SEARCHES_WIDTH;
		savedSearchesList.setLayoutData(gridData2);		
		ApplicationUI.setSavedSearchesList(savedSearchesList);

		albumList.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (albumList.getSelectionIndex() != -1 && GuiController.continueWithUnsavedModifications(ApplicationUI.getShell())) {
					GuiController.getGuiState().setUnsavedAlbumItem(false);
					
					ApplicationUI.setSelectedAlbumAndReload(albumList.getItem(albumList.getSelectionIndex()));
					ApplicationUI.changeRightCompositeTo(PanelType.EMPTY, EmptySidepane.build(ApplicationUI.getThreePanelComposite()));
					WelcomePageManager.increaseClickCountForAlbumOrView(albumList.getItem(albumList.getSelectionIndex()));
				}
			}
		});
		
		albumList.addListener(SWT.MenuDetect, new Listener() {
			@Override
			public void handleEvent(Event event) {				
				if ((albumList.getItemCount() > 0) && albumList.getSelectionIndex() < 0) {
					String firstAlbumNameInList = albumList.getItem(0);
					ApplicationUI.setSelectedAlbumAndReload(firstAlbumNameInList);					
				}
			}
		});
		
					
		final Menu popupMenuForAlbumList = new Menu(albumList);		
		albumList.setMenu(popupMenuForAlbumList);
			
		final MenuItem moveAlbumTop = new MenuItem(popupMenuForAlbumList, SWT.NONE);
		moveAlbumTop.setText(Translator.get(DictKeys.DROPDOWN_MOVE_TO_TOP));
		moveAlbumTop.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				AlbumManager.moveToFront(albumList.getSelectionIndex());
			}
		});
		final MenuItem moveAlbumOneUp = new MenuItem(popupMenuForAlbumList, SWT.NONE);
		moveAlbumOneUp.setText(Translator.get(DictKeys.DROPDOWN_MOVE_ONE_UP));
		moveAlbumOneUp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				AlbumManager.moveOneUp(albumList.getSelectionIndex());
			}
		});
		final MenuItem moveAlbumOneDown = new MenuItem(popupMenuForAlbumList, SWT.NONE);
		moveAlbumOneDown.setText(Translator.get(DictKeys.DROPDOWN_MOVE_ONE_DOWN));
		moveAlbumOneDown.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				AlbumManager.moveOneDown(albumList.getSelectionIndex());
			}
		});
		
		final MenuItem moveAlbumBottom = new MenuItem(popupMenuForAlbumList, SWT.NONE);
		moveAlbumBottom.setText(Translator.get(DictKeys.DROPDOWN_MOVE_TO_BOTTOM));
		moveAlbumBottom.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				AlbumManager.moveToBottom(albumList.getSelectionIndex());
			}			
		});

		final MenuItem albumListPopupMenuItemSeparator1 =  new MenuItem(popupMenuForAlbumList, SWT.SEPARATOR);

		final MenuItem createNewAlbum = new MenuItem(popupMenuForAlbumList, SWT.NONE);
		createNewAlbum.setText(Translator.get(DictKeys.DROPDOWN_CREATE_NEW_ALBUM));
		createNewAlbum.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ApplicationUI.changeRightCompositeTo(
						PanelType.ADD_ALBUM, CreateAlbumSidepane.build(ApplicationUI.getThreePanelComposite()));
			}
		});
		final MenuItem alterAlbum = new MenuItem(popupMenuForAlbumList, SWT.NONE);
		alterAlbum.setText(Translator.get(DictKeys.DROPDOWN_ALTER_ALBUM));
		alterAlbum.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ApplicationUI.changeRightCompositeTo(
						PanelType.ALTER_ALBUM, AlterAlbumSidepane.build(
								ApplicationUI.getThreePanelComposite(), ApplicationUI.getSelectedAlbum()));
			}
		});
		final MenuItem albumListPopupMenuItemSeparator2 = new MenuItem(popupMenuForAlbumList, SWT.SEPARATOR);

		final MenuItem sortBy = new MenuItem(popupMenuForAlbumList, SWT.NONE);
		sortBy.setText(Translator.get(DictKeys.DROPDOWN_SORT_BY));
		sortBy.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FieldSelectionDialog fieldSelectionDialog = new FieldSelectionDialog(ApplicationUI.getShell());
				
				String sortByField = fieldSelectionDialog.open(
						Translator.get(DictKeys.DIALOG_TITLE_SORT_BY),
						Translator.get(DictKeys.DIALOG_CONTENT_SORT_BY),
						GuiController.getGuiState().getSelectedAlbum(),
						Translator.get(DictKeys.BUTTON_SAVE),
						AlbumManager.getSortByField(GuiController.getGuiState().getSelectedAlbum()));
				
				String selectedName = GuiController.getGuiState().getSelectedAlbum();
				AlbumManager.setSortByField(selectedName, sortByField);
				BrowserFacade.performBrowserQueryAndShow(QueryBuilder.createOrderedSelectStarQuery(selectedName));
			}
		});
		
		final MenuItem albumListPopupMenuItemSeparator3 = new MenuItem(popupMenuForAlbumList, SWT.SEPARATOR);
		
		final MenuItem removeAlbum = new MenuItem(popupMenuForAlbumList, SWT.NONE);
		removeAlbum.setText(Translator.get(DictKeys.DROPDOWN_REMOVE));
		removeAlbum.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {	
				if (ApplicationUI.isAlbumSelectedAndShowMessageIfNot()) {					
					MessageBox messageBox = ComponentFactory.getMessageBox(
							Translator.get(DictKeys.DIALOG_TITLE_DELETE_ALBUM), 
							Translator.get(DictKeys.DIALOG_CONTENT_DELETE_ALBUM, ApplicationUI.getSelectedAlbum()), 
							SWT.ICON_WARNING | SWT.YES | SWT.NO);
					
					if (messageBox.open() == SWT.YES) {
						try {							
							DatabaseOperations.removeAlbumAndAlbumPictures(ApplicationUI.getSelectedAlbum());
							SavedSearchManager.removeSavedSearchesFromAlbum(ApplicationUI.getSelectedAlbum());
							BrowserFacade.showAlbumDeletedPage(ApplicationUI.getSelectedAlbum());
							GuiController.getGuiState().setSelectedAlbum(GuiState.NO_ALBUM_SELECTED);
							ApplicationUI.refreshAlbumList();
						} catch (DatabaseWrapperOperationException ex) {
							LOGGER.error("A database error occured while removing the following album: '" + ApplicationUI.getSelectedAlbum() + "'", ex);
						}
					}
				}
			}
		});
		
		popupMenuForAlbumList.addListener(SWT.Show, new Listener() {
			@Override
			public void handleEvent(Event event) {
				createNewAlbum.setEnabled(true);
				boolean extraMenuItemsEnabled = true;
				
				if (albumList.getItemCount() == 0) {
					extraMenuItemsEnabled = false;
				}
				
				moveAlbumTop.setEnabled(extraMenuItemsEnabled);
				moveAlbumOneDown.setEnabled(extraMenuItemsEnabled);
				moveAlbumOneUp.setEnabled(extraMenuItemsEnabled);
				moveAlbumBottom.setEnabled(extraMenuItemsEnabled);
				sortBy.setEnabled(extraMenuItemsEnabled);
				albumListPopupMenuItemSeparator1.setEnabled(extraMenuItemsEnabled);
				albumListPopupMenuItemSeparator2.setEnabled(extraMenuItemsEnabled);
				albumListPopupMenuItemSeparator3.setEnabled(extraMenuItemsEnabled);	
				removeAlbum.setEnabled(extraMenuItemsEnabled);
			}
		});			
		
		quickSearchText.setEnabled(false);
		
		savedSearchesList.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent selectionEvent) {}

			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {															
				if (savedSearchesList.getItemCount() == 0) {
					return;
				} else if (savedSearchesList.getSelectionIndex() < 0) {
					savedSearchesList.select(0);					
				}
				
				String savedSearchesListItem = savedSearchesList.getItem(savedSearchesList.getSelectionIndex());
					
				GuiController.getGuiState().setSelectedSavedSearch(savedSearchesListItem);
				EventObservable.addEventToQueue(SammelboxEvent.SAVED_SEARCH_SELECTED);
				
				BrowserFacade.performBrowserQueryAndShow(SavedSearchManager.getSqlQueryBySavedSearchName(
						GuiController.getGuiState().getSelectedAlbum(), GuiController.getGuiState().getSelectedSavedSearch()));

				WelcomePageManager.increaseClickCountForAlbumOrView(savedSearchesList.getItem(savedSearchesList.getSelectionIndex()));
			}
		});			

		Menu popupMenuForSavedSearchList = new Menu(savedSearchesList);
		
		savedSearchesList.addListener(SWT.MenuDetect, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (!GuiController.getGuiState().isAlbumSelected()) {
					// A saved search has to have a parent album. Don't do anything if no album has been selected
					event.doit = false;
				} else if ((savedSearchesList.getItemCount() > 0) && (savedSearchesList.getSelectionIndex() < 0)) {
					savedSearchesList.select(0);
				}
			}
		});		

		final MenuItem moveViewTop = new MenuItem(popupMenuForSavedSearchList, SWT.NONE);
		moveViewTop.setText(Translator.get(DictKeys.DROPDOWN_MOVE_TO_TOP));
		moveViewTop.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (savedSearchesList.getSelectionIndex() > 0) {
					SavedSearchManager.moveToFront(
							GuiController.getGuiState().getSelectedAlbum(), savedSearchesList.getSelectionIndex());
				}
			}
		});

		final MenuItem moveViewOneUp = new MenuItem(popupMenuForSavedSearchList, SWT.NONE);
		moveViewOneUp.setText(Translator.get(DictKeys.DROPDOWN_MOVE_ONE_UP));
		moveViewOneUp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (savedSearchesList.getSelectionIndex() > 0) {
					SavedSearchManager.moveOneUp(
							GuiController.getGuiState().getSelectedAlbum(), savedSearchesList.getSelectionIndex());
				}
			}
		});

		final MenuItem moveViewOneDown = new MenuItem(popupMenuForSavedSearchList, SWT.NONE);
		moveViewOneDown.setText(Translator.get(DictKeys.DROPDOWN_MOVE_ONE_DOWN));
		moveViewOneDown.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (savedSearchesList.getSelectionIndex() > 0) {
					SavedSearchManager.moveOneDown(
							GuiController.getGuiState().getSelectedAlbum(), savedSearchesList.getSelectionIndex());
				}
			}
		});

		final MenuItem moveViewBottom = new MenuItem(popupMenuForSavedSearchList, SWT.NONE);
		moveViewBottom.setText(Translator.get(DictKeys.DROPDOWN_MOVE_TO_BOTTOM));
		moveViewBottom.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (savedSearchesList.getSelectionIndex() < savedSearchesList.getItemCount()-1) {
					SavedSearchManager.moveToBottom(
							GuiController.getGuiState().getSelectedAlbum(), savedSearchesList.getSelectionIndex());
				}
			}
		});

		final MenuItem viewListPopupMenuItemSeparator1 = new MenuItem(popupMenuForSavedSearchList, SWT.SEPARATOR);

		final MenuItem editSavedSearch = new MenuItem(popupMenuForSavedSearchList, SWT.NONE);
		editSavedSearch.setText(Translator.get(DictKeys.DROPDOWN_EDIT_SAVED_SEARCH));
		editSavedSearch.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (savedSearchesList.getSelectionIndex() >= 0) {
					SavedSearch savedSearch = SavedSearchManager.getSavedSearchByName(
							GuiController.getGuiState().getSelectedAlbum(), GuiController.getGuiState().getSelectedSavedSearch());
					
					ApplicationUI.changeRightCompositeTo(PanelType.ADVANCED_SEARCH, 
							AdvancedSearchSidepane.build(ApplicationUI.getThreePanelComposite(), ApplicationUI.getSelectedAlbum(), savedSearch));
				}
			}
		});
		
		final MenuItem removeSavedSearch = new MenuItem(popupMenuForSavedSearchList, SWT.NONE);
		removeSavedSearch.setText(Translator.get(DictKeys.DROPDOWN_REMOVE));
		removeSavedSearch.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (savedSearchesList.getSelectionIndex() >= 0) {
					MessageBox messageBox = ComponentFactory.getMessageBox(Translator.get(DictKeys.DIALOG_TITLE_DELETE_SAVED_SEARCH), 
							Translator.get(DictKeys.DIALOG_CONTENT_DELETE_SAVED_SEARCH, savedSearchesList.getItem(savedSearchesList.getSelectionIndex())),
							SWT.ICON_WARNING | SWT.YES | SWT.NO);
					
					if (messageBox.open() == SWT.YES) {
						SavedSearchManager.removeSavedSearch(
								GuiController.getGuiState().getSelectedAlbum(), savedSearchesList.getItem(savedSearchesList.getSelectionIndex()));
					}
				}
			}
		});	

		final MenuItem viewListPopupMenuItemSeparator2 = new MenuItem(popupMenuForSavedSearchList, SWT.SEPARATOR);

		final MenuItem addSavedSearch = new MenuItem(popupMenuForSavedSearchList, SWT.NONE);
		addSavedSearch.setText(Translator.get(DictKeys.DROPDOWN_ADD_ANOTHER_SAVED_SEARCH));
		addSavedSearch.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (savedSearchesList.getSelectionIndex() >= 0) {
					ApplicationUI.changeRightCompositeTo(PanelType.ADVANCED_SEARCH, 
							AdvancedSearchSidepane.build(ApplicationUI.getThreePanelComposite(), ApplicationUI.getSelectedAlbum()));
				}
			}
		});		

		savedSearchesList.setMenu(popupMenuForSavedSearchList);
		
		popupMenuForSavedSearchList.addListener(SWT.Show, new Listener() {
			@Override
			public void handleEvent(Event event) {			
				boolean extraMenuItemsEnabled = true;
				
				if (savedSearchesList.getItemCount() == 0) {
					extraMenuItemsEnabled = false;
				}
				
				moveViewTop.setEnabled(extraMenuItemsEnabled);
				moveViewOneUp.setEnabled(extraMenuItemsEnabled);
				moveViewOneDown.setEnabled(extraMenuItemsEnabled);
				moveViewBottom.setEnabled(extraMenuItemsEnabled);
				viewListPopupMenuItemSeparator1.setEnabled(extraMenuItemsEnabled);
				viewListPopupMenuItemSeparator2.setEnabled(extraMenuItemsEnabled);
				removeSavedSearch.setEnabled(extraMenuItemsEnabled);
				addSavedSearch.setEnabled(true);
			}
		});

		return quickControlComposite;
	}
}
