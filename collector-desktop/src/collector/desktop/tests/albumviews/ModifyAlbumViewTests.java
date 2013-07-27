package collector.desktop.tests.albumviews;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException;
import collector.desktop.model.database.utilities.DatabaseIntegrityManager;
import collector.desktop.tests.TestExecuter;
import collector.desktop.view.managers.AlbumViewManager;

public class ModifyAlbumViewTests {
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
	public void testMoveViews() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestExecuter.PATH_TO_TEST_CBK);
			
			AlbumViewManager.initialize();
			
			AlbumViewManager.addAlbumView("Before 2000", "DVDs", "SELECT * FROM DVDs WHERE [Year] <= 2000");
			
			assertTrue("The first view should be: My favorite DVDs", 
					AlbumViewManager.getAlbumViews("DVDs").get(0).getName().equals("My favorite DVDs"));
			assertTrue("The second view should be: Unwatched", 
					AlbumViewManager.getAlbumViews("DVDs").get(1).getName().equals("Unwatched"));
			assertTrue("The third view should be: Before 2000", 
					AlbumViewManager.getAlbumViews("DVDs").get(2).getName().equals("Before 2000"));
			
			AlbumViewManager.moveOneUp("DVDs", 1);
			
			assertTrue("The first view should be: Unwatched", 
					AlbumViewManager.getAlbumViews("DVDs").get(0).getName().equals("Unwatched"));
			assertTrue("The second view should be: My favorite DVDs", 
					AlbumViewManager.getAlbumViews("DVDs").get(1).getName().equals("My favorite DVDs"));
			assertTrue("The third view should be: Before 2000", 
					AlbumViewManager.getAlbumViews("DVDs").get(2).getName().equals("Before 2000"));
			
			AlbumViewManager.moveOneUp("DVDs", 2);
			
			assertTrue("The first view should be: Unwatched", 
					AlbumViewManager.getAlbumViews("DVDs").get(0).getName().equals("Unwatched"));
			assertTrue("The second view should be: Before 2000", 
					AlbumViewManager.getAlbumViews("DVDs").get(1).getName().equals("Before 2000"));
			assertTrue("The third view should be: My favorite DVDs", 
					AlbumViewManager.getAlbumViews("DVDs").get(2).getName().equals("My favorite DVDs"));
			
			AlbumViewManager.moveOneDown("DVDs", 0);
			
			assertTrue("The first view should be: Before 2000", 
					AlbumViewManager.getAlbumViews("DVDs").get(0).getName().equals("Before 2000"));
			assertTrue("The second view should be: Unwatched", 
					AlbumViewManager.getAlbumViews("DVDs").get(1).getName().equals("Unwatched"));
			assertTrue("The third view should be: My favorite DVDs", 
					AlbumViewManager.getAlbumViews("DVDs").get(2).getName().equals("My favorite DVDs"));
			
			AlbumViewManager.moveToFront("DVDs", 1);
			
			assertTrue("The first view should be: Unwatched", 
					AlbumViewManager.getAlbumViews("DVDs").get(0).getName().equals("Unwatched"));
			assertTrue("The second view should be: Before 2000", 
					AlbumViewManager.getAlbumViews("DVDs").get(1).getName().equals("Before 2000"));
			assertTrue("The third view should be: My favorite DVDs", 
					AlbumViewManager.getAlbumViews("DVDs").get(2).getName().equals("My favorite DVDs"));
			
			AlbumViewManager.moveToBottom("DVDs", 0);
			
			assertTrue("The second view should be: Before 2000", 
					AlbumViewManager.getAlbumViews("DVDs").get(0).getName().equals("Before 2000"));
			assertTrue("The third view should be: My favorite DVDs", 
					AlbumViewManager.getAlbumViews("DVDs").get(1).getName().equals("My favorite DVDs"));
			assertTrue("The first view should be: Unwatched", 
					AlbumViewManager.getAlbumViews("DVDs").get(2).getName().equals("Unwatched"));
			
		} catch (DatabaseWrapperOperationException ex) {
			fail(ex.getMessage());
		}
	}
}
