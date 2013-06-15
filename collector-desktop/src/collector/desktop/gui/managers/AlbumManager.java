package collector.desktop.gui.managers;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import collector.desktop.database.DatabaseWrapper;
import collector.desktop.database.exceptions.FailedDatabaseWrapperOperationException;
import collector.desktop.filesystem.FileSystemAccessWrapper;
import collector.desktop.interfaces.UIObservable;
import collector.desktop.interfaces.UIObserver;

public class AlbumManager  implements UIObservable {
	private static AlbumManager instance;
	private static List<String> albums = new LinkedList<String>();
	private static Collection<UIObserver> uiObservers = new LinkedList<UIObserver>();
	
	private AlbumManager() {
		initialize();
	}
	
	public void mergeDatabaseAndXmlAlbums() {
		albums = FileSystemAccessWrapper.loadAlbums();
		
		try {
			for (String album : DatabaseWrapper.listAllAlbums()) {
				if (!albums.contains(album)) {
					albums.add(album);
				}
			}

			albums.retainAll(DatabaseWrapper.listAllAlbums());

		} catch (FailedDatabaseWrapperOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void initialize() {
		albums = FileSystemAccessWrapper.loadAlbums();
		
		mergeDatabaseAndXmlAlbums();
		
		FileSystemAccessWrapper.storeAlbums(albums);
	}
	
	public List<String> getAlbums() {
		albums = FileSystemAccessWrapper.loadAlbums();
		
		mergeDatabaseAndXmlAlbums();
		
		return albums;
	}
	
	private void storeAlbums() {
		FileSystemAccessWrapper.storeAlbums(albums);
	}
	
	public static AlbumManager getInstance() {
		if (instance == null) {
			instance = new AlbumManager();
		}
			
		return instance;
	}
	
	public void setAlbums(List<String> albums) {
		AlbumManager.albums = albums;
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
	
	public void moveToFront(int selectionIndex) {
		String tmp = ((LinkedList<String>) albums).get(selectionIndex);
		((LinkedList<String>) albums).remove(selectionIndex);
		((LinkedList<String>) albums).addFirst(tmp);
		
		storeAlbums();
		
		instance.notifyObservers();
	}

	public void moveOneUp(int selectionIndex) {
		if (selectionIndex-1 >= 0) {
			String tmp = ((LinkedList<String>) albums).get(selectionIndex-1);
			
			((LinkedList<String>) albums).set(selectionIndex-1, ((LinkedList<String>) albums).get(selectionIndex));
			((LinkedList<String>) albums).set(selectionIndex, tmp);
			
			storeAlbums();
			
			instance.notifyObservers();
		}
	}

	public void moveOneDown(int selectionIndex) {
		if (selectionIndex+1 <= albums.size()-1) {
			String tmp = ((LinkedList<String>) albums).get(selectionIndex+1);
			
			((LinkedList<String>) albums).set(selectionIndex+1, ((LinkedList<String>) albums).get(selectionIndex));
			((LinkedList<String>) albums).set(selectionIndex, tmp);
			
			storeAlbums();
			
			instance.notifyObservers();
		}
	}

	public void moveToBottom(int selectionIndex) {
		String tmp = ((LinkedList<String>) albums).get(selectionIndex);
		((LinkedList<String>) albums).remove(selectionIndex);
		((LinkedList<String>) albums).addLast(tmp);
		
		storeAlbums();
		
		instance.notifyObservers();
	}
}
