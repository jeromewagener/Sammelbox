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
import collector.desktop.database.DatabaseWrapper;
import collector.desktop.database.QueryBuilder;
import collector.desktop.database.QueryBuilder.QueryComponent;
import collector.desktop.database.QueryBuilder.QueryOperator;
import collector.desktop.database.exceptions.FailedDatabaseWrapperOperationException;
import collector.desktop.filesystem.FileSystemAccessWrapper;
import collector.desktop.tests.CollectorTestExecuter;

public class AdvancedSearchTests {
	private static void resetEverything() {
		try {			
			DatabaseWrapper.closeConnection();

			FileSystemAccessWrapper.removeCollectorHome();

			Class.forName("org.sqlite.JDBC");

			FileSystemAccessWrapper.updateCollectorFileStructure();			

			DatabaseWrapper.openConnection();

			FileSystemAccessWrapper.updateAlbumFileStructure(DatabaseWrapper.getConnection());
		} 
		catch (Exception e) {
			e.printStackTrace();
			fail("Could not open database!");
		}
	}


	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		resetEverything();
	}

	@Before
	public void setUp() {
		resetEverything();
	}

	@After
	public void tearDown() throws Exception {
		DatabaseWrapper.closeConnection();
		resetEverything();
	}

	@Test
	public void testSearchForTitleInDVDsUsingSQL() {
		try {
			// Use default test sample
			DatabaseWrapper.restoreFromFile(CollectorTestExecuter.PATH_TO_TEST_CBK);

			AlbumItemResultSet searchResults = DatabaseWrapper.executeSQLQuery("SELECT * FROM DVDs WHERE Title = 'The Simpsons Movie'");

			assertTrue("Resultset should not be null", searchResults != null);

			int counter = 0;
			while (searchResults.moveToNext()) {
				counter++;

				for (int i=1; i<searchResults.getFieldCount(); i++) {
					if (searchResults.getFieldName(i).equals("Title")) {
						assertTrue("The only result should be The Simpsons Movie", 
								searchResults.getFieldValue(i).equals("The Simpsons Movie"));
					}
				}
			}

			assertTrue("Resultset should contain 1 item", counter == 1);
		} catch (FailedDatabaseWrapperOperationException e) {
			fail("testSearchForTitleInDVDsUsingSQL failed");
		}
	}

	@Test
	public void testSearchForTitleAndActorsInDVDsUsingSQL() {
		try {
			// Use default test sample
			DatabaseWrapper.restoreFromFile(CollectorTestExecuter.PATH_TO_TEST_CBK);

			AlbumItemResultSet searchResults = DatabaseWrapper.executeSQLQuery(
					"SELECT * FROM DVDs WHERE Title = 'The Simpsons Movie' and Actors = 'The Simpsons'");

			assertTrue("Resultset should not be null", searchResults != null);

			int counter = 0;
			while (searchResults.moveToNext()) {
				counter++;

				for (int i=1; i<searchResults.getFieldCount(); i++) {
					if (searchResults.getFieldName(i).equals("Title")) {
						assertTrue("The only result should be The Simpsons Movie", 
								searchResults.getFieldValue(i).equals("The Simpsons Movie"));
					}
				}
			}

			assertTrue("Resultset should contain 1 item", counter == 1);
		} catch (FailedDatabaseWrapperOperationException e) {
			fail("testSearchForTitleAndActorsInDVDsUsingSQL failed");
		}
	}	

	@Test
	public void testSearchForTitleInDVDsUsingQueryBuilder() {
		try {
			// Use default test sample
			DatabaseWrapper.restoreFromFile(CollectorTestExecuter.PATH_TO_TEST_CBK);

			ArrayList<QueryComponent> queryComponents = new ArrayList<QueryComponent>();
			queryComponents.add(QueryBuilder.getQueryComponent("Title", QueryOperator.equals, "Short Circuit 2"));

			AlbumItemResultSet searchResults = DatabaseWrapper.executeSQLQuery(
					QueryBuilder.buildQuery(queryComponents, true, "DVDs"));

			assertTrue("Resultset should not be null", searchResults != null);

			int counter = 0;
			while (searchResults.moveToNext()) {
				counter++;

				for (int i=1; i<searchResults.getFieldCount(); i++) {
					if (searchResults.getFieldName(i).equals("Title")) {
						assertTrue("The only result should be Short Circuit 2", 
								searchResults.getFieldValue(i).equals("Short Circuit 2"));
					}
				}
			}

			assertTrue("Resultset should contain 1 item", counter == 1);
		} catch (FailedDatabaseWrapperOperationException e) {
			fail("testSearchForTitleInDVDsUsingQueryBuilder failed");
		}
	}

	@Test
	public void testSearchForTitleAndAuthorInDVDsUsingQueryBuilder() {
		try {
			// Use default test sample
			DatabaseWrapper.restoreFromFile(CollectorTestExecuter.PATH_TO_TEST_CBK);

			ArrayList<QueryComponent> queryComponents = new ArrayList<QueryComponent>();
			queryComponents.add(QueryBuilder.getQueryComponent("Title", QueryOperator.equals, "Short Circuit 2"));
			queryComponents.add(QueryBuilder.getQueryComponent("Actors", QueryOperator.equals, "Cynthia Gibb, Fisher Stevens"));

			AlbumItemResultSet searchResults = DatabaseWrapper.executeSQLQuery(
					QueryBuilder.buildQuery(queryComponents, true, "DVDs"));

			assertTrue("Resultset should not be null", searchResults != null);

			int counter = 0;
			while (searchResults.moveToNext()) {
				counter++;

				for (int i=1; i<searchResults.getFieldCount(); i++) {
					if (searchResults.getFieldName(i).equals("Title")) {
						assertTrue("The only result should be Short Circuit 2", 
								searchResults.getFieldValue(i).equals("Short Circuit 2"));
					}
				}
			}

			assertTrue("Resultset should contain 1 item", counter == 1);
		} catch (FailedDatabaseWrapperOperationException e) {
			fail("testSearchForTitleAndAuthorInDVDsUsingQueryBuilder failed");
		}
	}

	@Test
	public void testSearchForTitlesInDVDsUsingQueryBuilder() {
		try {
			// Use default test sample
			DatabaseWrapper.restoreFromFile(CollectorTestExecuter.PATH_TO_TEST_CBK);

			ArrayList<QueryComponent> queryComponents = new ArrayList<QueryComponent>();
			queryComponents.add(QueryBuilder.getQueryComponent("Title", QueryOperator.equals, "Short Circuit 2"));
			queryComponents.add(QueryBuilder.getQueryComponent("Title", QueryOperator.equals, "One Flew Over The Cuckoo's Nest"));
			queryComponents.add(QueryBuilder.getQueryComponent("Title", QueryOperator.equals, "RED"));

			AlbumItemResultSet searchResults = DatabaseWrapper.executeSQLQuery(QueryBuilder.buildQuery(queryComponents, false, "DVDs"));

			assertTrue("Resultset should not be null", searchResults != null);

			int counter = 0;
			while (searchResults.moveToNext()) {
				counter++;

				for (int i=1; i<searchResults.getFieldCount(); i++) {
					if (searchResults.getFieldName(i).equals("Title")) {
						assertTrue("The only results should be either Short Circuit 2, One Flew Over The Cuckoo's Nest or RED", 
								searchResults.getFieldValue(i).equals("Short Circuit 2") || 
								searchResults.getFieldValue(i).equals("One Flew Over The Cuckoo's Nest") ||
								searchResults.getFieldValue(i).equals("RED"));
					}
				}
			}

			assertTrue("Resultset should contain 3 item", counter == 3);
		} catch (FailedDatabaseWrapperOperationException e) {
			fail("testSearchForTitlesInDVDsUsingQueryBuilder failed");
		}
	}

	@Test
	public void testSearchByLikeForAuthorInBooksUsingQueryBuilder() {
		try {
			// Use default test sample
			DatabaseWrapper.restoreFromFile(CollectorTestExecuter.PATH_TO_TEST_CBK);

			ArrayList<QueryComponent> queryComponents = new ArrayList<QueryComponent>();
			queryComponents.add(QueryBuilder.getQueryComponent("Author", QueryOperator.like, "Helm"));

			AlbumItemResultSet searchResults = DatabaseWrapper.executeSQLQuery(
					QueryBuilder.buildQuery(queryComponents, true, "Books"));

			assertTrue("Resultset should not be null", searchResults != null);

			int counter = 0;
			while (searchResults.moveToNext()) {
				counter++;

				for (int i=1; i<searchResults.getFieldCount(); i++) {
					if (searchResults.getFieldName(i).equals("Book Title")) {
						assertTrue("The only result should be Design Patterns", 
								searchResults.getFieldValue(i).equals("Design Patterns"));
					}
				}
			}

			assertTrue("Resultset should contain 1 item", counter == 1);
		} catch (FailedDatabaseWrapperOperationException e) {
			fail("testSearchByLikeForAuthorInBooksUsingQueryBuilder failed");
		}

	}

	@Test
	public void testSearchByLikeForTitlesInBooksUsingQueryBuilder() {
		try {
			// Use default test sample
			DatabaseWrapper.restoreFromFile(CollectorTestExecuter.PATH_TO_TEST_CBK);

			ArrayList<QueryComponent> queryComponents = new ArrayList<QueryComponent>();
			queryComponents.add(QueryBuilder.getQueryComponent("Book Title", QueryOperator.like, "Code"));
			queryComponents.add(QueryBuilder.getQueryComponent("Book Title", QueryOperator.like, "Design"));
			queryComponents.add(QueryBuilder.getQueryComponent("Book Title", QueryOperator.like, "Programmer"));

			AlbumItemResultSet searchResults = DatabaseWrapper.executeSQLQuery(
					QueryBuilder.buildQuery(queryComponents, false, "Books"));

			assertTrue("Resultset should not be null", searchResults != null);

			int counter = 0;
			while (searchResults.moveToNext()) {
				counter++;

				for (int i=1; i<searchResults.getFieldCount(); i++) {
					if (searchResults.getFieldName(i).equals("Book Title")) {
						assertTrue("The only results should be either Design Patterns, Clean Code or 97 Things Every Programmer Should Know", 
								searchResults.getFieldValue(i).equals("Design Patterns") || 
								searchResults.getFieldValue(i).equals("Clean Code") ||
								searchResults.getFieldValue(i).equals("97 Things Every Programmer Should Know"));
					}
				}
			}

			assertTrue("Resultset should contain 3 item", counter == 3);
		} catch (FailedDatabaseWrapperOperationException e) {
			fail("testSearchByLikeForTitlesInBooksUsingQueryBuilder failed");
		}

	}

	@Test
	public void testSearchForHighPriceBooksUsingBiggerThan() {
		try {
			// Use default test sample
			DatabaseWrapper.restoreFromFile(CollectorTestExecuter.PATH_TO_TEST_CBK);

			ArrayList<QueryComponent> queryComponents = new ArrayList<QueryComponent>();
			queryComponents.add(QueryBuilder.getQueryComponent("Price", QueryOperator.biggerThan, "30.0"));

			AlbumItemResultSet searchResults = DatabaseWrapper.executeSQLQuery(
					QueryBuilder.buildQuery(queryComponents, true, "Books"));

			assertTrue("Resultset should not be null", searchResults != null);

			int counter = 0;
			while (searchResults.moveToNext()) {
				counter++;
			}

			assertTrue("Resultset should contain 5 items", counter == 5);
		} catch (FailedDatabaseWrapperOperationException e) {
			fail("testSearchForHighPriceBooksUsingBiggerThan failed");
		}
	}

	@Test
	public void testSearchForHighPriceBooksUsingBiggerThanOrEqual() {
		try {
			// Use default test sample
			DatabaseWrapper.restoreFromFile(CollectorTestExecuter.PATH_TO_TEST_CBK);

			ArrayList<QueryComponent> queryComponents = new ArrayList<QueryComponent>();
			queryComponents.add(QueryBuilder.getQueryComponent("Price", QueryOperator.biggerOrEqualThan, "30.0"));

			AlbumItemResultSet searchResults = DatabaseWrapper.executeSQLQuery(
					QueryBuilder.buildQuery(queryComponents, true, "Books"));

			assertTrue("Resultset should not be null", searchResults != null);

			int counter = 0;
			while (searchResults.moveToNext()) {
				counter++;
			}

			assertTrue("Resultset should contain 7 items", counter == 7);
		} catch (FailedDatabaseWrapperOperationException e) {
			fail("testSearchForHighPriceBooksUsingBiggerThanOrEqual failed");
		}
	}

	@Test
	public void testSearchArtistInEmptyMusicAlbumUsingQueryBuilder() {
		try {
			// Use default test sample
			DatabaseWrapper.restoreFromFile(CollectorTestExecuter.PATH_TO_TEST_CBK);

			ArrayList<QueryComponent> queryComponents = new ArrayList<QueryComponent>();
			queryComponents.add(QueryBuilder.getQueryComponent("Artist", QueryOperator.equals, "Rick Astley"));

			AlbumItemResultSet searchResults = DatabaseWrapper.executeSQLQuery(
					QueryBuilder.buildQuery(queryComponents, true, "Music CDs"));

			assertTrue("Resultset should not be null", searchResults != null);

			int counter = 0;
			while (searchResults.moveToNext()) {
				counter++;
			}

			assertTrue("Resultset should contain 0 item", counter == 0);
		} catch (FailedDatabaseWrapperOperationException e) {
			fail("testSearchArtistInEmptyMusicAlbumUsingQueryBuilder failed");
		}
	}


}
