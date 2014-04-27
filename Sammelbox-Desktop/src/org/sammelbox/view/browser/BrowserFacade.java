/** -----------------------------------------------------------------
 *    Sammelbox: Collection Manager - A free and open-source collection manager for Windows & Linux
 *    Copyright (C) 2011 Jerome Wagener & Paul Bicheler
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ** ----------------------------------------------------------------- */

package org.sammelbox.view.browser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sammelbox.controller.filesystem.FileSystemAccessWrapper;
import org.sammelbox.controller.i18n.Language;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.model.album.AlbumItem;
import org.sammelbox.model.album.AlbumItemResultSet;
import org.sammelbox.view.ApplicationUI;
import org.sammelbox.view.UIConstants;
import org.sammelbox.view.browser.spreadsheet.SpreadsheetAbortFunction;
import org.sammelbox.view.browser.spreadsheet.SpreadsheetUpdateFunction;

public final class BrowserFacade {	
	private BrowserFacade() {
	}
	
	public static void showAlbum() { BrowserUtils.showAlbum(); }
	public static void loadWelcomePage() { WelcomePageCreator.loadWelcomePage(); }
	public static String getAnchorForAlbumItemId(long anchorId) { return BrowserUtils.getAnchorForAlbumItemId(anchorId); }
	public static void jumpToAnchor(String anchor) { BrowserUtils.jumpToAnchor(anchor); }
	public static void performBrowserQueryAndShow(String sqlQuery) { BrowserUtils.performBrowserQueryAndShow(sqlQuery); }
	public static void showImageViewer(String pathToPicture, long albumItemId) { PictureViewCreator.showPicture(albumItemId); }
	public static void resetFutureJumpAnchor() {BrowserUtils.setFutureJumpAnchor(UIConstants.NO_ANCHOR_DEFINED);}
	public static void setFutureJumpAnchor(String futureJumpAnchor) { BrowserUtils.setFutureJumpAnchor(futureJumpAnchor); }
	public static String getFutureJumpAnchor() { return BrowserUtils.getFutureJumpAnchor(); }
	public static void returnFromImageViewer() { BrowserUtils.returnFromImageViewer(); }
	public static void showResultSet(AlbumItemResultSet resultSet) { BrowserUtils.showResultSet(resultSet); }
	public static void showCreateNewAlbumPage(AlbumItem albumItem) { FeedbackCreator.showCreateNewAlbumPage(ApplicationUI.createOrRetrieveAlbumItemBrowser(), albumItem); }
	public static void showCreateAlterAlbumPage(AlbumItem albumItem) { FeedbackCreator.showCreateAlterAlbumPage(ApplicationUI.createOrRetrieveAlbumItemBrowser(), albumItem); }
	public static void generateAlbumItemUpdatedPage(long albumItemId) { FeedbackCreator.generatAlbumItemUpdatedPage(albumItemId); }
	public static void generateAlbumItemAddedPage(long idOfNewAlbumItem) { FeedbackCreator.generateAlbumItemAddedPage(idOfNewAlbumItem); }
	public static void addModificationToAlterationList(String modification) { FeedbackCreator.addModificationToAlterationList(modification); }
	public static void clearAlterationList() { FeedbackCreator.clearAlterationList(); }
	
	public static Map<String, String> getInitializedContentMap() { return BrowserUtils.getInitializedContentMap(); }
	
	public static void loadHtmlFile(String htmlFileFromHtmlPackage) {
		String htmlFileString = FileSystemAccessWrapper.readInputStreamIntoString(
				ApplicationUI.getShell().getClass().getClassLoader().getResourceAsStream("html/" + htmlFileFromHtmlPackage));
		
		ApplicationUI.createOrRetrieveAlbumItemBrowser().setText(htmlFileString);
	}
	
	public static void fillAndLoadTemplate(String htmlTemplatFilename, Map<String, String> templateContent) { 
		BrowserUtils.fillAndLoadTemplate(ApplicationUI.createOrRetrieveAlbumItemBrowser(), htmlTemplatFilename, templateContent);
	}
		
	public static void showAlbumDeletedPage(String deletedAlbum) { FeedbackCreator.showAlbumDeletedPage(deletedAlbum);	}
	public static void showRestoreInProgressPage() { FeedbackCreator.showRestoreInProgressPage(); }
	public static void showBackupRestoredPage() { FeedbackCreator.showBackupRestoredPage(); }
	public static void showBackupInProgressPage() { FeedbackCreator.showBackupInProgressPage(); }
	public static void showBackupFinishedPage() { FeedbackCreator.showBackupFinishedPage(); }
	public static void showSynchronizePage(String messageToShow) { FeedbackCreator.showSynchronizationPage(messageToShow); }
	public static void showSynchronizePageWithProgressBar(String messageToShow) { FeedbackCreator.showSynchronizationPageWithProgressBar(messageToShow); }
	
	public static void showAddItemsSpreadsheet() { 
		showEditItemsSpreadsheet(new ArrayList<Long>());
	}
	
	public static void showEditItemsSpreadsheet(List<Long> selectedItemIds) { 
		BrowserUtils.showEditableSpreadsheet(selectedItemIds);
		new SpreadsheetUpdateFunction(ApplicationUI.createOrRetrieveAlbumItemBrowser(), "spreadsheetUpdateFunction");
		new SpreadsheetAbortFunction(ApplicationUI.createOrRetrieveAlbumItemBrowser(), "spreadsheetAbortFunction");
	}
	
	public static void showHtmlPage(String pageName) {
		String htmlHelpString = FileSystemAccessWrapper.readInputStreamIntoString(
				ApplicationUI.getShell().getClass().getClassLoader().getResourceAsStream(
						Language.getHtmlPage(Translator.getUsedLanguage(), pageName)));
		
		ApplicationUI.createOrRetrieveAlbumItemBrowser().setText(htmlHelpString);
	}
}