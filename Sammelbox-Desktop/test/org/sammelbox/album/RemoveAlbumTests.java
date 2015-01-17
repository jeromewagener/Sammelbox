/** -----------------------------------------------------------------
 *    Sammelbox: Collection Manager - A free and open-source collection manager for Windows & Linux
 *    Copyright (C) 2011 Jerome Wagener & Paul Bicheler
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

package org.sammelbox.album;

import org.junit.*;
import org.sammelbox.TestRunner;
import org.sammelbox.model.album.FieldType;
import org.sammelbox.model.album.MetaItemField;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseOperations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.fail;

public class RemoveAlbumTests {
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		TestRunner.resetTestHome();

		// Create Album to delete
		final String albumName = "Books";
		MetaItemField titleField = new MetaItemField("Book Title", FieldType.TEXT, true);
		MetaItemField authorField = new MetaItemField("Author", FieldType.TEXT, true);
		MetaItemField purchaseField = new MetaItemField("Purchased", FieldType.DATE, false);
		MetaItemField priceField = new MetaItemField("Price", FieldType.DECIMAL, false);
		MetaItemField lenttoField = new MetaItemField("Lent to", FieldType.TEXT, false);

		List<MetaItemField> columns = new ArrayList<MetaItemField>();
		columns.add(titleField);
		columns.add(authorField);
		columns.add(purchaseField);
		columns.add(priceField);
		columns.add(lenttoField);

		try {
			DatabaseOperations.createNewAlbum(albumName, columns, true);
		} catch (DatabaseWrapperOperationException e) {
			fail("Creation of album" + albumName + " failed");
		}	
	}

	@After
	public void tearDown() throws Exception {
		TestRunner.resetTestHome();
	}

	@Test
	public void testRemovalOfExistingAlbum() {
		final String albumName = "Books";
		
		try {
			DatabaseOperations.removeAlbumAndAlbumPictures(albumName);		
		} catch (DatabaseWrapperOperationException e) {
			fail ("Could not remove album: " + albumName);
		}
		
		try {
			List<String> albums = DatabaseOperations.getListOfAllAlbums();
			Assert.assertFalse(albums.contains(albumName));
		} catch (DatabaseWrapperOperationException e) {
			fail("Could not fetch album: " + albumName);
		}
	}
}
