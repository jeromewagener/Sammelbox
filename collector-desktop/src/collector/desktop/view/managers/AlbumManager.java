package collector.desktop.view.managers;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import collector.desktop.controller.events.EventObservable;
import collector.desktop.controller.events.SammelboxEvent;
import collector.desktop.controller.filesystem.FileSystemAccessWrapper;
import collector.desktop.model.database.DatabaseWrapper;
import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException;
import collector.desktop.model.database.exceptions.ExceptionHelper;

public class AlbumManager {
	private final static Logger LOGGER = LoggerFactory.getLogger(AlbumManager.class);
	private static List<String> albums = new LinkedList<String>();
	
	private AlbumManager() {
		initialize();
	}
	
	public static void mergeDatabaseAndXmlAlbums() {
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
	
	public static void initialize() {
		albums = FileSystemAccessWrapper.loadAlbums();
		
		mergeDatabaseAndXmlAlbums();
		
		FileSystemAccessWrapper.storeAlbums(albums);
	}
	
	public static List<String> getAlbums() {
		albums = FileSystemAccessWrapper.loadAlbums();
		
		mergeDatabaseAndXmlAlbums();
		
		return albums;
	}
	
	private static void storeAlbums() {
		FileSystemAccessWrapper.storeAlbums(albums);
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
