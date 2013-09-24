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

package org.sammelbox.sidepanes;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.swt.widgets.Composite;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sammelbox.TestExecuter;
import org.sammelbox.controller.listeners.BrowserListener;
import org.sammelbox.controller.managers.DatabaseIntegrityManager;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.view.ApplicationUI;
import org.sammelbox.view.composites.BrowserComposite;
import org.sammelbox.view.sidepanes.AddAlbumItemSidepane;
import org.sammelbox.view.sidepanes.AdvancedSearchSidepane;
import org.sammelbox.view.sidepanes.AlterAlbumSidepane;
import org.sammelbox.view.sidepanes.CreateAlbumSidepane;
import org.sammelbox.view.sidepanes.EmptySidepane;
import org.sammelbox.view.sidepanes.ImportSidepane;
import org.sammelbox.view.sidepanes.QuickControlSidepane;
import org.sammelbox.view.sidepanes.SettingsSidepane;
import org.sammelbox.view.sidepanes.SynchronizeSidepane;
import org.sammelbox.view.sidepanes.UpdateAlbumItemSidepane;

public class SidepaneCreationTests {
	private static final String NOT_INITIALIZED_ASSERT_MESSAGE = "The sidepane should have been properly initialized";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		TestExecuter.resetTestHome();
	}

	@Before
	public void setUp() { 
		TestExecuter.resetTestHome();
		try {
			DatabaseIntegrityManager.restoreFromFile(TestExecuter.PATH_TO_TEST_CBK);
		} catch (DatabaseWrapperOperationException e) {
			fail("Failed while restoring test albums");
		}
	}

	@After
	public void tearDown() {}

	@Test
	public void testAddAlbumItemSidepaneCreation() {
		Composite sidepane = null;
		
		// Initialize Application UI without showing the shell
		ApplicationUI.initialize(ApplicationUI.getShell(), false);
		
		// Create browser composite which is used by several sidepanes
		BrowserComposite.buildAndStore(ApplicationUI.getShell(), new BrowserListener(ApplicationUI.getShell()));
		
		// Test sidepane creation
		sidepane = AddAlbumItemSidepane.build(ApplicationUI.getShell(), "DVDs");
		assertTrue(NOT_INITIALIZED_ASSERT_MESSAGE, sidepane != null);
		
		sidepane = AdvancedSearchSidepane.build(ApplicationUI.getShell(), "DVDs");
		assertTrue(NOT_INITIALIZED_ASSERT_MESSAGE, sidepane != null);
				
		sidepane = AlterAlbumSidepane.build(ApplicationUI.getShell(), "DVDs");
		assertTrue(NOT_INITIALIZED_ASSERT_MESSAGE, sidepane != null);
		
		sidepane = CreateAlbumSidepane.build(ApplicationUI.getShell());
		assertTrue(NOT_INITIALIZED_ASSERT_MESSAGE, sidepane != null);
		
		sidepane = EmptySidepane.build(ApplicationUI.getShell());
		assertTrue(NOT_INITIALIZED_ASSERT_MESSAGE, sidepane != null);
		
		sidepane = ImportSidepane.build(ApplicationUI.getShell());
		assertTrue(NOT_INITIALIZED_ASSERT_MESSAGE, sidepane != null);
		
		sidepane = QuickControlSidepane.build(ApplicationUI.getShell());
		assertTrue(NOT_INITIALIZED_ASSERT_MESSAGE, sidepane != null);
		
		sidepane = SettingsSidepane.build(ApplicationUI.getShell());
		assertTrue(NOT_INITIALIZED_ASSERT_MESSAGE, sidepane != null);
		
		sidepane = SynchronizeSidepane.build(ApplicationUI.getShell());
		assertTrue(NOT_INITIALIZED_ASSERT_MESSAGE, sidepane != null);
		
		sidepane = UpdateAlbumItemSidepane.build(ApplicationUI.getShell(), "DVDs", 1);
		assertTrue(NOT_INITIALIZED_ASSERT_MESSAGE, sidepane != null);
	}
}

