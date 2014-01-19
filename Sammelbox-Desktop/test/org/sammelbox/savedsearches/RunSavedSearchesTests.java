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

package org.sammelbox.savedsearches;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sammelbox.TestExecuter;
import org.sammelbox.controller.managers.ConnectionManager;
import org.sammelbox.controller.managers.DatabaseIntegrityManager;
import org.sammelbox.controller.managers.SavedSearchManager;
import org.sammelbox.controller.managers.SavedSearchManager.SavedSearch;
import org.sammelbox.model.album.AlbumItemResultSet;
import org.sammelbox.model.album.AlbumItemStore;
import org.sammelbox.model.database.QueryBuilderException;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;

public class RunSavedSearchesTests {
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		TestExecuter.resetTestHome();
	}

	@Before
	public void setUp() {
		TestExecuter.resetTestHome();
	}

	@After
	public void tearDown() throws Exception {
		ConnectionManager.closeConnection();
		TestExecuter.resetTestHome();
	}

	@Test
	public void testExistingSavedSearchesForBooks() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestExecuter.PATH_TO_TEST_CBK);
			
			SavedSearchManager.initialize();
			
			List<SavedSearch> savedSearches = SavedSearchManager.getSavedSearches("Books");
			assertTrue("There should be one saved search for the book album", savedSearches.size() == 1);
			
			if (savedSearches.get(0).getName().equals("Programming Languages")) {
				AlbumItemStore.reinitializeStore(new AlbumItemResultSet(
						ConnectionManager.getConnection(), savedSearches.get(0).getSQLQueryString()));
				
				assertTrue("There should be two books about programming languages", AlbumItemStore.getAllAlbumItems().size() == 2);
			} else {
				fail("The first view should be Programming Languages");
			}
			
		} catch (DatabaseWrapperOperationException | QueryBuilderException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testExistingSavedSearchesForDVDs() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestExecuter.PATH_TO_TEST_CBK);
			
			SavedSearchManager.initialize();
			
			List<SavedSearch> albumViews = SavedSearchManager.getSavedSearches("DVDs");
			
			assertTrue("There should be two saved searches for the dvd album", albumViews.size() == 2);
			
			for (SavedSearch albumView : albumViews) {
				if (albumView.getName().equals("Unwatched")) {
					AlbumItemStore.reinitializeStore(new AlbumItemResultSet(
							ConnectionManager.getConnection(), albumView.getSQLQueryString()));
					
					assertTrue("Both views should show three items", AlbumItemStore.getAllAlbumItems().size() == 3);
				} else if (albumView.getName().equals("My favorite DVDs")) {
					AlbumItemStore.reinitializeStore(new AlbumItemResultSet(
							ConnectionManager.getConnection(), albumView.getSQLQueryString()));
					
					assertTrue("Both views should show three items", AlbumItemStore.getAllAlbumItems().size() == 3);
				} else {
					fail("There should be no other saved searches than Unwatched and My favorite DVDs");
				}
			}
			
		} catch (DatabaseWrapperOperationException | QueryBuilderException e) {
			fail(e.getMessage());
		}
	}
}
