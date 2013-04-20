package collector.desktop.gui;

import java.util.Arrays;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Text;

import collector.desktop.Collector;
import collector.desktop.database.DatabaseWrapper;

public class QuickSearchModifyListener implements ModifyListener {
	@Override
	/** This method launches a quick-search for the entered keywords */
	public void modifyText(ModifyEvent e) {
		if (((Text) e.widget).getText().equals("")) {
			BrowserContent.showResultSet(Collector.getAlbumItemSWTBrowser(), 
					DatabaseWrapper.executeQuickSearch(
							Collector.getSelectedAlbum(), 
							null));
		} else {
			BrowserContent.showResultSet(Collector.getAlbumItemSWTBrowser(), 
					DatabaseWrapper.executeQuickSearch(
							Collector.getSelectedAlbum(), 
							Arrays.asList(((Text) e.widget).getText().split(" "))));
		}
	}
}
