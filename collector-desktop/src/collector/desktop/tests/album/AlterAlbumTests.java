package collector.desktop.tests.album;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import collector.desktop.model.album.FieldType;
import collector.desktop.model.album.MetaItemField;
import collector.desktop.model.database.AlbumItemResultSet;
import collector.desktop.model.database.ConnectionManager;
import collector.desktop.model.database.DatabaseIntegrityManager;
import collector.desktop.model.database.DatabaseWrapper;
import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException;
import collector.desktop.tests.CollectorTestExecuter;

public class AlterAlbumTests {
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

	@Test
	public void testAddPublisherFieldToBookAlbum() {
		try {
			DatabaseIntegrityManager.restoreFromFile(CollectorTestExecuter.PATH_TO_TEST_CBK);

			MetaItemField metaItemField = new MetaItemField("Publisher", FieldType.Text, false);
			int originalAlbumItemCount = numberOfAlbumItems("Books");

			DatabaseWrapper.appendNewAlbumField("Books", metaItemField);
			List<MetaItemField> metaDataItems = DatabaseWrapper.getAlbumItemFieldNamesAndTypes("Books");

			assertTrue("New publisher text column should be added at the end", 
					metaDataItems.get(metaDataItems.size()-1).getName().equals("Publisher"));

			assertTrue("The album item count incorrectly changed", originalAlbumItemCount == numberOfAlbumItems("Books"));
		} catch (DatabaseWrapperOperationException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testAddQuickSearchablePublisherFieldToBookAlbum() {
		try {
			DatabaseIntegrityManager.restoreFromFile(CollectorTestExecuter.PATH_TO_TEST_CBK);

			MetaItemField metaItemField = new MetaItemField("Publisher", FieldType.Text, true);
			int originalAlbumItemCount = numberOfAlbumItems("Books");

			DatabaseWrapper.appendNewAlbumField("Books", metaItemField);
			List<MetaItemField> metaDataItems = DatabaseWrapper.getAlbumItemFieldNamesAndTypes("Books");

			assertTrue("New publisher text column should be added at the end", 
					metaDataItems.get(metaDataItems.size()-1).getName().equals("Publisher"));

			assertTrue("The album item count incorrectly changed", originalAlbumItemCount == numberOfAlbumItems("Books"));
		}catch (DatabaseWrapperOperationException e ) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testAddPublisherFieldAndMoveFourUp() {
		try {
			DatabaseIntegrityManager.restoreFromFile(CollectorTestExecuter.PATH_TO_TEST_CBK);
			int originalAlbumItemCount = numberOfAlbumItems("Books");

			MetaItemField metaItemField = new MetaItemField("Publisher", FieldType.Text, false);

			DatabaseWrapper.appendNewAlbumField("Books", metaItemField);
			List<MetaItemField> metaDataItems = DatabaseWrapper.getAlbumItemFieldNamesAndTypes("Books");
			
			assertTrue("New publisher text column should be added at the end", 
					metaDataItems.get(metaDataItems.size()-1).getName().equals("Publisher"));

			DatabaseWrapper.reorderAlbumItemField("Books", metaDataItems.get(metaDataItems.size()-1), metaDataItems.get(metaDataItems.size()-3));
			metaDataItems = DatabaseWrapper.getAlbumItemFieldNamesAndTypes("Books");
			
			DatabaseWrapper.reorderAlbumItemField("Books", metaDataItems.get(metaDataItems.size()-2), metaDataItems.get(metaDataItems.size()-4));
			metaDataItems = DatabaseWrapper.getAlbumItemFieldNamesAndTypes("Books");
			
			DatabaseWrapper.reorderAlbumItemField("Books", metaDataItems.get(metaDataItems.size()-3), metaDataItems.get(metaDataItems.size()-5));
			metaDataItems = DatabaseWrapper.getAlbumItemFieldNamesAndTypes("Books");
			
			DatabaseWrapper.reorderAlbumItemField("Books", metaDataItems.get(metaDataItems.size()-4), metaDataItems.get(metaDataItems.size()-6));
			metaDataItems = DatabaseWrapper.getAlbumItemFieldNamesAndTypes("Books");
			
			assertTrue("New publisher text column should be at the fourth position after reordering", 
					metaDataItems.get(2).getName().equals("Publisher"));

			assertTrue("The album item count incorrectly changed", originalAlbumItemCount == numberOfAlbumItems("Books"));
		} catch (DatabaseWrapperOperationException e ) {
			fail(e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	public void testMoveDvdTitleToEnd() {
		try {
			DatabaseIntegrityManager.restoreFromFile(CollectorTestExecuter.PATH_TO_TEST_CBK);

			List<MetaItemField> metaDataItems = DatabaseWrapper.getAlbumItemFieldNamesAndTypes("DVDs");
			int originalAlbumItemCount = numberOfAlbumItems("DVDs");

			assertTrue("Title text column should be at the beginning", 
					metaDataItems.get(0).getName().equals("Title"));

			DatabaseWrapper.reorderAlbumItemField("DVDs", metaDataItems.get(0), metaDataItems.get(1));

			metaDataItems = DatabaseWrapper.getAlbumItemFieldNamesAndTypes("DVDs");
			DatabaseWrapper.reorderAlbumItemField("DVDs", metaDataItems.get(1), metaDataItems.get(2));

			metaDataItems = DatabaseWrapper.getAlbumItemFieldNamesAndTypes("DVDs");
			DatabaseWrapper.reorderAlbumItemField("DVDs", metaDataItems.get(2), metaDataItems.get(3));

			metaDataItems = DatabaseWrapper.getAlbumItemFieldNamesAndTypes("DVDs");
			DatabaseWrapper.reorderAlbumItemField("DVDs", metaDataItems.get(3), metaDataItems.get(4));

			metaDataItems = DatabaseWrapper.getAlbumItemFieldNamesAndTypes("DVDs");
			DatabaseWrapper.reorderAlbumItemField("DVDs", metaDataItems.get(4), metaDataItems.get(5));

			metaDataItems = DatabaseWrapper.getAlbumItemFieldNamesAndTypes("DVDs");

			assertTrue("Title text column should be at the end", 
					metaDataItems.get(5).getName().equals("Title"));
			assertTrue("The album item count incorrectly changed", originalAlbumItemCount == numberOfAlbumItems("DVDs"));
		} catch (DatabaseWrapperOperationException e ) {
			e.printStackTrace();
			fail(e.getMessage());
			
		}
	}

	@Test
	public void testRenameColumnName_NonQuicksearchableField() {
		try {
			DatabaseIntegrityManager.restoreFromFile(CollectorTestExecuter.PATH_TO_TEST_CBK);

			List<MetaItemField> metaDataItems = DatabaseWrapper.getAlbumItemFieldNamesAndTypes("Books");
			int originalAlbumItemCount = numberOfAlbumItems("Books");

			assertTrue("The first column name should be 'Book Title'", metaDataItems.get(0).getName().equals("Book Title"));

			MetaItemField bookTitleField = metaDataItems.get(0);
			MetaItemField titleField = new MetaItemField("Title", FieldType.Text, false);

			DatabaseWrapper.renameAlbumItemField("Books", bookTitleField, titleField);

			metaDataItems = DatabaseWrapper.getAlbumItemFieldNamesAndTypes("Books");

			assertTrue("The first column name should now be 'Title'", metaDataItems.get(0).getName().equals("Title"));
			assertTrue("The album item count incorrectly changed", originalAlbumItemCount == numberOfAlbumItems("Books"));
		} catch (DatabaseWrapperOperationException e ) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testRenameColumnName_QuicksearchableField() {
		try {
			DatabaseIntegrityManager.restoreFromFile(CollectorTestExecuter.PATH_TO_TEST_CBK);

			List<MetaItemField> metaDataItems = DatabaseWrapper.getAlbumItemFieldNamesAndTypes("DVDs");
			int originalAlbumItemCount = numberOfAlbumItems("DVDs");
			if (!metaDataItems.get(0).getName().equals("Title")) {
				fail("The second column name should be 'Title'" );
			}

			MetaItemField dvdTitleField = metaDataItems.get(0);
			MetaItemField titleField = new MetaItemField("DVD Title", FieldType.Text, dvdTitleField.isQuickSearchable());

			DatabaseWrapper.renameAlbumItemField("DVDs", dvdTitleField, titleField);
			metaDataItems = DatabaseWrapper.getAlbumItemFieldNamesAndTypes("DVDs");

			assertTrue("The first column name should now be 'DVD Title'", metaDataItems.get(0).getName().equals("DVD Title"));
			assertTrue("The album item count incorrectly changed", originalAlbumItemCount == numberOfAlbumItems("DVDs"));
		} catch (DatabaseWrapperOperationException e ) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testRenameTableName() {
		try {
			DatabaseIntegrityManager.restoreFromFile(CollectorTestExecuter.PATH_TO_TEST_CBK);

			List<String> albumList = DatabaseWrapper.listAllAlbums();

			assertTrue("There should be three albums present", albumList.size() == 3);
			assertTrue("There should be an album with the name 'Books'", albumList.contains("Books"));
			assertTrue("There should be an album with the name 'DVDs'", albumList.contains("DVDs"));
			assertTrue("There should be an album with the name 'Music CDs'", albumList.contains("Music CDs"));

			DatabaseWrapper.renameAlbum("DVDs", "Movie DVDs");
			DatabaseWrapper.renameAlbum("Books", "My Books");

			albumList = DatabaseWrapper.listAllAlbums();

			assertTrue("There should be an album with the name 'My Books'", albumList.contains("My Books"));
			assertTrue("There should be an album with the name 'Movie DVDs'", albumList.contains("Movie DVDs"));
			assertTrue("There should be an album with the name 'Music CDs'", albumList.contains("Music CDs"));
		} catch (DatabaseWrapperOperationException e ) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testDeleteAuthorField() {
		try {
			DatabaseIntegrityManager.restoreFromFile(CollectorTestExecuter.PATH_TO_TEST_CBK);
			List<MetaItemField> metaDataItems = DatabaseWrapper.getAlbumItemFieldNamesAndTypes("Books");
			int originalAlbumItemCount = numberOfAlbumItems("Books");

			assertTrue("The second column name should be 'Author'", metaDataItems.get(1).getName().equals("Author"));

			MetaItemField authorMetaItemField = new MetaItemField("Author", FieldType.Text, false);
			DatabaseWrapper.removeAlbumItemField("Books", authorMetaItemField);
			metaDataItems = DatabaseWrapper.getAlbumItemFieldNamesAndTypes("Books");

			for (MetaItemField metaItemField : metaDataItems) {
				assertTrue("The 'Author' field should no longer be present", metaItemField.getName().equals("Books") == false);
			}

			assertTrue("The album item count incorrectly changed", originalAlbumItemCount == numberOfAlbumItems("Books"));
		} catch (DatabaseWrapperOperationException e ) {			
			fail(e.getMessage());
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
			int counter = 0;
			
			while (resultSet.moveToNext()) {
				counter++;
			}
			
			return counter;
		} catch (DatabaseWrapperOperationException e) {
			return -1;
		}
	}

}
