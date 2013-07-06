package collector.desktop.tests.album;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import collector.desktop.controller.filesystem.FileSystemAccessWrapper;
import collector.desktop.model.album.AlbumItem;
import collector.desktop.model.album.FieldType;
import collector.desktop.model.album.ItemField;
import collector.desktop.model.album.MetaItemField;
import collector.desktop.model.album.OptionType;
import collector.desktop.model.database.AlbumItemResultSet;
import collector.desktop.model.database.ConnectionManager;
import collector.desktop.model.database.DatabaseIntegrityManager;
import collector.desktop.model.database.DatabaseWrapper;
import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException;
import collector.desktop.tests.CollectorTestExecuter;

public class BackupRestoreTests {
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		CollectorTestExecuter.resetEverything();
	}

	@Before
	public void setUp() {
		CollectorTestExecuter.resetEverything();
	}

	@After
	public void tearDown() throws Exception {
		ConnectionManager.closeConnection();
	}

	private void createBookAlbum() {		
		final String albumName = "Books";

		MetaItemField titleField = new MetaItemField("Book Title", FieldType.Text, true);
		MetaItemField authorField = new MetaItemField("Author", FieldType.Text, true);
		MetaItemField purchaseField = new MetaItemField("Purchased", FieldType.Date, false);
		MetaItemField priceField = new MetaItemField("Price", FieldType.Number, false);
		MetaItemField lenttoField = new MetaItemField("Lent to", FieldType.Text, false);

		List<MetaItemField> columns = new ArrayList<MetaItemField>();
		columns.add(titleField);
		columns.add(authorField);
		columns.add(purchaseField);
		columns.add(priceField);
		columns.add(lenttoField);

		try {
			DatabaseWrapper.createNewAlbum(albumName, columns, true);			
		} catch (DatabaseWrapperOperationException e) {
			fail("Creation of album " + albumName + " failed");
		}
	}

	private void createDVDAlbum() {		
		final String albumName = "DVD Album";

		MetaItemField DVDTitleField = new MetaItemField("DVD Title", FieldType.Text, true);
		MetaItemField actorField = new MetaItemField("Actors", FieldType.Text, true);

		List<MetaItemField> columns = new ArrayList<MetaItemField>();
		columns.add(DVDTitleField);
		columns.add(actorField);

		try {
			DatabaseWrapper.createNewAlbum(albumName, columns, false);			
		} catch (DatabaseWrapperOperationException e) {
			fail("Creation of album " + albumName + " failed");
		}
	}

	private void createMusicAlbum() {
		final String albumName = "Music Album";

		MetaItemField titleField = new MetaItemField("Title", FieldType.Text, true);
		MetaItemField artistField = new MetaItemField("Artist", FieldType.Text, true);

		List<MetaItemField> columns = new ArrayList<MetaItemField>();
		columns.add(titleField);
		columns.add(artistField);

		try {
			DatabaseWrapper.createNewAlbum(albumName, columns, true);			
		} catch (DatabaseWrapperOperationException e) {
			fail("Creation of album "+ albumName + " failed");
		}
	}

	private void fillBookAlbum() {
		final String albumName = "Books";

		AlbumItem item = new AlbumItem(albumName);

		List<ItemField> fields = new ArrayList<ItemField>();
		fields.add( new ItemField("Book Title", FieldType.Text, "book title 1"));
		fields.add( new ItemField("Author", FieldType.Text, "the author 1"));
		fields.add( new ItemField("Purchased", FieldType.Date, new Date(System.currentTimeMillis())));
		fields.add( new ItemField("Price", FieldType.Number, 4.2d));
		fields.add( new ItemField("Lent to", FieldType.Text, "some random name 1"));

		item.setFields(fields);

		try {
			DatabaseWrapper.addNewAlbumItem(item, true);			
		} catch (DatabaseWrapperOperationException e) {
			fail("Album Item could not be inserted into album");
		}

		item = new AlbumItem(albumName);

		fields = new ArrayList<ItemField>();
		fields.add( new ItemField("Book Title", FieldType.Text, "book title 2"));
		fields.add( new ItemField("Author", FieldType.Text, "the author 2"));
		fields.add( new ItemField("Purchased", FieldType.Date, new Date(System.currentTimeMillis())));
		fields.add( new ItemField("Price", FieldType.Number, 4.22d));
		fields.add( new ItemField("Lent to", FieldType.Text, "some random name 2"));

		item.setFields(fields);

		try {
			DatabaseWrapper.addNewAlbumItem(item, true);			
		} catch (DatabaseWrapperOperationException e) {
			fail("Album Item could not be inserted into album");
		}

		item = new AlbumItem(albumName);

		fields = new ArrayList<ItemField>();
		fields.add( new ItemField("Book Title", FieldType.Text, "book title 3"));
		fields.add( new ItemField("Author", FieldType.Text, "the author 3"));
		fields.add( new ItemField("Purchased", FieldType.Date, new Date(System.currentTimeMillis())));
		fields.add( new ItemField("Price", FieldType.Number, 4.23d));
		fields.add( new ItemField("Lent to", FieldType.Text, "some random name 3"));

		item.setFields(fields);

		try {
			DatabaseWrapper.addNewAlbumItem(item, true);			
		} catch (DatabaseWrapperOperationException e) {
			fail("Album Item could not be inserted into album");
		}
	}

	private void fillDVDAlbum() {
		final String albumName = "DVD Album";

		AlbumItem item = new AlbumItem(albumName);
		List<ItemField> fields = new ArrayList<ItemField>();
		fields.add( new ItemField("DVD Title", FieldType.Text, "dvd title 1"));
		fields.add( new ItemField("Actors", FieldType.Text, "actor 1"));
		item.setFields(fields);

		try {
			DatabaseWrapper.addNewAlbumItem(item, true);			
		} catch (DatabaseWrapperOperationException e) {
			fail("Album Item could not be inserted into album");
		}

		item = new AlbumItem(albumName);
		fields = new ArrayList<ItemField>();
		fields.add( new ItemField("DVD Title", FieldType.Text, "dvd title 2"));
		fields.add( new ItemField("Actors", FieldType.Text, "actor 2"));
		item.setFields(fields);

		try {
			DatabaseWrapper.addNewAlbumItem(item, true);			
		} catch (DatabaseWrapperOperationException e) {
			fail("Album Item could not be inserted into album");
		}
	}

	@Test
	public void testBackupOfSingleAlbum() {
		createBookAlbum();
		fillBookAlbum();
		
		try {
			AlbumItemResultSet allAlbumItems = DatabaseWrapper.executeSQLQuery("SELECT * FROM Books");
			assertTrue("Resultset should not be null", allAlbumItems != null);

			int counter = 0;
			while (allAlbumItems.moveToNext()) {
				counter++;
			}

			assertTrue("Resultset should contain 3 items", counter == 3);
			DatabaseIntegrityManager.backupToFile(FileSystemAccessWrapper.TEMP_DIR + 
					File.separatorChar + "testBackupRestoreOfSingleAlbum.cbk");
		} catch (DatabaseWrapperOperationException e) {
			fail("testBackupOfSingleAlbum raised an exception");
		} 
	}

	@Test
	public void testBackupOfMultipleAlbums() {
		createBookAlbum();
		createDVDAlbum();
		createMusicAlbum();
		fillBookAlbum();
		fillDVDAlbum();
		
		try {
			AlbumItemResultSet allAlbumItems = DatabaseWrapper.executeSQLQuery("SELECT * FROM Books");
			assertTrue("Resultset should not be null", allAlbumItems != null);

			int counter = 0;
			while (allAlbumItems.moveToNext()) {
				counter++;
			}

			assertTrue("Resultset should contain 3 items", counter == 3);
			allAlbumItems = DatabaseWrapper.executeSQLQuery("SELECT * FROM 'DVD Album'");
			assertTrue("Resultset should not be null", allAlbumItems != null);

			counter = 0;
			while (allAlbumItems.moveToNext()) {
				counter++;
			}

			assertTrue("Resultset should contain 2 items", counter == 2);		
			allAlbumItems = DatabaseWrapper.executeSQLQuery("SELECT * FROM 'Music Album'");
			assertTrue("Resultset should not be null", allAlbumItems != null);

			counter = 0;
			while (allAlbumItems.moveToNext()) {
				counter++;
			}

			assertTrue("Resultset should contain 0 items", counter == 0);	
			DatabaseIntegrityManager.backupToFile(FileSystemAccessWrapper.TEMP_DIR + 
					File.separatorChar + "testBackupRestoreOfMultipleAlbums.cbk");
		} catch (DatabaseWrapperOperationException e) {
			fail("Failed on internal db error");			
		}
	}

	@Test
	public void testRestoreOfSingleAlbum() {
		testBackupOfSingleAlbum();
		try {
			DatabaseIntegrityManager.restoreFromFile(FileSystemAccessWrapper.TEMP_DIR + 
					File.separatorChar + "testBackupRestoreOfSingleAlbum.cbk");

			AlbumItemResultSet allAlbumItems = DatabaseWrapper.executeSQLQuery("SELECT * FROM Books");
			assertTrue("Resultset should not be null", allAlbumItems != null);

			int counter = 0;
			while (allAlbumItems.moveToNext()) {
				counter++;
			}

			assertTrue("Resultset should contain 3 items", counter == 3);
		} catch (DatabaseWrapperOperationException e) {
			fail("Failed on internal db error");
		}
	}

	@Test
	public void testRestoreOfMultipleAlbums() {
		testBackupOfMultipleAlbums();
		
		try {
			DatabaseIntegrityManager.restoreFromFile(FileSystemAccessWrapper.TEMP_DIR + 
					File.separatorChar + "testBackupRestoreOfMultipleAlbums.cbk");

			AlbumItemResultSet allAlbumItems = DatabaseWrapper.executeSQLQuery("SELECT * FROM Books");
			assertTrue("Resultset should not be null", allAlbumItems != null);

			int counter = 0;
			while (allAlbumItems.moveToNext()) {
				counter++;
			}

			assertTrue("Resultset should contain 3 items", counter == 3);
			allAlbumItems = DatabaseWrapper.executeSQLQuery("SELECT * FROM 'DVD Album'");
			assertTrue("Resultset should not be null", allAlbumItems != null);

			counter = 0;
			while (allAlbumItems.moveToNext()) {
				counter++;
			}

			assertTrue("Resultset should contain 2 items", counter == 2);		
			allAlbumItems = DatabaseWrapper.executeSQLQuery("SELECT * FROM 'Music Album'");
			assertTrue("Resultset should not be null", allAlbumItems != null);

			counter = 0;
			while (allAlbumItems.moveToNext()) {
				counter++;
			}

			assertTrue("Resultset should contain 0 items", counter == 0);
		} catch (DatabaseWrapperOperationException e) {
			fail("Failed on internal db error");
		}
	}

	@Test
	public void testRestoreOfTestDataAlbums() {
		try {
			assertTrue(new File(CollectorTestExecuter.PATH_TO_TEST_CBK).exists());
			DatabaseIntegrityManager.restoreFromFile(CollectorTestExecuter.PATH_TO_TEST_CBK);

			AlbumItemResultSet allAlbumItems = DatabaseWrapper.executeSQLQuery("SELECT * FROM Books");
			assertTrue("Resultset should not be null", allAlbumItems != null);

			int counter = 0;
			while (allAlbumItems.moveToNext()) {
				counter++;
			}

			assertTrue("Resultset should contain 10 items", counter == 10);
			allAlbumItems = DatabaseWrapper.executeSQLQuery("SELECT * FROM 'DVDs'");
			assertTrue("Resultset should not be null", allAlbumItems != null);

			counter = 0;
			while (allAlbumItems.moveToNext()) {
				counter++;
			}

			assertTrue("Resultset should contain 11 items", counter == 11);
		} catch (DatabaseWrapperOperationException e) {
			fail("Failed on internal db error");
		}
	}

	@Test
	public void testRestoreAndModificiationOfTestDataAlbums() {
		try {
			testRestoreOfTestDataAlbums();

			final String albumName = "Books";

			AlbumItem item = new AlbumItem(albumName);

			List<ItemField> fields = new ArrayList<ItemField>();
			fields.add( new ItemField("Book Title", FieldType.Text, "added title"));
			fields.add( new ItemField("Author", FieldType.Text, "added author"));
			fields.add( new ItemField("Purchased", FieldType.Date, new Date(System.currentTimeMillis())));
			fields.add( new ItemField("Lent to", FieldType.Text, "added person"));
			fields.add( new ItemField("Second Hand", FieldType.Option, OptionType.YES));

			item.setFields(fields);

			if (DatabaseWrapper.addNewAlbumItem(item, true) == -1) {
				fail("Album Item could not be inserted into album");
			}

			DatabaseIntegrityManager.backupToFile(FileSystemAccessWrapper.TEMP_DIR + 
					File.separatorChar + "testRestoreAndModificiationOfTestDataAlbums.cbk");
		} catch (DatabaseWrapperOperationException e) {
			fail("Failed on internal db error");
		}
	}

	@Test
	public void testRestoreOfModificiationOfTestDataAlbums() {
		try {
			DatabaseIntegrityManager.restoreFromFile(FileSystemAccessWrapper.TEMP_DIR + 
					File.separatorChar + "testRestoreAndModificiationOfTestDataAlbums.cbk");

			AlbumItemResultSet allAlbumItems = DatabaseWrapper.executeSQLQuery("SELECT * FROM Books");
			assertTrue("Resultset should not be null", allAlbumItems != null);

			int counter = 0;
			while (allAlbumItems.moveToNext()) {
				counter++;
			}

			assertTrue("Resultset should contain 11 items", counter == 11);
		} catch (DatabaseWrapperOperationException e) {
			fail("Failed on internal db error");
		}
	}
	
	/**
	 * Counts the number of items in an album. Expensive function to use. Not recommended to overuse.
	 * @param albumName The name of the album to be queried.
	 * @return The number of item in the specified album.
	 */
	public static int numberOfAlbumItems(String albumName) {
		try {
			AlbumItemResultSet resultSet = DatabaseWrapper.executeSQLQuery("SELECT * FROM " + albumName);
			int counter =0;
			
			while(resultSet.moveToNext()) {
				counter++;
			}
			
			return counter;
		} catch (DatabaseWrapperOperationException e ) {
			return -1;
		}
	}
}
