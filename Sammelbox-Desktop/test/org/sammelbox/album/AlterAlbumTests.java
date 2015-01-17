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

import org.junit.*;
import org.sammelbox.TestRunner;
import org.sammelbox.controller.filesystem.FileSystemAccessWrapper;
import org.sammelbox.controller.managers.ConnectionManager;
import org.sammelbox.controller.managers.DatabaseIntegrityManager;
import org.sammelbox.model.album.*;
import org.sammelbox.model.database.DatabaseStringUtilities;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseConstants;
import org.sammelbox.model.database.operations.DatabaseOperations;
import org.sammelbox.utilities.TestQueries;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AlterAlbumTests {
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		TestRunner.resetTestHome();
	}

	@Before
	public void setUp() {
		TestRunner.resetTestHome();
	}

	@After
	public void tearDown() throws Exception {
		ConnectionManager.closeConnection();
	}

	@Test
	public void testAddPublisherFieldToBookAlbum() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestRunner.PATH_TO_TEST_CBK);

			MetaItemField metaItemField = new MetaItemField("Publisher", FieldType.TEXT, false);
			long originalAlbumItemCount = DatabaseOperations.getNumberOfItemsInAlbum("Books");

			DatabaseOperations.appendNewAlbumField("Books", metaItemField);
			List<MetaItemField> metaDataItems = DatabaseOperations.getMetaItemFields("Books");

			assertTrue("New publisher text column should be added at the end", 
					metaDataItems.get(metaDataItems.size()-1).getName().equals("Publisher"));

			assertTrue("The album item count incorrectly changed", 
					originalAlbumItemCount == DatabaseOperations.getNumberOfItemsInAlbum("Books"));
		} catch (DatabaseWrapperOperationException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testAddQuickSearchablePublisherFieldToBookAlbum() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestRunner.PATH_TO_TEST_CBK);

			MetaItemField metaItemField = new MetaItemField("Publisher", FieldType.TEXT, true);
			long originalAlbumItemCount = DatabaseOperations.getNumberOfItemsInAlbum("Books");

			DatabaseOperations.appendNewAlbumField("Books", metaItemField);
			List<MetaItemField> metaDataItems = DatabaseOperations.getMetaItemFields("Books");

			assertTrue("New publisher text column should be added at the end", 
					metaDataItems.get(metaDataItems.size()-1).getName().equals("Publisher"));

			assertTrue("The album item count incorrectly changed", 
					originalAlbumItemCount == DatabaseOperations.getNumberOfItemsInAlbum("Books"));
		} catch (DatabaseWrapperOperationException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testAddPublisherFieldAndMoveFourUp() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestRunner.PATH_TO_TEST_CBK);
			long originalAlbumItemCount = DatabaseOperations.getNumberOfItemsInAlbum("Books");

			MetaItemField metaItemField = new MetaItemField("Publisher", FieldType.TEXT, false);

			DatabaseOperations.appendNewAlbumField("Books", metaItemField);
			List<MetaItemField> metaDataItems = DatabaseOperations.getMetaItemFields("Books");
			
			assertTrue("New publisher text column should be added at the end", 
					metaDataItems.get(metaDataItems.size()-1).getName().equals("Publisher"));

			DatabaseOperations.reorderAlbumItemField("Books", metaDataItems.get(metaDataItems.size()-1), metaDataItems.get(metaDataItems.size()-3));
			metaDataItems = DatabaseOperations.getMetaItemFields("Books");
			
			DatabaseOperations.reorderAlbumItemField("Books", metaDataItems.get(metaDataItems.size()-2), metaDataItems.get(metaDataItems.size()-4));
			metaDataItems = DatabaseOperations.getMetaItemFields("Books");
			
			DatabaseOperations.reorderAlbumItemField("Books", metaDataItems.get(metaDataItems.size()-3), metaDataItems.get(metaDataItems.size()-5));
			metaDataItems = DatabaseOperations.getMetaItemFields("Books");
			
			DatabaseOperations.reorderAlbumItemField("Books", metaDataItems.get(metaDataItems.size()-4), metaDataItems.get(metaDataItems.size()-6));
			metaDataItems = DatabaseOperations.getMetaItemFields("Books");
			
			assertTrue("New publisher text column should be at the fourth position after reordering", 
					metaDataItems.get(2).getName().equals("Publisher"));

			assertTrue("The album item count incorrectly changed", 
					originalAlbumItemCount == DatabaseOperations.getNumberOfItemsInAlbum("Books"));
		} catch (DatabaseWrapperOperationException e ) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testMoveDvdTitleToEnd() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestRunner.PATH_TO_TEST_CBK);

			List<MetaItemField> metaDataItems = DatabaseOperations.getMetaItemFields("DVDs");
			long originalAlbumItemCount = DatabaseOperations.getNumberOfItemsInAlbum("DVDs");

			assertTrue("Title text column should be at the beginning", 
					metaDataItems.get(0).getName().equals("Title"));

			DatabaseOperations.reorderAlbumItemField("DVDs", metaDataItems.get(0), metaDataItems.get(1));

			metaDataItems = DatabaseOperations.getMetaItemFields("DVDs");
			DatabaseOperations.reorderAlbumItemField("DVDs", metaDataItems.get(1), metaDataItems.get(2));

			metaDataItems = DatabaseOperations.getMetaItemFields("DVDs");
			DatabaseOperations.reorderAlbumItemField("DVDs", metaDataItems.get(2), metaDataItems.get(3));

			metaDataItems = DatabaseOperations.getMetaItemFields("DVDs");
			DatabaseOperations.reorderAlbumItemField("DVDs", metaDataItems.get(3), metaDataItems.get(4));

			metaDataItems = DatabaseOperations.getMetaItemFields("DVDs");
			DatabaseOperations.reorderAlbumItemField("DVDs", metaDataItems.get(4), metaDataItems.get(5));

			metaDataItems = DatabaseOperations.getMetaItemFields("DVDs");

			assertTrue("Title text column should be at the end", metaDataItems.get(5).getName().equals("Title"));
			assertTrue("The album item count incorrectly changed", 
					originalAlbumItemCount == DatabaseOperations.getNumberOfItemsInAlbum("DVDs"));
		} catch (DatabaseWrapperOperationException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testRenameNonQuicksearchableField() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestRunner.PATH_TO_TEST_CBK);

			List<MetaItemField> metaDataItems = DatabaseOperations.getMetaItemFields("Books");
			long originalAlbumItemCount = DatabaseOperations.getNumberOfItemsInAlbum("Books");

			assertTrue("The first column name should be 'Book Title'", metaDataItems.get(0).getName().equals("Book Title"));

			MetaItemField bookTitleField = metaDataItems.get(0);
			MetaItemField titleField = new MetaItemField("Title", FieldType.TEXT, false);

			DatabaseOperations.renameAlbumItemField("Books", bookTitleField, titleField);

			metaDataItems = DatabaseOperations.getMetaItemFields("Books");

			assertTrue("The first column name should now be 'Title'", metaDataItems.get(0).getName().equals("Title"));
			assertTrue("The album item count incorrectly changed", 
					originalAlbumItemCount == DatabaseOperations.getNumberOfItemsInAlbum("Books"));
		} catch (DatabaseWrapperOperationException e ) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testRenameQuicksearchableField() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestRunner.PATH_TO_TEST_CBK);

			List<MetaItemField> metaDataItems = DatabaseOperations.getMetaItemFields("DVDs");
			long originalAlbumItemCount = DatabaseOperations.getNumberOfItemsInAlbum("DVDs");
			
			if (!metaDataItems.get(0).getName().equals("Title")) {
				fail("The second column name should be 'Title'" );
			}

			MetaItemField dvdTitleField = metaDataItems.get(0);
			MetaItemField titleField = new MetaItemField("DVD Title", FieldType.TEXT, dvdTitleField.isQuickSearchable());

			DatabaseOperations.renameAlbumItemField("DVDs", dvdTitleField, titleField);
			metaDataItems = DatabaseOperations.getMetaItemFields("DVDs");

			assertTrue("The first column name should now be 'DVD Title'", metaDataItems.get(0).getName().equals("DVD Title"));
			assertTrue("The album item count incorrectly changed", originalAlbumItemCount == DatabaseOperations.getNumberOfItemsInAlbum("DVDs"));
		} catch (DatabaseWrapperOperationException e ) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testRenameAlbum() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestRunner.PATH_TO_TEST_CBK);

			List<String> albumList = DatabaseOperations.getListOfAllAlbums();

			assertTrue("There should be three albums present", albumList.size() == 3);
			assertTrue("There should be an album with the name 'Books'", albumList.contains("Books"));
			assertTrue("There should be an album with the name 'DVDs'", albumList.contains("DVDs"));
			assertTrue("There should be an album with the name 'Music CDs'", albumList.contains("Music CDs"));
			assertTrue("Each album should have its corresponding picture album folder",
					(new File(FileSystemAccessWrapper.getFilePathForAlbum("DVDs"))).exists());
			
			DatabaseOperations.renameAlbum("DVDs", "Movie DVDs");
			DatabaseOperations.renameAlbum("Books", "My Books");

			albumList = DatabaseOperations.getListOfAllAlbums();

			assertTrue("There should be an album with the name 'My Books'", albumList.contains("My Books"));
			assertTrue("There should be an album with the name 'Movie DVDs'", albumList.contains("Movie DVDs"));
			assertTrue("There should be an album with the name 'Music CDs'", albumList.contains("Music CDs"));
			assertTrue("Each album should have its corresponding picture album folder",
					(new File(FileSystemAccessWrapper.getFilePathForAlbum("Movie DVDs"))).exists());
		} catch (DatabaseWrapperOperationException e ) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testDeleteAuthorField() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestRunner.PATH_TO_TEST_CBK);
			List<MetaItemField> metaDataItems = DatabaseOperations.getMetaItemFields("Books");
			long originalAlbumItemCount = DatabaseOperations.getNumberOfItemsInAlbum("Books");

			assertTrue("The second column name should be 'Author'", metaDataItems.get(1).getName().equals("Author"));

			assertTrue("Picture table for Books should exist", 
					TestQueries.isDatabaseTablePresent(DatabaseStringUtilities.generatePictureTableName("Books")));
			assertTrue("Typeinfo table for Books should exist", 
					TestQueries.isDatabaseTablePresent(DatabaseStringUtilities.generateTypeInfoTableName("Books")));
			
			MetaItemField authorMetaItemField = new MetaItemField("Author", FieldType.TEXT, false);
			DatabaseOperations.removeAlbumItemField("Books", authorMetaItemField);
			metaDataItems = DatabaseOperations.getMetaItemFields("Books");

			for (MetaItemField metaItemField : metaDataItems) {
				assertTrue("The 'Author' field should no longer be present", metaItemField.getName().equals("Books") == false);
			}

			assertTrue("Picture table for Books should still exist", 
					TestQueries.isDatabaseTablePresent(DatabaseStringUtilities.generatePictureTableName("Books")));
			assertTrue("Typeinfo table for Books should still exist", 
					TestQueries.isDatabaseTablePresent(DatabaseStringUtilities.generateTypeInfoTableName("Books")));
			
			assertTrue("The album item count incorrectly changed", 
					originalAlbumItemCount == DatabaseOperations.getNumberOfItemsInAlbum("Books"));
		} catch (DatabaseWrapperOperationException e) {			
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testReplaceSinglePictureWithTwoOtherPictures() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestRunner.PATH_TO_TEST_CBK);
			AlbumItem albumItem = DatabaseOperations.getAlbumItem("DVDs", 1);
			
			assertTrue("The first DVD item should contain only one picture", albumItem.getPictures().size() == 1);
			
			List<AlbumItemPicture> pictures = new ArrayList<>();
			pictures.add(new AlbumItemPicture(
					TestRunner.PATH_TO_TEST_PICTURE_1, TestRunner.PATH_TO_TEST_PICTURE_1, "DVDs", 1));
			pictures.add(new AlbumItemPicture(
					TestRunner.PATH_TO_TEST_PICTURE_2, TestRunner.PATH_TO_TEST_PICTURE_2, "DVDs", 1));
			
			albumItem.setPictures(pictures);
			
			assertTrue("The *non* persisted DVD item should now contain two pictures", albumItem.getPictures().size() == 2);
			
			DatabaseOperations.updateAlbumItem(albumItem);
			
			AlbumItem newAlbumItem = DatabaseOperations.getAlbumItem("DVDs", 1);
			
			assertTrue("The *persisted* DVD item should now contain two pictures", newAlbumItem.getPictures().size() == 2);
		} catch (DatabaseWrapperOperationException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testRenameAlbumShouldRenameInternalTables() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestRunner.PATH_TO_TEST_CBK);
			DatabaseOperations.renameAlbum("DVDs", "My DVD Collection");
			
			if (!TestQueries.isDatabaseTablePresent("my_dvd_collection_typeinfo")) {
				fail("The typeinfo table should always be present");
			}
			
			if (!TestQueries.isDatabaseTablePresent("my_dvd_collection_pictures")) {
				fail("The picture table should always be present");
			}
		} catch (DatabaseWrapperOperationException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testPictureTableShouldAlwaysBePresent() {
		try {
			List<MetaItemField> fields = new ArrayList<>();
			
			fields.add(new MetaItemField("field1", FieldType.TEXT));
			fields.add(new MetaItemField("field2", FieldType.TEXT));
			
			DatabaseOperations.createNewAlbum("TestAlbum", fields, false);
			
			if (!TestQueries.isDatabaseTablePresent(DatabaseStringUtilities.generateTableName("TestAlbum"))) {
				fail("The picture table should always be present");
			}
		} catch (DatabaseWrapperOperationException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testTableNameTranslation() {
		try {
			List<MetaItemField> fields = new ArrayList<>();
			
			fields.add(new MetaItemField("field1", FieldType.TEXT));
			fields.add(new MetaItemField("field2", FieldType.TEXT));
			
			DatabaseOperations.createNewAlbum("Test Album 1", fields, false);
			
			if (!TestQueries.isDatabaseTablePresent("test_album_1_pictures")) {
				fail("The table name should be lower case and spaces should be replaced by underscores");
			}
		} catch (DatabaseWrapperOperationException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testPictureDisableEnable() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestRunner.PATH_TO_TEST_CBK);
			
			assertTrue("DVDs must have pictures within the test album", DatabaseOperations.isPictureAlbum("DVDs"));
			assertTrue("There must be picture records in the picture table", 
					TestQueries.getNumberOfRecordsInTable(DatabaseStringUtilities.generatePictureTableName("DVDs")) != 0);
			
			// disable pictures
			DatabaseOperations.setAlbumPictureFunctionality("DVDs", false);
			
			// picture records should be removed while picture table should remain
			assertTrue("Picture table should still exist", 
					TestQueries.isDatabaseTablePresent(DatabaseStringUtilities.generatePictureTableName("DVDs")));
			assertTrue("DVDs no longer have pictures associated", !DatabaseOperations.isPictureAlbum("DVDs"));
			assertTrue("There must be NO picture records since the picture functionality has been disabled", 
					TestQueries.getNumberOfRecordsInTable(DatabaseStringUtilities.generatePictureTableName("DVDs")) == 0);
			
			// enable pictures
			DatabaseOperations.setAlbumPictureFunctionality("DVDs", true);
			
			assertTrue("Picture table should still exist", 
					TestQueries.isDatabaseTablePresent(DatabaseStringUtilities.generatePictureTableName("DVDs")));
			assertTrue("DVDs must have pictures again", DatabaseOperations.isPictureAlbum("DVDs"));
			
			// add new pictures pictures to existing item
			AlbumItem albumItem = DatabaseOperations.getAlbumItem("DVDs", 1);
			
			List<AlbumItemPicture> albumItemPictures = new ArrayList<AlbumItemPicture>();
			albumItemPictures.add(new AlbumItemPicture(TestRunner.PATH_TO_TEST_PICTURE_1,
					TestRunner.PATH_TO_TEST_PICTURE_1, "DVDs", AlbumItemPicture.PICTURE_ID_UNDEFINED));
			albumItemPictures.add(new AlbumItemPicture(TestRunner.PATH_TO_TEST_PICTURE_2,
					TestRunner.PATH_TO_TEST_PICTURE_2, "DVDs", AlbumItemPicture.PICTURE_ID_UNDEFINED));
			albumItemPictures.add(new AlbumItemPicture(TestRunner.PATH_TO_TEST_PICTURE_3,
					TestRunner.PATH_TO_TEST_PICTURE_3, "DVDs", AlbumItemPicture.PICTURE_ID_UNDEFINED));
			
			albumItem.setPictures(albumItemPictures);
			DatabaseOperations.updateAlbumItem(albumItem);
			
			assertTrue("DVDs must have pictures once again", DatabaseOperations.isPictureAlbum("DVDs"));
			assertTrue("There should now be three pictures in the picture table", 
					TestQueries.getNumberOfRecordsInTable(DatabaseStringUtilities.generatePictureTableName("DVDs")) == 3);
		} catch (DatabaseWrapperOperationException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testAlterationsLeaveNumberOfItemsAndPicturesUnaffected() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestRunner.PATH_TO_TEST_CBK);
			
			final long numberOfItems = TestQueries.getNumberOfRecordsInTable(DatabaseStringUtilities.generateTableName("DVDs"));
			final long numberOfPictures = TestQueries.getNumberOfRecordsInTable(DatabaseStringUtilities.generatePictureTableName("DVDs"));
			
			Map<Integer, MetaItemField> metaItemFields = DatabaseOperations.getAlbumItemMetaMap("DVDs");
			MetaItemField oldField = metaItemFields.get(3);
			MetaItemField newField = new MetaItemField("Test A", FieldType.DECIMAL, false);
			DatabaseOperations.renameAlbumItemField("DVDs", oldField, newField);
			
			assertTrue("The number of items should not have changed after a field rename", 
					numberOfItems == TestQueries.getNumberOfRecordsInTable(DatabaseStringUtilities.generateTableName("DVDs")));
			assertTrue("The number of pictures should not have changed after a field rename", 
					numberOfPictures == TestQueries.getNumberOfRecordsInTable(DatabaseStringUtilities.generatePictureTableName("DVDs")));
						
			MetaItemField oscarWinningField = new MetaItemField("Oscar winning movie", FieldType.OPTION, false);
			DatabaseOperations.appendNewAlbumField("DVDs", oscarWinningField);
			
			assertTrue("The number of items should not have changed after a field has been added", 
					numberOfItems == TestQueries.getNumberOfRecordsInTable(DatabaseStringUtilities.generateTableName("DVDs")));
			assertTrue("The number of pictures should not have changed after a field has been added", 
					numberOfPictures == TestQueries.getNumberOfRecordsInTable(DatabaseStringUtilities.generatePictureTableName("DVDs")));
			
			DatabaseOperations.reorderAlbumItemField("DVDs", oscarWinningField, DatabaseOperations.getAlbumItemMetaMap("DVDs").get(1));
			
			assertTrue("The number of items should not have changed after reordering album items", 
					numberOfItems == TestQueries.getNumberOfRecordsInTable(DatabaseStringUtilities.generateTableName("DVDs")));
			assertTrue("The number of pictures should not have changed after reordering album items", 
					numberOfPictures == TestQueries.getNumberOfRecordsInTable(DatabaseStringUtilities.generatePictureTableName("DVDs")));
			
			metaItemFields = DatabaseOperations.getAlbumItemMetaMap("DVDs");			
			DatabaseOperations.removeAlbumItemField("DVDs", metaItemFields.get(1));
			
			assertTrue("The number of items should still be the same after removing a field", 
					numberOfItems == TestQueries.getNumberOfRecordsInTable(DatabaseStringUtilities.generateTableName("DVDs")));
			assertTrue("The number of pictures should still be the same after removing a field", 
					numberOfPictures == TestQueries.getNumberOfRecordsInTable(DatabaseStringUtilities.generatePictureTableName("DVDs")));
		} catch (DatabaseWrapperOperationException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testSetQuicksearableOnAlbumWithSpace() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestRunner.PATH_TO_TEST_CBK);

			Map<Integer, MetaItemField> metaItemFields = DatabaseOperations.getAlbumItemMetaMap("Music CDs");
			metaItemFields.get(2).setQuickSearchable(true);
			DatabaseOperations.updateQuickSearchable("Music CDs", metaItemFields.get(2));

			metaItemFields = DatabaseOperations.getAlbumItemMetaMap("Music CDs");
			assertTrue("The first field of the 'Music CDs' album should now be quick searchable!", metaItemFields.get(2).isQuickSearchable());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testAppendColumnsToAlbum() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestRunner.PATH_TO_TEST_CBK);

			final int initialColumnCount = DatabaseOperations.getAlbumItemMetaMap("Music CDs").size();
			
			MetaItemField price = new MetaItemField("Price", FieldType.DECIMAL);
			DatabaseOperations.appendNewAlbumField("Music CDs", price);
			
			MetaItemField releaseDate = new MetaItemField("Release Date", FieldType.DATE);
			DatabaseOperations.appendNewAlbumField("Music CDs", releaseDate);
			
			assertTrue("Music CDs should now have two additional columns", 
					DatabaseOperations.getAlbumItemMetaMap("Music CDs").size() == initialColumnCount + 2);
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testInserationOfScoreInExistingAlbum() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestRunner.PATH_TO_TEST_CBK);
			
			DatabaseOperations.getListOfAllAlbums().contains("Music CDs");
			DatabaseOperations.renameAlbum("Music CDs", "My-Music-CDs");
				
			TestQueries.isDatabaseTablePresent("my_music_cds");
			TestQueries.isDatabaseTablePresent("my_music_cds" + DatabaseConstants.PICTURE_TABLE_SUFFIX);
			TestQueries.isDatabaseTablePresent("my_music_cds" + DatabaseConstants.TYPE_INFO_SUFFIX);
			
			DatabaseOperations.getListOfAllAlbums().contains("My-Music-CDs");
		} catch (DatabaseWrapperOperationException e) {
			fail("Alteration of album failed");
		}
	}
	
	@Test
	public void testModificationAndMasterTableUpdating() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestRunner.PATH_TO_TEST_CBK);
			
			// rename
			MetaItemField oldMetaItemField = new MetaItemField("Artist", FieldType.TEXT);
			MetaItemField newMetaItemField = new MetaItemField("Artists", FieldType.TEXT);
			DatabaseOperations.renameAlbumItemField("Music CDs", oldMetaItemField, newMetaItemField);
			
			List<String> albumsFromMasterTable = DatabaseOperations.getListOfAllAlbums();
			assertTrue("There should be three albums", albumsFromMasterTable.size() == 3);
			assertTrue("Contains Music CDs", albumsFromMasterTable.contains("Music CDs"));
			assertTrue("Contains DVDs", albumsFromMasterTable.contains("DVDs"));
			assertTrue("Contains Books", albumsFromMasterTable.contains("Books"));
			
			// reorder
			DatabaseOperations.reorderAlbumItemField(
					"Music CDs", newMetaItemField, new MetaItemField(DatabaseConstants.ID_COLUMN_NAME, FieldType.ID));
			albumsFromMasterTable = DatabaseOperations.getListOfAllAlbums();
			assertTrue("There should be three albums", albumsFromMasterTable.size() == 3);
			assertTrue("Contains Music CDs", albumsFromMasterTable.contains("Music CDs"));
			assertTrue("Contains DVDs", albumsFromMasterTable.contains("DVDs"));
			assertTrue("Contains Books", albumsFromMasterTable.contains("Books"));
			
			// delete
			DatabaseOperations.removeAlbumItemField("Music CDs", newMetaItemField);
			albumsFromMasterTable = DatabaseOperations.getListOfAllAlbums();
			assertTrue("There should be three albums", albumsFromMasterTable.size() == 3);
			assertTrue("Contains Music CDs", albumsFromMasterTable.contains("Music CDs"));
			assertTrue("Contains DVDs", albumsFromMasterTable.contains("DVDs"));
			assertTrue("Contains Books", albumsFromMasterTable.contains("Books"));
		} catch (DatabaseWrapperOperationException e) {
			fail("Alteration of album failed");
		}
	}
	
	@Test
	public void testQuickSearchFlagsModificationAfterAlbumRename() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestRunner.PATH_TO_TEST_CBK);

			// Using the following search terms, only "Just for Fun" (Title) and Gamma (Author) 
			// should be returned if the according quick-searchable flags are set
			List<String> quickSearchTerms = new ArrayList<String>();
			quickSearchTerms.add("Fun");
			quickSearchTerms.add("Gamma");

			// Using the test backup, all book items should be returned as there is no quicksearchable flag set
			AlbumItemStore.reinitializeStore(DatabaseOperations.executeQuickSearch("Books", quickSearchTerms));
			assertTrue("Since no quicksearchable flag is set, no items should be returned", 
					AlbumItemStore.getAlbumItems().size() == 10);
			
			// The quicksearchable flag will be set on the title field and the album will be renamed  
			DatabaseOperations.updateQuickSearchable("Books", new MetaItemField("Book Title", FieldType.TEXT, true));
			DatabaseOperations.renameAlbum("Books", "My Books");
			
			// The quicksearch should now return 1 item
			AlbumItemStore.reinitializeStore(DatabaseOperations.executeQuickSearch("My Books", quickSearchTerms));
			assertTrue("Since the quicksearchable flag is set on the title, one item should be returned", 
					AlbumItemStore.getAlbumItems().size() == 1);
			
			// The quicksearchable flag will be set on the authors field and the album will be renamed again  
			DatabaseOperations.updateQuickSearchable("My Books", new MetaItemField("Author", FieldType.TEXT, true));
			
			// The quicksearch should now return 2 item
			AlbumItemStore.reinitializeStore(DatabaseOperations.executeQuickSearch("My Books", quickSearchTerms));
			assertTrue("Since the quicksearchable flag is set on the title and the authors, two item should be returned", 
					AlbumItemStore.getAlbumItems().size() == 2);
			
			// Rename once more, the results should not change
			DatabaseOperations.renameAlbum("My Books", "Favorite Books");
			
			// The quicksearch should still return 2 item
			AlbumItemStore.reinitializeStore(DatabaseOperations.executeQuickSearch("Favorite Books", quickSearchTerms));
			assertTrue("Since the quicksearchable flag is set on the title and the authors, two item should be returned", 
					AlbumItemStore.getAlbumItems().size() == 2);
			
		} catch (DatabaseWrapperOperationException e) {
			fail("Alteration of album failed");
		}		
	}
}
