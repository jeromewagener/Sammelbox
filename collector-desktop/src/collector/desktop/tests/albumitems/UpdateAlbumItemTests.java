package collector.desktop.tests.albumitems;

import static org.junit.Assert.fail;

import java.sql.Date;
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
import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException;
import collector.desktop.model.database.operations.DatabaseOperations;
import collector.desktop.tests.CollectorTestExecuter;

public class UpdateAlbumItemTests {
	final String albumName = "Books";

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
		fillBooksAlbum();
	}

	@After
	public void tearDown() throws Exception {
		CollectorTestExecuter.resetEverything();
	}

	@Test
	public void updateTextfieldOfAlbumItem() {
		try {
			AlbumItem originalAlbumItem = DatabaseOperations.getAlbumItem("Books", 1);
			originalAlbumItem.getField("Book Title").setValue("updated book title");
			DatabaseOperations.updateAlbumItem(originalAlbumItem);
			AlbumItem updatedAlbumItem = DatabaseOperations.getAlbumItem("Books", 1);
			
			if (updatedAlbumItem == null) {
				fail("The updatedAlbumItem is unexpectatly null");
			}
	
			Assert.assertTrue(originalAlbumItem.getAlbumName().equals(updatedAlbumItem.getAlbumName()));
			Assert.assertTrue(originalAlbumItem.getFields().containsAll(updatedAlbumItem.getFields()));		
		} catch (DatabaseWrapperOperationException e) {
			fail("updateTextfieldOfAlbumItem failed");
		}
	}

	@Test
	public void updateNumberfieldOfAlbumItem() {
		try {
			AlbumItem originalAlbumItem = DatabaseOperations.getAlbumItem("Books", 1);
			originalAlbumItem.getField("Price").setValue(42.42d);
			DatabaseOperations.updateAlbumItem(originalAlbumItem);
			AlbumItem updatedAlbumItem = DatabaseOperations.getAlbumItem("Books", 1);
			
			if (updatedAlbumItem == null) {
				fail("The updatedAlbumItem is unexpectatly null");
			}
	
			Assert.assertTrue(originalAlbumItem.getAlbumName().equals(updatedAlbumItem.getAlbumName()));
			Assert.assertTrue(originalAlbumItem.getFields().containsAll(updatedAlbumItem.getFields()));	
		} catch (DatabaseWrapperOperationException e) {
			fail("updateNumberfieldOfAlbumItem failed");
		}
	}

	@Test
	public void updateDatefieldOfAlbumItem() {
		try {
			AlbumItem originalAlbumItem = DatabaseOperations.getAlbumItem("Books", 1);
			originalAlbumItem.getField("Purchased").setValue(new Date(System.currentTimeMillis()));
			DatabaseOperations.updateAlbumItem(originalAlbumItem);
			AlbumItem updatedAlbumItem = DatabaseOperations.getAlbumItem("Books", 1);
			
			if (updatedAlbumItem == null) {
				fail("The updatedAlbumItem is unexpectatly null");
			}
	
			Assert.assertTrue(originalAlbumItem.getAlbumName().equals(updatedAlbumItem.getAlbumName()));
			Assert.assertTrue(originalAlbumItem.getFields().containsAll(updatedAlbumItem.getFields()));	
		} catch (DatabaseWrapperOperationException e) {
			fail("updateDatefieldOfAlbumItem failed");
		}
	}

	@Test
	public void updateYesNofieldOfAlbumItem() {
		try {
			AlbumItem originalAlbumItem = DatabaseOperations.getAlbumItem("Books", 1);
			originalAlbumItem.getField("Lent out").setValue(OptionType.NO);
			DatabaseOperations.updateAlbumItem(originalAlbumItem);
			AlbumItem updatedAlbumItem = DatabaseOperations.getAlbumItem("Books", 1);
			
			if (updatedAlbumItem == null) {
				fail("The updatedAlbumItem is unexpectatly null");
			}
	
			Assert.assertTrue(originalAlbumItem.getAlbumName().equals(updatedAlbumItem.getAlbumName()));
			Assert.assertTrue(originalAlbumItem.getFields().containsAll(updatedAlbumItem.getFields()));		
		} catch (DatabaseWrapperOperationException e) {
			fail("Update of Option field failed");
		}
	}

	@Test
	public void updatePicturesOfAlbumItem() {
		try {
			AlbumItem originalAlbumItem = DatabaseOperations.getAlbumItem("Books", 1);
			List<AlbumItemPicture> pictureList = originalAlbumItem.getPictures();
			pictureList.add(new AlbumItemPicture(CollectorTestExecuter.PATH_TO_TEST_PICTURE_3, CollectorTestExecuter.PATH_TO_TEST_PICTURE_3, "Books", 1));
			originalAlbumItem.setPictures(pictureList);
		
			DatabaseOperations.updateAlbumItem(originalAlbumItem);
			AlbumItem updatedAlbumItem = DatabaseOperations.getAlbumItem("Books", 1);
			
			if (updatedAlbumItem == null) {
				fail("The updatedAlbumItem is unexpectatly null");
			}
		} catch (DatabaseWrapperOperationException e) {
			fail("update of picture field failed");
		}
	}

	private void createBooksAlbum() {
		MetaItemField titleField = new MetaItemField("Book Title", FieldType.Text, true);
		MetaItemField authorField = new MetaItemField("Author", FieldType.Text, true);
		MetaItemField purchaseField = new MetaItemField("Purchased", FieldType.Date, false);
		MetaItemField priceField = new MetaItemField("Price", FieldType.Number, false);
		MetaItemField lenttoField = new MetaItemField("Lent out", FieldType.Option, true);

		List<MetaItemField> columns = new ArrayList<MetaItemField>();
		columns.add(titleField);
		columns.add(authorField);
		columns.add(purchaseField);
		columns.add(priceField);
		columns.add(lenttoField);

		try {
			DatabaseOperations.createNewAlbum(albumName, columns, true);
		} catch (DatabaseWrapperOperationException e) {
			fail("Creation of album"+ albumName + "failed");
		}
	}

	private void fillBooksAlbum() {
		final String albumName = "Books";
		AlbumItem referenceAlbumItem = createSampleAlbumItem(albumName);
		
		try {
			DatabaseOperations.addAlbumItem(referenceAlbumItem, false);
		} catch (DatabaseWrapperOperationException e) {
			fail("fillBooksAlbum failed");
		}
	}

	private AlbumItem createSampleAlbumItem(String albumName) {
		AlbumItem item = new AlbumItem(albumName);

		List<ItemField> fields = new ArrayList<ItemField>();
		fields.add(new ItemField("Book Title", FieldType.Text, "book title"));
		fields.add(new ItemField("Author", FieldType.Text, "the author"));
		fields.add(new ItemField("Purchased", FieldType.Date, new Date(System.currentTimeMillis())));
		fields.add(new ItemField("Price", FieldType.Number, 4.2d)); 
		fields.add(new ItemField("Lent out", FieldType.Option, OptionType.YES));

		List<AlbumItemPicture> albumItemPictures = new ArrayList<AlbumItemPicture>();
		albumItemPictures.add(new AlbumItemPicture(CollectorTestExecuter.PATH_TO_TEST_PICTURE_1, 
				CollectorTestExecuter.PATH_TO_TEST_PICTURE_1, albumName, AlbumItemPicture.PICTURE_ID_UNDEFINED));
		albumItemPictures.add(new AlbumItemPicture(CollectorTestExecuter.PATH_TO_TEST_PICTURE_2, 
				CollectorTestExecuter.PATH_TO_TEST_PICTURE_2, albumName, AlbumItemPicture.PICTURE_ID_UNDEFINED));
		albumItemPictures.add(new AlbumItemPicture(CollectorTestExecuter.PATH_TO_TEST_PICTURE_3, 
				CollectorTestExecuter.PATH_TO_TEST_PICTURE_3, albumName, AlbumItemPicture.PICTURE_ID_UNDEFINED));
		
		item.setFields(fields);
		item.setContentVersion(UUID.randomUUID());
		
		return item;
	}
}
