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

import collector.desktop.model.album.AlbumItemResultSet;
import collector.desktop.model.album.FieldType;
import collector.desktop.model.album.MetaItemField;
import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException;
import collector.desktop.model.database.operations.DatabaseOperations;
import collector.desktop.model.database.utilities.ConnectionManager;
import collector.desktop.model.database.utilities.DatabaseStringUtilities;
import collector.desktop.tests.CollectorTestExecuter;

public class CreateAlbumTests {

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
			DatabaseOperations.createNewAlbum(albumName, columns, true);
		} catch (DatabaseWrapperOperationException e) {
			fail("Creation of album"+ albumName + "failed");
		}
		
		try {
			List<MetaItemField> albumMetaFields = DatabaseOperations.getAlbumItemFieldNamesAndTypes(albumName);
			Assert.assertTrue(albumMetaFields.containsAll(columns));
		} catch (DatabaseWrapperOperationException e) {
			fail("Creation of album" + albumName + "failed");
		}				
	}
	
	@Test
	public void testAlbumCreationWithFieldNameWithSpaces() {		
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
			fail("Creation of album"+ albumName + "failed");
		}

		try {
			AlbumItemResultSet resultSet = DatabaseOperations.executeSQLQuery("SELECT * FROM " + DatabaseStringUtilities.generateTableName(albumName));		
			Assert.assertTrue(resultSet != null && resultSet.getAlbumName().equals(albumName));
		} catch (DatabaseWrapperOperationException e) {
			fail("Creation of album"+ albumName + "failed");
		}	
	}
}
