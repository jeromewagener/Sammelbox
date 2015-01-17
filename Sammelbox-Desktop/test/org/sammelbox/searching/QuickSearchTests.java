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
import org.sammelbox.model.album.MetaItemField;
import org.sammelbox.model.database.DatabaseStringUtilities;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseOperations;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class QuickSearchTests {
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
	public void testQuickSearchActorInDVDs() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestRunner.PATH_TO_TEST_CBK);
			ArrayList<String> quickSearchTerms = new ArrayList<String>();
			quickSearchTerms.add("Smith");
			AlbumItemResultSet searchResults = DatabaseOperations.executeQuickSearch("DVDs", quickSearchTerms);

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
			DatabaseIntegrityManager.restoreFromFile(TestRunner.PATH_TO_TEST_CBK);
			ArrayList<String> quickSearchTerms = new ArrayList<String>();
			quickSearchTerms.add("Cooper");
			quickSearchTerms.add("Wilson");
			AlbumItemResultSet searchResults = DatabaseOperations.executeQuickSearch("DVDs", quickSearchTerms);

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
			DatabaseIntegrityManager.restoreFromFile(TestRunner.PATH_TO_TEST_CBK);
			ArrayList<String> quickSearchTerms = new ArrayList<String>();
			quickSearchTerms.add("Me");
			AlbumItemResultSet searchResults = DatabaseOperations.executeQuickSearch("DVDs", quickSearchTerms);

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
	
	@Test
	public void testRemoveQuickSearchFlag() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestRunner.PATH_TO_TEST_CBK);
		
			MetaItemField dvdTitleMetaItemField = null;
			for (MetaItemField metaItemField : DatabaseOperations.getAlbumItemMetaMap("DVDs").values()) {
				if (metaItemField.getName().equals("Title")) {
					dvdTitleMetaItemField = metaItemField;
					break;
				}
			}
			
			if (dvdTitleMetaItemField != null && dvdTitleMetaItemField.isQuickSearchable()) {
				// disable quick searchability
				dvdTitleMetaItemField.setQuickSearchable(false);
				DatabaseOperations.updateQuickSearchable(
						DatabaseStringUtilities.generateTableName("DVDs"), dvdTitleMetaItemField);
			} else {
				fail("There should be a dvd title field that is quick searchable");
			}
			
		} catch (DatabaseWrapperOperationException e) {
			fail("Failed due to internal database error");
		}
	}
}