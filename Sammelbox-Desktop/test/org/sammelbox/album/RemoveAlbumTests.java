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

package org.sammelbox.album;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sammelbox.TestExecuter;
import org.sammelbox.model.album.FieldType;
import org.sammelbox.model.album.MetaItemField;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseOperations;

public class RemoveAlbumTests {
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		TestExecuter.resetTestHome();

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
		TestExecuter.resetTestHome();
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
