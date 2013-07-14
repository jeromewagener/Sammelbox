package collector.desktop.tests.albumitems;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import collector.desktop.model.album.AlbumItem;
import collector.desktop.model.album.AlbumItemResultSet;
import collector.desktop.model.album.FieldType;
import collector.desktop.model.album.ItemField;
import collector.desktop.model.album.MetaItemField;
import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException;
import collector.desktop.model.database.operations.DatabaseOperations;
import collector.desktop.tests.CollectorTestExecuter;

public class RemoveAlbumItemTests {
	/** Item field name to identify the item to be deleted.*/
	private final String DVD_TITLE_FIELD_NAME = "DVD Title";
	/** Item field value to identify the item to be deleted.*/
	private final String DVD_TITLE_FIELD_VALUE = "dvd title 1";
	/** Name of the album where an item will be deleted */
	private final String DVD_ALBUM_NAME = "DVD Album";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		CollectorTestExecuter.resetEverything();
		createDVDAlbum();
		fillDVDAlbum();
	}

	@After
	public void tearDown() throws Exception {
		CollectorTestExecuter.resetEverything();
	}

	@Test
	public void removeItemFromDVDAlbum() {
		long albumItemId = -1;
		String query = "SELECT id FROM '" + DVD_ALBUM_NAME + "' " +
					   "WHERE ([" + DVD_TITLE_FIELD_NAME + "] = '" + DVD_TITLE_FIELD_VALUE + "')";
		
		try {
			AlbumItemResultSet resultSet = DatabaseOperations.executeSQLQuery(query);
			
			if (resultSet.moveToNext() == false || !resultSet.getFieldName(1).equals("id")) {
				fail("The id of the item to be deleted could not be retrieved");
			} 
			
			albumItemId  = resultSet.getFieldValue(1);
			AlbumItem albumItem = DatabaseOperations.getAlbumItem(DVD_ALBUM_NAME, albumItemId);
			DatabaseOperations.deleteAlbumItem(albumItem);						
		} catch (DatabaseWrapperOperationException e) {
			fail("Deletion of item with id: " + albumItemId + " failed!");
		}
		
		try { 
			AlbumItem item = DatabaseOperations.getAlbumItem(DVD_ALBUM_NAME, albumItemId);
			Assert.assertNull("Item should be null since it has been deleted!", item);
		} catch (DatabaseWrapperOperationException e) {
			assertTrue(true);
		}
	}

	private void createDVDAlbum() {
		final String albumName = DVD_ALBUM_NAME;

		MetaItemField DVDTitleField = new MetaItemField(DVD_TITLE_FIELD_NAME, FieldType.Text, true);
		MetaItemField actorField = new MetaItemField("Actors", FieldType.Text, true);

		List<MetaItemField> columns = new ArrayList<MetaItemField>();
		columns.add(DVDTitleField);
		columns.add(actorField);
		try {
			DatabaseOperations.createNewAlbum(albumName, columns, false);
		} catch (DatabaseWrapperOperationException e) {
			fail("Creation of album "+ albumName + " failed");
		}
	}

	private void fillDVDAlbum() {
		final String albumName = "DVD Album";

		AlbumItem item = new AlbumItem(albumName);
		List<ItemField> fields = new ArrayList<ItemField>();
		fields.add( new ItemField(DVD_TITLE_FIELD_NAME, FieldType.Text, DVD_TITLE_FIELD_VALUE));
		fields.add( new ItemField("Actors", FieldType.Text, "actor 1"));
		item.setFields(fields);
		
		try {
			DatabaseOperations.addAlbumItem(item, true);
		}catch (DatabaseWrapperOperationException e) {
			fail("Album Item could not be inserted into album");
		}

		item = new AlbumItem(albumName);
		fields = new ArrayList<ItemField>();
		fields.add( new ItemField(DVD_TITLE_FIELD_NAME, FieldType.Text, "dvd title 2"));
		fields.add( new ItemField("Actors", FieldType.Text, "actor 2"));
		item.setFields(fields);
		
		try {
			DatabaseOperations.addAlbumItem(item, true);			
		} catch (DatabaseWrapperOperationException e) {
			fail("Album Item could not be inserted into album");
		}
	}
}
