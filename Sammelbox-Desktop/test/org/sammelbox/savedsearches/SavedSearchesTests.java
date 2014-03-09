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

import java.util.ArrayList;

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
import org.sammelbox.model.database.QueryComponent;
import org.sammelbox.model.database.QueryOperator;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;

public class SavedSearchesTests {
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
		TestExecuter.resetTestHome();
	}

	@Test
	public void addAnotherSavedSearchToDVDs() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestExecuter.PATH_TO_TEST_CBK);
			
			SavedSearchManager.initialize();
			
			assertTrue("The first saved search should be: My favorite DVDs", 
					SavedSearchManager.getSavedSearches("DVDs").get(0).getName().equals("My favorite DVDs"));
			assertTrue("The second saved search should be: Unwatched", 
					SavedSearchManager.getSavedSearches("DVDs").get(1).getName().equals("Unwatched"));
			
			java.util.List<QueryComponent> queryComponents = new ArrayList<QueryComponent>();
			queryComponents.add(new QueryComponent("Year", QueryOperator.SMALLER_OR_EQUAL, "2000"));
			
			SavedSearchManager.addSavedSearch("Before 2000", "DVDs", queryComponents, true);
			
			assertTrue("After adding another view, there should be 3 views in total for the DVD album", 
					SavedSearchManager.getSavedSearches("DVDs").size() == 3);
			
			for (SavedSearch savedSearch : SavedSearchManager.getSavedSearches("DVDs")) {
				if (savedSearch.getName().equals("Before 2000")) {
					AlbumItemStore.reinitializeStore(new AlbumItemResultSet(ConnectionManager.getConnection(), savedSearch.getSQLQueryString()));
					
					assertTrue("There should be 5 movies from before the year 2000 (or equal)", 
							AlbumItemStore.getAllAlbumItems().size() == 5);
				}
			}
			
			assertTrue("Since views are immediately saved, a reinitialization should still yield the three views", 
					SavedSearchManager.getSavedSearches("DVDs").size() == 3);
			
			assertTrue("The first view should be: My favorite DVDs", 
					SavedSearchManager.getSavedSearches("DVDs").get(0).getName().equals("My favorite DVDs"));
			assertTrue("The second view should be: Unwatched", 
					SavedSearchManager.getSavedSearches("DVDs").get(1).getName().equals("Unwatched"));
			assertTrue("The third view should be: Before 2000", 
					SavedSearchManager.getSavedSearches("DVDs").get(2).getName().equals("Before 2000"));
			
		} catch (DatabaseWrapperOperationException | QueryBuilderException ex) {
			fail(ex.getMessage());
		}
	}
	
	@Test
	public void testRemoveSavedSearch() {
		DatabaseIntegrityManager.restoreFromFile(TestExecuter.PATH_TO_TEST_CBK);
		
		SavedSearchManager.initialize();
		
		assertTrue("The first view should be: My favorite DVDs", 
				SavedSearchManager.getSavedSearches("DVDs").get(0).getName().equals("My favorite DVDs"));
		assertTrue("The second view should be: Unwatched", 
				SavedSearchManager.getSavedSearches("DVDs").get(1).getName().equals("Unwatched"));


		SavedSearchManager.removeSavedSearch("DVDs", "My favorite DVDs");
		
		assertTrue("The first view should be: Unwatched", 
				SavedSearchManager.getSavedSearches("DVDs").get(0).getName().equals("Unwatched"));
		
		SavedSearchManager.initialize();
		
		assertTrue("The first view should be: Unwatched", 
				SavedSearchManager.getSavedSearches("DVDs").get(0).getName().equals("Unwatched"));
	}
	
	@Test
	public void testRemoveAllSavedSearchesFromDVDs() {
		DatabaseIntegrityManager.restoreFromFile(TestExecuter.PATH_TO_TEST_CBK);
		
		SavedSearchManager.initialize();
					
		assertTrue("There should be two views for the DVD album", 
				SavedSearchManager.getSavedSearches("DVDs").size() == 2);
		
		SavedSearchManager.removeSavedSearchesFromAlbum("DVDs");
		
		assertTrue("There should no longer be views for the DVD album", 
				SavedSearchManager.getSavedSearches("DVDs").isEmpty());
	}
}
