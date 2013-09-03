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

package org.sammelbox.controller.managers;

import java.util.LinkedList;
import java.util.List;

import org.sammelbox.controller.events.EventObservable;
import org.sammelbox.controller.events.SammelboxEvent;
import org.sammelbox.controller.filesystem.XMLStorageWrapper;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlbumManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(AlbumManager.class);
	private static List<String> albums = new LinkedList<String>();
	
	private static void mergeDatabaseAndXmlAlbums() {
		albums = XMLStorageWrapper.retrieveAlbums();
		
		try {
			for (String album : DatabaseOperations.getListOfAllAlbums()) {
				if (!albums.contains(album)) {
					albums.add(album);
				}
			}

			albums.retainAll(DatabaseOperations.getListOfAllAlbums());

		} catch (DatabaseWrapperOperationException ex) {
			LOGGER.error("A problem occured while retrieving the list of albums from the database", ex);
		}
	}
	
	public static void initialize() {
		albums = XMLStorageWrapper.retrieveAlbums();
		
		mergeDatabaseAndXmlAlbums();
		
		XMLStorageWrapper.storeAlbums(albums);
	}
	
	public static List<String> getAlbums() {
		albums = XMLStorageWrapper.retrieveAlbums();
		
		mergeDatabaseAndXmlAlbums();
		
		return albums;
	}
	
	private static void storeAlbums() {
		XMLStorageWrapper.storeAlbums(albums);
	}
	
	public static void setAlbums(List<String> albums) {
		AlbumManager.albums = albums;
	}
	
	public static void moveToFront(int selectionIndex) {
		String tmp = ((LinkedList<String>) albums).get(selectionIndex);
		((LinkedList<String>) albums).remove(selectionIndex);
		((LinkedList<String>) albums).addFirst(tmp);
		
		storeAlbums();
		
		EventObservable.addEventToQueue(SammelboxEvent.ALBUM_LIST_UPDATED);
	}

	public static void moveOneUp(int selectionIndex) {
		if (selectionIndex-1 >= 0) {
			String tmp = ((LinkedList<String>) albums).get(selectionIndex-1);
			
			((LinkedList<String>) albums).set(selectionIndex-1, ((LinkedList<String>) albums).get(selectionIndex));
			((LinkedList<String>) albums).set(selectionIndex, tmp);
			
			storeAlbums();
			
			EventObservable.addEventToQueue(SammelboxEvent.ALBUM_LIST_UPDATED);
		}
	}

	public static void moveOneDown(int selectionIndex) {
		if (selectionIndex+1 <= albums.size()-1) {
			String tmp = ((LinkedList<String>) albums).get(selectionIndex+1);
			
			((LinkedList<String>) albums).set(selectionIndex+1, ((LinkedList<String>) albums).get(selectionIndex));
			((LinkedList<String>) albums).set(selectionIndex, tmp);
			
			storeAlbums();
			
			EventObservable.addEventToQueue(SammelboxEvent.ALBUM_LIST_UPDATED);
		}
	}

	public static void moveToBottom(int selectionIndex) {
		String tmp = ((LinkedList<String>) albums).get(selectionIndex);
		((LinkedList<String>) albums).remove(selectionIndex);
		((LinkedList<String>) albums).addLast(tmp);
		
		storeAlbums();
		
		EventObservable.addEventToQueue(SammelboxEvent.ALBUM_LIST_UPDATED);
	}
}
