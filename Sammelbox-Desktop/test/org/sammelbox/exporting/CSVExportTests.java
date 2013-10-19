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

package org.sammelbox.exporting;

import static org.junit.Assert.fail;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sammelbox.TestExecuter;
import org.sammelbox.controller.filesystem.exporting.CSVExporter;
import org.sammelbox.model.album.AlbumItem;
import org.sammelbox.model.album.FieldType;
import org.sammelbox.model.album.ItemField;
import org.sammelbox.model.album.MetaItemField;
import org.sammelbox.model.album.OptionType;
import org.sammelbox.model.album.StarRating;
import org.sammelbox.model.database.DatabaseStringUtilities;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseOperations;

public class CSVExportTests {
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
				
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		TestExecuter.resetTestHome();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCSVExportWithoutPictures() {
		try {
			List<MetaItemField> metaItemFields = new ArrayList<>();
			metaItemFields.add(new MetaItemField("a", FieldType.DATE));
			metaItemFields.add(new MetaItemField("b", FieldType.DECIMAL));
			metaItemFields.add(new MetaItemField("c", FieldType.INTEGER));
			metaItemFields.add(new MetaItemField("d", FieldType.OPTION));
			metaItemFields.add(new MetaItemField("e", FieldType.STAR_RATING));
			metaItemFields.add(new MetaItemField("f", FieldType.TEXT));
			metaItemFields.add(new MetaItemField("g", FieldType.URL));
			
			// Create test album for extraction
			DatabaseOperations.createNewAlbum("Test Table", metaItemFields, false);
			
			// Add dummy items
			List<ItemField> item1Fields = new ArrayList<>();
			item1Fields.add(new ItemField("a", FieldType.DATE, new Date(System.currentTimeMillis())));
			item1Fields.add(new ItemField("b", FieldType.DECIMAL, 111.111));
			item1Fields.add(new ItemField("c", FieldType.INTEGER, 1));
			item1Fields.add(new ItemField("d", FieldType.OPTION, OptionType.UNKNOWN));
			item1Fields.add(new ItemField("e", FieldType.STAR_RATING, StarRating.ONE_STAR));
			item1Fields.add(new ItemField("f", FieldType.TEXT, "Hello World 1"));
			item1Fields.add(new ItemField("g", FieldType.URL, "www.sammelbox.org"));

			AlbumItem albumItem = new AlbumItem("Test Table");
			albumItem.setFields(item1Fields);
			DatabaseOperations.addAlbumItem(albumItem, true);
			
			// Add dummy items
			List<ItemField> item2Fields = new ArrayList<>();
			item2Fields.add(new ItemField("a", FieldType.DATE, new Date(System.currentTimeMillis())));
			item2Fields.add(new ItemField("b", FieldType.DECIMAL, 222.222));
			item2Fields.add(new ItemField("c", FieldType.INTEGER, 2));
			item2Fields.add(new ItemField("d", FieldType.OPTION, OptionType.YES));
			item1Fields.add(new ItemField("e", FieldType.STAR_RATING, StarRating.TWO_STARS));
			item2Fields.add(new ItemField("f", FieldType.TEXT, "Hello World 2"));
			item2Fields.add(new ItemField("g", FieldType.URL, "www.sammelbox.org"));
			
			AlbumItem albumItem2 = new AlbumItem("Test Table");
			albumItem2.setFields(item2Fields);
			DatabaseOperations.addAlbumItem(albumItem2, true);
			
			// Add dummy items
			List<ItemField> item3Fields = new ArrayList<>();
			item3Fields.add(new ItemField("a", FieldType.DATE, new Date(System.currentTimeMillis())));
			item3Fields.add(new ItemField("b", FieldType.DECIMAL, 333.333));
			item3Fields.add(new ItemField("c", FieldType.INTEGER, 3));
			item3Fields.add(new ItemField("d", FieldType.OPTION, OptionType.NO));
			item1Fields.add(new ItemField("e", FieldType.STAR_RATING, StarRating.THREE_STARS));
			item3Fields.add(new ItemField("f", FieldType.TEXT, "Hello World 3"));
			item3Fields.add(new ItemField("g", FieldType.URL, "www.sammelbox.org"));
			
			AlbumItem albumItem3 = new AlbumItem("Test Table");
			albumItem3.setFields(item3Fields);
			DatabaseOperations.addAlbumItem(albumItem3, true);
			
			CSVExporter.exportAlbum(DatabaseOperations.getAlbumItems(
					"SELECT * FROM " + DatabaseStringUtilities.generateTableName("Test Table")),
					"/home/luxem/Desktop/test.csv", ";");
			
			String expectedOutput =
					"a;b;c;d;e;f;g;" + System.lineSeparator() +
					"2013-10-18;111.111;1;Unknown;1[!]  Stars;Hello World 1;www.sammelbox.org;" + System.lineSeparator() +
					"2013-10-18;222.222;2;Yes;0[!]  Stars;Hello World 2;www.sammelbox.org;" + System.lineSeparator() +
					"2013-10-18;333.333;3;No;0[!]  Stars;Hello World 3;www.sammelbox.org;";
			
		} catch (DatabaseWrapperOperationException dwoe) {
			fail(dwoe.getMessage());
		}
	}
}
