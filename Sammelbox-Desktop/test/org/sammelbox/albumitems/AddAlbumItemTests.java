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

import static org.junit.Assert.fail;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sammelbox.TestExecuter;
import org.sammelbox.model.album.AlbumItem;
import org.sammelbox.model.album.AlbumItemPicture;
import org.sammelbox.model.album.FieldType;
import org.sammelbox.model.album.ItemField;
import org.sammelbox.model.album.MetaItemField;
import org.sammelbox.model.album.OptionType;
import org.sammelbox.model.album.StarRating;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseOperations;

public class AddAlbumItemTests {
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		TestExecuter.resetTestHome();
		createBooksAlbum();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAlbumItemAdditionWithoutContentUpdate() { 
		final String albumName = "Books";

		AlbumItem item = new AlbumItem(albumName);

		List<ItemField> fields = new ArrayList<ItemField>();
		fields.add( new ItemField("Book Title", FieldType.TEXT, "book title"));
		fields.add( new ItemField("Author", FieldType.TEXT, "the author"));
		Date currentDate = new Date(System.currentTimeMillis());
		fields.add( new ItemField("Purchased", FieldType.DATE, currentDate));
		fields.add( new ItemField("Price", FieldType.DECIMAL, 4.2d));
		fields.add( new ItemField("Lent to", FieldType.TEXT, "some random name"));
		fields.add( new ItemField("In Stock", FieldType.OPTION, OptionType.NO));
		fields.add( new ItemField("Rating", FieldType.STAR_RATING, StarRating.FIVE_STARS));
		String url = "http://www.example.com";
		fields.add( new ItemField("Publisher Website", FieldType.URL, url));
		fields.add( new ItemField("Time Stamp", FieldType.TIME, new Time(System.currentTimeMillis())));

		item.setFields(fields);
		item.setContentVersion(UUID.randomUUID());
		try {
			long newAlbumID = DatabaseOperations.addAlbumItem(item, false);
			if (newAlbumID == -1) {
				fail("Album Item could not be inserted into album");
			}

			AlbumItem actualAlbumItem = DatabaseOperations.getAlbumItem(albumName, newAlbumID);

			if (actualAlbumItem == null) {
				fail("Inserted album item could not be retrieved");
			}

			List<MetaItemField> metaItemFields = DatabaseOperations.getAlbumItemFieldNamesAndTypes(albumName);

			for (MetaItemField metaItemField : metaItemFields) {
				for (ItemField itemField : fields) {
					if (itemField.getName().equals(metaItemField.getName())) {
						itemField.setQuickSearchable(metaItemField.isQuickSearchable());
					}
				}
			}

			// Truncate date to account for the database truncating the 
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")); 
			cal.setTime(currentDate);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			long timeAsMillis = cal.getTimeInMillis();
			Date truncatedDate = new Date(timeAsMillis);
			item.getField("Purchased").setValue(truncatedDate);	

			Assert.assertTrue("Some of the fields of the inserted item don't have the expected values.",actualAlbumItem.getFields().containsAll(item.getFields()));
			Assert.assertEquals("The carried over content version is not matching.", actualAlbumItem.getContentVersion(), item.getContentVersion());
		} catch( DatabaseWrapperOperationException e) {
			fail("Album Item could not be inserted into album");
		}
	}

	@Test
	public void testAlbumItemAdditionWithContentUpdate() {
		final String albumName = "Books";

		AlbumItem item = new AlbumItem(albumName);
		List<ItemField> fields = new ArrayList<ItemField>();
		fields.add( new ItemField("Book Title", FieldType.TEXT, "book title"));
		fields.add( new ItemField("Author", FieldType.TEXT, "the author"));
		Date currentDate = new Date(System.currentTimeMillis());
		fields.add( new ItemField("Purchased", FieldType.DATE, currentDate));
		fields.add( new ItemField("Price", FieldType.DECIMAL, 4.2d));
		fields.add( new ItemField("Lent to", FieldType.TEXT, "some random name"));
		fields.add( new ItemField("In Stock", FieldType.OPTION, OptionType.NO));
		fields.add( new ItemField("Rating", FieldType.STAR_RATING, StarRating.FIVE_STARS));
		String url = "http://www.example.com";
		fields.add( new ItemField("Publisher Website", FieldType.URL, url));
		fields.add( new ItemField("Time Stamp", FieldType.TIME, new Time(System.currentTimeMillis())));
		item.setFields(fields);
		
		try {
			long newAlbumID = DatabaseOperations.addAlbumItem(item, true);
			if (newAlbumID == -1) {
				fail("Album Item could not be inserted into album");
			}

			AlbumItem actualAlbumItem = DatabaseOperations.getAlbumItem(albumName, newAlbumID);
			if (actualAlbumItem == null) {
				fail("Inserted album item could not be retrieved");
			}

			List<MetaItemField> metaItemFields = DatabaseOperations.getAlbumItemFieldNamesAndTypes(albumName);

			for (MetaItemField metaItemField : metaItemFields) {
				for (ItemField itemField : fields) {
					if (itemField.getName().equals(metaItemField.getName())) {
						itemField.setQuickSearchable(metaItemField.isQuickSearchable());
					}
				}
			}
			
			// Truncate date to account for the database truncating the 
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")); 
			cal.setTime(currentDate);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			long timeAsMillis = cal.getTimeInMillis();
			Date truncatedDate = new Date(timeAsMillis);
			item.getField("Purchased").setValue(truncatedDate);	

			Assert.assertTrue(actualAlbumItem.getFields().containsAll(item.getFields()));
			List<AlbumItemPicture> pictures = actualAlbumItem.getPictures();
			Assert.assertTrue("The picture field contains values which have not been inserted.", pictures==null || pictures.isEmpty());
			Assert.assertNotNull(actualAlbumItem.getContentVersion());
		} catch( DatabaseWrapperOperationException e) {
			fail("Album Item could not be inserted into album");
		}
	}

	@Test
	public void testAlbumItemAdditionWithContentUpdateAndQuoteInValue() {
		final String albumName = "Books";

		AlbumItem item = new AlbumItem(albumName);
		List<ItemField> fields = new ArrayList<ItemField>();
		fields.add( new ItemField("Book Title", FieldType.TEXT, "book's title"));
		fields.add( new ItemField("Author", FieldType.TEXT, "the author"));
		Date currentDate = new Date(System.currentTimeMillis());
		fields.add( new ItemField("Purchased", FieldType.DATE, currentDate));
		fields.add( new ItemField("Price", FieldType.DECIMAL, 4.2d));
		fields.add( new ItemField("Lent to", FieldType.TEXT, "some random name"));
		fields.add( new ItemField("In Stock", FieldType.OPTION, OptionType.NO));
		fields.add( new ItemField("Rating", FieldType.STAR_RATING, StarRating.FIVE_STARS));
		String url = "http://www.example.com";
		fields.add( new ItemField("Publisher Website", FieldType.URL, url));
		fields.add( new ItemField("Time Stamp", FieldType.TIME, new Time(System.currentTimeMillis())));
		item.setFields(fields);
		
		try {
			long newAlbumID = DatabaseOperations.addAlbumItem(item, true);
			if (newAlbumID == -1) {
				fail("Album Item could not be inserted into album");
			}

			AlbumItem actualAlbumItem = DatabaseOperations.getAlbumItem(albumName, newAlbumID);
			if (actualAlbumItem == null) {
				fail("Inserted album item could not be retrieved");
			}

			List<MetaItemField> metaItemFields = DatabaseOperations.getAlbumItemFieldNamesAndTypes(albumName);
			for (MetaItemField metaItemField : metaItemFields) {
				for (ItemField itemField : fields) {
					if (itemField.getName().equals(metaItemField.getName())) {
						itemField.setQuickSearchable(metaItemField.isQuickSearchable());
					}
				}
			}
			
			// Truncate date to account for the database truncating the 
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")); 
			cal.setTime(currentDate);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			long timeAsMillis = cal.getTimeInMillis();
			Date truncatedDate = new Date(timeAsMillis);
			item.getField("Purchased").setValue(truncatedDate);				
			
			Assert.assertTrue(actualAlbumItem.getFields().containsAll(item.getFields()));		
			List<AlbumItemPicture> pictures = actualAlbumItem.getPictures();
			Assert.assertTrue("The picture field contains values which have not been inserted.", pictures==null || pictures.isEmpty());
			Assert.assertNotNull(actualAlbumItem.getContentVersion());
		} catch( DatabaseWrapperOperationException e) {
			fail("Album Item could not be inserted into album");
		}
	}

	private void createBooksAlbum() {
		final String albumName = "Books";
		MetaItemField titleField = new MetaItemField("Book Title", FieldType.TEXT, true);
		MetaItemField authorField = new MetaItemField("Author", FieldType.TEXT, true);
		MetaItemField purchaseField = new MetaItemField("Purchased", FieldType.DATE, false);
		MetaItemField priceField = new MetaItemField("Price", FieldType.DECIMAL, false);
		MetaItemField lenttoField = new MetaItemField("Lent to", FieldType.TEXT, false);
		MetaItemField inStock = new MetaItemField("In Stock", FieldType.OPTION, false);
		MetaItemField rating = new MetaItemField("Rating", FieldType.STAR_RATING, false);
		MetaItemField publisherWebsite = new MetaItemField("Publisher Website", FieldType.URL, false);
		MetaItemField timeStamp = new MetaItemField("Time Stamp", FieldType.TIME, false);

		List<MetaItemField> columns = new ArrayList<MetaItemField>();
		columns.add(titleField);
		columns.add(authorField);
		columns.add(purchaseField);
		columns.add(priceField);
		columns.add(lenttoField);
		columns.add(inStock);
		columns.add(rating);
		columns.add(publisherWebsite);
		columns.add(timeStamp);
		
		try {
			DatabaseOperations.createNewAlbum(albumName, columns, true);
		} catch (DatabaseWrapperOperationException e) {
			fail("Creation of album " + albumName + " failed");
		}
	}	
}
