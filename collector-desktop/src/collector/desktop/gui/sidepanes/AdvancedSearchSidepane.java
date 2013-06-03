package collector.desktop.gui.sidepanes;

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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import collector.desktop.Collector;
import collector.desktop.database.DatabaseWrapper;
import collector.desktop.database.FieldType;
import collector.desktop.database.MetaItemField;
import collector.desktop.gui.managers.AlbumViewManager;
import collector.desktop.gui.tobemoved.MetaItemFieldFilter;
import collector.desktop.gui.tobemoved.QueryBuilder;
import collector.desktop.gui.tobemoved.QueryBuilder.QueryComponent;
import collector.desktop.gui.tobemoved.QueryBuilder.QueryOperator;
import collector.desktop.gui.various.ComponentFactory;
import collector.desktop.gui.various.TextInputDialog;
import collector.desktop.internationalization.DictKeys;
import collector.desktop.internationalization.Translator;

public class AdvancedSearchSidepane {

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
		final Combo fieldToSearchCombo = new Combo(innerComposite, SWT.DROP_DOWN);
		fieldToSearchCombo.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Fill the comboBox
		fieldToSearchCombo.setData(
				"validMetaItemFields", MetaItemFieldFilter.getValidMetaItemFields(DatabaseWrapper.getAlbumItemFieldNamesAndTypes(album)));
		fieldToSearchCombo.setItems(
				MetaItemFieldFilter.getValidFieldNamesAsStringArray(DatabaseWrapper.getAlbumItemFieldNamesAndTypes(album)));	

		Label searchOperatorLabel = new Label(innerComposite, SWT.NONE);
		searchOperatorLabel.setText(Translator.get(DictKeys.LABEL_SEARCH_OPERATOR));
		final Combo searchOperatorCombo = new Combo(innerComposite, SWT.DROP_DOWN);	
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
							if (metaItemField.getType() == FieldType.Text) {
								searchOperatorCombo.setItems(QueryOperator.toTextOperatorStringArray());
								valueToSearchText.setText("");
							} else if (metaItemField.getType() == FieldType.Number) {
								searchOperatorCombo.setItems(QueryOperator.toNumberOperatorStringArray());
								valueToSearchText.setText("");
							} else if (metaItemField.getType() == FieldType.Date) {
								searchOperatorCombo.setItems(QueryOperator.toDateOperatorStringArray());

								SimpleDateFormat sdfmt = new SimpleDateFormat();
								sdfmt.applyPattern("d/M/yyyy");

								valueToSearchText.setText(sdfmt.format(new Date(System.currentTimeMillis())));
							} else if (metaItemField.getType() == FieldType.Time) {
								searchOperatorCombo.setItems(QueryOperator.toDateOperatorStringArray());
								valueToSearchText.setText("");
							} else if (metaItemField.getType() == FieldType.Option) {
								searchOperatorCombo.setItems(QueryOperator.toYesNoOperatorStringArray());

								searchOperatorCombo.select(0);
								valueToSearchText.setText(Translator.get(DictKeys.BROWSER_YES) + " | " + Translator.get(DictKeys.BROWSER_NO) + " | " + Translator.get(DictKeys.BROWSER_UNKNOWN));
							}							
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
				if ((fieldToSearchCombo.getSelectionIndex() == -1) 
						|| (searchOperatorCombo.getSelectionIndex() == -1) 
						|| valueToSearchText.getText().equals("")) {				
					MessageBox messageBox = ComponentFactory.getMessageBox(
							parentComposite.getShell(),
							Translator.get(DictKeys.DIALOG_TITLE_SELECT_QUERY_COMPONENTS),
							Translator.get(DictKeys.DIALOG_CONTENT_SELECT_QUERY_COMPONENTS),
							SWT.ICON_WARNING | SWT.OK);
					messageBox.open();
					return;
				}
				TableItem item = new TableItem(searchQueryTable, SWT.NONE);
				item.setText(0, fieldToSearchCombo.getItem(fieldToSearchCombo.getSelectionIndex()));
				item.setText(1, searchOperatorCombo.getItem(searchOperatorCombo.getSelectionIndex()));
				item.setText(2, valueToSearchText.getText());

				valueToSearchText.setText("");
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

		// Fill the comboBox
		fieldToSortCombo.setData("validMetaItemFields", MetaItemFieldFilter.getValidMetaItemFields(DatabaseWrapper.getAlbumItemFieldNamesAndTypes(album)));
		fieldToSortCombo.setItems(MetaItemFieldFilter.getValidFieldNamesAsStringArray(DatabaseWrapper.getAlbumItemFieldNamesAndTypes(album)));

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
				ArrayList<QueryComponent> queryComponents = getQueryComponentsForAdvancedSearch(parentComposite, searchQueryTable);

				boolean connectByAnd = false;
				if (andButton.getSelection() == true) {
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
				if (!Collector.hasSelectedAlbum()) {
					ComponentFactory.showErrorDialog(
							Collector.getShell(), 
							Translator.get(DictKeys.DIALOG_TITLE_NO_ALBUM_SELECTED), 
							Translator.get(DictKeys.DIALOG_CONTENT_NO_ALBUM_SELECTED));

					return;
				}

				ArrayList<QueryComponent> queryComponents = getQueryComponentsForAdvancedSearch(parentComposite, searchQueryTable);

				boolean connectByAnd = false;
				if (andButton.getSelection() == true) {
					connectByAnd = true;
				}

				TextInputDialog textInputDialog = new TextInputDialog(parentComposite.getShell());
				String viewName = textInputDialog.open(
						Translator.get(DictKeys.DIALOG_TITLE_ENTER_VIEW_NAME), 
						Translator.get(DictKeys.DIALOG_CONTENT_ENTER_VIEW_NAME), 
						Translator.get(DictKeys.DIALOG_TEXTBOX_ENTER_VIEW_NAME),
						Translator.get(DictKeys.DIALOG_BUTTON_ENTER_VIEW_NAME));

				if (viewName != null && !AlbumViewManager.hasViewWithName(viewName)) {				
					if (fieldToSortCombo.getSelectionIndex() != -1) {
						AlbumViewManager.addAlbumView(
								viewName, Collector.getSelectedAlbum(), 
								QueryBuilder.buildQuery(queryComponents, connectByAnd, album, 
										fieldToSortCombo.getItem(fieldToSortCombo.getSelectionIndex()), sortAscendingButton.getSelection()));
					} else {
						AlbumViewManager.addAlbumView(
								viewName, Collector.getSelectedAlbum(), 
								QueryBuilder.buildQuery(queryComponents, connectByAnd, album));						
					}
				} else {
					ComponentFactory.getMessageBox(
							parentComposite, 
							Translator.get(DictKeys.DIALOG_TITLE_VIEW_NAME_ALREADY_USED), 
							Translator.get(DictKeys.DIALOG_CONTENT_VIEW_NAME_ALREADY_USED), 
							SWT.ICON_INFORMATION).open();
				}
			}
		});

		return advancedSearchComposite;
	}
	
	private static ArrayList<QueryComponent> getQueryComponentsForAdvancedSearch(Composite parentComposite, Table searchQueryTable) {
		ArrayList<QueryComponent> queryComponents = new ArrayList<QueryComponent>();

		for ( int i=0 ; i < searchQueryTable.getItemCount() ; i++ ) {					
			// In case of a date
			if (DatabaseWrapper.isDateField(Collector.getSelectedAlbum(), searchQueryTable.getItem(i).getText(0))) {
				// Convert string to milliseconds
				DateFormat df = new SimpleDateFormat("d/M/yyyy");
				java.util.Date result = null;
				try {
					result = df.parse(searchQueryTable.getItem(i).getText(2));
					long dateInMilliseconds = result.getTime();

					queryComponents.add(QueryBuilder.getQueryComponent(
							searchQueryTable.getItem(i).getText(0),
							QueryOperator.toQueryOperator(searchQueryTable.getItem(i).getText(1)),
							String.valueOf(dateInMilliseconds)));
				} catch (ParseException e1) {
					MessageBox messageBox = ComponentFactory.getMessageBox(
							parentComposite.getShell(),
							Translator.get(DictKeys.DIALOG_TITLE_DATE_FORMAT),
							Translator.get(DictKeys.DIALOG_CONTENT_DATE_FORMAT),
							SWT.ICON_WARNING | SWT.OK);
					messageBox.open();
				}

				// In case of an option
			} else if (DatabaseWrapper.isOptionField(Collector.getSelectedAlbum(), searchQueryTable.getItem(i).getText(0))) {
				String value = searchQueryTable.getItem(i).getText(2);

				if (value.equals(Translator.get(DictKeys.BROWSER_YES)) || 
						value.equals(Translator.get(DictKeys.BROWSER_NO)) || 
						value.equals(Translator.get(DictKeys.BROWSER_UNKNOWN))) {

					if (value.equals(Translator.get(DictKeys.BROWSER_UNKNOWN))) {
						value = "Option"; // TODO stupid
					}

					queryComponents.add(QueryBuilder.getQueryComponent(
							searchQueryTable.getItem(i).getText(0),
							QueryOperator.toQueryOperator(searchQueryTable.getItem(i).getText(1)),
							value));
				} else {
					MessageBox messageBox = ComponentFactory.getMessageBox(
							parentComposite.getShell(),
							Translator.get(DictKeys.DIALOG_TITLE_ENTER_OPTION),
							Translator.get(DictKeys.DIALOG_CONTENT_ENTER_OPTION),
							SWT.ICON_WARNING | SWT.OK);
					messageBox.open();
				}

				// All other cases
			} else {
				queryComponents.add(QueryBuilder.getQueryComponent(
						searchQueryTable.getItem(i).getText(0),
						QueryOperator.toQueryOperator(searchQueryTable.getItem(i).getText(1)),
						searchQueryTable.getItem(i).getText(2)));
			}
		}

		return queryComponents;
	}
}
