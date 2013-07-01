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
import collector.desktop.database.DatabaseWrapper;
import collector.desktop.database.exceptions.DatabaseWrapperOperationException;
import collector.desktop.tests.CollectorTestExecuter;

public class RemoveAlbumTests {
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		CollectorTestExecuter.resetEverything();

		// Create Album to delete
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
		}catch (DatabaseWrapperOperationException e) {
			fail("Creation of album" + albumName + " failed");
		}	
	}

	@After
	public void tearDown() throws Exception {
		CollectorTestExecuter.resetEverything();
	}

	@Test
	public void testRemovalOfExistingAlbum() {
		final String albumName = "Books";
		
		try {
			DatabaseWrapper.removeAlbum(albumName);		
		} catch (DatabaseWrapperOperationException e) {
			fail ("Could not remove album: " + albumName);
		}
		
		try {
			List<String> albums = DatabaseWrapper.listAllAlbums();
			Assert.assertFalse(albums.contains(albumName));
		} catch (DatabaseWrapperOperationException e) {
			fail("Could not fetch album: " + albumName);
		}
	}
}
