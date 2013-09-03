/** -----------------------------------------------------------------
 *    Sammelbox: Collection Manager - A free and open-source collection manager for Windows & Linux
 *    Copyright (C) 2011 Jérôme Wagener & Paul Bicheler
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

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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
import org.sammelbox.controller.filesystem.FileSystemAccessWrapper;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.model.album.AlbumItemStore;
import org.sammelbox.model.album.FieldType;
import org.sammelbox.model.album.MetaItemField;
import org.sammelbox.model.database.QueryBuilder;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseOperations;
import org.sammelbox.view.ApplicationUI;
import org.sammelbox.view.browser.BrowserFacade;
import org.sammelbox.view.various.ComponentFactory;
import org.sammelbox.view.various.PanelType;
import org.sammelbox.view.various.TextInputDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CreateAlbumSidepane {
	private static final Logger LOGGER = LoggerFactory.getLogger(CreateAlbumSidepane.class);
	
	private CreateAlbumSidepane() {
		// use build method instead
	}
	
	/** Returns a "create new album" composite. This composite provides the user interface to create a new album. Meaning that an 
	 * album name can be specified, as well as an undefined number of fields (columns) with user defined types etc..
	 * @param parentComposite the parent composite
	 * @return a new "create new album" composite */
	public static Composite build(final Composite parentComposite) {				
		// setup create new album composite
		Composite createNewAlbumComposite = new Composite(parentComposite, SWT.NONE);	
		createNewAlbumComposite.setLayout(new GridLayout());

		// description (header) label
		ComponentFactory.getPanelHeaderComposite(createNewAlbumComposite, Translator.get(DictKeys.LABEL_CREATE_NEW_ALBUM));
		
		// album name label & text-box to enter album name
		Label albumNameLabel = new Label(createNewAlbumComposite, SWT.NONE);
		albumNameLabel.setText(Translator.get(DictKeys.LABEL_NAME_OF_NEW_ALBUM));
		final Text albumNameText = new Text(createNewAlbumComposite, SWT.BORDER);
		albumNameText.setLayoutData(new GridData(GridData.FILL_BOTH));
		albumNameText.setText(Translator.get(DictKeys.TEXTBOX_MY_NEW_ALBUM));

		Label seperator = new Label(createNewAlbumComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gridDataForSeperator = new GridData(GridData.FILL_BOTH);
		gridDataForSeperator.heightHint = 10;
		seperator.setLayoutData(gridDataForSeperator);

		// picture question label & radio buttons
		Composite pictureQuestionComposite = new Composite(createNewAlbumComposite, SWT.NULL);
		pictureQuestionComposite.setLayout(new GridLayout(3, false));
		pictureQuestionComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		Label label = new Label(pictureQuestionComposite, SWT.NONE);
		label.setText(Translator.get(DictKeys.LABEL_SHOULD_CONTAIN_IMAGES) + "   ");
		final Button yesButtonForIncludingImages = new Button(pictureQuestionComposite, SWT.RADIO);
		yesButtonForIncludingImages.setText(Translator.get(DictKeys.BUTTON_YES));
		yesButtonForIncludingImages.setSelection(true);
		Button noButtonForIncludingImages = new Button(pictureQuestionComposite, SWT.RADIO);
		noButtonForIncludingImages.setText(Translator.get(DictKeys.BUTTON_NO));
		
		seperator = new Label(createNewAlbumComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		seperator.setLayoutData(gridDataForSeperator);

		// inner composite for fieldname and fieldtype
		Composite innerCompositeForFieldname = new Composite(createNewAlbumComposite, SWT.BORDER);
		innerCompositeForFieldname.setLayout(new GridLayout(2, false));
		innerCompositeForFieldname.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		// fieldname label and text-box to enter the name of the field		
		Label fieldNameLabel = new Label(innerCompositeForFieldname, SWT.NONE);
		fieldNameLabel.setText(Translator.get(DictKeys.LABEL_FIELD_NAME));
		final Text fieldNameText = new Text(innerCompositeForFieldname, SWT.BORDER);
		fieldNameText.setLayoutData(new GridData(GridData.FILL_BOTH));

		// fieldtype label and combo-box to enter the type of the field
		Label fieldTypeLabel = new Label(innerCompositeForFieldname, SWT.NONE);
		fieldTypeLabel.setText(Translator.get(DictKeys.LABEL_FIELD_TYPE));
		final Combo fieldTypeCombo = new Combo(innerCompositeForFieldname, SWT.DROP_DOWN);
		fieldTypeCombo.setItems(FieldType.toUserTypeStringArray());	    
		fieldTypeCombo.setLayoutData(new GridData(GridData.FILL_BOTH));
		fieldTypeCombo.setText(fieldTypeCombo.getItem(0).toString());

		// Add-field-button --> listener comes after table
		Button addFieldButton = new Button(innerCompositeForFieldname, SWT.PUSH);
		addFieldButton.setText(Translator.get(DictKeys.BUTTON_ADD_FIELD));
		GridData gridDataForAddFieldButton = new GridData(GridData.FILL_BOTH);
		gridDataForAddFieldButton.horizontalSpan = 2;
		addFieldButton.setLayoutData(gridDataForAddFieldButton);
		
		seperator = new Label(createNewAlbumComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		seperator.setLayoutData(gridDataForSeperator);

		// Field table
		final Table albumFieldNamesAndTypesTable = 
				new Table(createNewAlbumComposite, SWT.CHECK | SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		albumFieldNamesAndTypesTable.setLinesVisible(true);
		albumFieldNamesAndTypesTable.setHeaderVisible(true);
		
		// Add menu to table
		Menu popupMenu = new Menu(albumFieldNamesAndTypesTable);
		MenuItem moveUp = new MenuItem(popupMenu, SWT.NONE);
		moveUp.setText(Translator.get(DictKeys.DROPDOWN_MOVE_ONE_DOWN));
		moveUp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (albumFieldNamesAndTypesTable.getSelectionIndex() > 0) {
					TableItem originalItem = albumFieldNamesAndTypesTable.getItem(albumFieldNamesAndTypesTable.getSelectionIndex());

					TableItem itemAtNewPosition = new TableItem(
							albumFieldNamesAndTypesTable, SWT.NONE, albumFieldNamesAndTypesTable.getSelectionIndex() - 1);
					itemAtNewPosition.setText(1, originalItem.getText(1));
					itemAtNewPosition.setText(2, originalItem.getText(2));

					originalItem.dispose();
				}
				
				CreateAlbumSidepane.updateCreateNewAlbumPage(yesButtonForIncludingImages, albumFieldNamesAndTypesTable);
			}
		});

		MenuItem moveDown = new MenuItem(popupMenu, SWT.NONE);
		moveDown.setText(Translator.get(DictKeys.DROPDOWN_MOVE_ONE_DOWN));
		moveDown.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (albumFieldNamesAndTypesTable.getSelectionIndex() != albumFieldNamesAndTypesTable.getItemCount()) {
					TableItem originalItem = albumFieldNamesAndTypesTable.getItem(albumFieldNamesAndTypesTable.getSelectionIndex());

					TableItem itemAtNewPosition = 
							new TableItem(albumFieldNamesAndTypesTable, SWT.NONE, albumFieldNamesAndTypesTable.getSelectionIndex() + 2);
					itemAtNewPosition.setText(1, originalItem.getText(1));
					itemAtNewPosition.setText(2, originalItem.getText(2));

					originalItem.dispose();
				}
				
				CreateAlbumSidepane.updateCreateNewAlbumPage(yesButtonForIncludingImages, albumFieldNamesAndTypesTable);
			}
		});

		new MenuItem(popupMenu, SWT.SEPARATOR);
		MenuItem rename = new MenuItem(popupMenu, SWT.NONE);
		rename.setText(Translator.get(DictKeys.DROPDOWN_RENAME));
		rename.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				TableItem item = albumFieldNamesAndTypesTable.getItem(albumFieldNamesAndTypesTable.getSelectionIndex());

				TextInputDialog textInputDialog = new TextInputDialog(parentComposite.getShell());
				String newName = textInputDialog.open(
						Translator.get(DictKeys.DIALOG_TITLE_RENAME_FIELD), 
						Translator.get(DictKeys.DIALOG_CONTENT_RENAME_FIELD), item.getText(1), 
						Translator.get(DictKeys.DIALOG_BUTTON_RENAME_FIELD));

				if (newName != null) {
					item.setText(1, newName);
				}
				
				CreateAlbumSidepane.updateCreateNewAlbumPage(yesButtonForIncludingImages, albumFieldNamesAndTypesTable);
			}
		});

		new MenuItem(popupMenu, SWT.SEPARATOR);
		MenuItem delete = new MenuItem(popupMenu, SWT.NONE);
		delete.setText(Translator.get(DictKeys.DROPDOWN_REMOVE));
		delete.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (albumFieldNamesAndTypesTable.getSelectionIndex() != -1) {
					TableItem item = albumFieldNamesAndTypesTable.getItem(albumFieldNamesAndTypesTable.getSelectionIndex());					
					item.dispose();
				}
				
				CreateAlbumSidepane.updateCreateNewAlbumPage(yesButtonForIncludingImages, albumFieldNamesAndTypesTable);
			}
		});

		albumFieldNamesAndTypesTable.setMenu(popupMenu);

		// Setup table
		TableColumn isImportantColumn = new TableColumn(albumFieldNamesAndTypesTable, SWT.NONE);
		isImportantColumn.setText(Translator.get(DictKeys.TABLE_COLUMN_QUICKSEARCH));
		TableColumn fieldNameColumn = new TableColumn(albumFieldNamesAndTypesTable, SWT.NONE);
		fieldNameColumn.setText(Translator.get(DictKeys.TABLE_COLUMN_FIELD_NAME));
		TableColumn fieldTypeColumn = new TableColumn(albumFieldNamesAndTypesTable, SWT.NONE);
		fieldTypeColumn.setText(Translator.get(DictKeys.TABLE_COLUMN_FIELD_TYPE));
		albumFieldNamesAndTypesTable.getColumn(0).pack ();
		albumFieldNamesAndTypesTable.getColumn(1).pack ();
		albumFieldNamesAndTypesTable.getColumn(2).pack ();

		// Set table layout data
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 175;
		albumFieldNamesAndTypesTable.setLayoutData(data);
		
		// Add listener to Add-field-button
		addFieldButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (fieldNameText.getText().isEmpty()) {				
					ComponentFactory.getMessageBox(
							parentComposite.getShell(),
							Translator.get(DictKeys.DIALOG_TITLE_FIELD_MUST_HAVE_NAME),
							Translator.get(DictKeys.DIALOG_CONTENT_FIELD_MUST_HAVE_NAME),
							SWT.ICON_WARNING | SWT.OK).open();
				} else {
					TableItem item = new TableItem(albumFieldNamesAndTypesTable, SWT.NONE);
					item.setText(1, fieldNameText.getText());
					item.setText(2, fieldTypeCombo.getText());
	
					fieldNameText.setText("");
					
					CreateAlbumSidepane.updateCreateNewAlbumPage(yesButtonForIncludingImages, albumFieldNamesAndTypesTable);
				}
			}
		});

		seperator = new Label(createNewAlbumComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		seperator.setLayoutData(gridDataForSeperator);

		// Add listeners to picture buttons
		yesButtonForIncludingImages.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				CreateAlbumSidepane.updateCreateNewAlbumPage(yesButtonForIncludingImages, albumFieldNamesAndTypesTable);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {}
		});
		
		noButtonForIncludingImages.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				CreateAlbumSidepane.updateCreateNewAlbumPage(yesButtonForIncludingImages, albumFieldNamesAndTypesTable);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {}
		});
		
		// Create album button
		Button createAlbumButton = new Button(createNewAlbumComposite, SWT.PUSH);
		createAlbumButton.setText(Translator.get(DictKeys.BUTTON_CREATE_ALBUM));
		createAlbumButton.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		FontData fontData = createAlbumButton.getFont().getFontData()[0];
		Font font = new Font(ApplicationUI.getShell().getDisplay(), fontData.getName(), fontData.getHeight(), SWT.BOLD);
		
		createAlbumButton.setFont(font);
		createAlbumButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}

			@Override
			public void widgetSelected(SelectionEvent e) {
				String albumName = albumNameText.getText();

				try {
					if (!DatabaseOperations.isAlbumNameAvailable(albumName)) {
						ComponentFactory.getMessageBox(
								parentComposite, 
								Translator.get(DictKeys.DIALOG_TITLE_ALBUM_NAME_ALREADY_USED), 
								Translator.get(DictKeys.DIALOG_CONTENT_ALBUM_NAME_ALREADY_USED), 
								SWT.ICON_INFORMATION).open();
						return;
					}
				} catch (DatabaseWrapperOperationException ex) {
					LOGGER.error("A database error occured while checking whether '" + albumName + "' is available as album name", ex);
				}

				if (!FileSystemAccessWrapper.isSAlbumNameFileSystemCompliant(albumName)) {
					ComponentFactory.getMessageBox(
							parentComposite,
							Translator.get(DictKeys.DIALOG_TITLE_ALBUM_NAME_INVALID),
							Translator.get(DictKeys.DIALOG_CONTENT_ALBUM_NAME_INVALID),
							SWT.ICON_WARNING)
						.open();					
					return;
				}				

				ArrayList<MetaItemField> metaItemFields = new ArrayList<MetaItemField>();

				for ( int i=0 ; i < albumFieldNamesAndTypesTable.getItemCount() ; i++ ) {					
					metaItemFields.add(
							new MetaItemField(
									albumFieldNamesAndTypesTable.getItem(i).getText(1),
									FieldType.valueOf(albumFieldNamesAndTypesTable.getItem(i).getText(2)),
									albumFieldNamesAndTypesTable.getItem(i).getChecked()));
				}

				boolean willContainImages = false;
				if (yesButtonForIncludingImages.getSelection()) {
					willContainImages = true;
				}

				try {
					DatabaseOperations.createNewAlbum(albumName, metaItemFields, willContainImages);
				} catch (DatabaseWrapperOperationException failedDatabaseWrapperOperationException) {
					ComponentFactory.getMessageBox(parentComposite,
							Translator.get(DictKeys.DIALOG_TITLE_ALBUM_CREATE_ERROR), 
							Translator.get(DictKeys.DIALOG_CONTENT_ALBUM_CREATE_ERROR, 
							albumName), 
							SWT.ICON_ERROR).open();
					return;
				}

				// Correctly select and display the selected album.
				ApplicationUI.refreshAlbumList();
				ApplicationUI.setSelectedAlbum(albumName);
				
				BrowserFacade.performBrowserQueryAndShow(QueryBuilder.createSelectStarQuery(albumName));

				ApplicationUI.changeRightCompositeTo(PanelType.EMPTY, EmptySidepane.build(parentComposite));
			}
		});

		GridData gridData = new GridData();
		gridData.widthHint = 600;
		gridData.heightHint = 600;
		createNewAlbumComposite.setLayoutData(gridData);

		// Load matching browser window
		BrowserFacade.showCreateNewAlbumPage(AlbumItemStore.getSamplePictureAlbumItemWithoutFields());
		
		return createNewAlbumComposite;
	}
	
	private static void updateCreateNewAlbumPage(Button yesButtonForIncludingImages, Table albumFieldNamesAndTypesTable) {
		boolean containsImages = false;
		if (yesButtonForIncludingImages.getSelection()) {
			containsImages = true;
		}
		
		ArrayList<MetaItemField> metaItemFields = new ArrayList<MetaItemField>();

		for ( int i=0 ; i < albumFieldNamesAndTypesTable.getItemCount() ; i++ ) {					
			metaItemFields.add(
					new MetaItemField(
							albumFieldNamesAndTypesTable.getItem(i).getText(1),
							FieldType.valueOf(albumFieldNamesAndTypesTable.getItem(i).getText(2)),
							albumFieldNamesAndTypesTable.getItem(i).getChecked()));
		}
		
		BrowserFacade.showCreateNewAlbumPage(AlbumItemStore.getSampleAlbumItem(containsImages, metaItemFields));
	}
}
