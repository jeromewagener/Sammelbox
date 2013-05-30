package collector.desktop.gui.managers;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import collector.desktop.database.DatabaseWrapper;
import collector.desktop.filesystem.FileSystemAccessWrapper;
import collector.desktop.internationalization.DictKeys;
import collector.desktop.internationalization.Translator;

public class WelcomePageManager {
	private static WelcomePageManager instance;
	private Map<String, Integer> albumAndViewsToClicks;
	private Map<String, Long> albumToLastModified;

	private WelcomePageManager() {
		initializeWelcomePageManager();
	}

	private void initializeWelcomePageManager() {
		albumAndViewsToClicks = FileSystemAccessWrapper.getAlbumAndViewsToClicks();
		albumToLastModified = FileSystemAccessWrapper.getAlbumToLastModified();
	}

	public static WelcomePageManager getInstance() {
		if (instance == null) {
			instance = new WelcomePageManager();
		}

		return WelcomePageManager.instance;
	}

	public void updateLastModifiedWithCurrentDate(String albumName) {
		albumToLastModified.put(albumName, System.currentTimeMillis());

		storeWelcomePageManagerInformation();
	}

	public void increaseClickCountForAlbumOrView(String albumOrViewName) {
		Integer count = albumAndViewsToClicks.get(albumOrViewName);

		if (count != null) {
			albumAndViewsToClicks.put(albumOrViewName, count + 1);
		} else {
			albumAndViewsToClicks.put(albumOrViewName, 1);
		}

		storeWelcomePageManagerInformation();
	}

	private void storeWelcomePageManagerInformation() {
		FileSystemAccessWrapper.storeWelcomePageManagerInformation(albumAndViewsToClicks, albumToLastModified);
	}

	public Map<String, Integer> getAlbumAndViewsSortedByClicks() {
		return sortByValue(albumAndViewsToClicks);
	}

	public String getLastModifiedDate(String albumName) {
		for (String key : albumToLastModified.keySet()) {
			if (key.equals(albumName)) {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
				
				return simpleDateFormat.format(new Date(albumToLastModified.get(key)));
			}
		}

		return Translator.get(DictKeys.BROWSER_NEVER);
	}

	public Long getNumberOfItemsInAlbum(String albumName) {
		return DatabaseWrapper.getNumberOfItemsInAlbum(albumName);
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue( Map<K, V> map ) {
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
