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

package org.sammelbox.album;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sammelbox.TestExecuter;
import org.sammelbox.controller.filesystem.FileSystemLocations;
import org.sammelbox.controller.managers.ConnectionManager;
import org.sammelbox.model.album.AlbumItemResultSet;
import org.sammelbox.model.album.FieldType;
import org.sammelbox.model.album.MetaItemField;
import org.sammelbox.model.database.DatabaseStringUtilities;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseConstants;
import org.sammelbox.model.database.operations.DatabaseOperations;
import org.sammelbox.utilities.TestQueries;

public class CreateAlbumTests {

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

	@Test
	public void testBookCreation() {		
		final String albumName = "Books";
		MetaItemField titleField = new MetaItemField("Book Title", FieldType.TEXT, true);
		MetaItemField authorField = new MetaItemField("Author", FieldType.TEXT, true);
		MetaItemField purchaseField = new MetaItemField("Purchased", FieldType.DATE, false);
		MetaItemField priceField = new MetaItemField("Price", FieldType.DECIMAL, false);
		MetaItemField lenttoField = new MetaItemField("Lent to", FieldType.TEXT, false);
		
		List<MetaItemField> columns = new ArrayList<MetaItemField>();
		columns.add(titleField);
		columns.add(authorField);
		columns.add(purchaseField);
		columns.add(priceField);
		columns.add(lenttoField);
		
		try {
			DatabaseOperations.createNewAlbum(albumName, columns, false);
		} catch (DatabaseWrapperOperationException e) {
			fail("Creation of album"+ albumName + "failed");
		}
		
		try {
			List<MetaItemField> albumMetaFields = DatabaseOperations.getAlbumItemFieldNamesAndTypes(albumName);
			Assert.assertTrue(albumMetaFields.containsAll(columns));
			
			assertTrue("Picture table should always be present", 
					TestQueries.isDatabaseTablePresent(DatabaseStringUtilities.generatePictureTableName("Books")));
		} catch (DatabaseWrapperOperationException e) {
			fail("Creation of album" + albumName + "failed");
		}				
	}
	
	@Test
	public void testAlbumCreationWithFieldNameWithSpaces() {		
		final String albumName = "My Books";
		MetaItemField titleField = new MetaItemField("Book Title", FieldType.TEXT, true);
		MetaItemField authorField = new MetaItemField("Author", FieldType.TEXT, true);
		MetaItemField purchaseField = new MetaItemField("Purchased", FieldType.DATE, false);
		MetaItemField priceField = new MetaItemField("Price", FieldType.DECIMAL, false);
		MetaItemField lenttoField = new MetaItemField("Lent to", FieldType.TEXT, false);
		
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
		
		try {
			List<MetaItemField> albumMetaFields = DatabaseOperations.getAlbumItemFieldNamesAndTypes(albumName);
			
			boolean isTitleOk = false;
			boolean isAuthorOk = false;
			boolean isPurchasedOk = false;
			boolean isPriceOk = false;
			boolean isLentToOk = false;
			
			for (MetaItemField metaItemField : albumMetaFields) {
				if (metaItemField.getName().equals(titleField.getName())
					&& metaItemField.isQuickSearchable() == titleField.isQuickSearchable()) {
					isTitleOk = true;
				}
				
				if (metaItemField.getName().equals(authorField.getName())
					&& metaItemField.isQuickSearchable() == authorField.isQuickSearchable()) {
					isAuthorOk = true;
				}
				
				if (metaItemField.getName().equals(purchaseField.getName())
					&& metaItemField.isQuickSearchable() == purchaseField.isQuickSearchable()) {
					isPurchasedOk = true;
				}
				
				if (metaItemField.getName().equals(priceField.getName())
					&& metaItemField.isQuickSearchable() == priceField.isQuickSearchable()) {
					isPriceOk = true;
				}
				
				if (metaItemField.getName().equals(lenttoField.getName())
					&& metaItemField.isQuickSearchable() == lenttoField.isQuickSearchable()) {
					isLentToOk = true;
				}
			}
			
			Assert.assertTrue("All fields should have been successfully added", 
					isTitleOk && isAuthorOk && isPurchasedOk && isPriceOk && isLentToOk);
		} catch (DatabaseWrapperOperationException e) {
			fail(e.getMessage());
		}	
	}
	
	@Test
	public void testAlbumCreationWithEmptyFieldList() {		
		final String albumName = "Books";
		List<MetaItemField> columns = new ArrayList<MetaItemField>();

		try {
			DatabaseOperations.createNewAlbum(albumName, columns, true);
		} catch (DatabaseWrapperOperationException e) {
			fail("Creation of album " + albumName + " failed");
		}

		try {
			AlbumItemResultSet resultSet = DatabaseOperations.executeSQLQuery("SELECT * FROM " + DatabaseStringUtilities.generateTableName(albumName));		
			Assert.assertTrue(resultSet != null && resultSet.getAlbumName().equals(albumName));
		} catch (DatabaseWrapperOperationException e) {
			fail("Creation of album " + albumName + " failed");
		}	
	}
	
	@Test
	public void testFolderCreationForNonPictureAlbums() {		
		final String albumName = "Books";
		List<MetaItemField> columns = new ArrayList<MetaItemField>();

		try {
			DatabaseOperations.createNewAlbum(albumName, columns, false);
		} catch (DatabaseWrapperOperationException e) {
			fail("Creation of album " + albumName + " failed");
		}

		try {
			AlbumItemResultSet resultSet = DatabaseOperations.executeSQLQuery("SELECT * FROM " + DatabaseStringUtilities.generateTableName(albumName));		
			Assert.assertTrue(resultSet != null && resultSet.getAlbumName().equals(albumName));
		} catch (DatabaseWrapperOperationException e) {
			fail("Creation of album " + albumName + " failed");
		}	
		
		File bookPictureDirectory = new File(FileSystemLocations.DEFAULT_SAMMELBOX_TEST_HOME + File.separatorChar + 
				FileSystemLocations.ALBUM_PICTURES_DIR_NAME + File.separatorChar + albumName );
		
		if (!bookPictureDirectory.exists()) {
			fail("Creation of album picture dir for " + albumName + " failed");
		}
	}
	
	@Test
	public void testAlbumWithScoreCreation() {
		final String albumName = "My-Books";
		List<MetaItemField> columns = new ArrayList<MetaItemField>();
		columns.add(new MetaItemField("Book-Title", FieldType.TEXT));
		columns.add(new MetaItemField("Book-Author", FieldType.TEXT));

		try {
			DatabaseOperations.createNewAlbum(albumName, columns, false);
		} catch (DatabaseWrapperOperationException e) {
			fail("Creation of album " + albumName + " failed");
		}

		try {
			AlbumItemResultSet resultSet = DatabaseOperations.executeSQLQuery("SELECT * FROM " + DatabaseStringUtilities.generateTableName(albumName));		
			Assert.assertTrue(resultSet != null && resultSet.getAlbumName().equals(albumName));
		} catch (DatabaseWrapperOperationException e) {
			fail("Creation of album " + albumName + " failed");
		}
		
		try {
			TestQueries.isDatabaseTablePresent("my_books");
			TestQueries.isDatabaseTablePresent("my_books" + DatabaseConstants.PICTURE_TABLE_SUFFIX);
			TestQueries.isDatabaseTablePresent("my_books" + DatabaseConstants.TYPE_INFO_SUFFIX);
		} catch (DatabaseWrapperOperationException e) {
			fail("Creation of album " + albumName + " failed");
		}
	}
}
