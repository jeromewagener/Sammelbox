package org.sammelbox.view.sidepanes;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.sammelbox.controller.events.EventObservable;
import org.sammelbox.controller.events.SammelboxEvent;
import org.sammelbox.controller.filesystem.importing.CSVImporter;
import org.sammelbox.controller.filesystem.importing.ImportException;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.view.ApplicationUI;
import org.sammelbox.view.various.ComponentFactory;

public final class ImportSidepane {
	private ImportSidepane() {
		// use build method
	}
	
	public static Composite build(Composite parentComposite) {		
		// setup import composite
		final Composite importComposite = new Composite(parentComposite, SWT.NONE);
		importComposite.setLayout(new GridLayout(1, false));
		importComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		// separator grid data
		GridData seperatorGridData = new GridData(GridData.FILL_BOTH);
		seperatorGridData.minimumHeight = 15;
		
		// Span grid data
		GridData spanGridData = new GridData(GridData.VERTICAL_ALIGN_END);
	    spanGridData.horizontalSpan = 2;
	    spanGridData.horizontalAlignment = GridData.FILL;
		
		// label header
		ComponentFactory.getPanelHeaderComposite(importComposite, Translator.get(DictKeys.LABEL_CSV_IMPORT));
		
		// setup two column inner composite
		Composite innerComposite = new Composite(importComposite, SWT.NONE);
		innerComposite.setLayout(new GridLayout(2, false));
		innerComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		// album name 
		final Button selectCSVButton = new Button(innerComposite, SWT.NONE);
		selectCSVButton.setText(Translator.get(DictKeys.BUTTON_SELECT_CSV_FILE));
		
		// album name label
		final Label csvFileNameLabel = new Label(innerComposite, SWT.NONE);
		csvFileNameLabel.setText("...");
		csvFileNameLabel.setLayoutData(new GridData(GridData.FILL_BOTH));
		
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
				
		// album name 
		Label albumNameLabel = new Label(innerComposite, SWT.NONE);
		albumNameLabel.setText(Translator.get(DictKeys.LABEL_ALBUM_NAME));
		
		// album name text-box 
		final Text albumNameText = new Text(innerComposite, SWT.BORDER);
		albumNameText.setLayoutData(new GridData(GridData.FILL_BOTH));
		albumNameText.setText(Translator.get(DictKeys.TEXT_SAMPLE_IMPORT_NAME));
		
		// separator char label
		Label separatorCharLabel = new Label(innerComposite, SWT.NONE);
		separatorCharLabel.setText(Translator.get(DictKeys.LABEL_SEPARATOR));
		
		// separator char text-box
		final Text separatorCharText = new Text(innerComposite, SWT.BORDER);
		separatorCharText.setLayoutData(new GridData(GridData.FILL_BOTH));
		separatorCharText.setText(";");
		
		// separator
		Label separator = new Label(innerComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(spanGridData);
		
		GridData compositeGridData = new GridData(SWT.FILL, SWT.FILL, false, false);
		compositeGridData.horizontalSpan = 2;
		compositeGridData.horizontalAlignment = GridData.FILL;
		compositeGridData.heightHint = 25;
		
		Label lastColumnPictureLabel = new Label(innerComposite, SWT.HORIZONTAL);
		lastColumnPictureLabel.setText(Translator.get(DictKeys.LABEL_IMPORT_IMG_FROM_COLUMN));		
	    lastColumnPictureLabel.setLayoutData(compositeGridData);
		
		Composite lastColumnPictureLinksComposite = new Composite(innerComposite, SWT.FILL);
		lastColumnPictureLinksComposite.setLayoutData(compositeGridData);
		lastColumnPictureLinksComposite.setLayout(new RowLayout());

		final Button yesButton = new Button(lastColumnPictureLinksComposite, SWT.RADIO);
		yesButton.setText(Translator.get(DictKeys.BUTTON_YES));
		Button noButton = new Button(lastColumnPictureLinksComposite, SWT.RADIO);
		noButton.setText(Translator.get(DictKeys.BUTTON_NO));
		noButton.setSelection(true);
		
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
		Label separator2 = new Label(innerComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator2.setLayoutData(spanGridData);
		
		yesButton.addSelectionListener(new SelectionAdapter() {
			@Override()
			public void widgetSelected(SelectionEvent event) {
				imageColumnText.setEnabled(true);
				imageSeparatorText.setEnabled(true);
			}
		});
		
		noButton.addSelectionListener(new SelectionAdapter() {
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
			public void widgetSelected(SelectionEvent e) {
				try {
					if (yesButton.getSelection()) {
						CSVImporter.importCSV(albumNameText.getText(), csvFileNameLabel.getData("CSV_FILE") != null 
								? ((File) csvFileNameLabel.getData("CSV_FILE")).getAbsolutePath() 
								: "", 
								separatorCharText.getText(), imageColumnText.getText(), imageSeparatorText.getText(), true);
					} else {
						CSVImporter.importCSV(albumNameText.getText(), csvFileNameLabel.getData("CSV_FILE") != null 
								? ((File) csvFileNameLabel.getData("CSV_FILE")).getAbsolutePath() 
								: "", 
								separatorCharText.getText(), true);
					}
					
					importButton.setEnabled(true);
					
					ComponentFactory.getMessageBox(
							Translator.get(DictKeys.DIALOG_TITLE_SIMULATION_SUCCESSFUL), 
							Translator.get(DictKeys.DIALOG_CONTENT_SIMULATION_SUCCESSFUL), 
							SWT.ICON_INFORMATION).open();
				} catch (ImportException ex) {
					ComponentFactory.getMessageBox(
							Translator.get(DictKeys.DIALOG_TITLE_SIMULATION_UNSUCCESSFUL), 
							Translator.get(DictKeys.DIALOG_CONTENT_SIMULATION_UNSUCCESSFUL, ex.getMessage()), 
							SWT.ICON_WARNING).open();
				}
			}
		});
		
		importButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					if (yesButton.getSelection()) {
						CSVImporter.importCSV(albumNameText.getText(), csvFileNameLabel.getData("CSV_FILE") != null ? ((File) csvFileNameLabel.getData("CSV_FILE")).getAbsolutePath() : "", 
							separatorCharText.getText(), imageColumnText.getText(), imageSeparatorText.getText(), false);
					} else {
						CSVImporter.importCSV(albumNameText.getText(), csvFileNameLabel.getData("CSV_FILE") != null ? ((File) csvFileNameLabel.getData("CSV_FILE")).getAbsolutePath() : "", 
								separatorCharText.getText(), false);
					}
					
					ComponentFactory.getMessageBox(
							Translator.get(DictKeys.DIALOG_TITLE_IMPORT_SUCCESSFUL), 
							Translator.get(DictKeys.DIALOG_CONTENT_IMPORT_SUCCESSFUL), 
							SWT.ICON_INFORMATION).open();
					
					EventObservable.addEventToQueue(SammelboxEvent.ALBUM_LIST_UPDATED);
				} catch (ImportException ex) {
					ComponentFactory.getMessageBox(
							Translator.get(DictKeys.DIALOG_TITLE_IMPORT_UNSUCCESSFUL), 
							Translator.get(DictKeys.DIALOG_CONTENT_IMPORT_UNSUCCESSFUL, ex.getMessage()),
							SWT.ERROR).open();
				}
			}
		});
		
		return importComposite;
	}
}
