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

import collector.desktop.model.album.AlbumItem;
import collector.desktop.model.album.AlbumItemPicture;
import collector.desktop.model.album.FieldType;
import collector.desktop.model.album.ItemField;
import collector.desktop.model.album.MetaItemField;
import collector.desktop.model.album.OptionType;
import collector.desktop.model.album.StarRating;
import collector.desktop.model.database.DatabaseFacade;
import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException;
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
		CollectorTestExecuter.resetEverything();
		createBooksAlbum();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAlbumItemAdditionWithoutContentUpdate() { 
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
			long newAlbumID = DatabaseFacade.addNewAlbumItem(item, false);
			if (newAlbumID == -1) {
				fail("Album Item could not be inserted into album");
			}

			AlbumItem actualAlbumItem = DatabaseFacade.fetchAlbumItem(albumName, newAlbumID);

			if (actualAlbumItem == null) {
				fail("Inserted album item could not be retrieved");
			}

			List<MetaItemField> metaItemFields = DatabaseFacade.getAlbumItemFieldNamesAndTypes(albumName);

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
	public void testAlbumItemAdditionWithContentUpdate() {
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
			long newAlbumID = DatabaseFacade.addNewAlbumItem(item, true);
			if (newAlbumID == -1) {
				fail("Album Item could not be inserted into album");
			}

			AlbumItem actualAlbumItem = DatabaseFacade.fetchAlbumItem(albumName, newAlbumID);
			if (actualAlbumItem == null) {
				fail("Inserted album item could not be retrieved");
			}

			List<MetaItemField> metaItemFields = DatabaseFacade.getAlbumItemFieldNamesAndTypes(albumName);

			for (MetaItemField metaItemField : metaItemFields) {
				for (ItemField itemField : fields) {
					if (itemField.getName().equals(metaItemField.getName())) {
						itemField.setQuickSearchable(metaItemField.isQuickSearchable());
					}
				}
			}

			Assert.assertTrue(actualAlbumItem.getFields().containsAll(fields));
			List<AlbumItemPicture> pictures = actualAlbumItem.getPictures();
			Assert.assertTrue("The picture field contains values which have not been inserted.", pictures==null || pictures.isEmpty());
			Assert.assertNotNull(actualAlbumItem.getContentVersion());
		} catch( DatabaseWrapperOperationException e) {
			fail("Album Item could not be inserted into album");
		}
	}

	@Test
	public void testAlbumItemAdditionWithContentUpdateAndQuoteInValue() {
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
			long newAlbumID = DatabaseFacade.addNewAlbumItem(item, true);
			if (newAlbumID == -1) {
				fail("Album Item could not be inserted into album");
			}

			AlbumItem actualAlbumItem = DatabaseFacade.fetchAlbumItem(albumName, newAlbumID);
			if (actualAlbumItem == null) {
				fail("Inserted album item could not be retrieved");
			}

			List<MetaItemField> metaItemFields = DatabaseFacade.getAlbumItemFieldNamesAndTypes(albumName);
			for (MetaItemField metaItemField : metaItemFields) {
				for (ItemField itemField : fields) {
					if (itemField.getName().equals(metaItemField.getName())) {
						itemField.setQuickSearchable(metaItemField.isQuickSearchable());
					}
				}
			}

			Assert.assertTrue(actualAlbumItem.getFields().containsAll(fields));
			List<AlbumItemPicture> pictures = actualAlbumItem.getPictures();
			Assert.assertTrue("The picture field contains values which have not been inserted.", pictures==null || pictures.isEmpty());
			Assert.assertNotNull(actualAlbumItem.getContentVersion());
		} catch( DatabaseWrapperOperationException e) {
			fail("Album Item could not be inserted into album");
		}
	}

	private void createBooksAlbum() {
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
			DatabaseFacade.createNewAlbum(albumName, columns, true);
		} catch (DatabaseWrapperOperationException e) {
			fail("Creation of album"+ albumName + "failed");
		}
	}	
}
