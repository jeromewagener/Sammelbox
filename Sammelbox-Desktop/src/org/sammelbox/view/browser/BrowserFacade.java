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

import org.sammelbox.controller.filesystem.FileSystemAccessWrapper;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.model.album.AlbumItem;
import org.sammelbox.model.album.AlbumItemResultSet;
import org.sammelbox.view.ApplicationUI;
import org.sammelbox.view.UIConstants;

public final class BrowserFacade {
	private BrowserFacade() {
	}
	
	public static void showAlbum() { Utilities.showAlbum(ApplicationUI.getAlbumItemBrowser()); }
	public static void loadWelcomePage() { WelcomePageCreator.loadWelcomePage(); }
	public static void loadHelpPage() { Utilities.loadHelpPage(); }
	public static String getAnchorForAlbumItemId(long anchorId) { return Utilities.getAnchorForAlbumItemId(anchorId); }
	public static void jumpToAnchor(String anchor) { Utilities.jumpToAnchor(anchor); }
	public static void performBrowserQueryAndShow(String sqlQuery) { Utilities.performBrowserQueryAndShow(ApplicationUI.getAlbumItemBrowser(), sqlQuery); }
	public static void showImageViewer(String pathToPicture, long albumItemId) { PictureViewCreator.showPicture(albumItemId); }
	public static void resetFutureJumpAnchor() {Utilities.setFutureJumpAnchor(UIConstants.NO_ANCHOR_DEFINED);}
	public static void setFutureJumpAnchor(String futureJumpAnchor) { Utilities.setFutureJumpAnchor(futureJumpAnchor); }
	public static String getFutureJumpAnchor() { return Utilities.getFutureJumpAnchor(); }
	public static void returnFromImageViewer() { Utilities.returnFromImageViewer(); }
	public static void addAdditionalAlbumItems() { Utilities.addAdditionalAlbumItems(); }
	public static void showResultSet(AlbumItemResultSet resultSet) { Utilities.showResultSet(ApplicationUI.getAlbumItemBrowser(), resultSet); }
	public static void loadHtmlFromTranslatedFile(String filename) { 
		String htmlFile = FileSystemAccessWrapper.readInputStreamIntoString(ApplicationUI.getShell().getClass().getClassLoader().getResourceAsStream("textfiles/" + Translator.getUsedLanguage() + "/" + filename));
		Utilities.loadHtml(ApplicationUI.getAlbumItemBrowser(), htmlFile); }
	public static void showCreateNewAlbumPage(AlbumItem albumItem) { FeedbackCreator.showCreateNewAlbumPage(ApplicationUI.getAlbumItemBrowser(), albumItem); }
	public static void showCreateAlterAlbumPage(AlbumItem albumItem) { FeedbackCreator.showCreateAlterAlbumPage(ApplicationUI.getAlbumItemBrowser(), albumItem); }
	public static void generateAlbumItemUpdatedPage(long albumItemId) { FeedbackCreator.generatAlbumItemUpdatedPage(albumItemId); }
	public static void generateAlbumItemAddedPage(long idOfNewAlbumItem) { FeedbackCreator.generateAlbumItemAddedPage(idOfNewAlbumItem); }
	public static void addModificationToAlterationList(String modification) { FeedbackCreator.addModificationToAlterationList(modification); }
	public static void clearAlterationList() { FeedbackCreator.clearAlterationList(); }
	public static void showAlbumDeletedPage(String deletedAlbum) { Utilities.loadHtml(ApplicationUI.getAlbumItemBrowser(), FeedbackCreator.generateAlbumDeletedPage(deletedAlbum));	}
	public static void showAlbumRestoredPage() { Utilities.loadHtml(ApplicationUI.getAlbumItemBrowser(), FeedbackCreator.generateAlbumsRestoredPage()); }
	public static void showBackupInProgressPage() { Utilities.loadHtml(ApplicationUI.getAlbumItemBrowser(), FeedbackCreator.generateBackupInProgressPage()); }
	public static void showBackupFinishedPage() { Utilities.loadHtml(ApplicationUI.getAlbumItemBrowser(), FeedbackCreator.generateBackupFinishedPage()); }
	public static void showSynchronizePage() { Utilities.loadHtml(ApplicationUI.getAlbumItemBrowser(), FeedbackCreator.generateSynchronizationPage()); }
}