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

package org.sammelbox.albumitems;

import junit.framework.Assert;
import org.junit.*;
import org.sammelbox.TestRunner;
import org.sammelbox.model.album.*;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseOperations;

import java.sql.Date;
import java.util.*;

import static org.junit.Assert.fail;

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
		TestRunner.resetTestHome();
		createBooksAlbum();
		fillBooksAlbum();
	}

	@After
	public void tearDown() throws Exception {
		TestRunner.resetTestHome();
	}

	@Test
	public void updateTextfieldOfAlbumItem() {
		try {
			AlbumItem originalAlbumItem = DatabaseOperations.getAlbumItem("Books", 1);
			originalAlbumItem.getField("Book Title").setValue("updated book title");
			DatabaseOperations.updateAlbumItem(originalAlbumItem);
			AlbumItem updatedAlbumItem = DatabaseOperations.getAlbumItem("Books", 1);
			
			if (updatedAlbumItem == null) {
				fail("The updatedAlbumItem is unexpectatly null");
			}
	
			Assert.assertTrue(originalAlbumItem.getAlbumName().equals(updatedAlbumItem.getAlbumName()));
			Assert.assertTrue(originalAlbumItem.getFields().containsAll(updatedAlbumItem.getFields()));		
		} catch (DatabaseWrapperOperationException e) {
			fail("updateTextfieldOfAlbumItem failed");
		}
	}

	@Test
	public void updateNumberfieldOfAlbumItem() {
		try {
			AlbumItem originalAlbumItem = DatabaseOperations.getAlbumItem("Books", 1);
			originalAlbumItem.getField("Price").setValue(42.42d);
			DatabaseOperations.updateAlbumItem(originalAlbumItem);
			AlbumItem updatedAlbumItem = DatabaseOperations.getAlbumItem("Books", 1);
			
			if (updatedAlbumItem == null) {
				fail("The updatedAlbumItem is unexpectatly null");
			}
	
			Assert.assertTrue(originalAlbumItem.getAlbumName().equals(updatedAlbumItem.getAlbumName()));
			Assert.assertTrue(originalAlbumItem.getFields().containsAll(updatedAlbumItem.getFields()));	
		} catch (DatabaseWrapperOperationException e) {
			fail("updateNumberfieldOfAlbumItem failed");
		}
	}

	@Test
	public void updateDatefieldOfAlbumItem() {
		try {
			AlbumItem originalAlbumItem = DatabaseOperations.getAlbumItem("Books", 1);
			Date currentDate = new Date(System.currentTimeMillis());
			originalAlbumItem.getField("Purchased").setValue(currentDate);
			DatabaseOperations.updateAlbumItem(originalAlbumItem);
			AlbumItem updatedAlbumItem = DatabaseOperations.getAlbumItem("Books", 1);
			
			if (updatedAlbumItem == null) {
				fail("The updatedAlbumItem is unexpectatly null");
			}
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(currentDate);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			long timeAsMillis = cal.getTimeInMillis();
			Date truncatedDate = new Date(timeAsMillis);
			originalAlbumItem.getField("Purchased").setValue(truncatedDate);
	
			Assert.assertTrue(originalAlbumItem.getAlbumName().equals(updatedAlbumItem.getAlbumName()));
			Assert.assertTrue(originalAlbumItem.getFields().containsAll(updatedAlbumItem.getFields()));	
		} catch (DatabaseWrapperOperationException e) {
			fail("updateDatefieldOfAlbumItem failed");
		}
	}

	@Test
	public void updateYesNofieldOfAlbumItem() {
		try {
			AlbumItem originalAlbumItem = DatabaseOperations.getAlbumItem("Books", 1);
			originalAlbumItem.getField("Lent out").setValue(OptionType.NO);
			DatabaseOperations.updateAlbumItem(originalAlbumItem);
			AlbumItem updatedAlbumItem = DatabaseOperations.getAlbumItem("Books", 1);
			
			if (updatedAlbumItem == null) {
				fail("The updatedAlbumItem is unexpectatly null");
			}
	
			Assert.assertTrue(originalAlbumItem.getAlbumName().equals(updatedAlbumItem.getAlbumName()));
			Assert.assertTrue(originalAlbumItem.getFields().containsAll(updatedAlbumItem.getFields()));		
		} catch (DatabaseWrapperOperationException e) {
			fail("Update of Option field failed");
		}
	}

	@Test
	public void updatePicturesOfAlbumItem() {
		try {
			AlbumItem originalAlbumItem = DatabaseOperations.getAlbumItem("Books", 1);
			List<AlbumItemPicture> pictureList = originalAlbumItem.getPictures();
			pictureList.add(new AlbumItemPicture(TestRunner.PATH_TO_TEST_PICTURE_3, TestRunner.PATH_TO_TEST_PICTURE_3, "Books", 1));
			originalAlbumItem.setPictures(pictureList);
		
			DatabaseOperations.updateAlbumItem(originalAlbumItem);
			AlbumItem updatedAlbumItem = DatabaseOperations.getAlbumItem("Books", 1);
			
			if (updatedAlbumItem == null) {
				fail("The updatedAlbumItem is unexpectatly null");
			}
		} catch (DatabaseWrapperOperationException e) {
			fail("update of picture field failed");
		}
	}

	private void createBooksAlbum() {
		MetaItemField titleField = new MetaItemField("Book Title", FieldType.TEXT, true);
		MetaItemField authorField = new MetaItemField("Author", FieldType.TEXT, true);
		MetaItemField purchaseField = new MetaItemField("Purchased", FieldType.DATE, false);
		MetaItemField priceField = new MetaItemField("Price", FieldType.DECIMAL, false);
		MetaItemField lenttoField = new MetaItemField("Lent out", FieldType.OPTION, true);

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
	}

	private void fillBooksAlbum() {
		final String albumName = "Books";
		AlbumItem referenceAlbumItem = createSampleAlbumItem(albumName);
		
		try {
			DatabaseOperations.addAlbumItem(referenceAlbumItem, false);
		} catch (DatabaseWrapperOperationException e) {
			fail("fillBooksAlbum failed");
		}
	}

	private AlbumItem createSampleAlbumItem(String albumName) {
		AlbumItem item = new AlbumItem(albumName);

		List<ItemField> fields = new ArrayList<ItemField>();
		fields.add(new ItemField("Book Title", FieldType.TEXT, "book title"));
		fields.add(new ItemField("Author", FieldType.TEXT, "the author"));
		fields.add(new ItemField("Purchased", FieldType.DATE, new Date(System.currentTimeMillis())));
		fields.add(new ItemField("Price", FieldType.DECIMAL, 4.2d)); 
		fields.add(new ItemField("Lent out", FieldType.OPTION, OptionType.YES));

		List<AlbumItemPicture> albumItemPictures = new ArrayList<AlbumItemPicture>();
		albumItemPictures.add(new AlbumItemPicture(TestRunner.PATH_TO_TEST_PICTURE_1,
				TestRunner.PATH_TO_TEST_PICTURE_1, albumName, AlbumItemPicture.PICTURE_ID_UNDEFINED));
		albumItemPictures.add(new AlbumItemPicture(TestRunner.PATH_TO_TEST_PICTURE_2,
				TestRunner.PATH_TO_TEST_PICTURE_2, albumName, AlbumItemPicture.PICTURE_ID_UNDEFINED));
		albumItemPictures.add(new AlbumItemPicture(TestRunner.PATH_TO_TEST_PICTURE_3,
				TestRunner.PATH_TO_TEST_PICTURE_3, albumName, AlbumItemPicture.PICTURE_ID_UNDEFINED));
		
		item.setFields(fields);
		item.setContentVersion(UUID.randomUUID());
		
		return item;
	}
}
