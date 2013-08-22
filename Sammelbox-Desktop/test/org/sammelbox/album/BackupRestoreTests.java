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

package org.sammelbox.album;

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
import org.sammelbox.TestExecuter;
import org.sammelbox.controller.filesystem.FileSystemLocations;
import org.sammelbox.controller.managers.ConnectionManager;
import org.sammelbox.controller.managers.DatabaseIntegrityManager;
import org.sammelbox.model.album.AlbumItem;
import org.sammelbox.model.album.AlbumItemResultSet;
import org.sammelbox.model.album.FieldType;
import org.sammelbox.model.album.ItemField;
import org.sammelbox.model.album.MetaItemField;
import org.sammelbox.model.album.OptionType;
import org.sammelbox.model.database.DatabaseStringUtilities;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseOperations;

public class BackupRestoreTests {
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
		MetaItemField priceField = new MetaItemField("Price", FieldType.Decimal, false);
		MetaItemField lenttoField = new MetaItemField("Lent to", FieldType.Text, false);

		List<MetaItemField> columns = new ArrayList<MetaItemField>();
		columns.add(titleField);
		columns.add(authorField);
		columns.add(purchaseField);
		columns.add(priceField);
		columns.add(lenttoField);

		try {
			DatabaseOperations.createNewAlbum(albumName, columns, true);			
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
			DatabaseOperations.createNewAlbum(albumName, columns, false);			
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
			DatabaseOperations.createNewAlbum(albumName, columns, true);			
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
		fields.add( new ItemField("Price", FieldType.Decimal, 4.2d));
		fields.add( new ItemField("Lent to", FieldType.Text, "some random name 1"));

		item.setFields(fields);

		try {
			DatabaseOperations.addAlbumItem(item, true);			
		} catch (DatabaseWrapperOperationException e) {
			fail("Album Item could not be inserted into album");
		}

		item = new AlbumItem(albumName);

		fields = new ArrayList<ItemField>();
		fields.add( new ItemField("Book Title", FieldType.Text, "book title 2"));
		fields.add( new ItemField("Author", FieldType.Text, "the author 2"));
		fields.add( new ItemField("Purchased", FieldType.Date, new Date(System.currentTimeMillis())));
		fields.add( new ItemField("Price", FieldType.Decimal, 4.22d));
		fields.add( new ItemField("Lent to", FieldType.Text, "some random name 2"));

		item.setFields(fields);

		try {
			DatabaseOperations.addAlbumItem(item, true);			
		} catch (DatabaseWrapperOperationException e) {
			fail("Album Item could not be inserted into album");
		}

		item = new AlbumItem(albumName);

		fields = new ArrayList<ItemField>();
		fields.add( new ItemField("Book Title", FieldType.Text, "book title 3"));
		fields.add( new ItemField("Author", FieldType.Text, "the author 3"));
		fields.add( new ItemField("Purchased", FieldType.Date, new Date(System.currentTimeMillis())));
		fields.add( new ItemField("Price", FieldType.Decimal, 4.23d));
		fields.add( new ItemField("Lent to", FieldType.Text, "some random name 3"));

		item.setFields(fields);

		try {
			DatabaseOperations.addAlbumItem(item, true);			
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
			DatabaseOperations.addAlbumItem(item, true);			
		} catch (DatabaseWrapperOperationException e) {
			fail("Album Item could not be inserted into album");
		}

		item = new AlbumItem(albumName);
		fields = new ArrayList<ItemField>();
		fields.add( new ItemField("DVD Title", FieldType.Text, "dvd title 2"));
		fields.add( new ItemField("Actors", FieldType.Text, "actor 2"));
		item.setFields(fields);

		try {
			DatabaseOperations.addAlbumItem(item, true);			
		} catch (DatabaseWrapperOperationException e) {
			fail("Album Item could not be inserted into album");
		}
	}

	@Test
	public void testBackupOfSingleAlbum() {
		createBookAlbum();
		fillBookAlbum();
		
		try {
			AlbumItemResultSet allAlbumItems = DatabaseOperations.executeSQLQuery("SELECT * FROM Books");
			assertTrue("Resultset should not be null", allAlbumItems != null);

			int counter = 0;
			while (allAlbumItems.moveToNext()) {
				counter++;
			}

			assertTrue("Resultset should contain 3 items", counter == 3);
			DatabaseIntegrityManager.backupToFile(FileSystemLocations.TEMP_DIR + 
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
			AlbumItemResultSet allAlbumItems = DatabaseOperations.executeSQLQuery("SELECT * FROM Books");
			assertTrue("Resultset should not be null", allAlbumItems != null);

			int counter = 0;
			while (allAlbumItems.moveToNext()) {
				counter++;
			}

			assertTrue("Resultset should contain 3 items", counter == 3);
			allAlbumItems = DatabaseOperations.executeSQLQuery("SELECT * FROM" + 
					DatabaseStringUtilities.encloseNameWithQuotes(DatabaseStringUtilities.generateTableName("DVD Album")));
			assertTrue("Resultset should not be null", allAlbumItems != null);

			counter = 0;
			while (allAlbumItems.moveToNext()) {
				counter++;
			}

			assertTrue("Resultset should contain 2 items", counter == 2);		
			allAlbumItems = DatabaseOperations.executeSQLQuery("SELECT * FROM" + 
					DatabaseStringUtilities.encloseNameWithQuotes(DatabaseStringUtilities.generateTableName("Music Album")));
			assertTrue("Resultset should not be null", allAlbumItems != null);

			counter = 0;
			while (allAlbumItems.moveToNext()) {
				counter++;
			}

			assertTrue("Resultset should contain 0 items", counter == 0);	
			DatabaseIntegrityManager.backupToFile(FileSystemLocations.TEMP_DIR + 
					File.separatorChar + "testBackupRestoreOfMultipleAlbums.cbk");
		} catch (DatabaseWrapperOperationException e) {
			fail("Failed on internal db error");			
		}
	}

	@Test
	public void testRestoreOfSingleAlbum() {
		testBackupOfSingleAlbum();
		try {
			DatabaseIntegrityManager.restoreFromFile(FileSystemLocations.TEMP_DIR + 
					File.separatorChar + "testBackupRestoreOfSingleAlbum.cbk");

			AlbumItemResultSet allAlbumItems = DatabaseOperations.executeSQLQuery("SELECT * FROM Books");
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
			DatabaseIntegrityManager.restoreFromFile(FileSystemLocations.TEMP_DIR + 
					File.separatorChar + "testBackupRestoreOfMultipleAlbums.cbk");

			AlbumItemResultSet allAlbumItems = DatabaseOperations.executeSQLQuery("SELECT * FROM " + 
					DatabaseStringUtilities.encloseNameWithQuotes(DatabaseStringUtilities.generateTableName("Books")));
			
			assertTrue("Resultset should not be null", allAlbumItems != null);

			int counter = 0;
			while (allAlbumItems.moveToNext()) {
				counter++;
			}

			assertTrue("Resultset should contain 3 items", counter == 3);
			allAlbumItems = DatabaseOperations.executeSQLQuery("SELECT * FROM " +
					DatabaseStringUtilities.encloseNameWithQuotes(DatabaseStringUtilities.generateTableName("DVD Album")));
			assertTrue("Resultset should not be null", allAlbumItems != null);

			counter = 0;
			while (allAlbumItems.moveToNext()) {
				counter++;
			}

			assertTrue("Resultset should contain 2 items", counter == 2);		
			allAlbumItems = DatabaseOperations.executeSQLQuery("SELECT * FROM " +
					DatabaseStringUtilities.encloseNameWithQuotes(DatabaseStringUtilities.generateTableName("Music Album")));
			assertTrue("Resultset should not be null", allAlbumItems != null);

			counter = 0;
			while (allAlbumItems.moveToNext()) {
				counter++;
			}

			assertTrue("Resultset should contain 0 items", counter == 0);
		} catch (DatabaseWrapperOperationException e) {
			fail("Failed on internal database error");
		}
	}

	@Test
	public void testRestoreOfTestDataAlbums() {
		try {
			assertTrue(new File(TestExecuter.PATH_TO_TEST_CBK).exists());
			DatabaseIntegrityManager.restoreFromFile(TestExecuter.PATH_TO_TEST_CBK);

			AlbumItemResultSet allAlbumItems = DatabaseOperations.executeSQLQuery("SELECT * FROM Books");
			assertTrue("Resultset should not be null", allAlbumItems != null);

			int counter = 0;
			while (allAlbumItems.moveToNext()) {
				counter++;
			}

			assertTrue("Resultset should contain 10 items", counter == 10);
			allAlbumItems = DatabaseOperations.executeSQLQuery("SELECT * FROM 'DVDs'");
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

			if (DatabaseOperations.addAlbumItem(item, true) == -1) {
				fail("Album Item could not be inserted into album");
			}

			DatabaseIntegrityManager.backupToFile(FileSystemLocations.TEMP_DIR + 
					File.separatorChar + "testRestoreAndModificiationOfTestDataAlbums.cbk");
		} catch (DatabaseWrapperOperationException e) {
			fail("Failed on internal db error");
		}
	}

	@Test
	public void testRestoreOfModificiationOfTestDataAlbums() {
		try {
			DatabaseIntegrityManager.restoreFromFile(FileSystemLocations.TEMP_DIR + 
					File.separatorChar + "testRestoreAndModificiationOfTestDataAlbums.cbk");

			AlbumItemResultSet allAlbumItems = DatabaseOperations.executeSQLQuery("SELECT * FROM Books");
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
			AlbumItemResultSet resultSet = DatabaseOperations.executeSQLQuery("SELECT * FROM " + albumName);
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
