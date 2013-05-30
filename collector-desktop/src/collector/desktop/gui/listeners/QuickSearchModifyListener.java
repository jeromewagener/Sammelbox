package collector.desktop.gui.listeners;

import java.util.Arrays;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Text;

import collector.desktop.Collector;
import collector.desktop.database.DatabaseWrapper;
import collector.desktop.gui.browser.BrowserContent;
import collector.desktop.gui.various.ComponentFactory;
import collector.desktop.internationalization.DictKeys;
import collector.desktop.internationalization.Translator;

public class QuickSearchModifyListener implements ModifyListener {
	@Override
	/** This method launches a quick-search for the entered keywords if an album has been selected*/
	public void modifyText(ModifyEvent e) {
		if (!Collector.hasSelectedAlbum()) {
			ComponentFactory.showErrorDialog(
					Collector.getShell(), 
					Translator.get(DictKeys.DIALOG_TITLE_NO_ALBUM_SELECTED), 
					Translator.get(DictKeys.DIALOG_CONTENT_NO_ALBUM_SELECTED));
			
			return;
		}
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