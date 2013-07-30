package collector.desktop.tests.albumviews;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import collector.desktop.controller.managers.AlbumViewManager;
import collector.desktop.controller.managers.ConnectionManager;
import collector.desktop.controller.managers.DatabaseIntegrityManager;
import collector.desktop.controller.managers.AlbumViewManager.AlbumView;
import collector.desktop.model.album.AlbumItemResultSet;
import collector.desktop.model.album.AlbumItemStore;
import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException;
import collector.desktop.tests.TestExecuter;

public class RunAlbumViewTests {
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
		ConnectionManager.closeConnection();
		TestExecuter.resetEverything();
	}

	@Test
	public void testExistingViewsForBooks() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestExecuter.PATH_TO_TEST_CBK);
			
			AlbumViewManager.initialize();
			
			List<AlbumView> albumViews = AlbumViewManager.getAlbumViews("Books");
			
			assertTrue("There should be one album view for the book album", albumViews.size() == 1);
			
			if (albumViews.get(0).getName().equals("Programming Languages")) {
				AlbumItemStore.reinitializeStore(new AlbumItemResultSet(
						ConnectionManager.getConnection(), albumViews.get(0).getSqlQuery()));
				
				assertTrue("There should be two books about programming languages", AlbumItemStore.getAllAlbumItems().size() == 2);
			} else {
				
			}
			
		} catch (DatabaseWrapperOperationException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testExistingViewsForDVDs() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestExecuter.PATH_TO_TEST_CBK);
			
			AlbumViewManager.initialize();
			
			List<AlbumView> albumViews = AlbumViewManager.getAlbumViews("DVDs");
			
			assertTrue("There should be two album views for the dvd album", albumViews.size() == 2);
			
			for (AlbumView albumView : albumViews) {
				if (albumView.getName().equals("Unwatched")) {
					AlbumItemStore.reinitializeStore(new AlbumItemResultSet(
							ConnectionManager.getConnection(), albumView.getSqlQuery()));
					
					assertTrue("Both views should show three items", AlbumItemStore.getAllAlbumItems().size() == 3);
				} else if (albumView.getName().equals("My favorite DVDs")) {
					AlbumItemStore.reinitializeStore(new AlbumItemResultSet(
							ConnectionManager.getConnection(), albumView.getSqlQuery()));
					
					assertTrue("Both views should show three items", AlbumItemStore.getAllAlbumItems().size() == 3);
				} else {
					fail("There should be no ither album view than ");
				}
			}
			
		} catch (DatabaseWrapperOperationException e) {
			fail(e.getMessage());
		}
	}
}
