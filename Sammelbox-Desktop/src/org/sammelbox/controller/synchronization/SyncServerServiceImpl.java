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

package org.sammelbox.controller.synchronization;

import java.io.File;
import java.io.IOException;

import org.sammelbox.controller.filesystem.FileSystemAccessWrapper;
import org.sammelbox.controller.filesystem.FileSystemLocations;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class SyncServerServiceImpl implements SyncServerService {
	private static final Logger LOGGER = LoggerFactory.getLogger(SyncServerServiceImpl.class);
	private static final String SYNC_FOLDER = "sammelbox-sync";
	
	public File createZipForSynchronziation() {
		File syncFolder = new File(FileSystemLocations.TEMP_DIR + SYNC_FOLDER);
		
		if (syncFolder.exists()) {
			FileSystemAccessWrapper.deleteDirectoryRecursively(syncFolder);
		}
		
		try {
			syncFolder.mkdir();

			FileSystemAccessWrapper.copyFile(
					new File(FileSystemLocations.getDatabaseFile()), 
					new File(FileSystemLocations.TEMP_DIR + SYNC_FOLDER + File.separatorChar + FileSystemLocations.DATABASE_NAME));

			FileSystemAccessWrapper.copyDirectory(
					new File(FileSystemLocations.getThumbnailsDir()), 
					new File(FileSystemLocations.TEMP_DIR + SYNC_FOLDER + File.separatorChar + FileSystemLocations.THUMBNAILS_DIR_NAME));

			FileSystemAccessWrapper.zipFolderToFile(
					syncFolder.getAbsolutePath(), FileSystemLocations.TEMP_DIR + SYNC_FOLDER + ".zip");
		} catch (IOException ioe) {
			LOGGER.error("An error occured while packaging the information before synchronization", ioe);
		}
		
		return syncFolder;
	}
}
