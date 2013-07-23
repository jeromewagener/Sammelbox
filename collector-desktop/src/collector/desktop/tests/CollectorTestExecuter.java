package collector.desktop.tests;

import static org.junit.Assert.fail;

import java.io.File;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import collector.desktop.controller.filesystem.FileSystemAccessWrapper;
import collector.desktop.model.database.utilities.ConnectionManager;
import collector.desktop.tests.album.AlterAlbumTests;
import collector.desktop.tests.album.BackupRestoreTests;
import collector.desktop.tests.album.CreateAlbumTests;
import collector.desktop.tests.album.RemoveAlbumTests;
import collector.desktop.tests.albumitems.AddAlbumItemTests;
import collector.desktop.tests.albumitems.RemoveAlbumItemTests;
import collector.desktop.tests.albumitems.UpdateAlbumItemTests;
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

	AdvancedSearchTests.class,
	QuickSearchTests.class
	})

public class CollectorTestExecuter {
	public static final String PATH_TO_TEST_CBK = 
			System.getProperty("user.dir") + File.separatorChar + "testdata" + 
					File.separatorChar + "test-albums-version-2.6.cbk";
	
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
