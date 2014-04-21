package org.sammelbox.view.various;

import java.util.List;

import org.eclipse.swt.SWT;
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
import org.sammelbox.model.album.MetaItemField;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseOperations;

public class FieldSelectionDialog extends Dialog {
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
	 * @param labelText the text within the dialog. (E.g. "Please enter a new name") 
	 * @param albumName the album name for which a field should be selected
	 * @param buttonText the text for the dialog button (E.g. "Rename") 
	 * @return A string holding the value that the user entered or null, if the dialog was canceled/closed */
	public String open(String title, String labelText, String albumName, String buttonText) {
		final Shell shell = new Shell(getParent(), SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL);
		shell.setText(title);
		shell.setLayout(new GridLayout(1, true));
				
		ComponentFactory.getH3Label(shell, labelText);

		final Combo fieldSelection = new Combo(shell, SWT.READ_ONLY);
		
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		fieldSelection.setLayoutData(gridData);
		
		String[] fieldNames = null;
		try {
			fieldNames = MetaItemFieldFilter.getValidFieldNamesAsStringArray(DatabaseOperations.getMetaItemFields(albumName));
		} catch (DatabaseWrapperOperationException e) {
			// TODO Auto-generated catch block
		}
		
		fieldSelection.setItems(fieldNames);
		
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
