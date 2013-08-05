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

package collector.desktop.view.composites;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import collector.desktop.controller.i18n.DictKeys;
import collector.desktop.controller.i18n.Translator;
import collector.desktop.controller.managers.BuildInformationManager;
import collector.desktop.view.various.ComponentFactory;

public class StatusBarComposite {
	private static final int TIME_UNTIL_CLEAR_IN_MILLI_SECONDS = 10000;
	private static StatusBarComposite instance = null;
	private Composite statusbarComposite = null;
	private Label statusLabel = null;
	private Runnable timer = null;
	
	/** Returns a status bar composite which is used to display various information
	 * @param parentComposite the parent composite
	 * @return a new status bar composite */
	private StatusBarComposite(Composite parentComposite) {
		statusbarComposite = new Composite(parentComposite, SWT.NONE);

		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;

		statusbarComposite.setLayout(gridLayout);

		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		statusbarComposite.setLayoutData(gridData);

		GridData seperatorGridData = new GridData(GridData.FILL_BOTH);
		seperatorGridData.minimumHeight = 0;

		new Label(statusbarComposite, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(seperatorGridData);

		statusLabel = ComponentFactory.getSmallItalicLabel(statusbarComposite, getDefaultStatus());

		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		statusLabel.setLayoutData(gridData);
	}

	public static StatusBarComposite getInstance(Shell parentShell) {
		if (instance == null) {
			instance = new StatusBarComposite(parentShell);
		}

		return instance;
	}

	public Composite getStatusbarComposite() {
		return statusbarComposite;
	}

	/** Writes a status to the status bar, and automatically resets the status after TIME_UNTIL_CLEAR_IN_MILLI_SECONDS 
	 * @param status the status to be written */
	public void writeStatus(String status) {
		writeStatus(status, true);
	}
	
	/** Writes a status to the status bar
	 * @param status the status to be written
	 * @param autoResetDefaultStatus if set to true, the status is automatically reset after TIME_UNTIL_CLEAR_IN_MILLI_SECONDS.
	 * if set to false, the last status will be remain until a new status is written. */
	public void writeStatus(String status, boolean autoResetDefaultStatus) {
		statusLabel.setText(status);

		if (!status.equals(getDefaultStatus()) && autoResetDefaultStatus) {
			if (timer != null) {
				Display.getCurrent().timerExec(-1, timer);
			} else {
				timer = new Runnable() {
					public void run() {
						writeStatus(getDefaultStatus());
					}
				};
			}
			
			Display.getCurrent().timerExec(TIME_UNTIL_CLEAR_IN_MILLI_SECONDS, timer);
		}
	}
	
	private String getDefaultStatus() {
		return Translator.get(DictKeys.STATUSBAR_PROGRAM_STARTED, BuildInformationManager.instance().getVersion(), 
				BuildInformationManager.instance().getBuildTimeStamp(), BuildInformationManager.instance().getBuildType());
	}
}
