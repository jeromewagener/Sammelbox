package collector.desktop.tests.savepoints;

import static org.junit.Assert.*;

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
import collector.desktop.database.DatabaseWrapper;
import collector.desktop.filesystem.FileSystemAccessWrapper;

public class CreateAndReleaseSavepoints {
	private static void resetEverything() {
		System.out.println("Reset everything");
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
	}

	@Before
	public void setUp() throws Exception {
		resetEverything();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void simpleRollbackTest() {
		final String albumName = "Books";
		MetaItemField titleField = new MetaItemField("Book Title", FieldType.Text, true);

		List<MetaItemField> columns = new ArrayList<MetaItemField>();
		columns.add(titleField);

		String savepointName = DatabaseWrapper.createSavepoint();
		Assert.assertNotNull(savepointName);

		if (DatabaseWrapper.createNewAlbum(albumName, columns, true) == false) {
			fail("Creation of album"+ albumName + "failed");
		}

		// Check that the album has been created properly
		List<MetaItemField> albumMetaFields = DatabaseWrapper.getAlbumItemFieldNamesAndTypes(albumName);		
		Assert.assertTrue(albumMetaFields.containsAll(columns));

		// Rollback
		DatabaseWrapper.rollbackToSavepoint(savepointName);

		albumMetaFields = DatabaseWrapper.getAlbumItemFieldNamesAndTypes(albumName);

		Assert.assertTrue(albumMetaFields == null || albumMetaFields.isEmpty());		
	}

	@Test
	public void nestedInnerRollbackOuterReleaseTest() {
		// ------------------ Outer album --------------------------- 
		final String outerAlbumName = "outerAlbum";
		MetaItemField titleFieldForOuterAlbum = new MetaItemField("titleFieldForOuterAlbum", FieldType.Text, true);				
		List<MetaItemField> columnsForOuterAlbum = new ArrayList<MetaItemField>();
		columnsForOuterAlbum.add(titleFieldForOuterAlbum);

		String savepointNameForOuterAlbum = DatabaseWrapper.createSavepoint();
		Assert.assertNotNull(savepointNameForOuterAlbum);

		if (DatabaseWrapper.createNewAlbum(outerAlbumName, columnsForOuterAlbum, true) == false) {
			fail("Creation of album"+ outerAlbumName + "failed");
		}

		// Check that the album has been created properly
		List<MetaItemField> albumMetaFieldsForOuterAlbum = DatabaseWrapper.getAlbumItemFieldNamesAndTypes(outerAlbumName);		
		Assert.assertTrue(albumMetaFieldsForOuterAlbum.containsAll(columnsForOuterAlbum));

		// ------------------ Inner album ---------------------------

		final String innerAlbumName = "innerAlbum";
		MetaItemField titleFieldForInnerAlbum = new MetaItemField("titleFieldForInnerAlbum", FieldType.Text, true);				
		List<MetaItemField> columnsForInnerAlbum = new ArrayList<MetaItemField>();
		columnsForInnerAlbum.add(titleFieldForInnerAlbum);

		String savepointNameForInnerAlbum = DatabaseWrapper.createSavepoint();
		Assert.assertNotNull(savepointNameForInnerAlbum);

		if (DatabaseWrapper.createNewAlbum(innerAlbumName, columnsForInnerAlbum, true) == false) {
			fail("Creation of album"+ innerAlbumName + "failed");
		}

		// Check that the album has been created properly
		List<MetaItemField> albumMetaFieldsForInnerAlbum = DatabaseWrapper.getAlbumItemFieldNamesAndTypes(innerAlbumName);		
		Assert.assertTrue(albumMetaFieldsForInnerAlbum.containsAll(columnsForInnerAlbum));

		// Rollback Inner album
		DatabaseWrapper.rollbackToSavepoint(savepointNameForInnerAlbum);

		albumMetaFieldsForInnerAlbum = DatabaseWrapper.getAlbumItemFieldNamesAndTypes(innerAlbumName);		
		Assert.assertTrue(albumMetaFieldsForInnerAlbum == null || albumMetaFieldsForInnerAlbum.isEmpty());		


		// Release outer savepoint
		DatabaseWrapper.releaseSavepoint(savepointNameForOuterAlbum);
		albumMetaFieldsForInnerAlbum = DatabaseWrapper.getAlbumItemFieldNamesAndTypes(innerAlbumName);

		albumMetaFieldsForOuterAlbum = DatabaseWrapper.getAlbumItemFieldNamesAndTypes(outerAlbumName);		
		Assert.assertTrue(albumMetaFieldsForOuterAlbum.containsAll(columnsForOuterAlbum));


	}

}
