package org.sammelbox.view.sidepanes;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.sammelbox.controller.events.EventObservable;
import org.sammelbox.controller.events.SammelboxEvent;
import org.sammelbox.controller.filesystem.importing.CSVAppender;
import org.sammelbox.controller.filesystem.importing.CSVImporter;
import org.sammelbox.controller.filesystem.importing.ImportException;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseOperations;
import org.sammelbox.view.ApplicationUI;
import org.sammelbox.view.various.ComponentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ImportSidepane {
	private static final Logger LOGGER = LoggerFactory.getLogger(ImportSidepane.class);
	private static final int DEFAULT_COMPOSITE_HEIGHT_IN_PIXELS = 25;

	private ImportSidepane() {
		// use build method
	}
	
	public static Composite build(Composite parentComposite, final boolean appendCSVEntries) {		
		// setup import composite
		final Composite importComposite = new Composite(parentComposite, SWT.NONE);
		importComposite.setLayout(new GridLayout(1, false));
		importComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		// separator grid data
		GridData seperatorGridData = new GridData(GridData.FILL_BOTH);
		seperatorGridData.minimumHeight = DEFAULT_COMPOSITE_HEIGHT_IN_PIXELS;
		
		// Span grid data
		GridData spanGridData = new GridData(GridData.VERTICAL_ALIGN_END);
	    spanGridData.horizontalSpan = 2;
	    spanGridData.horizontalAlignment = GridData.FILL;
		
		// label header
		if (appendCSVEntries) {
			ComponentFactory.getPanelHeaderComposite(importComposite, Translator.toBeTranslated("Appending CSV Data"));
		} else {
			ComponentFactory.getPanelHeaderComposite(importComposite, Translator.get(DictKeys.LABEL_CSV_IMPORT));
		}
	    
		// setup two column inner composite
		Composite innerComposite = new Composite(importComposite, SWT.NONE);
		innerComposite.setLayout(new GridLayout(2, false));
		innerComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		// select CSV Button
		final Button selectCSVButton = new Button(innerComposite, SWT.NONE);
		selectCSVButton.setText(Translator.get(DictKeys.BUTTON_SELECT_CSV_FILE));
		
		// CSV filename label
		final Label csvFileNameLabel = new Label(innerComposite, SWT.NONE);
		csvFileNameLabel.setText("...");
		csvFileNameLabel.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		// separator
		Label separator = new Label(innerComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(spanGridData);
		
		selectCSVButton.addSelectionListener(new SelectionAdapter() {
			@Override()
			public void widgetSelected(SelectionEvent event) {
				FileDialog openFileDialog = new FileDialog(ApplicationUI.getShell(), SWT.OPEN);
				openFileDialog.setText(Translator.get(DictKeys.DIALOG_TITLE_SELECT_CSV_FILE));
				openFileDialog.setFilterPath(System.getProperty("user.home"));
				String[] filterExt = { "*.csv" };
				openFileDialog.setFilterExtensions(filterExt);

				String filename = openFileDialog.open();
				
				if (filename != null) {
					File file = new File(filename);
					csvFileNameLabel.setText(file.getName());
					csvFileNameLabel.setData("CSV_FILE", file);
				}
			}
		});
				
    	// album selection when appending 
		final Label selectAlbumNameLabel = new Label(innerComposite, SWT.NONE);
		selectAlbumNameLabel.setText(Translator.toBeTranslated("Select the album:"));
    	
		// album name combo-box
		final Combo albumNameCombo = new Combo(innerComposite, SWT.BORDER);
		try {
			albumNameCombo.setItems(DatabaseOperations.getArrayOfAllAlbums());
		} catch (DatabaseWrapperOperationException databaseWrapperOperationException) {
			LOGGER.error("Could not retrieve the list of albums", databaseWrapperOperationException);
		}
		
		// album name 
		Label albumNameLabel = new Label(innerComposite, SWT.NONE);
		albumNameLabel.setText(Translator.get(DictKeys.LABEL_ALBUM_NAME));
		
		// album name text-box 
	    final Text albumNameText = new Text(innerComposite, SWT.BORDER);
		albumNameText.setLayoutData(new GridData(GridData.FILL_BOTH));
		if (appendCSVEntries) {
			albumNameText.setText("");
			albumNameText.setEnabled(false);
			albumNameLabel.setVisible(false);
		} else {
			selectAlbumNameLabel.setVisible(false);
			albumNameCombo.setVisible(false);
			
			albumNameText.setText(Translator.get(DictKeys.TEXT_SAMPLE_IMPORT_NAME));
		}
		
		albumNameCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				albumNameText.setText(albumNameCombo.getItem(albumNameCombo.getSelectionIndex()));
			}
		});
		
		// separator char label
		Label separatorCharLabel = new Label(innerComposite, SWT.NONE);
		separatorCharLabel.setText(Translator.get(DictKeys.LABEL_SEPARATOR));
		
		// separator char text-box
		final Text separatorCharText = new Text(innerComposite, SWT.BORDER);
		separatorCharText.setLayoutData(new GridData(GridData.FILL_BOTH));
		separatorCharText.setText(";");
		
		// separator
		separator = new Label(innerComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(spanGridData);
		
		GridData compositeGridData = new GridData(SWT.FILL, SWT.FILL, false, false);
		compositeGridData.horizontalSpan = 2;
		compositeGridData.horizontalAlignment = GridData.FILL;
		compositeGridData.heightHint = DEFAULT_COMPOSITE_HEIGHT_IN_PIXELS;
		
		Label importImgFromColumn = new Label(innerComposite, SWT.HORIZONTAL);
		importImgFromColumn.setText(Translator.get(DictKeys.LABEL_IMPORT_IMG_FROM_COLUMN));		
	    importImgFromColumn.setLayoutData(compositeGridData);
		
		Composite lastColumnPictureLinksComposite = new Composite(innerComposite, SWT.FILL);
		lastColumnPictureLinksComposite.setLayoutData(compositeGridData);
		lastColumnPictureLinksComposite.setLayout(new RowLayout());

		final Button addImagesYesButton = new Button(lastColumnPictureLinksComposite, SWT.RADIO);
		addImagesYesButton.setText(Translator.get(DictKeys.BUTTON_YES));
		Button addImagesNoButton = new Button(lastColumnPictureLinksComposite, SWT.RADIO);
		addImagesNoButton.setText(Translator.get(DictKeys.BUTTON_NO));
		addImagesNoButton.setSelection(true);
		
		// separator
		separator = new Label(innerComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(spanGridData);
		
		// image column name
		Label imageColumnLabel = new Label(innerComposite, SWT.NONE);
		imageColumnLabel.setText(Translator.get(DictKeys.LABEL_IMG_COLUM_NAME));
		
		// image column name text-box
		final Text imageColumnText = new Text(innerComposite, SWT.BORDER);
		imageColumnText.setLayoutData(new GridData(GridData.FILL_BOTH));
		imageColumnText.setEnabled(false);
		imageColumnText.setText("IMG");
		
		// image separator
		Label imageSeparatorLabel = new Label(innerComposite, SWT.NONE);
		imageSeparatorLabel.setText(Translator.get(DictKeys.LABEL_IMG_SEPARATOR));
		
		// image separator column text-box
		final Text imageSeparatorText = new Text(innerComposite, SWT.BORDER);
		imageSeparatorText.setLayoutData(new GridData(GridData.FILL_BOTH));
		imageSeparatorText.setEnabled(false);
		imageSeparatorText.setText("!");
		
		// separator
		separator = new Label(innerComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(spanGridData);
		
		addImagesYesButton.addSelectionListener(new SelectionAdapter() {
			@Override()
			public void widgetSelected(SelectionEvent event) {
				imageColumnText.setEnabled(true);
				imageSeparatorText.setEnabled(true);
			}
		});
		
		addImagesNoButton.addSelectionListener(new SelectionAdapter() {
			@Override()
			public void widgetSelected(SelectionEvent event) {
				imageColumnText.setEnabled(false);
				imageSeparatorText.setEnabled(false);
			}
		});
		
		final Button simulateButton = new Button(importComposite, SWT.PUSH);
		simulateButton.setText(Translator.get(DictKeys.BUTTON_SIMULATE));
		simulateButton.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		final Button importButton = new Button(importComposite, SWT.PUSH);
		importButton.setText(Translator.get(DictKeys.BUTTON_IMPORT));
		importButton.setEnabled(false);
		importButton.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		simulateButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				try {
					if (appendCSVEntries) {
						if (addImagesYesButton.getSelection()) {
							CSVAppender.appendItemsFromCSV(albumNameText.getText(), csvFileNameLabel.getData("CSV_FILE") != null 
									? ((File) csvFileNameLabel.getData("CSV_FILE")).getAbsolutePath() : "", 
									separatorCharText.getText(), imageColumnText.getText(), imageSeparatorText.getText(), true);
						} else {
							CSVAppender.appendItemsFromCSV(albumNameText.getText(), csvFileNameLabel.getData("CSV_FILE") != null 
									? ((File) csvFileNameLabel.getData("CSV_FILE")).getAbsolutePath() : "", 
									separatorCharText.getText(), true);
						}
					} else {
						if (addImagesYesButton.getSelection()) {
							CSVImporter.importCSV(albumNameText.getText(), csvFileNameLabel.getData("CSV_FILE") != null 
									? ((File) csvFileNameLabel.getData("CSV_FILE")).getAbsolutePath() : "", 
									separatorCharText.getText(), imageColumnText.getText(), imageSeparatorText.getText(), true);
						} else {
							CSVImporter.importCSV(albumNameText.getText(), csvFileNameLabel.getData("CSV_FILE") != null 
									? ((File) csvFileNameLabel.getData("CSV_FILE")).getAbsolutePath() : "", 
									separatorCharText.getText(), true);
						}
					}
					
					importButton.setEnabled(true);
					
					ComponentFactory.getMessageBox(
							Translator.get(DictKeys.DIALOG_TITLE_SIMULATION_SUCCESSFUL), 
							Translator.get(DictKeys.DIALOG_CONTENT_SIMULATION_SUCCESSFUL), 
							SWT.ICON_INFORMATION).open();
				} catch (ImportException importException) {
					LOGGER.error("An error occurred while importing/appending CSV data", importException);
					
					ComponentFactory.getMessageBox(
							Translator.get(DictKeys.DIALOG_TITLE_SIMULATION_UNSUCCESSFUL), 
							Translator.get(DictKeys.DIALOG_CONTENT_SIMULATION_UNSUCCESSFUL, importException.getMessage()), 
							SWT.ICON_WARNING).open();
				}
			}
		});
		
		importButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					if (appendCSVEntries) {
						if (addImagesYesButton.getSelection()) {
							CSVAppender.appendItemsFromCSV(albumNameText.getText(), csvFileNameLabel.getData("CSV_FILE") != null ? ((File) csvFileNameLabel.getData("CSV_FILE")).getAbsolutePath() : "", 
								separatorCharText.getText(), imageColumnText.getText(), imageSeparatorText.getText(), false);
						} else {
							CSVAppender.appendItemsFromCSV(albumNameText.getText(), csvFileNameLabel.getData("CSV_FILE") != null ? ((File) csvFileNameLabel.getData("CSV_FILE")).getAbsolutePath() : "", 
									separatorCharText.getText(), false);
						}
					} else {
						if (addImagesYesButton.getSelection()) {
							CSVImporter.importCSV(albumNameText.getText(), csvFileNameLabel.getData("CSV_FILE") != null ? ((File) csvFileNameLabel.getData("CSV_FILE")).getAbsolutePath() : "", 
								separatorCharText.getText(), imageColumnText.getText(), imageSeparatorText.getText(), false);
						} else {
							CSVImporter.importCSV(albumNameText.getText(), csvFileNameLabel.getData("CSV_FILE") != null ? ((File) csvFileNameLabel.getData("CSV_FILE")).getAbsolutePath() : "", 
									separatorCharText.getText(), false);
						}
					}
					
					ComponentFactory.getMessageBox(
							Translator.get(DictKeys.DIALOG_TITLE_IMPORT_SUCCESSFUL), 
							Translator.get(DictKeys.DIALOG_CONTENT_IMPORT_SUCCESSFUL), 
							SWT.ICON_INFORMATION).open();
					
					EventObservable.addEventToQueue(SammelboxEvent.ALBUM_LIST_UPDATED);
				} catch (ImportException importException) {
					LOGGER.error("An error occurred while importing/appending CSV data", importException);
					
					ComponentFactory.getMessageBox(
							Translator.get(DictKeys.DIALOG_TITLE_IMPORT_UNSUCCESSFUL), 
							Translator.get(DictKeys.DIALOG_CONTENT_IMPORT_UNSUCCESSFUL, importException.getMessage()),
							SWT.ERROR).open();
				}
			}
		});
		
		return importComposite;
	}
}
