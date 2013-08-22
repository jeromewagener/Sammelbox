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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sammelbox.TestExecuter;
import org.sammelbox.controller.managers.AlbumViewManager;
import org.sammelbox.controller.managers.DatabaseIntegrityManager;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.view.ApplicationUI;

public class ModifyAlbumViewTests {
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
	public void testMoveViews() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestExecuter.PATH_TO_TEST_CBK);
			
			AlbumViewManager.initialize();
			
			AlbumViewManager.addAlbumView("Before 2000", "DVDs", "SELECT * FROM DVDs WHERE [Year] <= 2000");
			
			assertTrue("The first view should be: My favorite DVDs", 
					AlbumViewManager.getAlbumViews("DVDs").get(0).getName().equals("My favorite DVDs"));
			assertTrue("The second view should be: Unwatched", 
					AlbumViewManager.getAlbumViews("DVDs").get(1).getName().equals("Unwatched"));
			assertTrue("The third view should be: Before 2000", 
					AlbumViewManager.getAlbumViews("DVDs").get(2).getName().equals("Before 2000"));
			
			AlbumViewManager.moveOneUp("DVDs", 1);
			
			assertTrue("The first view should be: Unwatched", 
					AlbumViewManager.getAlbumViews("DVDs").get(0).getName().equals("Unwatched"));
			assertTrue("The second view should be: My favorite DVDs", 
					AlbumViewManager.getAlbumViews("DVDs").get(1).getName().equals("My favorite DVDs"));
			assertTrue("The third view should be: Before 2000", 
					AlbumViewManager.getAlbumViews("DVDs").get(2).getName().equals("Before 2000"));
			
			AlbumViewManager.moveOneUp("DVDs", 2);
			
			assertTrue("The first view should be: Unwatched", 
					AlbumViewManager.getAlbumViews("DVDs").get(0).getName().equals("Unwatched"));
			assertTrue("The second view should be: Before 2000", 
					AlbumViewManager.getAlbumViews("DVDs").get(1).getName().equals("Before 2000"));
			assertTrue("The third view should be: My favorite DVDs", 
					AlbumViewManager.getAlbumViews("DVDs").get(2).getName().equals("My favorite DVDs"));
			
			AlbumViewManager.moveOneDown("DVDs", 0);
			
			assertTrue("The first view should be: Before 2000", 
					AlbumViewManager.getAlbumViews("DVDs").get(0).getName().equals("Before 2000"));
			assertTrue("The second view should be: Unwatched", 
					AlbumViewManager.getAlbumViews("DVDs").get(1).getName().equals("Unwatched"));
			assertTrue("The third view should be: My favorite DVDs", 
					AlbumViewManager.getAlbumViews("DVDs").get(2).getName().equals("My favorite DVDs"));
			
			AlbumViewManager.moveToFront("DVDs", 1);
			
			assertTrue("The first view should be: Unwatched", 
					AlbumViewManager.getAlbumViews("DVDs").get(0).getName().equals("Unwatched"));
			assertTrue("The second view should be: Before 2000", 
					AlbumViewManager.getAlbumViews("DVDs").get(1).getName().equals("Before 2000"));
			assertTrue("The third view should be: My favorite DVDs", 
					AlbumViewManager.getAlbumViews("DVDs").get(2).getName().equals("My favorite DVDs"));
			
			AlbumViewManager.moveToBottom("DVDs", 0);
			
			assertTrue("The second view should be: Before 2000", 
					AlbumViewManager.getAlbumViews("DVDs").get(0).getName().equals("Before 2000"));
			assertTrue("The third view should be: My favorite DVDs", 
					AlbumViewManager.getAlbumViews("DVDs").get(1).getName().equals("My favorite DVDs"));
			assertTrue("The first view should be: Unwatched", 
					AlbumViewManager.getAlbumViews("DVDs").get(2).getName().equals("Unwatched"));
			
		} catch (DatabaseWrapperOperationException ex) {
			fail(ex.getMessage());
		}
	}
}
