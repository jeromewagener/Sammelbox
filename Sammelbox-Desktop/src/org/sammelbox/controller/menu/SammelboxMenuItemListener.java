package org.sammelbox.controller.menu;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.FileDialog;
import org.sammelbox.controller.events.EventObservable;
import org.sammelbox.controller.events.SammelboxEvent;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.controller.managers.DatabaseIntegrityManager;
import org.sammelbox.view.ApplicationUI;
import org.sammelbox.view.browser.BrowserFacade;
import org.sammelbox.view.sidepanes.EmptySidepane;
import org.sammelbox.view.sidepanes.SynchronizeSidepane;
import org.sammelbox.view.various.ComponentFactory;
import org.sammelbox.view.various.PanelType;

public final class SammelboxMenuItemListener {
	
	private SammelboxMenuItemListener() {
		// not needed
	}
	
	static SelectionAdapter getBackupListener() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				FileDialog saveFileDialog = new FileDialog(ApplicationUI.getShell(), SWT.SAVE);
				saveFileDialog.setText(Translator.get(DictKeys.DIALOG_BACKUP_TO_FILE));
				saveFileDialog.setFilterPath(System.getProperty("user.home"));
				String[] filterExt = { "*.cbk" };
				saveFileDialog.setFilterExtensions(filterExt);

				final String backupPath = saveFileDialog.open();
				if (backupPath != null) {
					EventObservable.addEventToQueue(SammelboxEvent.DISABLE_SAMMELBOX);
					BrowserFacade.showBackupInProgressPage();
					DatabaseIntegrityManager.getBackupThread(backupPath).start();
				}
			}
		};
	}
	
	static SelectionAdapter getRestoreListener() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				ApplicationUI.changeRightCompositeTo(PanelType.EMPTY, EmptySidepane.build(ApplicationUI.getThreePanelComposite()));

				// Show restore backup warning - do you really want to delete these items?
				if (!ComponentFactory.showYesNoDialog(ApplicationUI.getShell(), 
						Translator.toBeTranslated("Warning: You might lose recent modifications"), 
						Translator.toBeTranslated("Do you really want to restore a previous backup? Please note that " +
								"all recent modifications will be lost. The restore process will overwrite everything!"))) {
					return;
				}
				
				// Open backup selection dialog
				FileDialog openFileDialog = new FileDialog(ApplicationUI.getShell(), SWT.OPEN);
				openFileDialog.setText(Translator.get(DictKeys.DIALOG_RESTORE_FROM_FILE));
				openFileDialog.setFilterPath(System.getProperty("user.home"));
				String[] filterExt = { "*.cbk" };
				openFileDialog.setFilterExtensions(filterExt);

				String path = openFileDialog.open();
				if (path != null) {
					EventObservable.addEventToQueue(SammelboxEvent.DISABLE_SAMMELBOX);
					BrowserFacade.showRestoreInProgressPage();
					DatabaseIntegrityManager.getRestoreThread(path).start();
				}
			}
		};
	}
	
	static SelectionAdapter getExitListener() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				ApplicationUI.getShell().close();
			}
		};
	}

	static SelectionAdapter getSynchronizeListener() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				BrowserFacade.showHtmlPage("synchronize");
				
				ApplicationUI.changeRightCompositeTo(
						PanelType.SYNCHRONIZATION, SynchronizeSidepane.build(ApplicationUI.getThreePanelComposite()));
			}
		};
	}
}
