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

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.view.various.ComponentFactory;
import org.sammelbox.view.various.SynchronizeCompositeHelper;
import org.sammelbox.view.various.SynchronizeStep;

public final class SynchronizeSidepane {
	private SynchronizeSidepane() {
		// use build method instead
	}
	
	public static Composite build(Composite parentComposite) {		
		// setup synchronize composite
		Composite synchronizeComposite = new Composite(parentComposite, SWT.NONE);
		synchronizeComposite.setLayout(new GridLayout(1, false));
		synchronizeComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		// label header
		ComponentFactory.getPanelHeaderComposite(synchronizeComposite, Translator.get(DictKeys.LABEL_SYNCHRONIZE));

		// min height griddata
		GridData minHeightGridData = new GridData(GridData.FILL_BOTH);
		minHeightGridData.minimumHeight = 20;

		final Button startButton = new Button(synchronizeComposite, SWT.PUSH);
		startButton.setText(Translator.get(DictKeys.BUTTON_START_SYNCHRONIZATION));
		startButton.setLayoutData(new GridData(GridData.FILL_BOTH));
		startButton.setEnabled(false); 
		// TODO implement the synchronization with mobile devices
		// listener after cancel button since this button reference is needed

		// separator
		new Label(synchronizeComposite, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(minHeightGridData);

		final HashMap<SynchronizeStep, Label> synchronizeStepsToLabelsMap = new HashMap<SynchronizeStep, Label>();

		final Label establishConnectionLabel = ComponentFactory.getH4Label(synchronizeComposite, "\u25CF " + Translator.get(DictKeys.LABEL_ESTABLISHING_CONNECTION));
		establishConnectionLabel.setLayoutData(minHeightGridData);
		establishConnectionLabel.setEnabled(false);
		synchronizeStepsToLabelsMap.put(SynchronizeStep.ESTABLISH_CONNECTION, establishConnectionLabel);

		final Label uploadDataLabel = ComponentFactory.getH4Label(synchronizeComposite, "\u25CF " + Translator.get(DictKeys.LABEL_UPLOAD_DATA));
		uploadDataLabel.setLayoutData(minHeightGridData);
		uploadDataLabel.setEnabled(false);
		synchronizeStepsToLabelsMap.put(SynchronizeStep.UPLOAD_DATA, uploadDataLabel);

		final Label installDataLabel = ComponentFactory.getH4Label(synchronizeComposite, "\u25CF " + Translator.get(DictKeys.LABEL_INSTALL_DATA));
		installDataLabel.setLayoutData(minHeightGridData);
		installDataLabel.setEnabled(false);
		synchronizeStepsToLabelsMap.put(SynchronizeStep.INSTALL_DATA, installDataLabel);

		final Label finishLabel = ComponentFactory.getH4Label(synchronizeComposite, "\u25CF " + Translator.get(DictKeys.LABEL_FINISH));
		finishLabel.setLayoutData(minHeightGridData);
		finishLabel.setEnabled(false);
		synchronizeStepsToLabelsMap.put(SynchronizeStep.FINISH, finishLabel);

		// Add Observers
		SynchronizeCompositeHelper synchronizeCompositeHelper = new SynchronizeCompositeHelper();
		synchronizeCompositeHelper.storeSynchronizeCompositeLabels(synchronizeStepsToLabelsMap);

		// separator
		new Label(synchronizeComposite, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(minHeightGridData);

		final Button cancelButton = new Button(synchronizeComposite, SWT.PUSH);
		cancelButton.setText(Translator.get(DictKeys.BUTTON_CANCEL_SYNCHRONIZATION));
		cancelButton.setEnabled(false);
		cancelButton.setLayoutData(new GridData(GridData.FILL_BOTH));

		startButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				establishConnectionLabel.setEnabled(true);
				cancelButton.setEnabled(true);
				startButton.setEnabled(false);
			}
		});

		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				establishConnectionLabel.setEnabled(false);
				uploadDataLabel.setEnabled(false);
				installDataLabel.setEnabled(false);
				finishLabel.setEnabled(false);
				cancelButton.setEnabled(false);
				startButton.setEnabled(true);
			}
		});

		return synchronizeComposite;
	}
}
