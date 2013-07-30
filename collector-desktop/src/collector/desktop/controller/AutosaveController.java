package collector.desktop.controller;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import collector.desktop.controller.i18n.DictKeys;
import collector.desktop.controller.i18n.Translator;
import collector.desktop.controller.managers.DatabaseIntegrityManager;
import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException;
import collector.desktop.view.ApplicationUI;
import collector.desktop.view.various.LoadingOverlayShell;

// TODO remove or adapt
public class AutosaveController {
	private final static Logger LOGGER = LoggerFactory.getLogger(AutosaveController.class);
	
	public static LoadingOverlayShell createAutosaveOverlay () {
		final Shell shell = ApplicationUI.getShell();
		
		final LoadingOverlayShell loadingOverlayShell = new LoadingOverlayShell(shell, Translator.get(DictKeys.LABEL_AUTOSAVE_DATABASE));
		loadingOverlayShell.setCloseParentWhenDone(true);
		shell.addListener(SWT.Close, new Listener() {			
			@Override
			public void handleEvent(Event event) {
				// Show the loading overlay while creating a database autosave
				if (!loadingOverlayShell.isDone()) {
					launchLoadingOverlayShell(loadingOverlayShell, false);					
					event.doit =  false;
				}
			}
		});
		
		return loadingOverlayShell;
	}
	
	public static void launchLoadingOverlayShell(final LoadingOverlayShell shell, boolean useWorkerThread) {
		shell.start();
		if (useWorkerThread) {
			final Thread performer = new Thread(new Runnable() {
				@Override
				public void run() {
					createAutoSaveOfDatabase(shell);
				}
			});
			performer.start();
		} else {
			createAutoSaveOfDatabase(shell);
		}
	}

	public static void createAutoSaveOfDatabase(LoadingOverlayShell shell) {
		// Backup the database in a Thread running in parallel to the SWT UI Thread. 
		try {
			DatabaseIntegrityManager.backupAutoSave();
		} catch (DatabaseWrapperOperationException ex) {
			LOGGER.error("Couldn't backup the auto save", ex);
		}
		shell.stop();
	}
}
