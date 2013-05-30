package collector.desktop.tests.albumitems;

import static org.junit.Assert.fail;

import java.io.File;
import java.net.URI;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


import collector.desktop.database.AlbumItem;
import collector.desktop.database.DatabaseWrapper;
import collector.desktop.database.FieldType;
import collector.desktop.database.ItemField;
import collector.desktop.database.MetaItemField;
import collector.desktop.database.OptionType;
import collector.desktop.filesystem.FileSystemAccessWrapper;
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
		resetFolderStructure();
		createBooksAlbum();
		fillBooksAlbum();
	}

	@After
	public void tearDown() throws Exception {
		resetFolderStructure();
	}

	@Test
	public void updateTextfieldOfAlbumItem() {
		AlbumItem originalAlbumItem = DatabaseWrapper.fetchAlbumItem("Books", 1);

		// Change a text field
		originalAlbumItem.getField("Book Title").setValue("updated book title");

		DatabaseWrapper.updateAlbumItem(originalAlbumItem);

		AlbumItem updatedAlbumItem = DatabaseWrapper.fetchAlbumItem("Books", 1);
		if (updatedAlbumItem == null) {
			fail("The updatedAlbumItem is unexpectatly null");
		}

		Assert.assertTrue(originalAlbumItem.getAlbumName().equals(updatedAlbumItem.getAlbumName()));
		Assert.assertTrue(originalAlbumItem.getFields().containsAll(updatedAlbumItem.getFields()));		
	}

	@Test
	public void updateNumberfieldOfAlbumItem() {
		AlbumItem originalAlbumItem = DatabaseWrapper.fetchAlbumItem("Books", 1);

		// Change a text field
		originalAlbumItem.getField("Price").setValue(42.42d);

		DatabaseWrapper.updateAlbumItem(originalAlbumItem);

		AlbumItem updatedAlbumItem = DatabaseWrapper.fetchAlbumItem("Books", 1);
		if (updatedAlbumItem == null) {
			fail("The updatedAlbumItem is unexpectatly null");
		}

		Assert.assertTrue(originalAlbumItem.getAlbumName().equals(updatedAlbumItem.getAlbumName()));
		Assert.assertTrue(originalAlbumItem.getFields().containsAll(updatedAlbumItem.getFields()));	
	}

	@Test
	public void updateDatefieldOfAlbumItem() {
		AlbumItem originalAlbumItem = DatabaseWrapper.fetchAlbumItem("Books", 1);

		// Change a text field
		originalAlbumItem.getField("Purchased").setValue(new Date(System.currentTimeMillis()));

		DatabaseWrapper.updateAlbumItem(originalAlbumItem);

		AlbumItem updatedAlbumItem = DatabaseWrapper.fetchAlbumItem("Books", 1);
		if (updatedAlbumItem == null) {
			fail("The updatedAlbumItem is unexpectatly null");
		}

		Assert.assertTrue(originalAlbumItem.getAlbumName().equals(updatedAlbumItem.getAlbumName()));
		Assert.assertTrue(originalAlbumItem.getFields().containsAll(updatedAlbumItem.getFields()));	
	}

	@Test
	public void updateYesNofieldOfAlbumItem() {
		AlbumItem originalAlbumItem = DatabaseWrapper.fetchAlbumItem("Books", 1);

		// Change a text field
		originalAlbumItem.getField("Lent out").setValue(false);

		DatabaseWrapper.updateAlbumItem(originalAlbumItem);

		AlbumItem updatedAlbumItem = DatabaseWrapper.fetchAlbumItem("Books", 1);
		if (updatedAlbumItem == null) {
			fail("The updatedAlbumItem is unexpectatly null");
		}

		Assert.assertTrue(originalAlbumItem.getAlbumName().equals(updatedAlbumItem.getAlbumName()));
		Assert.assertTrue(originalAlbumItem.getFields().containsAll(updatedAlbumItem.getFields()));		
	}

	@Test
	public void updatePicturefieldOfAlbumItem() {
		AlbumItem originalAlbumItem = DatabaseWrapper.fetchAlbumItem("Books", 1);

		// Change a text field
		ArrayList<URI> pictureList = originalAlbumItem.getField("collectorPicture").getValue();
		pictureList.add(new File(CollectorTestExecuter.PATH_TO_TEST_PIC3).toURI());
		originalAlbumItem.getField("collectorPicture").setValue(pictureList);

		DatabaseWrapper.updateAlbumItem(originalAlbumItem);

		AlbumItem updatedAlbumItem = DatabaseWrapper.fetchAlbumItem("Books", 1);
		if (updatedAlbumItem == null) {
			fail("The updatedAlbumItem is unexpectatly null");
		}
		
		final String albumPicturePath = FileSystemAccessWrapper.COLLECTOR_HOME_ALBUM_PICTURES + File.separator + albumName;
		File picFile1 = new File (albumPicturePath + File.separator + "test Pic1.png");
		File picFile2 = new File (albumPicturePath + File.separator + "test Pic2.png");
		File picFile3 = new File (albumPicturePath + File.separator + "test Pic3.png");
		ItemField referencePictureField = new ItemField("collectorPicture", FieldType.Picture, Arrays.asList(picFile1.toURI(), picFile2.toURI(), picFile3.toURI()));
		
		// remove the picture field and replace it with the correct reference field
		originalAlbumItem.removeField(originalAlbumItem.getField("collectorPicture"));
		originalAlbumItem.getFields().add(referencePictureField);

		Assert.assertTrue(originalAlbumItem.getAlbumName().equals(updatedAlbumItem.getAlbumName()));
		System.out.println("albumFields: "+originalAlbumItem.getFields());
		System.out.println("updated Fields: "+updatedAlbumItem.getFields());
		Assert.assertTrue(originalAlbumItem.getFields().containsAll(updatedAlbumItem.getFields()));
	}

	private void resetFolderStructure() {
		// Reset folder structure of the COLLECTOR HOME
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

	private void createBooksAlbum() {
		// Create Album for insertion
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


		if (DatabaseWrapper.createNewAlbum(albumName, columns, true) == false) {
			fail("Creation of album"+ albumName + "failed");
		}
	}

	private void fillBooksAlbum() {
		final String albumName = "Books";

		AlbumItem referenceAlbumItem = createReferenceAlbumItem(albumName);
		
		DatabaseWrapper.addNewAlbumItem(referenceAlbumItem, false, false);
	}

	private AlbumItem createReferenceAlbumItem(String albumName) {
		AlbumItem item = new AlbumItem(albumName);

		List<ItemField> fields = new ArrayList<ItemField>();
		fields.add( new ItemField("Book Title", FieldType.Text, "book title"));
		fields.add( new ItemField("Author", FieldType.Text, "the author"));
		fields.add( new ItemField("Purchased", FieldType.Date, new Date(System.currentTimeMillis())));
		fields.add( new ItemField("Price", FieldType.Number, 4.2d));
//		fields.add( new ItemField("Lent out", FieldType.Yes_No, true));//TODO: check 
		fields.add( new ItemField("Lent out", FieldType.Option, OptionType.Yes));

		// Create picture field with 3 pictures
		List<URI> pictureURIs = Arrays.asList(	new File(CollectorTestExecuter.PATH_TO_TEST_PIC1).toURI(), 
				new File(CollectorTestExecuter.PATH_TO_TEST_PIC2).toURI());	

		item.setFields(fields);
		item.addField("collectorPicture", FieldType.Picture, pictureURIs);
		item.setContentVersion(UUID.randomUUID());
		return item;
	}
}
