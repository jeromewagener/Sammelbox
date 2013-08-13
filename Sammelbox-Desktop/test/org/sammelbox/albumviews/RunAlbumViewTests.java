/** -----------------------------------------------------------------
 *    Sammelbox: Collection Manager - A free and open-source collection manager for Windows & Linux
 *    Copyright (C) 2011 Jérôme Wagener & Paul Bicheler
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

package org.sammelbox.albumviews;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sammelbox.TestExecuter;
import org.sammelbox.controller.managers.AlbumViewManager;
import org.sammelbox.controller.managers.ConnectionManager;
import org.sammelbox.controller.managers.DatabaseIntegrityManager;
import org.sammelbox.controller.managers.AlbumViewManager.AlbumView;
import org.sammelbox.model.album.AlbumItemResultSet;
import org.sammelbox.model.album.AlbumItemStore;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;

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
