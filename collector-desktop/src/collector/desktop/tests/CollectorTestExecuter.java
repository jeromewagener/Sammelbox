package collector.desktop.tests;

import java.io.File;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

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
			System.getProperty("user.dir") + File.separatorChar + "test-data" + 
					File.separatorChar + "Test-Albums-Version-1.cbk";
	public static final String PATH_TO_TEST_PIC1 = 
			System.getProperty("user.dir") + File.separatorChar + "test-data" + 
					File.separator + "testPictures"+
					File.separatorChar + "test Pic1.png";
	public static final String PATH_TO_TEST_PIC2 = 
			System.getProperty("user.dir") + File.separatorChar + "test-data" + 
					File.separator + "testPictures"+
					File.separatorChar + "test Pic2.png";
	public static final String PATH_TO_TEST_PIC3 = 
			System.getProperty("user.dir") + File.separatorChar + "test-data" + 
					File.separator + "testPictures"+
					File.separatorChar + "test Pic3.png";
}
