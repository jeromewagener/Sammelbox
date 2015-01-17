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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.junit.*;
import org.sammelbox.TestRunner;
import org.sammelbox.controller.listeners.BrowserListener;
import org.sammelbox.controller.managers.DatabaseIntegrityManager;
import org.sammelbox.view.ApplicationUI;
import org.sammelbox.view.composites.BrowserComposite;
import org.sammelbox.view.sidepanes.*;

import static org.junit.Assert.assertTrue;

public class SidepaneCreationTests {
	private static final String NOT_INITIALIZED_ASSERT_MESSAGE = "The sidepane should have been properly initialized";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		TestRunner.resetTestHome();
	}

	@Before
	public void setUp() { 
		TestRunner.resetTestHome();
	}

	@After
	public void tearDown() {}

	@Test
	public void testAddAlbumItemSidepaneCreation() {
		DatabaseIntegrityManager.restoreFromFile(TestRunner.PATH_TO_TEST_CBK);
		Composite sidepane = null;
		
		// Initialize Application UI without showing the shell
		ApplicationUI.initialize(false);
		
		// Create browser composite which is used by several sidepanes
		Composite browserComposite = new BrowserComposite(
				ApplicationUI.getShell(), SWT.NONE, new BrowserListener(ApplicationUI.getShell()));
		ApplicationUI.changeCenterCompositeTo(browserComposite);
		
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
		
		sidepane = ImportSidepane.build(ApplicationUI.getShell(), true);
		assertTrue(NOT_INITIALIZED_ASSERT_MESSAGE, sidepane != null);
		
		sidepane = ImportSidepane.build(ApplicationUI.getShell(), false);
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

