package collector.desktop.tests.albumviews;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import collector.desktop.model.album.AlbumItemResultSet;
import collector.desktop.model.album.AlbumItemStore;
import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException;
import collector.desktop.model.database.utilities.ConnectionManager;
import collector.desktop.model.database.utilities.DatabaseIntegrityManager;
import collector.desktop.tests.TestExecuter;
import collector.desktop.view.managers.AlbumViewManager;
import collector.desktop.view.managers.AlbumViewManager.AlbumView;

public class CreateAndRenameAlbumViewTests {
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		TestExecuter.resetEverything();
	}

	@Before
	public void setUp() {
		TestExecuter.resetEverything();
	}

	@After
	public void tearDown() throws Exception {
		TestExecuter.resetEverything();
	}

	@Test
	public void addAnotherViewToDVDs() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestExecuter.PATH_TO_TEST_CBK);
			
			AlbumViewManager.initialize();
			
			assertTrue("The first view should be: My favorite DVDs", 
					AlbumViewManager.getAlbumViews("DVDs").get(0).getName().equals("My favorite DVDs"));
			assertTrue("The second view should be: Unwatched", 
					AlbumViewManager.getAlbumViews("DVDs").get(1).getName().equals("Unwatched"));
			
			AlbumViewManager.addAlbumView("Before 2000", "DVDs", "SELECT * FROM DVDs WHERE [Year] <= 2000");
			
			assertTrue("After adding another view, there should be 3 views in total for the DVD album", 
					AlbumViewManager.getAlbumViews("DVDs").size() == 3);
			
			for (AlbumView albumView : AlbumViewManager.getAlbumViews("DVDs")) {
				if (albumView.getName().equals("Before 2000")) {
					AlbumItemStore.reinitializeStore(new AlbumItemResultSet(
							ConnectionManager.getConnection(), albumView.getSqlQuery()));
					
					assertTrue("There should be 5 movies from before the year 2000 (or equal)", 
							AlbumItemStore.getAllAlbumItems().size() == 5);
				}
			}
			
			assertTrue("Since views are immediately saved, a reinitialization should still yield the three views", 
					AlbumViewManager.getAlbumViews("DVDs").size() == 3);
			
			assertTrue("The first view should be: My favorite DVDs", 
					AlbumViewManager.getAlbumViews("DVDs").get(0).getName().equals("My favorite DVDs"));
			assertTrue("The second view should be: Unwatched", 
					AlbumViewManager.getAlbumViews("DVDs").get(1).getName().equals("Unwatched"));
			assertTrue("The third view should be: Before 2000", 
					AlbumViewManager.getAlbumViews("DVDs").get(2).getName().equals("Before 2000"));
			
		} catch (DatabaseWrapperOperationException ex) {
			fail(ex.getMessage());
		}
	}
	
	@Test
	public void testRemoveView() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestExecuter.PATH_TO_TEST_CBK);
			
			AlbumViewManager.initialize();
			
			assertTrue("The first view should be: My favorite DVDs", 
					AlbumViewManager.getAlbumViews("DVDs").get(0).getName().equals("My favorite DVDs"));
			assertTrue("The second view should be: Unwatched", 
					AlbumViewManager.getAlbumViews("DVDs").get(1).getName().equals("Unwatched"));

			AlbumViewManager.removeAlbumView("DVDs", "My favorite DVDs");
			
			assertTrue("The first view should be: Unwatched", 
					AlbumViewManager.getAlbumViews("DVDs").get(0).getName().equals("Unwatched"));
			
			AlbumViewManager.initialize();
			
			assertTrue("The first view should be: Unwatched", 
					AlbumViewManager.getAlbumViews("DVDs").get(0).getName().equals("Unwatched"));
			
		} catch (DatabaseWrapperOperationException ex) {
			fail(ex.getMessage());
		}
	}
	
	@Test
	public void testRemoveAllViewFromDVDs() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestExecuter.PATH_TO_TEST_CBK);
			
			AlbumViewManager.initialize();
						
			assertTrue("There should be two views for the DVD album", 
					AlbumViewManager.getAlbumViews("DVDs").size() == 2);
			
			AlbumViewManager.removeAlbumViewsFromAlbum("DVDs");
			
			assertTrue("There should no longer be views for the DVD album", 
					AlbumViewManager.getAlbumViews("DVDs").isEmpty());
			
		} catch (DatabaseWrapperOperationException ex) {
			fail(ex.getMessage());
		}
	}
}
