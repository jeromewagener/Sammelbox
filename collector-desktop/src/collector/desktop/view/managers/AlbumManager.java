package collector.desktop.view.managers;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import collector.desktop.controller.filesystem.FileSystemAccessWrapper;
import collector.desktop.controller.interfaces.UIObservable;
import collector.desktop.controller.interfaces.UIObserver;
import collector.desktop.model.database.DatabaseWrapper;
import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException;
import collector.desktop.model.database.exceptions.ExceptionHelper;

public class AlbumManager  implements UIObservable {
	private final static Logger LOGGER = LoggerFactory.getLogger(AlbumManager.class);
	
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

		} catch (DatabaseWrapperOperationException ex) {
			LOGGER.error("A problem occured while retrieving the list of albums from the database \n Stacktrace: " + ExceptionHelper.toString(ex));
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
