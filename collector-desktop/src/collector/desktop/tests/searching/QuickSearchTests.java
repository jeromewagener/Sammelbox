package collector.desktop.tests.searching;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import collector.desktop.database.AlbumItemResultSet;
import collector.desktop.database.ConnectionManager;
import collector.desktop.database.DatabaseIntegrityManager;
import collector.desktop.database.DatabaseWrapper;
import collector.desktop.database.exceptions.DatabaseWrapperOperationException;
import collector.desktop.tests.CollectorTestExecuter;

public class QuickSearchTests {
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		CollectorTestExecuter.resetEverything();
	}

	@Before
	public void setUp() {
		CollectorTestExecuter.resetEverything();
	}

	@After
	public void tearDown() throws Exception {
		ConnectionManager.closeConnection();
	}

	@Test
	public void testQuickSearchActorInDVDs() {
		try {
			DatabaseIntegrityManager.restoreFromFile(CollectorTestExecuter.PATH_TO_TEST_CBK);
			ArrayList<String> quickSearchTerms = new ArrayList<String>();
			quickSearchTerms.add("Smith");
			AlbumItemResultSet searchResults = DatabaseWrapper.executeQuickSearch("DVDs", quickSearchTerms);

			assertTrue("Resultset should not be null", searchResults != null);

			int counter = 0;
			while (searchResults.moveToNext()) {
				counter++;
				for (int i=1; i<searchResults.getFieldCount(); i++) {
					if (searchResults.getFieldName(i).equals("Title")) {
						assertTrue("The only results should be either Independence Day or Wild Wild West", 
								searchResults.getFieldValue(i).equals("Independence Day") || 
								searchResults.getFieldValue(i).equals("Wild Wild West"));
					}
				}
			}

			assertTrue("Resultset should contain 2 items", counter == 2);
		} catch (DatabaseWrapperOperationException e) {
			fail("Failed due to internal database error");
		}
	}

	@Test
	public void testQuickSearchActorsInDVDs() {
		try {
			DatabaseIntegrityManager.restoreFromFile(CollectorTestExecuter.PATH_TO_TEST_CBK);
			ArrayList<String> quickSearchTerms = new ArrayList<String>();
			quickSearchTerms.add("Cooper");
			quickSearchTerms.add("Wilson");
			AlbumItemResultSet searchResults = DatabaseWrapper.executeQuickSearch("DVDs", quickSearchTerms);

			assertTrue("Resultset should not be null", searchResults != null);

			int counter = 0;
			while (searchResults.moveToNext()) {
				counter++;
				for (int i=1; i<searchResults.getFieldCount(); i++) {
					if (searchResults.getFieldName(i).equals("Title")) {
						assertTrue("The only results should be either Limitless or Marley & Me", 
								searchResults.getFieldValue(i).equals("Marley & Me") || 
								searchResults.getFieldValue(i).equals("Limitless"));
					}
				}
			}

			assertTrue("Resultset should contain 2 items", counter == 2);
		} catch (DatabaseWrapperOperationException e) {
			fail("Failed due to internal database error");
		}
	}

	@Test
	public void testQuickSearchTitleInDVDs() {
		try {
			DatabaseIntegrityManager.restoreFromFile(CollectorTestExecuter.PATH_TO_TEST_CBK);
			ArrayList<String> quickSearchTerms = new ArrayList<String>();
			quickSearchTerms.add("Me");
			AlbumItemResultSet searchResults = DatabaseWrapper.executeQuickSearch("DVDs", quickSearchTerms);

			assertTrue("Resultset should not be null", searchResults != null);

			int counter = 0;
			while (searchResults.moveToNext()) {
				counter++;
				for (int i=1; i<searchResults.getFieldCount(); i++) {
					if (searchResults.getFieldName(i).equals("Title")) {
						assertTrue("The only result should be Marley & Me", 
								searchResults.getFieldValue(i).equals("Marley & Me"));
					}
				}
			}

			assertTrue("Resultset should contain 1 items", counter == 1);
		} catch (DatabaseWrapperOperationException e) {
			fail("Failed due to internal database error");
		}
	}
}