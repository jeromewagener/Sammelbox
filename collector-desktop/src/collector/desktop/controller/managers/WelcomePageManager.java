package collector.desktop.controller.managers;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import collector.desktop.controller.filesystem.FileSystemAccessWrapper;
import collector.desktop.controller.i18n.DictKeys;
import collector.desktop.controller.i18n.Translator;
import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException;
import collector.desktop.model.database.operations.DatabaseOperations;

public class WelcomePageManager {
	private final static Logger LOGGER = LoggerFactory.getLogger(WelcomePageManager.class);
	private static Map<String, Integer> albumAndViewsToClicks;
	private static Map<String, Long> albumToLastModified;
	
	public static void initializeFromWelcomeFile() {
		albumAndViewsToClicks = FileSystemAccessWrapper.getAlbumAndViewsToClicks();
		albumToLastModified = FileSystemAccessWrapper.getAlbumToLastModified();
	}

	public static void updateLastModifiedWithCurrentDate(String albumName) {
		albumToLastModified.put(albumName, System.currentTimeMillis());

		storeWelcomePageManagerInformation();
	}

	public static void increaseClickCountForAlbumOrView(String albumOrViewName) {
		Integer count = albumAndViewsToClicks.get(albumOrViewName);

		if (count != null) {
			albumAndViewsToClicks.put(albumOrViewName, count + 1);
		} else {
			albumAndViewsToClicks.put(albumOrViewName, 1);
		}

		storeWelcomePageManagerInformation();
	}

	private static void storeWelcomePageManagerInformation() {
		FileSystemAccessWrapper.storeWelcomePageManagerInformation(albumAndViewsToClicks, albumToLastModified);
	}

	public static Map<String, Integer> getAlbumAndViewsSortedByClicks() {
		return sortByValue(albumAndViewsToClicks);
	}

	public static String getLastModifiedDate(String albumName) {
		for (String key : albumToLastModified.keySet()) {
			if (key.equals(albumName)) {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
				
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

	private static <K, V extends Comparable<? super V>> Map<K, V> sortByValue( Map<K, V> map ) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>( map.entrySet() );
		Collections.sort( list, new Comparator<Map.Entry<K, V>>() {
			public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 ) {
				if ((o1.getValue()).compareTo( o2.getValue()) < 0 ) {
					return 1;
				} else {
					return -1;
				}
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put( entry.getKey(), entry.getValue() );
		}

		return result;
	}
}
