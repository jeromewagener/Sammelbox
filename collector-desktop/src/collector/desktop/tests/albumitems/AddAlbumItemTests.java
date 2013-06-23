package collector.desktop.tests.albumitems;

import static org.junit.Assert.fail;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import collector.desktop.album.AlbumItem;
import collector.desktop.album.AlbumItem.AlbumItemPicture;
import collector.desktop.album.FieldType;
import collector.desktop.album.ItemField;
import collector.desktop.album.MetaItemField;
import collector.desktop.album.OptionType;
import collector.desktop.album.StarRating;
import collector.desktop.database.ConnectionManager;
import collector.desktop.database.DatabaseWrapper;
import collector.desktop.database.exceptions.DatabaseWrapperOperationException;
import collector.desktop.filesystem.FileSystemAccessWrapper;
import collector.desktop.tests.CollectorTestExecuter;

public class AddAlbumItemTests {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		resetFolderStructure();
		createBooksAlbum();
	}

	@After
	public void tearDown() throws Exception {
		//resetFolderStructure();
	}

	@Test
	public void addAlbumItem_SingleItemNoPicStringNoContentUpdate_insertionSucceeds() {
		final String albumName = "Books";

		AlbumItem item = new AlbumItem(albumName);

		List<ItemField> fields = new ArrayList<ItemField>();
		fields.add( new ItemField("Book Title", FieldType.Text, "book title"));
		fields.add( new ItemField("Author", FieldType.Text, "the author"));
		fields.add( new ItemField("Purchased", FieldType.Date, new Date(System.currentTimeMillis())));
		fields.add( new ItemField("Price", FieldType.Number, 4.2d));
		fields.add( new ItemField("Lent to", FieldType.Text, "some random name"));
		fields.add( new ItemField("In Stock", FieldType.Option, OptionType.NO));
		fields.add( new ItemField("Rating", FieldType.StarRating, StarRating.FiveStars));
		String url = "http://www.example.com";
		fields.add( new ItemField("Publisher Website", FieldType.URL, url));
		fields.add( new ItemField("Time Stamp", FieldType.Time, new Time(System.currentTimeMillis())));

		item.setFields(fields);
		item.setContentVersion(UUID.randomUUID());
		try {
			long newAlbumID = DatabaseWrapper.addNewAlbumItem(item, true);
			if (newAlbumID == -1) {
				fail("Album Item could not be inserted into album");
			}

			AlbumItem actualAlbumItem = DatabaseWrapper.fetchAlbumItem(albumName, newAlbumID);

			if (actualAlbumItem == null) {
				fail("Inserted album item could not be retrieved");
			}

			List<MetaItemField> metaItemFields = DatabaseWrapper.getAlbumItemFieldNamesAndTypes(albumName);
			// Get the correct quickSearchFlags from the db schema.
			for (MetaItemField metaItemField : metaItemFields) {
				for (ItemField itemField : fields) {
					if (itemField.getName().equals(metaItemField.getName())) {
						itemField.setQuickSearchable(metaItemField.isQuickSearchable());
					}
				}
			}

			Assert.assertTrue("Some of the fields of the inserted item don't have the expected values.",actualAlbumItem.getFields().containsAll(fields));
			Assert.assertEquals("The carried over content version is not matching.", actualAlbumItem.getContentVersion(), item.getContentVersion());
		} catch( DatabaseWrapperOperationException e) {
			fail("Album Item could not be inserted into album");
		}
	}

	@Test
	public void addAlbumItem_SingleItemNoPicStringWithContentUpdate_insertionSucceeds() {
		final String albumName = "Books";

		AlbumItem item = new AlbumItem(albumName);

		List<ItemField> fields = new ArrayList<ItemField>();
		fields.add( new ItemField("Book Title", FieldType.Text, "book title"));
		fields.add( new ItemField("Author", FieldType.Text, "the author"));
		fields.add( new ItemField("Purchased", FieldType.Date, new Date(System.currentTimeMillis())));
		fields.add( new ItemField("Price", FieldType.Number, 4.2d));
		fields.add( new ItemField("Lent to", FieldType.Text, "some random name"));
		fields.add( new ItemField("In Stock", FieldType.Option, OptionType.NO));
		fields.add( new ItemField("Rating", FieldType.StarRating, StarRating.FiveStars));
		String url = "http://www.example.com";
		fields.add( new ItemField("Publisher Website", FieldType.URL, url));
		fields.add( new ItemField("Time Stamp", FieldType.Time, new Time(System.currentTimeMillis())));

		item.setFields(fields);
		try {
			long newAlbumID = DatabaseWrapper.addNewAlbumItem(item, true);
			if (newAlbumID == -1) {
				fail("Album Item could not be inserted into album");
			}

			AlbumItem actualAlbumItem = DatabaseWrapper.fetchAlbumItem(albumName, newAlbumID);

			if (actualAlbumItem == null) {
				fail("Inserted album item could not be retrieved");
			}

			List<MetaItemField> metaItemFields = DatabaseWrapper.getAlbumItemFieldNamesAndTypes(albumName);
			// Get the correct quickSearchFlags from the db schema.
			for (MetaItemField metaItemField : metaItemFields) {
				for (ItemField itemField : fields) {
					if (itemField.getName().equals(metaItemField.getName())) {
						itemField.setQuickSearchable(metaItemField.isQuickSearchable());
					}
				}
			}
// FIXME repair this testcase
//			// Test if all set fields are present
//			Assert.assertTrue(actualAlbumItem.getFields().containsAll(fields));
//			// Test that picture field is empty
//			for (ItemField itemField : actualAlbumItem.getFields()) {
//				if (itemField.getName().equals("collectorPicture") && itemField.getType().equals(FieldType.Picture)) {
//					@SuppressWarnings("rawtypes")
//					ArrayList pictures =itemField.getValue();
//					Assert.assertTrue("The picture field contains values which have not been inserted.", pictures==null || pictures.isEmpty());
//				}
//			}

			Assert.assertNotNull(actualAlbumItem.getContentVersion());
		} catch( DatabaseWrapperOperationException e) {
			fail("Album Item could not be inserted into album");
		}
	}

	@Test
	public void addAlbumItem_SingleItemNoPicStringWithContentUpdateAndQuoteInFieldValue_insertionSucceeds() {
		final String albumName = "Books";

		AlbumItem item = new AlbumItem(albumName);

		List<ItemField> fields = new ArrayList<ItemField>();
		fields.add( new ItemField("Book Title", FieldType.Text, "book's title"));
		fields.add( new ItemField("Author", FieldType.Text, "the author"));
		fields.add( new ItemField("Purchased", FieldType.Date, new Date(System.currentTimeMillis())));
		fields.add( new ItemField("Price", FieldType.Number, 4.2d));
		fields.add( new ItemField("Lent to", FieldType.Text, "some random name"));
		fields.add( new ItemField("In Stock", FieldType.Option, OptionType.NO));
		fields.add( new ItemField("Rating", FieldType.StarRating, StarRating.FiveStars));
		String url = "http://www.example.com";
		fields.add( new ItemField("Publisher Website", FieldType.URL, url));
		fields.add( new ItemField("Time Stamp", FieldType.Time, new Time(System.currentTimeMillis())));

		item.setFields(fields);
		try {
			long newAlbumID = DatabaseWrapper.addNewAlbumItem(item, true);
			if (newAlbumID == -1) {
				fail("Album Item could not be inserted into album");
			}

			AlbumItem actualAlbumItem = DatabaseWrapper.fetchAlbumItem(albumName, newAlbumID);

			if (actualAlbumItem == null) {
				fail("Inserted album item could not be retrieved");
			}

			List<MetaItemField> metaItemFields = DatabaseWrapper.getAlbumItemFieldNamesAndTypes(albumName);
			// Get the correct quickSearchFlags from the db schema.
			for (MetaItemField metaItemField : metaItemFields) {
				for (ItemField itemField : fields) {
					if (itemField.getName().equals(metaItemField.getName())) {
						itemField.setQuickSearchable(metaItemField.isQuickSearchable());
					}
				}
			}

			// FIXME repair this testcase
//			// Test if all set fields are present
//			Assert.assertTrue(actualAlbumItem.getFields().containsAll(fields));
//			// Test that picture field is empty
//			for (ItemField itemField : actualAlbumItem.getFields()) {
//				if (itemField.getName().equals("collectorPicture") && itemField.getType().equals(FieldType.Picture)) {
//					@SuppressWarnings("rawtypes")
//					ArrayList pictures = itemField.getValue();
//					Assert.assertTrue("The picture field contains values which have not been inserted.", pictures==null || pictures.isEmpty());
//				}
//			}

			Assert.assertNotNull(actualAlbumItem.getContentVersion());
		} catch( DatabaseWrapperOperationException e) {
			fail("Album Item could not be inserted into album");
		}
	}

	@Test
	public void addAlbumItem_SingleItemWithPicStringWithContentUpdate_insertionSucceeds() {
		//final String albumName = "Books";

		//AlbumItem item = new AlbumItem(albumName);

		List<ItemField> fields = new ArrayList<ItemField>();
		fields.add( new ItemField("Book Title", FieldType.Text, "title"));
		fields.add( new ItemField("Author", FieldType.Text, "the author"));
		fields.add( new ItemField("Purchased", FieldType.Date, new Date(System.currentTimeMillis())));
		fields.add( new ItemField("Price", FieldType.Number, 4.2d));
		fields.add( new ItemField("Lent to", FieldType.Text, "some random name"));
		fields.add( new ItemField("In Stock", FieldType.Option, OptionType.NO));
		fields.add( new ItemField("Rating", FieldType.StarRating, StarRating.FiveStars));
		String url = "http://www.example.com";
		fields.add( new ItemField("Publisher Website", FieldType.URL, url));
		fields.add( new ItemField("Time Stamp", FieldType.Time, new Time(System.currentTimeMillis())));

		// Create picture field with 3 pictures
		List<AlbumItemPicture> pictures = new ArrayList<AlbumItemPicture>();
		
		pictures.add(new AlbumItemPicture(CollectorTestExecuter.PATH_TO_TEST_PIC1, CollectorTestExecuter.PATH_TO_TEST_PIC1));
		pictures.add(new AlbumItemPicture(CollectorTestExecuter.PATH_TO_TEST_PIC1, CollectorTestExecuter.PATH_TO_TEST_PIC1));
		pictures.add(new AlbumItemPicture(CollectorTestExecuter.PATH_TO_TEST_PIC1, CollectorTestExecuter.PATH_TO_TEST_PIC1));
		
		// FIXME repair this testcase
//		item.setFields(fields);
//		item.addField("collectorPicture", FieldType.Picture, pictures);
//		try {
//			long newAlbumID = DatabaseWrapper.addNewAlbumItem(item, false, true);
//			if (newAlbumID == -1) {
//				fail("Album Item could not be inserted into album");
//			}
//
//			AlbumItem actualAlbumItem = DatabaseWrapper.fetchAlbumItem(albumName, newAlbumID);
//
//			if (actualAlbumItem == null) {
//				fail("Inserted album item could not be retrieved");
//			}
//
//			List<MetaItemField> metaItemFields = DatabaseWrapper.getAlbumItemFieldNamesAndTypes(albumName);
//			// Get the correct quickSearchFlags from the db schema.
//			for (MetaItemField metaItemField : metaItemFields) {
//				for (ItemField itemField : fields) {
//					if (itemField.getName().equals(metaItemField.getName())) {
//						itemField.setQuickSearchable(metaItemField.isQuickSearchable());
//					}
//				}
//			}

			// Test that picture field contains the right pictures and that they are physically present
			// create reference picture Field
			//final String albumPicturePath = FileSystemAccessWrapper.COLLECTOR_HOME_ALBUM_PICTURES + File.separator + albumName;
			//File picFile1 = new File (albumPicturePath + File.separator + "test Pic1.png");
			//		Assert.assertTrue("test Pic1 at location " + picFile1 + " does not exist",picFile1.exists());
			//File picFile2 = new File (albumPicturePath + File.separator + "test Pic2.png");
			//		Assert.assertTrue("test Pic2 at location " + picFile2 + " does not exist", picFile2.exists());
			//File picFile3 = new File (albumPicturePath + File.separator + "test Pic3.png");
			//		Assert.assertTrue("test Pic3 at location " + picFile3 + " does not exist", picFile3.exists());
			
			//FIXME repair this testcase
			//ItemField referencePictureField = new ItemField("collectorPicture", FieldType.Picture, Arrays.asList(picFile1.toURI(), picFile2.toURI(), picFile3.toURI()));
			fail();
			
			// remove the picture field and replace it with the correct reference field
			//fields.remove(fields.size()-1);
			//fields.add(referencePictureField);
			//Assert.assertTrue("Actual fields do not contain all inserted fields",actualAlbumItem.getFields().containsAll(fields));

			//Assert.assertTrue("Actual fields should contain the reference Picture field",actualAlbumItem.getFields().contains(referencePictureField));
			//Assert.assertNotNull(actualAlbumItem.getContentVersion());
//		} catch( FailedDatabaseWrapperOperationException e) {
//			fail("Album Item could not be inserted into album");
//		}
	}

	private void resetFolderStructure() {
		// Reset folder structure of the COLLECTOR HOME
		try {			
			ConnectionManager.closeConnection();

			FileSystemAccessWrapper.removeCollectorHome();

			Class.forName("org.sqlite.JDBC");

			FileSystemAccessWrapper.updateCollectorFileStructure();			

			ConnectionManager.openConnection();

			FileSystemAccessWrapper.updateAlbumFileStructure(ConnectionManager.getConnection());
		} 
		catch (Exception e) {
			e.printStackTrace();
			fail("Could not open database!");
		}
	}

	private void createBooksAlbum() {
		// Create Album for insertion
		final String albumName = "Books";
		MetaItemField titleField = new MetaItemField("Book Title", FieldType.Text, true);
		MetaItemField authorField = new MetaItemField("Author", FieldType.Text, true);
		MetaItemField purchaseField = new MetaItemField("Purchased", FieldType.Date, false);
		MetaItemField priceField = new MetaItemField("Price", FieldType.Number, false);
		MetaItemField lenttoField = new MetaItemField("Lent to", FieldType.Text, false);
		MetaItemField inStock = new MetaItemField("In Stock", FieldType.Option, false);
		MetaItemField rating = new MetaItemField("Rating", FieldType.StarRating, false);
		MetaItemField publisherWebsite = new MetaItemField("Publisher Website", FieldType.URL, false);
		MetaItemField timeStamp = new MetaItemField("Time Stamp", FieldType.Time, false);

		List<MetaItemField> columns = new ArrayList<MetaItemField>();
		columns.add(titleField);
		columns.add(authorField);
		columns.add(purchaseField);
		columns.add(priceField);
		columns.add(lenttoField);
		columns.add(inStock);
		columns.add(rating);
		columns.add(publisherWebsite);
		columns.add(timeStamp);
		try {
			DatabaseWrapper.createNewAlbum(albumName, columns, true);
		} catch (DatabaseWrapperOperationException e) {
			fail("Creation of album"+ albumName + "failed");
		}
	}	
}
