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

package collector.desktop.tests;

import static org.junit.Assert.fail;

import java.io.File;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import collector.desktop.controller.filesystem.FileSystemAccessWrapper;
import collector.desktop.controller.managers.ConnectionManager;
import collector.desktop.tests.album.AlterAlbumTests;
import collector.desktop.tests.album.BackupRestoreTests;
import collector.desktop.tests.album.CreateAlbumTests;
import collector.desktop.tests.album.RemoveAlbumTests;
import collector.desktop.tests.albumitems.AddAlbumItemTests;
import collector.desktop.tests.albumitems.RemoveAlbumItemTests;
import collector.desktop.tests.albumitems.UpdateAlbumItemTests;
import collector.desktop.tests.albumviews.GeneralAlbumViewTests;
import collector.desktop.tests.albumviews.ModifyAlbumViewTests;
import collector.desktop.tests.albumviews.RunAlbumViewTests;
import collector.desktop.tests.searching.AdvancedSearchTests;
import collector.desktop.tests.searching.QuickSearchTests;

@RunWith(Suite.class)

@Suite.SuiteClasses({
	AlterAlbumTests.class, 
	BackupRestoreTests.class,  
	CreateAlbumTests.class,
	AddAlbumItemTests.class,
	RemoveAlbumTests.class,
	
	AddAlbumItemTests.class,
	RemoveAlbumItemTests.class,
	UpdateAlbumItemTests.class,

	GeneralAlbumViewTests.class,
	ModifyAlbumViewTests.class,
	RunAlbumViewTests.class,
	
	AdvancedSearchTests.class,
	QuickSearchTests.class
	})

public class TestExecuter {
	public static final String PATH_TO_TEST_CBK = 
			System.getProperty("user.dir") + File.separatorChar + "testdata" + 
					File.separatorChar + "test-albums-version-2.8.1.cbk";
	
	public static final String PATH_TO_TEST_PICTURE_1 = 
			System.getProperty("user.dir") + File.separatorChar + "res" + 
					File.separator + "graphics"+
					File.separatorChar + "placeholder1.png";
	
	public static final String PATH_TO_TEST_PICTURE_2 = 
			System.getProperty("user.dir") + File.separatorChar + "res" + 
					File.separator + "graphics"+
					File.separatorChar + "placeholder2.png";
	
	public static final String PATH_TO_TEST_PICTURE_3 = 
			System.getProperty("user.dir") + File.separatorChar + "res" + 
					File.separator + "graphics"+
					File.separatorChar + "placeholder3.png";
	
	public static void resetEverything() {
		try {			
			ConnectionManager.closeConnection();
			FileSystemAccessWrapper.removeCollectorHome();
			Class.forName("org.sqlite.JDBC");
			FileSystemAccessWrapper.updateCollectorFileStructure();			
			ConnectionManager.openConnection();
			FileSystemAccessWrapper.updateAlbumFileStructure(ConnectionManager.getConnection());
		} catch (Exception e) {
			fail("A problem occured while resetting everything for the alter album tests");
		}
	}
}
