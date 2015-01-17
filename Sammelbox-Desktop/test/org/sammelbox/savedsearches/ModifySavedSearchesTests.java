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

import org.junit.*;
import org.sammelbox.TestRunner;
import org.sammelbox.controller.managers.DatabaseIntegrityManager;
import org.sammelbox.controller.managers.SavedSearchManager;
import org.sammelbox.model.database.QueryComponent;
import org.sammelbox.model.database.QueryOperator;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

public class ModifySavedSearchesTests {
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
	public void testMoveSavedSearches() {
		DatabaseIntegrityManager.restoreFromFile(TestRunner.PATH_TO_TEST_CBK);
		
		SavedSearchManager.initialize();
		
		java.util.List<QueryComponent> queryComponents = new ArrayList<QueryComponent>();
		queryComponents.add(new QueryComponent("Year", QueryOperator.SMALLER_OR_EQUAL, "2000"));
		SavedSearchManager.addSavedSearch("Before 2000", "DVDs", queryComponents, true);
		
		assertTrue("The first view should be: My favorite DVDs", 
				SavedSearchManager.getSavedSearches("DVDs").get(0).getName().equals("My favorite DVDs"));
		assertTrue("The second view should be: Unwatched", 
				SavedSearchManager.getSavedSearches("DVDs").get(1).getName().equals("Unwatched"));
		assertTrue("The third view should be: Before 2000", 
				SavedSearchManager.getSavedSearches("DVDs").get(2).getName().equals("Before 2000"));
		
		SavedSearchManager.moveOneUp("DVDs", 1);
		
		assertTrue("The first view should be: Unwatched", 
				SavedSearchManager.getSavedSearches("DVDs").get(0).getName().equals("Unwatched"));
		assertTrue("The second view should be: My favorite DVDs", 
				SavedSearchManager.getSavedSearches("DVDs").get(1).getName().equals("My favorite DVDs"));
		assertTrue("The third view should be: Before 2000", 
				SavedSearchManager.getSavedSearches("DVDs").get(2).getName().equals("Before 2000"));
		
		SavedSearchManager.moveOneUp("DVDs", 2);
		
		assertTrue("The first view should be: Unwatched", 
				SavedSearchManager.getSavedSearches("DVDs").get(0).getName().equals("Unwatched"));
		assertTrue("The second view should be: Before 2000", 
				SavedSearchManager.getSavedSearches("DVDs").get(1).getName().equals("Before 2000"));
		assertTrue("The third view should be: My favorite DVDs", 
				SavedSearchManager.getSavedSearches("DVDs").get(2).getName().equals("My favorite DVDs"));
		
		SavedSearchManager.moveOneDown("DVDs", 0);
		
		assertTrue("The first view should be: Before 2000", 
				SavedSearchManager.getSavedSearches("DVDs").get(0).getName().equals("Before 2000"));
		assertTrue("The second view should be: Unwatched", 
				SavedSearchManager.getSavedSearches("DVDs").get(1).getName().equals("Unwatched"));
		assertTrue("The third view should be: My favorite DVDs", 
				SavedSearchManager.getSavedSearches("DVDs").get(2).getName().equals("My favorite DVDs"));
		
		SavedSearchManager.moveToFront("DVDs", 1);
		
		assertTrue("The first view should be: Unwatched", 
				SavedSearchManager.getSavedSearches("DVDs").get(0).getName().equals("Unwatched"));
		assertTrue("The second view should be: Before 2000", 
				SavedSearchManager.getSavedSearches("DVDs").get(1).getName().equals("Before 2000"));
		assertTrue("The third view should be: My favorite DVDs", 
				SavedSearchManager.getSavedSearches("DVDs").get(2).getName().equals("My favorite DVDs"));
		
		SavedSearchManager.moveToBottom("DVDs", 0);
		
		assertTrue("The second view should be: Before 2000", 
				SavedSearchManager.getSavedSearches("DVDs").get(0).getName().equals("Before 2000"));
		assertTrue("The third view should be: My favorite DVDs", 
				SavedSearchManager.getSavedSearches("DVDs").get(1).getName().equals("My favorite DVDs"));
		assertTrue("The first view should be: Unwatched", 
				SavedSearchManager.getSavedSearches("DVDs").get(2).getName().equals("Unwatched"));
	}
}
