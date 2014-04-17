package org.sammelbox.view.composites;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.sammelbox.controller.GuiController;
import org.sammelbox.controller.filters.ItemFieldFilterPlusID;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.controller.managers.SettingsManager;
import org.sammelbox.model.album.AlbumItem;
import org.sammelbox.model.album.AlbumItemStore;
import org.sammelbox.model.album.FieldType;
import org.sammelbox.model.album.ItemField;
import org.sammelbox.model.album.OptionType;
import org.sammelbox.model.album.StarRating;
import org.sammelbox.model.database.QueryBuilder;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseConstants;
import org.sammelbox.model.database.operations.DatabaseOperations;
import org.sammelbox.view.browser.BrowserFacade;
import org.sammelbox.view.browser.BrowserUtils;
import org.sammelbox.view.various.ComponentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

			int fieldIndex = 0;
			for (ItemField itemField : ItemFieldFilterPlusID.getValidItemFields(albumItem.getFields())) {
				if (itemField.getName().equals(DatabaseConstants.ID_COLUMN_NAME)) {
					tableItem.setData(ID_TABLE_DATA_KEY, itemField.getValue());
				} else if (!itemField.getType().equals(FieldType.ID)) {
					if (itemField.getType().equals(FieldType.OPTION)) {
						values[fieldIndex] = OptionType.getTranslation((OptionType) itemField.getValue());
					} else if (itemField.getType().equals(FieldType.STAR_RATING)) {
						values[fieldIndex] = StarRating.getTranslation((StarRating) itemField.getValue());
					} else {
						values[fieldIndex] = String.valueOf(itemField.getValue());
					}
				}  else if (itemField.getType().equals(FieldType.DATE)) {
					java.sql.Date sqlDate = itemField.getValue();
					if (sqlDate != null) {
						java.util.Date utilDate = new java.util.Date(sqlDate.getTime());
		
						SimpleDateFormat dateFormater = new SimpleDateFormat(SettingsManager.getSettings().getDateFormat());
						values[fieldIndex] = dateFormater.format(utilDate);
					} else {
						values[fieldIndex] = "";
					}
				}
				
				fieldIndex++;
			}
						
			tableItem.setText(values);
		}
	}
	
	public static Composite build(Composite parentComposite) {
		Composite tableComposite = new Composite(parentComposite, SWT.NONE);
		GridLayout tableCompositeGridLayout = new GridLayout();
		tableComposite.setLayout(tableCompositeGridLayout);

		final Table table = new Table(tableComposite, SWT.BORDER | SWT.CHECK);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		table.setLayoutData(gridData);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		for (ItemField itemField : ItemFieldFilterPlusID.getValidItemFields(
				AlbumItemStore.getAlbumItems().get(0).getFields())) {
			
			TableColumn tableColumn = new TableColumn(table, SWT.LEFT);
			
			if (itemField.getName().equals(DatabaseConstants.ID_COLUMN_NAME)) {
				tableColumn.setWidth(CHECKBOX_COLUMN_WIDTH_PIXELS);
			} else if (!itemField.getType().equals(FieldType.ID)) {
				tableColumn.setText(itemField.getName());
				tableColumn.setWidth(COLUMN_WIDTH_PIXELS);
			}
		}
		
		initializeWithItemsFromAlbumItemStore(table);

		Composite buttonComposite = new Composite(tableComposite, SWT.NONE);
		buttonComposite.setLayout(new GridLayout(5, false));
		
		Button btnEdit = new Button(buttonComposite, SWT.PUSH);
		btnEdit.setText(Translator.toBeTranslated("Edit Row"));
		btnEdit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				TableItem[] tableItems = table.getItems();
				List<Long> selectedItemIds = new ArrayList<Long>();
				
				for (TableItem tableItem : tableItems) {
					if (tableItem.getChecked()) {
						selectedItemIds.add((Long) tableItem.getData(ID_TABLE_DATA_KEY));
					}
				}
				
				BrowserFacade.showEditableSpreadsheet();
			}
		});

		Button btnCloneRow = new Button(buttonComposite, SWT.PUSH);
		btnCloneRow.setText(Translator.toBeTranslated("Clone Row"));
		btnCloneRow.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				TableItem[] tableItems = table.getItems();
				List<Long> selectedItemIds = new ArrayList<Long>();
				
				// search for selected table items
				for (TableItem tableItem : tableItems) {
					if (tableItem.getChecked()) {
						selectedItemIds.add((Long) tableItem.getData(ID_TABLE_DATA_KEY));
					}
				}

				// do you really want to clone these items
				MessageBox messageBox = ComponentFactory.getMessageBox(
						Translator.toBeTranslated("Clone selected items?"), 
						Translator.toBeTranslated("Are you sure that you want to clone the selected items?"), 
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
									Translator.toBeTranslated("An error occurred"),
									Translator.toBeTranslated("An error occurred while cloneing the selected items."), 
									SWT.ICON_ERROR | SWT.OK);
						}
					}
				}
				
				// update album item store
				try {
					AlbumItemStore.reinitializeStore(DatabaseOperations.executeSQLQuery(
							QueryBuilder.createSelectStarQuery(GuiController.getGuiState().getSelectedAlbum())));
				} catch (DatabaseWrapperOperationException e) {
					LOGGER.error("An error occurred while reinitializing the album item store", e);
					ComponentFactory.getMessageBox(
							Translator.toBeTranslated("An error occurred"),
							Translator.toBeTranslated("An error occurred while refreshing the view. Please refresh manually!"), 
							SWT.ICON_ERROR | SWT.OK);
				}
				
				initializeWithItemsFromAlbumItemStore(table);
			}
		});

		Button btnDelete = new Button(buttonComposite, SWT.PUSH);
		btnDelete.setText(Translator.toBeTranslated("Delete Row"));
		btnDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				TableItem[] tableItems = table.getItems();
				List<Long> selectedItemIds = new ArrayList<Long>();
				
				// search for selected table items
				for (TableItem tableItem : tableItems) {
					if (tableItem.getChecked()) {
						selectedItemIds.add((Long) tableItem.getData(ID_TABLE_DATA_KEY));
					}
				}
				
				// do you really want to delete these items?
				MessageBox messageBox = ComponentFactory.getMessageBox(
						Translator.toBeTranslated("Delete selected items?"), 
						Translator.toBeTranslated("Are you sure that you want to delete the selected items?"), 
						SWT.ICON_WARNING | SWT.YES | SWT.NO);
				
				// if yes, delete selected items
				if (messageBox.open() == SWT.YES) {
					for (Long selectedId : selectedItemIds) {
						try {
							DatabaseOperations.deleteAlbumItem(AlbumItemStore.getAlbumItem(selectedId));
						} catch (DatabaseWrapperOperationException e) {
							LOGGER.error("An error occurred while deleting the album item", e);
							ComponentFactory.getMessageBox(
									Translator.toBeTranslated("An error occurred"),
									Translator.toBeTranslated("An error occurred while deleting the selected items."), 
									SWT.ICON_ERROR | SWT.OK);
						}
					}
				}
				
				// update album item store
				try {
					AlbumItemStore.reinitializeStore(DatabaseOperations.executeSQLQuery(
							QueryBuilder.createSelectStarQuery(GuiController.getGuiState().getSelectedAlbum())));
				} catch (DatabaseWrapperOperationException e) {
					LOGGER.error("An error occurred while reinitializing the album item store", e);
					ComponentFactory.getMessageBox(
							Translator.toBeTranslated("An error occurred"),
							Translator.toBeTranslated("An error occurred while refreshing the view. Please refresh manually!"), 
							SWT.ICON_ERROR | SWT.OK);
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
		btnAddItems.setText(Translator.toBeTranslated("Add Items"));
		
		return tableComposite;
	}
}
