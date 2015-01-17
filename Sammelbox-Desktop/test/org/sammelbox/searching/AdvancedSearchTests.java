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

package org.sammelbox.searching;

import org.junit.*;
import org.sammelbox.TestRunner;
import org.sammelbox.controller.managers.DatabaseIntegrityManager;
import org.sammelbox.model.album.AlbumItemResultSet;
import org.sammelbox.model.database.QueryBuilder;
import org.sammelbox.model.database.QueryBuilderException;
import org.sammelbox.model.database.QueryComponent;
import org.sammelbox.model.database.QueryOperator;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseOperations;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AdvancedSearchTests {
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		TestRunner.resetTestHome();
	}

	@Before
	public void setUp() {
		TestRunner.resetTestHome();
	}

	@After
	public void tearDown() throws Exception {
		TestRunner.resetTestHome();
	}

	@Test
	public void testSearchForTitleInDVDsUsingSQL() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestRunner.PATH_TO_TEST_CBK);
			AlbumItemResultSet searchResults = DatabaseOperations.executeSQLQuery("SELECT * FROM DVDs WHERE Title = 'The Simpsons Movie'");
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
		} catch (DatabaseWrapperOperationException e) {
			fail("testSearchForTitleInDVDsUsingSQL failed");
		}
	}

	@Test
	public void testSearchForTitleAndActorsInDVDsUsingSQL() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestRunner.PATH_TO_TEST_CBK);
			AlbumItemResultSet searchResults = DatabaseOperations.executeSQLQuery(
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
		} catch (DatabaseWrapperOperationException e) {
			fail("testSearchForTitleAndActorsInDVDsUsingSQL failed");
		}
	}	

	@Test
	public void testSearchForTitleInDVDsUsingQueryBuilder() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestRunner.PATH_TO_TEST_CBK);
			ArrayList<QueryComponent> queryComponents = new ArrayList<QueryComponent>();
			queryComponents.add(QueryBuilder.getQueryComponent("Title", QueryOperator.EQUALS, "Short Circuit 2"));
			AlbumItemResultSet searchResults = DatabaseOperations.executeSQLQuery(
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
		} catch (DatabaseWrapperOperationException | QueryBuilderException e) {
			fail("testSearchForTitleInDVDsUsingQueryBuilder failed");
		}
	}

	@Test
	public void testSearchForTitleAndAuthorInDVDsUsingQueryBuilder() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestRunner.PATH_TO_TEST_CBK);

			ArrayList<QueryComponent> queryComponents = new ArrayList<QueryComponent>();
			queryComponents.add(QueryBuilder.getQueryComponent("Title", QueryOperator.EQUALS, "Short Circuit 2"));
			queryComponents.add(QueryBuilder.getQueryComponent("Actors", QueryOperator.EQUALS, "Cynthia Gibb, Fisher Stevens"));

			AlbumItemResultSet searchResults = DatabaseOperations.executeSQLQuery(
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
		} catch (DatabaseWrapperOperationException | QueryBuilderException e) {
			fail("testSearchForTitleAndAuthorInDVDsUsingQueryBuilder failed");
		}
	}

	@Test
	public void testSearchForTitlesInDVDsUsingQueryBuilder() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestRunner.PATH_TO_TEST_CBK);
			ArrayList<QueryComponent> queryComponents = new ArrayList<QueryComponent>();
			queryComponents.add(QueryBuilder.getQueryComponent("Title", QueryOperator.EQUALS, "Short Circuit 2"));
			queryComponents.add(QueryBuilder.getQueryComponent("Title", QueryOperator.EQUALS, "One Flew Over The Cuckoo's Nest"));
			queryComponents.add(QueryBuilder.getQueryComponent("Title", QueryOperator.EQUALS, "RED"));
			AlbumItemResultSet searchResults = DatabaseOperations.executeSQLQuery(
					QueryBuilder.buildQuery(queryComponents, false, "DVDs"));

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
		} catch (DatabaseWrapperOperationException | QueryBuilderException e) {
			fail("testSearchForTitlesInDVDsUsingQueryBuilder failed");
		}
	}

	@Test
	public void testSearchByLikeForAuthorInBooksUsingQueryBuilder() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestRunner.PATH_TO_TEST_CBK);
			ArrayList<QueryComponent> queryComponents = new ArrayList<QueryComponent>();
			queryComponents.add(QueryBuilder.getQueryComponent("Author", QueryOperator.CONTAINS, "Helm"));
			AlbumItemResultSet searchResults = DatabaseOperations.executeSQLQuery(
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
		} catch (DatabaseWrapperOperationException | QueryBuilderException e) {
			fail("testSearchByLikeForAuthorInBooksUsingQueryBuilder failed");
		}

	}

	@Test
	public void testSearchByLikeForTitlesInBooksUsingQueryBuilder() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestRunner.PATH_TO_TEST_CBK);
			ArrayList<QueryComponent> queryComponents = new ArrayList<QueryComponent>();
			queryComponents.add(QueryBuilder.getQueryComponent("Book Title", QueryOperator.CONTAINS, "Code"));
			queryComponents.add(QueryBuilder.getQueryComponent("Book Title", QueryOperator.CONTAINS, "Design"));
			queryComponents.add(QueryBuilder.getQueryComponent("Book Title", QueryOperator.CONTAINS, "Programmer"));
			AlbumItemResultSet searchResults = DatabaseOperations.executeSQLQuery(
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
		} catch (DatabaseWrapperOperationException | QueryBuilderException e) {
			fail("testSearchByLikeForTitlesInBooksUsingQueryBuilder failed");
		}
	}

	@Test
	public void testSearchForHighPriceBooksUsingBiggerThan() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestRunner.PATH_TO_TEST_CBK);
			ArrayList<QueryComponent> queryComponents = new ArrayList<QueryComponent>();
			queryComponents.add(QueryBuilder.getQueryComponent("Price", QueryOperator.BIGGER, "30.0"));
			AlbumItemResultSet searchResults = DatabaseOperations.executeSQLQuery(
					QueryBuilder.buildQuery(queryComponents, true, "Books"));

			assertTrue("Resultset should not be null", searchResults != null);

			int counter = 0;
			while (searchResults.moveToNext()) {
				counter++;
			}

			assertTrue("Resultset should contain 5 items", counter == 5);
		} catch (DatabaseWrapperOperationException | QueryBuilderException e) {
			fail("testSearchForHighPriceBooksUsingBiggerThan failed");
		}
	}

	@Test
	public void testSearchForHighPriceBooksUsingBiggerThanOrEqual() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestRunner.PATH_TO_TEST_CBK);
			ArrayList<QueryComponent> queryComponents = new ArrayList<QueryComponent>();
			queryComponents.add(QueryBuilder.getQueryComponent("Price", QueryOperator.BIGGER_OR_EQUAL, "30.0"));
			AlbumItemResultSet searchResults = DatabaseOperations.executeSQLQuery(
					QueryBuilder.buildQuery(queryComponents, true, "Books"));

			assertTrue("Resultset should not be null", searchResults != null);

			int counter = 0;
			while (searchResults.moveToNext()) {
				counter++;
			}

			assertTrue("Resultset should contain 7 items", counter == 7);
		} catch (DatabaseWrapperOperationException | QueryBuilderException e) {
			fail("testSearchForHighPriceBooksUsingBiggerThanOrEqual failed");
		}
	}

	@Test
	public void testSearchArtistInEmptyMusicAlbumUsingQueryBuilder() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestRunner.PATH_TO_TEST_CBK);
			ArrayList<QueryComponent> queryComponents = new ArrayList<QueryComponent>();
			queryComponents.add(QueryBuilder.getQueryComponent("Artist", QueryOperator.EQUALS, "Rick Astley"));
			AlbumItemResultSet searchResults = DatabaseOperations.executeSQLQuery(
					QueryBuilder.buildQuery(queryComponents, true, "Music CDs"));

			assertTrue("Resultset should not be null", searchResults != null);

			int counter = 0;
			while (searchResults.moveToNext()) {
				counter++;
			}

			assertTrue("Resultset should contain 0 item", counter == 0);
		} catch (DatabaseWrapperOperationException | QueryBuilderException e) {
			fail("testSearchArtistInEmptyMusicAlbumUsingQueryBuilder failed");
		}
	}
}
