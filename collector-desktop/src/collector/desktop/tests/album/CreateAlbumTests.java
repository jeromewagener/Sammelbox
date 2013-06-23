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
import collector.desktop.database.ConnectionManager;
import collector.desktop.database.DatabaseWrapper;
import collector.desktop.database.exceptions.DatabaseWrapperOperationException;
import collector.desktop.filesystem.FileSystemAccessWrapper;

public class CreateAlbumTests {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
				
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
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

	@Before
	public void setUp() {
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

	@After
	public void tearDown() throws Exception {
		ConnectionManager.closeConnection();
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
		
		try {
			DatabaseWrapper.createNewAlbum(albumName, columns, true);
		} catch (DatabaseWrapperOperationException e) {
			fail("Creation of album"+ albumName + "failed");
		}
		
		try {
		List<MetaItemField> albumMetaFields = DatabaseWrapper.getAlbumItemFieldNamesAndTypes(albumName);
		
		Assert.assertTrue(albumMetaFields.containsAll(columns));
		} catch (DatabaseWrapperOperationException e) {
			fail("Creation of album"+ albumName + "failed");
		}				
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

		try {
			DatabaseWrapper.createNewAlbum(albumName, columns, true);
		} catch (DatabaseWrapperOperationException e) {
			fail("Creation of album"+ albumName + "failed");
		}		
		try {
		List<MetaItemField> albumMetaFields = DatabaseWrapper.getAlbumItemFieldNamesAndTypes(albumName);
		
		Assert.assertTrue(albumMetaFields.containsAll(columns));
		}catch (DatabaseWrapperOperationException e) {
			fail(e.getMessage());
		}	
	}
	
	@Test
	public void testAlbumCreation_EmptyFieldList() {		
		final String albumName = "Books";
		List<MetaItemField> columns = new ArrayList<MetaItemField>();

		try {
			DatabaseWrapper.createNewAlbum(albumName, columns, true);
		} catch (DatabaseWrapperOperationException e) {
			fail("Creation of album"+ albumName + "failed");
		}

		try {
		AlbumItemResultSet resultSet =  DatabaseWrapper.executeSQLQuery("SELECT * FROM "+albumName);
				
		Assert.assertTrue(resultSet != null && resultSet.getAlbumName().equals(albumName));
		}catch (DatabaseWrapperOperationException e) {
			fail("Creation of album"+ albumName + "failed");
		}	
	}
}
