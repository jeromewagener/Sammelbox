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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.sammelbox.controller.events.EventObservable;
import org.sammelbox.controller.events.SammelboxEvent;
import org.sammelbox.controller.filesystem.xml.XmlStorageWrapper;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.model.database.QueryBuilder;
import org.sammelbox.model.database.QueryBuilderException;
import org.sammelbox.model.database.QueryComponent;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseOperations;
import org.sammelbox.view.various.ComponentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SavedSearchManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(SavedSearchManager.class);
	private static Map<String, List<SavedSearch>> albumNamesToSavedSearches = new HashMap<String, List<SavedSearch>>();
	
	private SavedSearchManager() {
		// not needed
	}
	
	/** Initializes saved searches without notifying any attached observers */
	public static void initialize() {
		SavedSearchManager.loadViews();
	}
	
	/** Returns the complete list of saved searches names for all available albums */
	public static List<String> getSavedSearchesNames() {
		List<String> allViewNames = new LinkedList<String>();
		
		for (String albumName : albumNamesToSavedSearches.keySet()) {
			for (SavedSearch albumView : albumNamesToSavedSearches.get(albumName)) {
				allViewNames.add(albumView.name);
			}
		}
		
		return allViewNames;
	}
	
	/** Returns the list of saved searches for a specific album */
	public static List<SavedSearch> getSavedSearches(String albumName) {
		List<SavedSearch> savedSearches = albumNamesToSavedSearches.get(albumName);
		
		if (savedSearches != null) {
			return savedSearches;
		}
		
		return new LinkedList<SavedSearch>();
	}
	
	public static String[] getSavedSearchesNamesArray(String albumName) {
		List<SavedSearch> albumViews = getSavedSearches(albumName);
		
		String[] albumViewNames = new String[albumViews.size()];
		
		for (int i=0; i<albumViews.size(); i++) {
			albumViewNames[i] = albumViews.get(i).getName();
		}
		
		return albumViewNames;
	}
	
	public static void addSavedSearch(String name, String album, List<QueryComponent> queryComponents, boolean connectByAnd) {
		addSavedSearch(name, album, null, true, queryComponents, connectByAnd);
	}
	
	public static void addSavedSearch(String name, String album, String orderByField, boolean orderAscending, 
			List<QueryComponent> queryComponents, boolean connectByAnd) {
		if (albumNamesToSavedSearches.get(album) == null) {
			List<SavedSearch> albumViews = new LinkedList<>();
			albumViews.add(new SavedSearch(name, album, orderByField, orderAscending, queryComponents, connectByAnd));
			albumNamesToSavedSearches.put(album, albumViews);
		} else {
			List<SavedSearch> albumViews = albumNamesToSavedSearches.get(album);
			albumViews.add(new SavedSearch(name, album, orderByField, orderAscending, queryComponents, connectByAnd));
			albumNamesToSavedSearches.put(album, albumViews);
		}
		
		storeSavedSearchesAndAddListUpdatedEvent();
	}
	
	public static void removeSavedSearch(String albumName, String viewName) {
		List<SavedSearch> albumViews = albumNamesToSavedSearches.get(albumName);
		
		for (SavedSearch albumView : albumViews) {
			if (albumView.getName().equals(viewName)) {
				albumViews.remove(albumView);
				break;
			}
		}
		
		storeSavedSearchesAndAddListUpdatedEvent();
	}
	
	public static boolean isNameAlreadyUsed(String albumName, String viewName) {
		List<SavedSearch> albumViews = albumNamesToSavedSearches.get(albumName);
		
		if (albumViews == null) {
			return false;
		}
		
		for (SavedSearch albumView : albumViews) {
			if (albumView.name.equals(viewName)) {
				return true;
			}
		}
		
		return false;
	}
	
	private static void storeViews() {
		XmlStorageWrapper.storeSavedSearches(albumNamesToSavedSearches);
	}
	
	private static void loadViews() {
		albumNamesToSavedSearches = XmlStorageWrapper.retrieveSavedSearches();
		
		for (String albumName : albumNamesToSavedSearches.keySet()) {
			try {
				if (!DatabaseOperations.getListOfAllAlbums().contains(albumName)) {
					albumNamesToSavedSearches.remove(albumName);
				}
			} catch (DatabaseWrapperOperationException ex) {
				LOGGER.error("An error occured while retrieving the list of albums from the database \n Stacktrace: ", ex);
			}
		}
		
		SavedSearchManager.storeViews();
	}
	
	public static class SavedSearch {
		private String name;
		private String album;
		private String orderByField;
		private boolean orderAscending;
		private List<QueryComponent> queryComponents;
		private boolean connectedByAnd;
				
		public SavedSearch(String name, String album, String orderByField, boolean orderAscending, 
				List<QueryComponent> queryComponents, boolean connectedByAnd) {
			this.name = name;
			this.album = album;
			this.orderByField = orderByField;
			this.orderAscending = orderAscending;
			this.queryComponents = queryComponents;
			this.connectedByAnd = connectedByAnd;
		}
				
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public String getAlbum() {
			return album;
		}

		public void setAlbum(String album) {
			this.album = album;
		}

		public String getOrderByField() {
			return orderByField;
		}

		public void setOrderByField(String orderByField) {
			this.orderByField = orderByField;
		}

		public List<QueryComponent> getQueryComponents() {
			return queryComponents;
		}

		public void setQueryComponents(List<QueryComponent> queryComponents) {
			this.queryComponents = queryComponents;
		}

		public boolean isConnectedByAnd() {
			return connectedByAnd;
		}

		public void setConnectedByAnd(boolean connectedByAnd) {
			this.connectedByAnd = connectedByAnd;
		}

		public boolean isOrderAscending() {
			return orderAscending;
		}

		public void setOrderAscending(boolean orderAscending) {
			this.orderAscending = orderAscending;
		}
		
		public String getSQLQueryString() throws QueryBuilderException {
			if (this.getOrderByField() == null || this.getOrderByField().isEmpty()) {
				return QueryBuilder.buildQuery(this.getQueryComponents(), 
						this.isConnectedByAnd(), this.getAlbum());
			} else {
				return QueryBuilder.buildQuery(this.getQueryComponents(), 
						this.isConnectedByAnd(), this.getAlbum(), 
						this.getOrderByField(), this.isOrderAscending());
			}
		}
	}

	public static String getSqlQueryBySavedSearchName(String albumName, String savedSearchName) {
		List<SavedSearch> savedSearches = albumNamesToSavedSearches.get(albumName);
		
		for (SavedSearch savedSearch : savedSearches) {
			if (savedSearch.getName().equals(savedSearchName)) {
				try {
					if (savedSearch.getOrderByField() == null || savedSearch.getOrderByField().isEmpty()) {
						return QueryBuilder.buildQuery(savedSearch.getQueryComponents(), 
								savedSearch.isConnectedByAnd(), savedSearch.getAlbum());
					} else {
						return QueryBuilder.buildQuery(savedSearch.getQueryComponents(), 
								savedSearch.isConnectedByAnd(), savedSearch.getAlbum(), 
								savedSearch.getOrderByField(), savedSearch.isOrderAscending());
					}
				} catch (QueryBuilderException queryBuilderException) {
					ComponentFactory.getMessageBox(Translator.toBeTranslated("An error occurred"), queryBuilderException.getMessage(), SWT.ICON_ERROR);
					LOGGER.error("An error occurred while executing the saved search: ", queryBuilderException);
				}
			}
		}
		
		LOGGER.error("The following saved search (" + savedSearchName + ") could not be found within " + albumName);
		return null;
	}

	public static SavedSearch getSavedSearchByName(String albumName, String savedSearchName) {
		List<SavedSearch> savedSearches = albumNamesToSavedSearches.get(albumName);
		
		for (SavedSearch savedSearch : savedSearches) {
			if (savedSearch.getName().equals(savedSearchName)) {
				return savedSearch;
			}
		}
		
		LOGGER.error("The following saved search (" + savedSearchName + ") could not be found within " + albumName);
		return null;
	}
	
	public static boolean hasAlbumSavedSearches(String albumName) {
		List<SavedSearch> albumViews = albumNamesToSavedSearches.get(albumName);
		
		if (albumViews != null) {
			for (SavedSearch albumView : albumViews) {
				if (albumView.getAlbum().equals(albumName)) {
					return true;
				}
			}
		}
		
		return false;
	}

	public static void moveToFront(String albumName, int selectionIndex) {
		List<SavedSearch> albumViews = albumNamesToSavedSearches.get(albumName);
		
		SavedSearch tmp = ((LinkedList<SavedSearch>) albumViews).get(selectionIndex);
		((LinkedList<SavedSearch>) albumViews).remove(selectionIndex);
		((LinkedList<SavedSearch>) albumViews).addFirst(tmp);
		
		storeSavedSearchesAndAddListUpdatedEvent();
	}

	public static void moveOneUp(String albumName, int selectionIndex) {
		List<SavedSearch> albumViews = albumNamesToSavedSearches.get(albumName);
		
		if (selectionIndex-1 >= 0) {
			SavedSearch tmp = ((LinkedList<SavedSearch>) albumViews).get(selectionIndex-1);
			
			((LinkedList<SavedSearch>) albumViews).set(selectionIndex-1, ((LinkedList<SavedSearch>) albumViews).get(selectionIndex));
			((LinkedList<SavedSearch>) albumViews).set(selectionIndex, tmp);
			
			storeSavedSearchesAndAddListUpdatedEvent();
		}
	}

	public static void moveOneDown(String albumName, int selectionIndex) {
		List<SavedSearch> albumViews = albumNamesToSavedSearches.get(albumName);
		
		if (selectionIndex+1 <= albumViews.size()-1) {
			SavedSearch tmp = ((LinkedList<SavedSearch>) albumViews).get(selectionIndex+1);
			
			((LinkedList<SavedSearch>) albumViews).set(selectionIndex+1, ((LinkedList<SavedSearch>) albumViews).get(selectionIndex));
			((LinkedList<SavedSearch>) albumViews).set(selectionIndex, tmp);
			
			storeSavedSearchesAndAddListUpdatedEvent();
		}
	}

	public static void moveToBottom(String albumName, int selectionIndex) {
		List<SavedSearch> albumViews = albumNamesToSavedSearches.get(albumName);
		
		SavedSearch tmp = ((LinkedList<SavedSearch>) albumViews).get(selectionIndex);
		((LinkedList<SavedSearch>) albumViews).remove(selectionIndex);
		((LinkedList<SavedSearch>) albumViews).addLast(tmp);
		
		storeSavedSearchesAndAddListUpdatedEvent();
	}

	public static void removeSavedSearchesFromAlbum(String albumName) {
		albumNamesToSavedSearches.remove(albumName);
		
		storeSavedSearchesAndAddListUpdatedEvent();
	}
	
	private static void storeSavedSearchesAndAddListUpdatedEvent() {
		storeViews();
		EventObservable.addEventToQueue(SammelboxEvent.ALBUM_VIEW_LIST_UPDATED);
	}

	public static void updateAlbumNameIfNecessary(String oldAlbumName, String newAlbumName) {
		List<SavedSearch> savedSearches = albumNamesToSavedSearches.get(oldAlbumName);
		
		for (SavedSearch savedSearch : savedSearches) {
			savedSearch.album = newAlbumName;			
		}
		
		storeViews();
		initialize();
	}
}
