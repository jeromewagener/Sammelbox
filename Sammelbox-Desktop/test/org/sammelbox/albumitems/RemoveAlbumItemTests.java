/** -----------------------------------------------------------------
 *    Sammelbox: Collection Manager - A free and open-source collection manager for Windows & Linux
 *    Copyright (C) 2011 Jérôme Wagener & Paul Bicheler
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ** ----------------------------------------------------------------- */

package org.sammelbox.albumitems;

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
import org.sammelbox.TestExecuter;
import org.sammelbox.model.album.AlbumItem;
import org.sammelbox.model.album.AlbumItemResultSet;
import org.sammelbox.model.album.FieldType;
import org.sammelbox.model.album.ItemField;
import org.sammelbox.model.album.MetaItemField;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseOperations;

public class RemoveAlbumItemTests {
	/** Item field name to identify the item to be deleted.*/
	private final String DVD_TITLE_FIELD_NAME = "DVD Title";
	/** Item field value to identify the item to be deleted.*/
	private final String DVD_TITLE_FIELD_VALUE = "dvd title 1";
	/** Name of the album where an item will be deleted */
	private final String DVD_ALBUM_NAME = "dvd_album";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		TestExecuter.resetEverything();
		createDVDAlbum();
		fillDVDAlbum();
	}

	@After
	public void tearDown() throws Exception {
		TestExecuter.resetEverything();
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
		} catch (DatabaseWrapperOperationException e) {
			fail("Album Item could not be inserted into album");
		}

		item = new AlbumItem(albumName);
		fields = new ArrayList<ItemField>();
		fields.add(new ItemField(DVD_TITLE_FIELD_NAME, FieldType.Text, "dvd title 2"));
		fields.add(new ItemField("Actors", FieldType.Text, "actor 2"));
		item.setFields(fields);
		
		try {
			DatabaseOperations.addAlbumItem(item, true);			
		} catch (DatabaseWrapperOperationException e) {
			fail("Album Item could not be inserted into album");
		}
	}
}
