package collector.desktop.controller.listeners;

import java.util.Arrays;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import collector.desktop.controller.i18n.DictKeys;
import collector.desktop.controller.i18n.Translator;
import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException;
import collector.desktop.model.database.operations.DatabaseOperations;
import collector.desktop.view.ApplicationUI;
import collector.desktop.view.browser.BrowserFacade;
import collector.desktop.view.various.ComponentFactory;

public class QuickSearchModifyListener implements ModifyListener {
	private final static Logger LOGGER = LoggerFactory.getLogger(QuickSearchModifyListener.class);
	
	@Override
	/** This method launches a quick-search for the entered keywords if an album has been selected*/
	public void modifyText(ModifyEvent e) {
		if (!ApplicationUI.hasSelectedAlbum()) {
			ComponentFactory.showErrorDialog(
					ApplicationUI.getShell(), 
					Translator.get(DictKeys.DIALOG_TITLE_NO_ALBUM_SELECTED), 
					Translator.get(DictKeys.DIALOG_CONTENT_NO_ALBUM_SELECTED));
			
			return;
		}
		try {
			if (((Text) e.widget).getText().equals("")) {
				BrowserFacade.showResultSet(DatabaseOperations.executeQuickSearch(ApplicationUI.getSelectedAlbum(), null));
			} else {
				BrowserFacade.showResultSet(DatabaseOperations.executeQuickSearch(ApplicationUI.getSelectedAlbum(), 
						Arrays.asList(((Text) e.widget).getText().split(" "))));
			}
		} catch (DatabaseWrapperOperationException ex) {
			LOGGER.error("An error occured while performing a quick search on the following string '" + ((Text) e.widget).getText() + "'", ex);
		}
	}
}
