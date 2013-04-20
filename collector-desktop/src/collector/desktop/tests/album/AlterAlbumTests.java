package collector.desktop.tests.album;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


import collector.desktop.database.AlbumItemResultSet;
import collector.desktop.database.DatabaseWrapper;
import collector.desktop.database.FieldType;
import collector.desktop.database.MetaItemField;
import collector.desktop.filesystem.FileSystemAccessWrapper;
import collector.desktop.tests.CollectorTestExecuter;

public class AlterAlbumTests {
	public static void resetEverything() {
		try {			
			DatabaseWrapper.closeConnection();

			FileSystemAccessWrapper.removeCollectorHome();

			Class.forName("org.sqlite.JDBC");

			FileSystemAccessWrapper.updateCollectorFileStructure();			

			DatabaseWrapper.openConnection();

			FileSystemAccessWrapper.updateAlbumFileStructure(DatabaseWrapper.getConnection());
		} 
		catch (Exception e) {
			e.printStackTrace();
			fail("Could not open database!");
		}
	}
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		resetEverything();
	}

	@Before
	public void setUp() {
		resetEverything();
	}

	@After
	public void tearDown() throws Exception {
		DatabaseWrapper.closeConnection();
	}

	@Test
	public void testAddPublisherFieldToBookAlbum() {
		// Use default test sample
		DatabaseWrapper.restoreFromFile(CollectorTestExecuter.PATH_TO_TEST_CBK);
		
		MetaItemField metaItemField = new MetaItemField("Publisher", FieldType.Text, false);
		int originalAlbumItemCount = numberOfAlbumItems("Books");

		DatabaseWrapper.appendNewAlbumFields("Books", metaItemField);
		
		List<MetaItemField> metaDataItems = DatabaseWrapper.getAlbumItemFieldNamesAndTypes("Books");
		
		assertTrue("New publisher text column should be added at the end", 
				metaDataItems.get(metaDataItems.size()-1).getName().equals("Publisher"));
		
		assertTrue("The album item count incorrectly changed", originalAlbumItemCount == numberOfAlbumItems("Books"));
	}
	
	@Test
	public void testAddQuickSearchablePublisherFieldToBookAlbum() {
		// Use default test sample
		DatabaseWrapper.restoreFromFile(CollectorTestExecuter.PATH_TO_TEST_CBK);
		
		MetaItemField metaItemField = new MetaItemField("Publisher", FieldType.Text, true);
		int originalAlbumItemCount = numberOfAlbumItems("Books");

		DatabaseWrapper.appendNewAlbumFields("Books", metaItemField);
		
		List<MetaItemField> metaDataItems = DatabaseWrapper.getAlbumItemFieldNamesAndTypes("Books");
		
		assertTrue("New publisher text column should be added at the end", 
				metaDataItems.get(metaDataItems.size()-1).getName().equals("Publisher"));
		
		assertTrue("The album item count incorrectly changed", originalAlbumItemCount == numberOfAlbumItems("Books"));
	}
	
	@Test
	public void testAddPublisherFieldAndMoveFourUp() {
		// Use default test sample
		DatabaseWrapper.restoreFromFile(CollectorTestExecuter.PATH_TO_TEST_CBK);
		int originalAlbumItemCount = numberOfAlbumItems("Books");

		MetaItemField metaItemField = new MetaItemField("Publisher", FieldType.Text, false);
		
		DatabaseWrapper.appendNewAlbumFields("Books", metaItemField);
		
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
	}
	
	@Test
	public void testMoveDvdTitleToEnd() {
		// Use default test sample
		DatabaseWrapper.restoreFromFile(CollectorTestExecuter.PATH_TO_TEST_CBK);

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
		
		for (MetaItemField mdi : metaDataItems) {
			System.err.println("~~> " + mdi.getName());
		}		
		
		assertTrue("Title text column should be at the end", 
				metaDataItems.get(5).getName().equals("Title"));
		assertTrue("The album item count incorrectly changed", originalAlbumItemCount == numberOfAlbumItems("DVDs"));

	}
	
	@Test
	public void testRenameColumnName_NonQuicksearchableField() {
		// Use default test sample
		DatabaseWrapper.restoreFromFile(CollectorTestExecuter.PATH_TO_TEST_CBK);
		
		List<MetaItemField> metaDataItems = DatabaseWrapper.getAlbumItemFieldNamesAndTypes("Books");
		int originalAlbumItemCount = numberOfAlbumItems("Books");

		assertTrue("The first column name should be 'Book Title'", metaDataItems.get(0).getName().equals("Book Title"));
		
		MetaItemField bookTitleField = metaDataItems.get(0);
		MetaItemField titleField = new MetaItemField("Title", FieldType.Text, false);
		
		DatabaseWrapper.renameAlbumItemField("Books", bookTitleField, titleField);
		
		metaDataItems = DatabaseWrapper.getAlbumItemFieldNamesAndTypes("Books");
		
		assertTrue("The first column name should now be 'Title'", metaDataItems.get(0).getName().equals("Title"));
		assertTrue("The album item count incorrectly changed", originalAlbumItemCount == numberOfAlbumItems("Books"));
	}
	
	@Test
	public void testRenameColumnName_QuicksearchableField() {
		// Use default test sample
		DatabaseWrapper.restoreFromFile(CollectorTestExecuter.PATH_TO_TEST_CBK);
		
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
	}
	
	@Test
	public void testRenameTableName() {
		// Use default test sample
		DatabaseWrapper.restoreFromFile(CollectorTestExecuter.PATH_TO_TEST_CBK);
		
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
	}
		
	@Test
	public void testDeleteActorsFieldWithAlternativeMetaItemConstructor() {
		// Use default test sample
		DatabaseWrapper.restoreFromFile(CollectorTestExecuter.PATH_TO_TEST_CBK);
		
		List<MetaItemField> metaDataItems = DatabaseWrapper.getAlbumItemFieldNamesAndTypes("DVDs");
		int originalAlbumItemCount = numberOfAlbumItems("DVDs");
		
		if (!metaDataItems.get(1).getName().equals("Actors")) {
			fail("The second column name should be 'Actors'" );
		}
		
		MetaItemField actorsMetaItemField = new MetaItemField("Actors", FieldType.Text, metaDataItems.get(1).isQuickSearchable());
		DatabaseWrapper.removeAlbumItemField("DVDs", actorsMetaItemField);
		
		metaDataItems = DatabaseWrapper.getAlbumItemFieldNamesAndTypes("DVDs");
		
		for (MetaItemField metaItemField : metaDataItems) {
			assertTrue("The actors field should no longer be present", metaItemField.getName().equals("Actors") == false);
		}
		
		assertTrue("The album item count incorrectly changed", originalAlbumItemCount == numberOfAlbumItems("DVDs"));
	}
	
	@Test
	public void testDeleteAuthorField() {
		// Use default test sample
		DatabaseWrapper.restoreFromFile(CollectorTestExecuter.PATH_TO_TEST_CBK);
		
		List<MetaItemField> metaDataItems = DatabaseWrapper.getAlbumItemFieldNamesAndTypes("Books");
		int originalAlbumItemCount = numberOfAlbumItems("Books");

		assertTrue("The second column name should be 'Author'", metaDataItems.get(1).getName().equals("Author"));
		
		MetaItemField authorMetaItemField = new MetaItemField("Author", FieldType.Text, false);
		
		assertTrue("DatabaseWrapper should be able to remove the 'Author' field", 
				DatabaseWrapper.removeAlbumItemField("Books", authorMetaItemField) == true);
		
		metaDataItems = DatabaseWrapper.getAlbumItemFieldNamesAndTypes("Books");
		
		for (MetaItemField metaItemField : metaDataItems) {
			assertTrue("The 'Author' field should no longer be present", metaItemField.getName().equals("Books") == false);
		}
		
		assertTrue("The album item count incorrectly changed", originalAlbumItemCount == numberOfAlbumItems("Books"));
	}
	
	/**
	 * Counts the number of items in an album. Expensive function to use. Not recommended to overuse.
	 * @param albumName The name of the album to be queried.
	 * @return The number of item in the specified album.
	 */
	public static int numberOfAlbumItems(String albumName) {
		AlbumItemResultSet resultSet = DatabaseWrapper.executeSQLQuery("SELECT * FROM " + albumName);
		int counter =0;
		while(resultSet.moveToNext()) {
			counter++;
		}
		return counter;		
	}
}
