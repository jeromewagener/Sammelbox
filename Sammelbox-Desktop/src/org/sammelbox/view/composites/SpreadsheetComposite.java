package org.sammelbox.view.composites;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.sammelbox.controller.GuiController;
import org.sammelbox.controller.filters.ItemFieldFilterPlusID;
import org.sammelbox.controller.filters.MetaItemFieldFilterPlusID;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.controller.managers.SettingsManager;
import org.sammelbox.model.album.*;
import org.sammelbox.model.database.QueryBuilder;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseConstants;
import org.sammelbox.model.database.operations.DatabaseOperations;
import org.sammelbox.view.ApplicationUI;
import org.sammelbox.view.browser.BrowserFacade;
import org.sammelbox.view.browser.BrowserUtils;
import org.sammelbox.view.sidepanes.AddAlbumItemSidepane;
import org.sammelbox.view.sidepanes.ImageViewerSidepane;
import org.sammelbox.view.sidepanes.UpdateAlbumItemSidepane;
import org.sammelbox.view.various.ComponentFactory;
import org.sammelbox.view.various.PanelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class SpreadsheetComposite {
	private static final String ID_TABLE_DATA_KEY = "ID";
	private static final int CHECKBOX_COLUMN_WIDTH_PIXELS = 25;
	private static final int COLUMN_WIDTH_PIXELS = 125;
	private static final Logger LOGGER = LoggerFactory.getLogger(BrowserUtils.class);
	
	public static void initializeWithItemsFromAlbumItemStore(Table table) {
		table.removeAll();
				
		for (AlbumItem albumItem : AlbumItemStore.getAlbumItems()) {
			TableItem tableItem = new TableItem(table, SWT.NONE);
			String[] values = new String[albumItem.getFields().size()];

			int fieldIndex = 1;
			for (ItemField itemField : ItemFieldFilterPlusID.getValidItemFields(albumItem.getFields())) {
				if (itemField.getName().equals(DatabaseConstants.ID_COLUMN_NAME)) {
					tableItem.setData(ID_TABLE_DATA_KEY, itemField.getValue());
				} else if (!itemField.getType().equals(FieldType.ID)) {
					if (itemField.getType().equals(FieldType.OPTION)) {
						values[fieldIndex] = OptionType.getTranslation((OptionType) itemField.getValue());
					} else if (itemField.getType().equals(FieldType.STAR_RATING)) {
						values[fieldIndex] = StarRating.getTranslation((StarRating) itemField.getValue());
					} else if (itemField.getType().equals(FieldType.DATE)) {
						java.sql.Date sqlDate = itemField.getValue();
						if (sqlDate != null) {
							java.util.Date utilDate = new java.util.Date(sqlDate.getTime());
			
							SimpleDateFormat dateFormater = new SimpleDateFormat(SettingsManager.getSettings().getDateFormat());
							values[fieldIndex] = dateFormater.format(utilDate);
						} else {
							values[fieldIndex] = "";
						}
					} else {
						values[fieldIndex] = String.valueOf(itemField.getValue());
					}
					
					fieldIndex++;
				}
			}
						
			tableItem.setText(values);
		}
	}
	
	private static void showNoItemSelectedMessage() {
		ComponentFactory.getMessageBox(Translator.get(
				DictKeys.ERROR_NO_ITEM_SELECTED_HEADER),
				Translator.get(DictKeys.ERROR_NO_ITEM_SELECTED),
				SWT.OK | SWT.ICON_INFORMATION).open();
	}
	
	public static Composite build(Composite parentComposite) {
		Composite tableComposite = new Composite(parentComposite, SWT.NONE);
		GridLayout tableCompositeGridLayout = new GridLayout();
		tableComposite.setLayout(tableCompositeGridLayout);

		final Table table = new Table(tableComposite, SWT.BORDER | SWT.CHECK | SWT.FULL_SELECTION);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		table.setLayoutData(gridData);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		addTablePopupMenu(table);
		
		List<MetaItemField> metaItemFields = new ArrayList<>();
		try {
			metaItemFields.addAll(DatabaseOperations.getMetaItemFields(GuiController.getGuiState().getSelectedAlbum()));
		} catch (DatabaseWrapperOperationException dwoe) {
			LOGGER.error("An error occurred while retrieving the meta item fields for the selected album.", dwoe);
		}
		
		TableColumn checkboxColumn = new TableColumn(table, SWT.LEFT);
		checkboxColumn.setWidth(CHECKBOX_COLUMN_WIDTH_PIXELS);
		
		for (MetaItemField metaItemField : MetaItemFieldFilterPlusID.getValidMetaItemFields(metaItemFields)) {
			TableColumn tableColumn = new TableColumn(table, SWT.LEFT);
			
			tableColumn.setText(metaItemField.getName());
			tableColumn.setWidth(COLUMN_WIDTH_PIXELS);
		}
		
		initializeWithItemsFromAlbumItemStore(table);

		Composite buttonComposite = new Composite(tableComposite, SWT.NONE);
		buttonComposite.setLayout(new GridLayout(6, false));
		
		Label lblDescription = new Label (buttonComposite, SWT.NONE);
		lblDescription.setText(Translator.get(DictKeys.LABEL_SPREADSHEET_ACTION));
		
		Button btnEdit = new Button(buttonComposite, SWT.PUSH);
		btnEdit.setText(Translator.get(DictKeys.BUTTON_EDIT_ROWS));
		btnEdit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				TableItem[] tableItems = table.getItems();
				List<Long> selectedItemIds = new ArrayList<>();
				
				for (TableItem tableItem : tableItems) {
					if (tableItem.getChecked()) {
						selectedItemIds.add((Long) tableItem.getData(ID_TABLE_DATA_KEY));
					}
				}
				
				// abort if no items have been selected
				if (selectedItemIds.isEmpty()) {
					showNoItemSelectedMessage();
				} else {				
					BrowserFacade.showEditItemsSpreadsheet(selectedItemIds);
				}
			}
		});

		Button btnCloneRow = new Button(buttonComposite, SWT.PUSH);
		btnCloneRow.setText(Translator.get(DictKeys.BUTTON_CLONE_ROWS));
		btnCloneRow.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				TableItem[] tableItems = table.getItems();
				List<Long> selectedItemIds = new ArrayList<>();
				
				// search for selected table items
				for (TableItem tableItem : tableItems) {
					if (tableItem.getChecked()) {
						selectedItemIds.add((Long) tableItem.getData(ID_TABLE_DATA_KEY));
					}
				}

				// abort if no items have been selected
				if (selectedItemIds.isEmpty()) {
					showNoItemSelectedMessage();
					return;
				}
				
				// do you really want to clone these items
				MessageBox messageBox = ComponentFactory.getMessageBox(
						Translator.get(DictKeys.WARNING), Translator.get(DictKeys.WARNING_CLONE_SELECTED), 
						SWT.ICON_WARNING | SWT.YES | SWT.NO);
				
				// if yes, clone them
				if (messageBox.open() == SWT.YES) {
					for (Long selectedId : selectedItemIds) {
						AlbumItem clonedAlbumItem = AlbumItemStore.getAlbumItem(selectedId).clone();
						try {
							DatabaseOperations.addAlbumItem(clonedAlbumItem, true);
						} catch (DatabaseWrapperOperationException e) {
							LOGGER.error("An error occurred while cloning the album item", e);
							ComponentFactory.getMessageBox(
									Translator.get(DictKeys.ERROR_AN_ERROR_OCCURRED_HEADER),
									Translator.get(DictKeys.ERROR_AN_ERROR_OCCURRED, e.getMessage()), 
									SWT.ICON_ERROR | SWT.OK);
						}
					}
				}
				
				// update album item store
				try {
					AlbumItemStore.reinitializeStore(DatabaseOperations.executeSQLQuery(
							QueryBuilder.createOrderedSelectStarQuery(GuiController.getGuiState().getSelectedAlbum())));
				} catch (DatabaseWrapperOperationException e) {
					LOGGER.error("An error occurred while reinitializing the album item store", e);
					ComponentFactory.getMessageBox(
							Translator.get(DictKeys.ERROR_AN_ERROR_OCCURRED_HEADER),
							Translator.get(DictKeys.ERROR_AN_ERROR_OCCURRED, e.getMessage()), 
							SWT.ICON_ERROR | SWT.OK);
				}
				
				initializeWithItemsFromAlbumItemStore(table);
			}
		});

		Button btnDelete = new Button(buttonComposite, SWT.PUSH);
		btnDelete.setText(Translator.get(DictKeys.BUTTON_DELETE_ROWS));
		btnDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				TableItem[] tableItems = table.getItems();
				List<Long> selectedItemIds = new ArrayList<>();
				
				// search for selected table items
				for (TableItem tableItem : tableItems) {
					if (tableItem.getChecked()) {
						selectedItemIds.add((Long) tableItem.getData(ID_TABLE_DATA_KEY));
					}
				}
				
				// abort if no items have been selected
				if (selectedItemIds.isEmpty()) {
					showNoItemSelectedMessage();
					return;
				}
				
				// do you really want to delete these items?
				MessageBox messageBox = ComponentFactory.getMessageBox(
						Translator.get(DictKeys.WARNING), Translator.get(DictKeys.WARNING_DELETE_SELECTED), 
						SWT.ICON_WARNING | SWT.YES | SWT.NO);
				
				// if yes, delete selected items
				if (messageBox.open() == SWT.YES) {
					for (Long selectedId : selectedItemIds) {
						try {
							DatabaseOperations.deleteAlbumItem(AlbumItemStore.getAlbumItem(selectedId));
						} catch (DatabaseWrapperOperationException e) {
							LOGGER.error("An error occurred while deleting the album item", e);
						}
					}
				}
				
				// update album item store
				try {
					AlbumItemStore.reinitializeStore(DatabaseOperations.executeSQLQuery(
							QueryBuilder.createOrderedSelectStarQuery(GuiController.getGuiState().getSelectedAlbum())));
				} catch (DatabaseWrapperOperationException e) {
					LOGGER.error("An error occurred while reinitializing the album item store", e);
				}
				
				initializeWithItemsFromAlbumItemStore(table);
			}
		});
		
		// min height griddata
		GridData minSizeGridData = new GridData(GridData.FILL_BOTH);
		minSizeGridData.widthHint = 10;
		minSizeGridData.heightHint = 10;
		
		// separator
		new Label(buttonComposite, SWT.SEPARATOR | SWT.VERTICAL).setLayoutData(minSizeGridData);
		
		Button btnAddItems = new Button(buttonComposite, SWT.PUSH);
		btnAddItems.setText(Translator.get(DictKeys.BUTTON_ADD_ROWS));
		btnAddItems.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				BrowserFacade.showAddItemsSpreadsheet();
			}
		});
		
		return tableComposite;
	}
	
	public static void addTablePopupMenu(final Table table) {
		Menu popupMenu = new Menu(table);
		
		MenuItem select = new MenuItem(popupMenu, SWT.NONE);
		select.setText(Translator.get(DictKeys.DROPDOWN_SELECT));
		select.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (table.getSelectionIndex() == -1) {
					return;
				}
				
				table.getItem(table.getSelectionIndex()).setChecked(
						!table.getItem(table.getSelectionIndex()).getChecked());
			}
		});
		
		new MenuItem(popupMenu, SWT.SEPARATOR);
		
		MenuItem editUsingSidepane = new MenuItem(popupMenu, SWT.NONE);
		editUsingSidepane.setText(Translator.get(DictKeys.DROPDOWN_EDIT_ITEM));
		editUsingSidepane.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (table.getSelectionIndex() == -1) {
					return;
				}
				
				Long albumItemId = (Long) table.getItem(table.getSelectionIndex()).getData(ID_TABLE_DATA_KEY);
				ApplicationUI.changeRightCompositeTo(
					PanelType.UPDATE_ENTRY, 
					UpdateAlbumItemSidepane.build(ApplicationUI.getThreePanelComposite(), GuiController.getGuiState().getSelectedAlbum(), albumItemId),
					albumItemId);
			}
		});
		
		MenuItem addUsingSidepane = new MenuItem(popupMenu, SWT.NONE);
		addUsingSidepane.setText(Translator.get(DictKeys.DROPDOWN_ADD_SINGLE_ITEM));
		addUsingSidepane.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ApplicationUI.changeRightCompositeTo(
					PanelType.ADD_ENTRY, 
					AddAlbumItemSidepane.build(
						ApplicationUI.getThreePanelComposite(), 
						GuiController.getGuiState().getSelectedAlbum()));
			}
		});
		
		new MenuItem(popupMenu, SWT.SEPARATOR);
		
		MenuItem showImages = new MenuItem(popupMenu, SWT.NONE);
		showImages.setText(Translator.get(DictKeys.DROPDOWN_SHOW_IMAGES));
		showImages.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (table.getSelectionIndex() == -1) {
					return;
				}
				
				Long albumItemId = (Long) table.getItem(table.getSelectionIndex()).getData(ID_TABLE_DATA_KEY);
				ApplicationUI.changeRightCompositeTo(
						PanelType.IMAGE_VIEWER,
						new ImageViewerSidepane(
								ApplicationUI.getThreePanelComposite(), 
								AlbumItemStore.getAlbumItem(albumItemId).getPictures()));
			}
		});
		
		table.setMenu(popupMenu);
	}
}
