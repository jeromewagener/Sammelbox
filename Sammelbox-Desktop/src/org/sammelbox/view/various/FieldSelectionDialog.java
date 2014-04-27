package org.sammelbox.view.various;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.sammelbox.controller.filters.MetaItemFieldFilter;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.model.album.Album;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FieldSelectionDialog extends Dialog {
	private static final String SEPARATOR = "---------------";

	private static final Logger LOGGER = LoggerFactory.getLogger(FieldSelectionDialog.class);
	
	/** Stores the value which is eventually entered */
	private String value = null;

	/** Creates a new TextInputDialog as a child of the provided parent shell 
	 * @param parent the parent of the text input dialog*/
	public FieldSelectionDialog(Shell parent) {
		super(parent);
	}

	/** Creates a new TextInputDialog as a child of the provided parent shell, while accepting SWT styles
	 * @param style an SWT style constant (E.g. SWT.BORDER) */
	public FieldSelectionDialog(Shell parent, int style) {
		super(parent, style);
	}

	/** Opens the text input dialog based on the parameters provided
	 * @param title the window caption
	 * @param labelText the text within the dialog. (E.g. "Sort by") 
	 * @param albumName the album name for which a field should be selected
	 * @param buttonText the text for the dialog button (E.g. "Rename") 
	 * @return A string holding the value that the user entered or null, if the dialog was canceled/closed */
	public String open(String title, String labelText, String albumName, String buttonText, String defaultSelectedField) {
		final Shell shell = new Shell(getParent(), SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL);
		shell.setText(title);
		shell.setLayout(new GridLayout(1, true));
				
		ComponentFactory.getH3Label(shell, labelText);

		final Combo fieldSelection = new Combo(shell, SWT.READ_ONLY);
		fieldSelection.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				value = fieldSelection.getItem(fieldSelection.getSelectionIndex());
				
				if (value.equals(SEPARATOR) || value.equals(Translator.get(DictKeys.COMBOBOX_CONTENT_DO_NOT_SORT))) {
					value = Album.NO_SORTING;
				}
			}
		});
		
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		fieldSelection.setLayoutData(gridData);
		
		String[] fieldNames = null;
		try {
			fieldNames = MetaItemFieldFilter.getValidFieldNamesAsStringArray(DatabaseOperations.getMetaItemFields(albumName));
		} catch (DatabaseWrapperOperationException dwoe) {
			LOGGER.error("An error occurred while retrieving meta information for " + albumName, dwoe);
		}
		
		fieldSelection.setItems(fieldNames);
		fieldSelection.add(SEPARATOR);
		fieldSelection.add(Translator.get(DictKeys.COMBOBOX_CONTENT_DO_NOT_SORT));
		
		// select first item by default
		if (fieldNames.length != 0) {
			fieldSelection.select(0);
		}
		
		if (defaultSelectedField.equals(Album.NO_SORTING)) {
			// select the last item in the list which must be the "do not sort" option
			fieldSelection.select(fieldSelection.getItemCount() - 1);
			value = defaultSelectedField;
		} else {
			// select given fieldname if possible
			for (int i=0; i<fieldNames.length; i++) {
				if (fieldNames[i].equals(defaultSelectedField)) {
					fieldSelection.select(i);
					value = defaultSelectedField;
					break;
				}
			}
		}
		
		final Button button = new Button(shell, SWT.PUSH);
		button.setText(buttonText);
		button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		button.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				shell.dispose();
			}
		});

		shell.addListener(SWT.Traverse, new Listener() {
			public void handleEvent(Event event) {
				if (event.detail == SWT.TRAVERSE_ESCAPE) {
					event.doit = false;
				}
			}
		});

		shell.pack();
		shell.open();
		
		// Center shell to primary screen
		Monitor primaryMonitor = shell.getDisplay().getPrimaryMonitor();
		Rectangle primaryMonitorBounds = primaryMonitor.getBounds();
		Rectangle shellBounds = shell.getBounds();
		int xCoordinateForShell = primaryMonitorBounds.x + (primaryMonitorBounds.width - shellBounds.width) / 2;
		int yCoordinateForShell = primaryMonitorBounds.y + (primaryMonitorBounds.height - shellBounds.height) / 2;
		shell.setLocation(xCoordinateForShell, yCoordinateForShell);
		
		while (!shell.isDisposed()) {
			if (!shell.getDisplay().readAndDispatch()) {
				shell.getDisplay().sleep();
			}
		}
		
		return value;
	}
}
