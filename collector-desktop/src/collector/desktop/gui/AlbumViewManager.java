package collector.desktop.gui;

import java.util.Collection;
import java.util.LinkedList;

import collector.desktop.filesystem.FileSystemAccessWrapper;
import collector.desktop.interfaces.UIObservable;
import collector.desktop.interfaces.UIObserver;

public class AlbumViewManager implements UIObservable {
	private static AlbumViewManager instance = new AlbumViewManager();
	private static Collection<AlbumView> albumViews = new LinkedList<AlbumView>();
	private static Collection<UIObserver> uiObservers = new LinkedList<UIObserver>();
	
	private AlbumViewManager() {
		initialize();
	}
	
	/** Initializes album views without notifying any attached observers */
	public static void initialize() {
		albumViews = FileSystemAccessWrapper.loadViews();
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
		
		storeViews();
		
		instance.notifyObservers();
	}
	
	public static void removeAlbumView(String name) {
		for (AlbumView albumView : albumViews) {
			if (albumView.getName().equals(name)) {
				albumViews.remove(albumView);
			}
		}
		
		storeViews();
		
		instance.notifyObservers();
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
		AlbumViewManager.setAlbumViews(FileSystemAccessWrapper.loadViews());
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
	
	@Override
	public void registerObserver(UIObserver observer) {
		uiObservers.add(observer);		
	}

	@Override
	public void unregisterObserver(UIObserver observer) {
		uiObservers.remove(observer);
	}

	@Override
	public void unregisterAllObservers() {
		uiObservers.clear();
	}

	@Override
	public void notifyObservers() {
		for (UIObserver uiObserver : uiObservers) {
			uiObserver.update(this.getClass());
		}
	}
	
	public static AlbumViewManager getInstance() {
		return instance;
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
		
		storeViews();
		
		instance.notifyObservers();
	}

	public static void moveOneUp(int selectionIndex) {
		if (selectionIndex-1 >= 0) {
			AlbumView tmp = ((LinkedList<AlbumView>) albumViews).get(selectionIndex-1);
			
			((LinkedList<AlbumView>) albumViews).set(selectionIndex-1, ((LinkedList<AlbumView>) albumViews).get(selectionIndex));
			((LinkedList<AlbumView>) albumViews).set(selectionIndex, tmp);
			
			storeViews();
			
			instance.notifyObservers();
		}
	}

	public static void moveOneDown(int selectionIndex) {
		if (selectionIndex+1 <= albumViews.size()-1) {
			AlbumView tmp = ((LinkedList<AlbumView>) albumViews).get(selectionIndex+1);
			
			((LinkedList<AlbumView>) albumViews).set(selectionIndex+1, ((LinkedList<AlbumView>) albumViews).get(selectionIndex));
			((LinkedList<AlbumView>) albumViews).set(selectionIndex, tmp);
			
			storeViews();
			
			instance.notifyObservers();
		}
	}

	public static void moveToBottom(int selectionIndex) {
		AlbumView tmp = ((LinkedList<AlbumView>) albumViews).get(selectionIndex);
		((LinkedList<AlbumView>) albumViews).remove(selectionIndex);
		((LinkedList<AlbumView>) albumViews).addLast(tmp);
		
		storeViews();
		
		instance.notifyObservers();
	}
}
