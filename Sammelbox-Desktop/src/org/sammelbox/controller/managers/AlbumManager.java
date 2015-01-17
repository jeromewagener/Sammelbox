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

import org.sammelbox.controller.events.EventObservable;
import org.sammelbox.controller.events.SammelboxEvent;
import org.sammelbox.controller.filesystem.xml.XmlStorageWrapper;
import org.sammelbox.controller.filters.MetaItemFieldFilter;
import org.sammelbox.model.album.Album;
import org.sammelbox.model.album.MetaItemField;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public final class AlbumManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(AlbumManager.class);
	private static List<Album> albums = new LinkedList<>();
	
	private AlbumManager() {
		// not needed
	}
	
	private static void mergeDatabaseAndXmlAlbums() {
		albums = XmlStorageWrapper.retrieveAlbums();
		
		try {
			for (String albumName : DatabaseOperations.getListOfAllAlbums()) {
				Album album = new Album();
				album.setAlbumName(albumName);
				
				if (!albums.contains(album)) {					
					List<MetaItemField> metaItemFields = MetaItemFieldFilter.getValidMetaItemFields(
							DatabaseOperations.getMetaItemFields(albumName));
					
					if (!metaItemFields.isEmpty()) {
						album.setSortByField(metaItemFields.get(0).getName());
					}
					
					albums.add(album);
				}
			}

			// retain all albums that are contained by the database
			List<String> allAlbums = DatabaseOperations.getListOfAllAlbums();
			List<Album> toBeRemovedAlbums = new ArrayList<>();
			for (Album album : albums) {
				if (!allAlbums.contains(album.getAlbumName())) {
					toBeRemovedAlbums.add(album);
				}
			}
			
			albums.removeAll(toBeRemovedAlbums);
			storeAlbums();
			
		} catch (DatabaseWrapperOperationException ex) {
			LOGGER.error("A problem occurred while retrieving the list of albums from the database", ex);
		}
	}
	
	public static void initialize() {
		albums = XmlStorageWrapper.retrieveAlbums();
		
		mergeDatabaseAndXmlAlbums();
		
		XmlStorageWrapper.storeAlbums(albums);
	}
	
	public static List<Album> getAlbums() {
		albums = XmlStorageWrapper.retrieveAlbums();
		
		mergeDatabaseAndXmlAlbums();
		
		return albums;
	}
	
	private static void storeAlbums() {
		XmlStorageWrapper.storeAlbums(albums);
	}
	
	public static void setAlbums(List<Album> albums) {
		AlbumManager.albums = albums;
	}
	
	public static void moveToFront(int selectionIndex) {
		Album tmp = ((LinkedList<Album>) albums).get(selectionIndex);
		((LinkedList<Album>) albums).remove(selectionIndex);
		((LinkedList<Album>) albums).addFirst(tmp);
		
		storeAlbums();
		
		EventObservable.addEventToQueue(SammelboxEvent.ALBUM_LIST_UPDATED);
	}

	public static void moveOneUp(int selectionIndex) {
		if (selectionIndex-1 >= 0) {
			Album tmp = ((LinkedList<Album>) albums).get(selectionIndex-1);
			
			((LinkedList<Album>) albums).set(selectionIndex-1, ((LinkedList<Album>) albums).get(selectionIndex));
			((LinkedList<Album>) albums).set(selectionIndex, tmp);
			
			storeAlbums();
			
			EventObservable.addEventToQueue(SammelboxEvent.ALBUM_LIST_UPDATED);
		}
	}

	public static void moveOneDown(int selectionIndex) {
		if (selectionIndex+1 <= albums.size()-1) {
			Album tmp = ((LinkedList<Album>) albums).get(selectionIndex+1);
			
			((LinkedList<Album>) albums).set(selectionIndex+1, ((LinkedList<Album>) albums).get(selectionIndex));
			((LinkedList<Album>) albums).set(selectionIndex, tmp);
			
			storeAlbums();
			
			EventObservable.addEventToQueue(SammelboxEvent.ALBUM_LIST_UPDATED);
		}
	}

	public static void moveToBottom(int selectionIndex) {
		Album tmp = ((LinkedList<Album>) albums).get(selectionIndex);
		((LinkedList<Album>) albums).remove(selectionIndex);
		((LinkedList<Album>) albums).addLast(tmp);
		
		storeAlbums();
		
		EventObservable.addEventToQueue(SammelboxEvent.ALBUM_LIST_UPDATED);
	}

	public static String getSortByField(String albumName) {
		for (Album album : albums) {
			if (album.getAlbumName().equals(albumName)) {
				return album.getSortByField();
			}
		}
		
		return null;
	}

	public static void setSortByField(String albumName, String sortByField) {
		for (Album album : albums) {
			if (album.getAlbumName().equals(albumName)) {
				album.setSortByField(sortByField);
			}
		}
		
		storeAlbums();
	}
}
