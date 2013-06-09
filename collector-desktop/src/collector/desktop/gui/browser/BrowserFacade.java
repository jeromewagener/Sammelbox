package collector.desktop.gui.browser;

import java.io.InputStream;

import collector.desktop.Collector;
import collector.desktop.album.AlbumItem;
import collector.desktop.database.AlbumItemResultSet;

public class BrowserFacade {
	public static void loadWelcomePage() { WelcomePageCreator.loadWelcomePage(); }
	public static void rerunLastQuery() { Utilities.performLastQuery(Collector.getAlbumItemSWTBrowser()); }
	public static void loadHelpPage() { Utilities.loadHelpPage(); }
	public static String getAnchorForAlbumItemId(long anchorId) { return Utilities.getAnchorForAlbumItemId(anchorId); }
	public static void jumpToAnchor(String anchor) { Utilities.jumpToAnchor(anchor); }
	public static void performBrowserQueryAndShow(String sqlQuery) { Utilities.performBrowserQueryAndShow(Collector.getAlbumItemSWTBrowser(), sqlQuery); }
	public static void showPicture(String pathToPicture, long albumItemId) { PictureViewCreator.showPicture(pathToPicture, albumItemId); }
	public static void setFutureJumpAnchor(String futureJumpAnchor) { Utilities.setFutureJumpAnchor(futureJumpAnchor); }
	public static String getFutureJumpAnchor() { return Utilities.getFutureJumpAnchor(); }
	public static void goBackToLastPage() { Utilities.goBackToLastPage(); }
	public static void addAdditionalAlbumItems() { Utilities.addAdditionalAlbumItems(); }
	public static void showResultSet(AlbumItemResultSet resultSet) { Utilities.showResultSet(Collector.getAlbumItemSWTBrowser(), resultSet); }
	public static void loadHtmlFromInputStream(InputStream fileInputStream) { Utilities.loadHtmlPage(Collector.getAlbumItemSWTBrowser(), fileInputStream); }
	public static void showCreateNewAlbumPage(AlbumItem albumItem) { FeedbackCreator.showCreateNewAlbumPage(Collector.getAlbumItemSWTBrowser(), albumItem); }
	public static void showCreateAlterAlbumPage(AlbumItem albumItem) { FeedbackCreator.showCreateAlterAlbumPage(Collector.getAlbumItemSWTBrowser(), albumItem); }
	public static void generatAlbumItemUpdatedPage(long albumItemId) { FeedbackCreator.generatAlbumItemUpdatedPage(albumItemId); }
	public static void generateAlbumItemAddedPage(long idOfNewAlbumItem) { FeedbackCreator.generateAlbumItemAddedPage(idOfNewAlbumItem); }
	public static void addModificationToAlterationList(String modification) { FeedbackCreator.addModificationToAlterationList(modification); }
	public static void clearAlterationList() { FeedbackCreator.clearAlterationList(); }
}