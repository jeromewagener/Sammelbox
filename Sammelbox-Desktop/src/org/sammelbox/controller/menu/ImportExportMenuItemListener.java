package org.sammelbox.controller.menu;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.FileDialog;
import org.sammelbox.controller.filesystem.exporting.CSVExporter;
import org.sammelbox.controller.filesystem.exporting.HTMLExporter;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.model.album.AlbumItemStore;
import org.sammelbox.view.ApplicationUI;
import org.sammelbox.view.browser.BrowserFacade;
import org.sammelbox.view.sidepanes.ImportSidepane;
import org.sammelbox.view.various.PanelType;
import org.sammelbox.view.various.TextInputDialog;

public class ImportExportMenuItemListener {
	static SelectionAdapter getImportCSVAlbumItemsListener() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				BrowserFacade.showHtmlPage("import_csv");
				ApplicationUI.changeRightCompositeTo(PanelType.IMPORT, ImportSidepane.build(ApplicationUI.getThreePanelComposite(), false));
			}
		};
	}
	
	static SelectionAdapter getAppendCSVAlbumItemsListener() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				BrowserFacade.showHtmlPage("append_csv");
				ApplicationUI.changeRightCompositeTo(PanelType.IMPORT, ImportSidepane.build(ApplicationUI.getThreePanelComposite(), true));
			}
		};
	}
	
	static SelectionAdapter getExportAlbumItemsToCSVListener() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				if (ApplicationUI.isAlbumSelectedAndShowMessageIfNot()) {
					FileDialog saveFileDialog = new FileDialog(ApplicationUI.getShell(), SWT.SAVE);
					saveFileDialog.setText(Translator.get(DictKeys.DIALOG_EXPORT_VISIBLE_ITEMS));
					saveFileDialog.setFilterPath(System.getProperty("user.home"));
					String[] filterExt = { "*.csv"};
					saveFileDialog.setFilterExtensions(filterExt);
					String[] filterNames = { Translator.get(DictKeys.DIALOG_CSV_FOR_SPREADSHEET) };
					saveFileDialog.setFilterNames(filterNames);
	
					String filepath = saveFileDialog.open();
					if (filepath != null) {
						// ask for separation character
						TextInputDialog separationCharacterInput = new TextInputDialog(ApplicationUI.getShell());
						String separationCharacter = separationCharacterInput.open(
								Translator.get(DictKeys.DIALOG_TITLE_SELECT_SEPARATION_CHAR),
								Translator.get(DictKeys.DIALOG_CONTENT_SELECT_SEPARATION_CHAR), 
								CSVExporter.DEFAULT_SEPARATION_CHARACTER, 
								Translator.get(DictKeys.DIALOG_BUTTON_SELECT_SEPARATION_CHAR));
						
						if (separationCharacter == null) {
							separationCharacter = CSVExporter.DEFAULT_SEPARATION_CHARACTER;
						}
						
						CSVExporter.exportAlbum(AlbumItemStore.getAllAlbumItems(), filepath, separationCharacter);
					}
				}
			}
		};
	}	
	
	static SelectionAdapter getExportAlbumItemsToHTMLListener() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				if (ApplicationUI.isAlbumSelectedAndShowMessageIfNot()) {
					FileDialog saveFileDialog = new FileDialog(ApplicationUI.getShell(), SWT.SAVE);
					saveFileDialog.setText(Translator.get(DictKeys.DIALOG_EXPORT_VISIBLE_ITEMS));
					saveFileDialog.setFilterPath(System.getProperty("user.home"));
					String[] filterExt = { "*.html" };
					saveFileDialog.setFilterExtensions(filterExt);
					String[] filterNames = { Translator.get(DictKeys.DIALOG_HTML_FOR_PRINT) };
					saveFileDialog.setFilterNames(filterNames);
	
					String filepath = saveFileDialog.open();
					if (filepath != null) {
						HTMLExporter.exportAlbum(AlbumItemStore.getAllAlbumItems(), filepath);
					}
				}
			}
		};
	}
}
