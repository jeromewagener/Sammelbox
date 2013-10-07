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

import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.sammelbox.controller.MetaItemFieldFilter;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.controller.managers.AlbumViewManager;
import org.sammelbox.controller.settings.SettingsManager;
import org.sammelbox.model.album.FieldType;
import org.sammelbox.model.album.MetaItemField;
import org.sammelbox.model.album.OptionType;
import org.sammelbox.model.database.QueryBuilder;
import org.sammelbox.model.database.QueryComponent;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseOperations;
import org.sammelbox.view.ApplicationUI;
import org.sammelbox.view.various.ComponentFactory;
import org.sammelbox.view.various.TextInputDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AdvancedSearchSidepane {
	private static final Logger LOGGER = LoggerFactory.getLogger(AdvancedSearchSidepane.class);
	
	private AdvancedSearchSidepane() {
		// use build method instead
	}
	
	/** Returns an advanced search composite providing the means for easily building and executing SQL queries. The composite is 
	 * automatically created based on the fields of the specified album.
	 * @param parentComposite the parent composite
	 * @param album the album upon which the query should be executed. The composite will be based on the fields of this album.
	 * @return a new advanced search composite */
	public static Composite build(final Composite parentComposite, final String album) {
		// setup advanced composite
		final Composite advancedSearchComposite = new Composite(parentComposite, SWT.NONE);
		advancedSearchComposite.setLayout(new GridLayout(1, false));
		advancedSearchComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		ComponentFactory.getPanelHeaderComposite(advancedSearchComposite, Translator.get(DictKeys.LABEL_ADVANCED_SEARCH));

		Composite innerComposite = new Composite(advancedSearchComposite, SWT.BORDER);
		innerComposite.setLayout(new GridLayout(2, false));
		innerComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label fieldToSearchLabel = new Label(innerComposite, SWT.NONE);
		fieldToSearchLabel.setText(Translator.get(DictKeys.LABEL_FIELD_TO_SEARCH));
		final Combo fieldToSearchCombo = new Combo(innerComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		fieldToSearchCombo.setLayoutData(new GridData(GridData.FILL_BOTH));

		try {
			// Fill the comboBox
			fieldToSearchCombo.setData("validMetaItemFields", MetaItemFieldFilter.getValidMetaItemFields(DatabaseOperations.getAlbumItemFieldNamesAndTypes(album)));
			fieldToSearchCombo.setItems(MetaItemFieldFilter.getValidFieldNamesAsStringArray(DatabaseOperations.getAlbumItemFieldNamesAndTypes(album)));	
		} catch (DatabaseWrapperOperationException ex) {
			LOGGER.error("A database related error occured", ex);
		}
		Label searchOperatorLabel = new Label(innerComposite, SWT.NONE);
		searchOperatorLabel.setText(Translator.get(DictKeys.LABEL_SEARCH_OPERATOR));
		final Combo searchOperatorCombo = new Combo(innerComposite, SWT.DROP_DOWN | SWT.READ_ONLY);	
		searchOperatorCombo.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label valueToSearchLabel = new Label(innerComposite, SWT.NONE);
		valueToSearchLabel.setText(Translator.get(DictKeys.LABEL_VALUE_TO_SEARCH));
		final Text valueToSearchText = new Text(innerComposite, SWT.BORDER);
		valueToSearchText.setLayoutData(new GridData(GridData.FILL_BOTH));

		fieldToSearchCombo.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (fieldToSearchCombo.getSelectionIndex() != -1) {
					for (MetaItemField metaItemField : (java.util.List<MetaItemField>) fieldToSearchCombo.getData("validMetaItemFields")) {
						if (metaItemField.getName().equals(fieldToSearchCombo.getItem(fieldToSearchCombo.getSelectionIndex()))) {
							if (metaItemField.getType() == FieldType.TEXT) {
								searchOperatorCombo.setItems(QueryBuilder.toTextOperatorStringArray());
								valueToSearchText.setText("");
							} else if (metaItemField.getType() == FieldType.STAR_RATING) {
								searchOperatorCombo.setItems(QueryBuilder.toNumberOperatorStringArray());
								valueToSearchText.setText("[0..5]");
							} else if (metaItemField.getType() == FieldType.DECIMAL) {
								searchOperatorCombo.setItems(QueryBuilder.toNumberOperatorStringArray());
								valueToSearchText.setText("");
							} else if (metaItemField.getType() == FieldType.INTEGER) {
								searchOperatorCombo.setItems(QueryBuilder.toNumberOperatorStringArray());
								valueToSearchText.setText("");
							} else if (metaItemField.getType() == FieldType.DATE) {
								searchOperatorCombo.setItems(QueryBuilder.toDateOperatorStringArray());

								SimpleDateFormat sdfmt = new SimpleDateFormat();
								sdfmt.applyPattern("d/M/yyyy");

								valueToSearchText.setText(sdfmt.format(new Date(System.currentTimeMillis())));
							} else if (metaItemField.getType() == FieldType.TIME) {
								searchOperatorCombo.setItems(QueryBuilder.toDateOperatorStringArray());
								valueToSearchText.setText("");
							} else if (metaItemField.getType() == FieldType.OPTION) {
								searchOperatorCombo.setItems(QueryBuilder.toYesNoOperatorStringArray());

								searchOperatorCombo.select(0);
								valueToSearchText.setText(
										Translator.get(DictKeys.BROWSER_YES) + " | " + 
										Translator.get(DictKeys.BROWSER_NO) + " | " + 
										Translator.get(DictKeys.BROWSER_UNKNOWN));
							}
							
							// Auto select first operator
							searchOperatorCombo.select(0);
						}
					}
				}
			}
		});	
		
		Button addToSearchButton = new Button(innerComposite, SWT.PUSH);
		addToSearchButton.setText(Translator.get(DictKeys.BUTTON_ADD_TO_SEARCH));
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 2;
		addToSearchButton.setLayoutData(gridData);
		// add-search-component button listener after table definition

		// Field table
		final Table searchQueryTable = new Table(advancedSearchComposite, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		searchQueryTable.setLinesVisible(true);
		searchQueryTable.setHeaderVisible(true);

		// add-search-component button listener
		addToSearchButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if ((fieldToSearchCombo.getSelectionIndex() == -1) || (searchOperatorCombo.getSelectionIndex() == -1) || valueToSearchText.getText().isEmpty()) {				
					ComponentFactory.getMessageBox(Translator.get(DictKeys.DIALOG_TITLE_SELECT_QUERY_COMPONENTS),
							Translator.get(DictKeys.DIALOG_CONTENT_SELECT_QUERY_COMPONENTS),
							SWT.ICON_WARNING | SWT.OK).open();
				} else {
					TableItem item = new TableItem(searchQueryTable, SWT.NONE);
					item.setText(0, fieldToSearchCombo.getItem(fieldToSearchCombo.getSelectionIndex()));
					item.setText(1, searchOperatorCombo.getItem(searchOperatorCombo.getSelectionIndex()));
					item.setText(2, valueToSearchText.getText());
	
					valueToSearchText.setText("");
				}
			}
		});	

		// Setup table
		TableColumn fieldNameColumn = new TableColumn(searchQueryTable, SWT.NONE);
		fieldNameColumn.setText(Translator.get(DictKeys.TABLE_COLUMN_FIELD_NAME));
		TableColumn operatorColumn = new TableColumn(searchQueryTable, SWT.NONE);
		operatorColumn.setText(Translator.get(DictKeys.TABLE_COLUMN_OPERATOR));
		TableColumn valueColumn = new TableColumn(searchQueryTable, SWT.NONE);
		valueColumn.setText(Translator.get(DictKeys.TABLE_COLUMN_VALUE));
		searchQueryTable.getColumn(0).pack ();
		searchQueryTable.getColumn(1).pack ();
		searchQueryTable.getColumn(2).pack ();

		// Pop-Up menu
		Menu popupMenu = new Menu(searchQueryTable);
		MenuItem remove = new MenuItem(popupMenu, SWT.NONE);
		remove.setText(Translator.get(DictKeys.DROPDOWN_REMOVE));
		remove.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (searchQueryTable.getSelectionIndex() != -1) {
					searchQueryTable.getItem(searchQueryTable.getSelectionIndex()).dispose();
				}
			}
		});

		MenuItem removeAll = new MenuItem(popupMenu, SWT.NONE);
		removeAll.setText(Translator.get(DictKeys.DROPDOWN_REMOVE_ALL));
		removeAll.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (searchQueryTable.getSelectionIndex() != -1) {
					for (TableItem tableItem : searchQueryTable.getItems()) {
						tableItem.dispose();
					}
				}
			}
		});

		searchQueryTable.setMenu(popupMenu);

		// Set table layout data
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 110;
		searchQueryTable.setLayoutData(data);	

		ComponentFactory.getSmallBoldItalicLabel(advancedSearchComposite, Translator.get(DictKeys.LABEL_CONNECT_SEARCH_TERMS_BY));
		Composite composite = new Composite(advancedSearchComposite, SWT.BORDER);
		composite.setLayout(new RowLayout());
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		final Button andButton = new Button(composite, SWT.RADIO);
		andButton.setText(Translator.get(DictKeys.BUTTON_AND));
		andButton.setSelection(true);
		Button orButton = new Button(composite, SWT.RADIO);
		orButton.setText(Translator.get(DictKeys.BUTTON_OR));

		ComponentFactory.getSmallBoldItalicLabel(advancedSearchComposite, Translator.get(DictKeys.LABEL_SORT_BY));
		composite = new Composite(advancedSearchComposite, SWT.BORDER);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		Label fieldToSortLabel = new Label(composite, SWT.NONE);
		fieldToSortLabel.setText(Translator.get(DictKeys.LABEL_FIELD_TO_SORT));
		final Combo fieldToSortCombo = new Combo(composite, SWT.DROP_DOWN);
		fieldToSortCombo.setLayoutData(new GridData(GridData.FILL_BOTH));

		try {
			// Fill the comboBox
			fieldToSortCombo.setData("validMetaItemFields", MetaItemFieldFilter.getValidMetaItemFields(DatabaseOperations.getAlbumItemFieldNamesAndTypes(album)));
			fieldToSortCombo.setItems(MetaItemFieldFilter.getValidFieldNamesAsStringArray(DatabaseOperations.getAlbumItemFieldNamesAndTypes(album)));
		} catch (DatabaseWrapperOperationException ex) {
			LOGGER.error("A database related error occured", ex);
		}
		
		final Button sortAscendingButton = new Button(composite, SWT.RADIO);
		sortAscendingButton.setText(Translator.get(DictKeys.BUTTON_SORT_ASCENDING));
		sortAscendingButton.setSelection(true);
		Button sortDescendingButton = new Button(composite, SWT.RADIO);
		sortDescendingButton.setText(Translator.get(DictKeys.BUTTON_SORT_DESCENDING));

		Button searchButton = new Button(advancedSearchComposite, SWT.PUSH);
		searchButton.setText(Translator.get(DictKeys.BUTTON_EXECUTE_SEARCH));
		searchButton.setLayoutData(new GridData(GridData.FILL_BOTH));
		searchButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ArrayList<org.sammelbox.model.database.QueryComponent> queryComponents = getQueryComponentsForAdvancedSearch(searchQueryTable);

				boolean connectByAnd = false;
				if (andButton.getSelection()) {
					connectByAnd = true;
				}

				if (fieldToSortCombo.getSelectionIndex() != -1) {
					QueryBuilder.buildQueryAndExecute(queryComponents, connectByAnd, album, fieldToSortCombo.getItem(fieldToSortCombo.getSelectionIndex()), sortAscendingButton.getSelection());
				} else {
					QueryBuilder.buildQueryAndExecute(queryComponents, connectByAnd, album);
				}
			}
		});

		Button saveAsViewButton = new Button(advancedSearchComposite, SWT.PUSH);
		saveAsViewButton.setText(Translator.get(DictKeys.BUTTON_SAVE_THIS_SEARCH));
		saveAsViewButton.setLayoutData(new GridData(GridData.FILL_BOTH));
		saveAsViewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!ApplicationUI.isAlbumSelectedAndShowMessageIfNot()) {
					return;
				}

				ArrayList<QueryComponent> queryComponents = getQueryComponentsForAdvancedSearch(searchQueryTable);

				boolean connectByAnd = false;
				if (andButton.getSelection()) {
					connectByAnd = true;
				}

				TextInputDialog textInputDialog = new TextInputDialog(parentComposite.getShell());
				String viewName = textInputDialog.open(
						Translator.get(DictKeys.DIALOG_TITLE_ENTER_VIEW_NAME), 
						Translator.get(DictKeys.DIALOG_CONTENT_ENTER_VIEW_NAME), 
						Translator.get(DictKeys.DIALOG_TEXTBOX_ENTER_VIEW_NAME),
						Translator.get(DictKeys.DIALOG_BUTTON_ENTER_VIEW_NAME));

				if (viewName != null && !AlbumViewManager.hasViewWithName(album, viewName)) {				
					if (fieldToSortCombo.getSelectionIndex() != -1) {
						AlbumViewManager.addAlbumView(
								viewName, ApplicationUI.getSelectedAlbum(), 
								QueryBuilder.buildQuery(queryComponents, connectByAnd, album, 
										fieldToSortCombo.getItem(fieldToSortCombo.getSelectionIndex()), sortAscendingButton.getSelection()));
					} else {
						AlbumViewManager.addAlbumView(
								viewName, ApplicationUI.getSelectedAlbum(), 
								QueryBuilder.buildQuery(queryComponents, connectByAnd, album));						
					}
				} else {
					ComponentFactory.getMessageBox(
							Translator.get(DictKeys.DIALOG_TITLE_VIEW_NAME_ALREADY_USED), 
							Translator.get(DictKeys.DIALOG_CONTENT_VIEW_NAME_ALREADY_USED), 
							SWT.ICON_INFORMATION).open();
				}
			}
		});

		return advancedSearchComposite;
	}
	
	private static ArrayList<QueryComponent> getQueryComponentsForAdvancedSearch(Table searchQueryTable) {
		ArrayList<QueryComponent> queryComponents = new ArrayList<QueryComponent>();
		try {
			for (int i=0; i < searchQueryTable.getItemCount(); i++) {					
				// In case of a date
				if (DatabaseOperations.isDateField(ApplicationUI.getSelectedAlbum(), searchQueryTable.getItem(i).getText(0))) {
					// Convert string to milliseconds
					DateFormat df = new SimpleDateFormat(SettingsManager.getSettings().getDateFormat());
					java.util.Date result = null;
					try {
						result = df.parse(searchQueryTable.getItem(i).getText(2));
						long dateInMilliseconds = result.getTime();

						queryComponents.add(QueryBuilder.getQueryComponent(
								searchQueryTable.getItem(i).getText(0),
								QueryBuilder.getQueryOperator(searchQueryTable.getItem(i).getText(1)),
								String.valueOf(dateInMilliseconds)));
					} catch (ParseException e1) {
						ComponentFactory.getMessageBox(
								Translator.get(DictKeys.DIALOG_TITLE_DATE_FORMAT),
								Translator.get(DictKeys.DIALOG_CONTENT_DATE_FORMAT),
								SWT.ICON_WARNING | SWT.OK).open();
					}

				// In case of an option
				} else if (DatabaseOperations.isOptionField(ApplicationUI.getSelectedAlbum(), searchQueryTable.getItem(i).getText(0))) {
					String value = searchQueryTable.getItem(i).getText(2);

					String option = null;

					if (value.equals(Translator.get(DictKeys.BROWSER_YES))) {
						option = OptionType.getDatabaseOptionValue(DictKeys.BROWSER_YES);
					} else if (value.equals(Translator.get(DictKeys.BROWSER_NO))) {
						option = OptionType.getDatabaseOptionValue(DictKeys.BROWSER_NO);
					} else if (value.equals(Translator.get(DictKeys.BROWSER_UNKNOWN))) {
						option = OptionType.getDatabaseOptionValue(DictKeys.BROWSER_UNKNOWN);
					}

					if (option != null) {
						queryComponents.add(QueryBuilder.getQueryComponent(
								searchQueryTable.getItem(i).getText(0),
								QueryBuilder.getQueryOperator(searchQueryTable.getItem(i).getText(1)),
								option));
					} else {
						ComponentFactory.getMessageBox(
								Translator.get(DictKeys.DIALOG_TITLE_ENTER_OPTION),
								Translator.get(DictKeys.DIALOG_CONTENT_ENTER_OPTION, searchQueryTable.getItem(i).getText(0)),
								SWT.ICON_WARNING | SWT.OK).open();
					}
				// All other cases
				} else {
					queryComponents.add(QueryBuilder.getQueryComponent(
							searchQueryTable.getItem(i).getText(0),
							QueryBuilder.getQueryOperator(searchQueryTable.getItem(i).getText(1)),
							searchQueryTable.getItem(i).getText(2)));
				}
			}			
		} catch (DatabaseWrapperOperationException dwoe) {
			LOGGER.error("A database related error occured", dwoe);
		}
		
		return queryComponents;	
	}
}
