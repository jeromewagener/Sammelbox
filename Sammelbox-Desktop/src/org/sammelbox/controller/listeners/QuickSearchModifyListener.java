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

package org.sammelbox.controller.listeners;

import java.util.Arrays;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Text;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseOperations;
import org.sammelbox.view.ApplicationUI;
import org.sammelbox.view.browser.BrowserFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuickSearchModifyListener implements ModifyListener {
	private final static Logger LOGGER = LoggerFactory.getLogger(QuickSearchModifyListener.class);
	
	@Override
	/** This method launches a quick-search for the entered keywords if an album has been selected*/
	public void modifyText(ModifyEvent e) {
		if (!ApplicationUI.isAlbumSelectedAndShowMessageIfNot()) {
			return;
		}
		try {
			if (((Text) e.widget).getText().equals("")) {
				BrowserFacade.showResultSet(DatabaseOperations.executeQuickSearch(ApplicationUI.getSelectedAlbum(), null));
			} else {
				BrowserFacade.showResultSet(DatabaseOperations.executeQuickSearch(ApplicationUI.getSelectedAlbum(), 
						Arrays.asList(((Text) e.widget).getText().split(" "))));
			}
		} catch (DatabaseWrapperOperationException ex) {
			LOGGER.error("An error occured while performing a quick search on the following string '" + ((Text) e.widget).getText() + "'", ex);
		}
	}
}
