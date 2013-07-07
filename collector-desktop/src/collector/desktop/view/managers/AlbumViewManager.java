package collector.desktop.view.managers;

import java.util.Collection;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import collector.desktop.controller.events.EventObservable;
import collector.desktop.controller.events.SammelboxEvent;
import collector.desktop.controller.filesystem.FileSystemAccessWrapper;
import collector.desktop.model.database.DatabaseWrapper;
import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException;
import collector.desktop.model.database.exceptions.ExceptionHelper;

public class AlbumViewManager {
	private final static Logger LOGGER = LoggerFactory.getLogger(AlbumViewManager.class);
	private static Collection<AlbumView> albumViews = new LinkedList<AlbumView>();
	
	private AlbumViewManager() {
		initialize();
	}
	
	/** Initializes album views without notifying any attached observers */
	public static void initialize() {
		AlbumViewManager.loadViews();
	}
	
	public static Collection<AlbumView> getAlbumViews(String albumName) {
		Collection<AlbumView> filteredAlbumViews = new LinkedList<AlbumView>();
		
		for (AlbumView albumView : albumViews) {
			if (albumView.getAlbum().equals(albumName)) {
				filteredAlbumViews.add(albumView);
			}
		}
		
		return filteredAlbumViews;
	}

	private static void setAlbumViews(Collection<AlbumView> albumViews) {
		AlbumViewManager.albumViews = albumViews;
	}
	
	public static void addAlbumView(String name, String album, String sqlQuery) {
		albumViews.add(new AlbumView(name, album, sqlQuery));
		
		storeViewAndAddAlbumViewListUpdatedEvent();
	}
	
	public static void removeAlbumView(String name) {
		for (AlbumView albumView : albumViews) {
			if (albumView.getName().equals(name)) {
				albumViews.remove(albumView);
			}
		}
		
		storeViewAndAddAlbumViewListUpdatedEvent();
	}
	
	public static boolean hasViewWithName(String viewName) {
		for (AlbumView albumView : albumViews) {
			if (albumView.name.equals(viewName)) {
				return true;
			}
		}
		
		return false;
	}
	
	private static void storeViews() {
		FileSystemAccessWrapper.storeViews(albumViews);
	}
	
	public static void loadViews() {
		Collection<AlbumView> validAlbumViews = new LinkedList<AlbumView>();
		
		for (AlbumView albumView : FileSystemAccessWrapper.loadViews()) {
			try {
				if (DatabaseWrapper.listAllAlbums().contains(albumView.getAlbum())) {
					validAlbumViews.add(albumView);
				}
			} catch (DatabaseWrapperOperationException ex) {
				LOGGER.error("An error occured while retrieving the list of albums from the database \n Stacktrace: " + ExceptionHelper.toString(ex));
			}
		}
		
		AlbumViewManager.setAlbumViews(validAlbumViews);
		AlbumViewManager.storeViews();
	}
	
	public static class AlbumView {
		private String name;
		private String album;
		private String sqlQuery;
				
		public AlbumView(String name, String album, String sqlQuery) {
			this.name = name;
			this.album = album;
			this.sqlQuery = sqlQuery;
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

		public String getSqlQuery() {
			return sqlQuery;
		}
		
		public void setSqlQuery(String sqlQuery) {
			this.sqlQuery = sqlQuery;
		}
	}

	public static String getSqlQueryByName(String queryName) {
		for (AlbumView albumView : albumViews) {
			if (albumView.getName().equals(queryName)) {
				return albumView.getSqlQuery();
			}
		}
		
		return null;
	}

	public static boolean hasAlbumViewsAttached(String albumName) {
		for (AlbumView albumView : albumViews) {
			if (albumView.getAlbum().equals(albumName)) {
				return true;
			}
		}
		
		return false;
	}

	public static void moveToFront(int selectionIndex) {
		AlbumView tmp = ((LinkedList<AlbumView>) albumViews).get(selectionIndex);
		((LinkedList<AlbumView>) albumViews).remove(selectionIndex);
		((LinkedList<AlbumView>) albumViews).addFirst(tmp);
		
		storeViewAndAddAlbumViewListUpdatedEvent();
	}

	public static void moveOneUp(int selectionIndex) {
		if (selectionIndex-1 >= 0) {
			AlbumView tmp = ((LinkedList<AlbumView>) albumViews).get(selectionIndex-1);
			
			((LinkedList<AlbumView>) albumViews).set(selectionIndex-1, ((LinkedList<AlbumView>) albumViews).get(selectionIndex));
			((LinkedList<AlbumView>) albumViews).set(selectionIndex, tmp);
			
			storeViewAndAddAlbumViewListUpdatedEvent();
		}
	}

	public static void moveOneDown(int selectionIndex) {
		if (selectionIndex+1 <= albumViews.size()-1) {
			AlbumView tmp = ((LinkedList<AlbumView>) albumViews).get(selectionIndex+1);
			
			((LinkedList<AlbumView>) albumViews).set(selectionIndex+1, ((LinkedList<AlbumView>) albumViews).get(selectionIndex));
			((LinkedList<AlbumView>) albumViews).set(selectionIndex, tmp);
			
			storeViewAndAddAlbumViewListUpdatedEvent();
		}
	}

	public static void moveToBottom(int selectionIndex) {
		AlbumView tmp = ((LinkedList<AlbumView>) albumViews).get(selectionIndex);
		((LinkedList<AlbumView>) albumViews).remove(selectionIndex);
		((LinkedList<AlbumView>) albumViews).addLast(tmp);
		
		storeViewAndAddAlbumViewListUpdatedEvent();
	}

	public static void removeAlbumViews(String selectedAlbum) {
		for (AlbumView albumView : albumViews) {
			if (albumView.getAlbum().equals(selectedAlbum)) {
				albumViews.remove(albumView);
			}
		}
		
		storeViewAndAddAlbumViewListUpdatedEvent();
	}
	
	private static void storeViewAndAddAlbumViewListUpdatedEvent() {
		storeViews();
		EventObservable.addEventToQueue(SammelboxEvent.ALBUM_VIEW_LIST_UPDATED);
	}
}
