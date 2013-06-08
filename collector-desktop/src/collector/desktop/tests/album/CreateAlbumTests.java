package collector.desktop.tests.album;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import collector.desktop.album.FieldType;
import collector.desktop.album.MetaItemField;
import collector.desktop.database.AlbumItemResultSet;
import collector.desktop.database.DatabaseWrapper;
import collector.desktop.filesystem.FileSystemAccessWrapper;

public class CreateAlbumTests {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
				
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
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

	@Before
	public void setUp() {
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

	@After
	public void tearDown() throws Exception {
		DatabaseWrapper.closeConnection();
	}

	@Test
	public void testBookCreation() {		
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
		
		
		if (DatabaseWrapper.createNewAlbum(albumName, columns, true) == false) {
			fail("Creation of album"+ albumName + "failed");
		}
		
		List<MetaItemField> albumMetaFields = DatabaseWrapper.getAlbumItemFieldNamesAndTypes(albumName);
		
		Assert.assertTrue(albumMetaFields.containsAll(columns));
		
	}
	
	@Test
	public void testAlbumCreation_TitleWithSpaces() {		
		final String albumName = "My Books";
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
		
		if (DatabaseWrapper.createNewAlbum(albumName, columns, true) == false) {
			fail("Creation of album"+ albumName + "failed");
		}
		
		List<MetaItemField> albumMetaFields = DatabaseWrapper.getAlbumItemFieldNamesAndTypes(albumName);
		
		Assert.assertTrue(albumMetaFields.containsAll(columns));
	}
	
	@Test
	public void testAlbumCreation_EmptyFieldList() {		
		final String albumName = "Books";
		List<MetaItemField> columns = new ArrayList<MetaItemField>();

		if (DatabaseWrapper.createNewAlbum(albumName, columns, true) == false) {
			fail("Creation of album"+ albumName + "failed");
		}

		AlbumItemResultSet resultSet =  DatabaseWrapper.executeSQLQuery("SELECT * FROM "+albumName);
				
		Assert.assertTrue(resultSet != null && resultSet.getAlbumName().equals(albumName));
	}
}
