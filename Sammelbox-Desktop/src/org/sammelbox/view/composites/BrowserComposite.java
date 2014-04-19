/** -----------------------------------------------------------------
 *    Sammelbox: Collection Manager - A free and open-source collection manager for Windows & Linux
 *    Copyright (C) 2011 Jerome Wagener & Paul Bicheler
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

package org.sammelbox.view.composites;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.sammelbox.controller.listeners.BrowserListener;

public final class BrowserComposite extends Composite {
	private Browser browser;
	
	public BrowserComposite(Composite parentComposite, int flags, BrowserListener browserListener) {
		super(parentComposite, flags);

		setLayout(new GridLayout());

		// the browser itself
		browser = new Browser(this, SWT.NONE);
		browser.setDragDetect(false);

		// setup layout data for the browser
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		browser.setLayoutData(gridData);

		browser.addLocationListener(browserListener);
		browser.addProgressListener(browserListener);
		browser.addMenuDetectListener(browserListener);
	}
	
	public Browser getBrowser() {
		return browser;
	}
}
