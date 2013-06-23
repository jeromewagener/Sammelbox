package collector.desktop.gui.listeners;

import java.util.Arrays;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import collector.desktop.Collector;
import collector.desktop.database.DatabaseWrapper;
import collector.desktop.database.exceptions.DatabaseWrapperOperationException;
import collector.desktop.database.exceptions.ExceptionHelper;
import collector.desktop.gui.browser.BrowserFacade;
import collector.desktop.gui.various.ComponentFactory;
import collector.desktop.internationalization.DictKeys;
import collector.desktop.internationalization.Translator;

public class QuickSearchModifyListener implements ModifyListener {
	private final static Logger LOGGER = LoggerFactory.getLogger(QuickSearchModifyListener.class);
	
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
		try {
			if (((Text) e.widget).getText().equals("")) {
				BrowserFacade.showResultSet(DatabaseWrapper.executeQuickSearch(Collector.getSelectedAlbum(), null));
			} else {
				BrowserFacade.showResultSet(DatabaseWrapper.executeQuickSearch(Collector.getSelectedAlbum(), 
						Arrays.asList(((Text) e.widget).getText().split(" "))));
			}
		} catch (DatabaseWrapperOperationException ex) {
			LOGGER.error("An error occured while performing a quick search on the following string '" + ((Text) e.widget).getText() + "'" + 
					" \n Stacktrace:" + ExceptionHelper.toString(ex));
		}
	}
}
