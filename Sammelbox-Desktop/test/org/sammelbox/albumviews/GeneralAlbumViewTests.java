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

package org.sammelbox.albumviews;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.List;
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
import org.sammelbox.view.ApplicationUI;

public class GeneralAlbumViewTests {
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
		
		// Although the list is wrongly assigned to the shell itself, this allows to test the view behavior
		ApplicationUI.setViewList(new List(ApplicationUI.getShell(), SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL));
	}

	@After
	public void tearDown() throws Exception {
		TestExecuter.resetTestHome();
	}

	@Test
	public void addAnotherViewToDVDs() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestExecuter.PATH_TO_TEST_CBK);
			
			AlbumViewManager.initialize();
			
			assertTrue("The first view should be: My favorite DVDs", 
					AlbumViewManager.getAlbumViews("DVDs").get(0).getName().equals("My favorite DVDs"));
			assertTrue("The second view should be: Unwatched", 
					AlbumViewManager.getAlbumViews("DVDs").get(1).getName().equals("Unwatched"));
			
			AlbumViewManager.addAlbumView("Before 2000", "DVDs", "SELECT * FROM DVDs WHERE [Year] <= 2000");
			
			assertTrue("After adding another view, there should be 3 views in total for the DVD album", 
					AlbumViewManager.getAlbumViews("DVDs").size() == 3);
			
			for (AlbumView albumView : AlbumViewManager.getAlbumViews("DVDs")) {
				if (albumView.getName().equals("Before 2000")) {
					AlbumItemStore.reinitializeStore(new AlbumItemResultSet(
							ConnectionManager.getConnection(), albumView.getSqlQuery()));
					
					assertTrue("There should be 5 movies from before the year 2000 (or equal)", 
							AlbumItemStore.getAllAlbumItems().size() == 5);
				}
			}
			
			assertTrue("Since views are immediately saved, a reinitialization should still yield the three views", 
					AlbumViewManager.getAlbumViews("DVDs").size() == 3);
			
			assertTrue("The first view should be: My favorite DVDs", 
					AlbumViewManager.getAlbumViews("DVDs").get(0).getName().equals("My favorite DVDs"));
			assertTrue("The second view should be: Unwatched", 
					AlbumViewManager.getAlbumViews("DVDs").get(1).getName().equals("Unwatched"));
			assertTrue("The third view should be: Before 2000", 
					AlbumViewManager.getAlbumViews("DVDs").get(2).getName().equals("Before 2000"));
			
		} catch (DatabaseWrapperOperationException ex) {
			fail(ex.getMessage());
		}
	}
	
	@Test
	public void testRemoveView() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestExecuter.PATH_TO_TEST_CBK);
			
			AlbumViewManager.initialize();
			
			assertTrue("The first view should be: My favorite DVDs", 
					AlbumViewManager.getAlbumViews("DVDs").get(0).getName().equals("My favorite DVDs"));
			assertTrue("The second view should be: Unwatched", 
					AlbumViewManager.getAlbumViews("DVDs").get(1).getName().equals("Unwatched"));


			AlbumViewManager.removeAlbumView("DVDs", "My favorite DVDs");
			
			assertTrue("The first view should be: Unwatched", 
					AlbumViewManager.getAlbumViews("DVDs").get(0).getName().equals("Unwatched"));
			
			AlbumViewManager.initialize();
			
			assertTrue("The first view should be: Unwatched", 
					AlbumViewManager.getAlbumViews("DVDs").get(0).getName().equals("Unwatched"));
			
		} catch (DatabaseWrapperOperationException ex) {
			fail(ex.getMessage());
		}
	}
	
	@Test
	public void testRemoveAllViewFromDVDs() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestExecuter.PATH_TO_TEST_CBK);
			
			AlbumViewManager.initialize();
						
			assertTrue("There should be two views for the DVD album", 
					AlbumViewManager.getAlbumViews("DVDs").size() == 2);
			
			AlbumViewManager.removeAlbumViewsFromAlbum("DVDs");
			
			assertTrue("There should no longer be views for the DVD album", 
					AlbumViewManager.getAlbumViews("DVDs").isEmpty());
			
		} catch (DatabaseWrapperOperationException ex) {
			fail(ex.getMessage());
		}
	}
}
