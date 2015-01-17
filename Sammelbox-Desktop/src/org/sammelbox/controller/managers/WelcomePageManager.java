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

package org.sammelbox.controller.managers;

import org.sammelbox.controller.filesystem.xml.XmlStorageWrapper;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.model.album.Album;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

public final class WelcomePageManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(WelcomePageManager.class);
	private static Map<String, Long> albumToLastModified;
	
	private WelcomePageManager() {
		// not needed
	}
	
	public static void initializeFromWelcomeFile() {
		albumToLastModified = XmlStorageWrapper.retrieveAlbumToLastModified();
	}

	public static void updateLastModifiedWithCurrentDate(String albumName) {
		albumToLastModified.put(albumName, System.currentTimeMillis());

		storeWelcomePageManagerInformation();
	}

	private static void storeWelcomePageManagerInformation() {
		// perform last updated cleanup
		Iterator<String> albumNameIterator = albumToLastModified.keySet().iterator();
		while (albumNameIterator.hasNext()) {
			String albumName = albumNameIterator.next();

			boolean found = false;
			for (Album album : AlbumManager.getAlbums()) {
				if (album.getAlbumName().equals(albumName)) {
					found = true;
				}
			}

			if (!found) {
				albumNameIterator.remove();
			}
		}
		
		// store
		XmlStorageWrapper.storeWelcomePageManagerInformation(albumToLastModified);
	}

	public static String getLastModifiedDate(String albumName) {
		for (String key : albumToLastModified.keySet()) {
			if (key.equals(albumName)) {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SettingsManager.getSettings().getDateFormat());
				return simpleDateFormat.format(new Date(albumToLastModified.get(key)));
			}
		}

		return Translator.get(DictKeys.BROWSER_NEVER);
	}

	public static Long getNumberOfItemsInAlbum(String albumName) {
		try {
			return DatabaseOperations.getNumberOfItemsInAlbum(albumName);
		} catch (DatabaseWrapperOperationException ex) {
			LOGGER.error("Could not retrieve the number of items in the '" + albumName + "' album", ex);

			return 0L;
		}
	}
}
