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

package org.sammelbox.controller;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.controller.managers.DatabaseIntegrityManager;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.view.ApplicationUI;
import org.sammelbox.view.various.LoadingOverlayShell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
