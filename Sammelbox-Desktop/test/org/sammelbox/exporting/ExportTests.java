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

package org.sammelbox.exporting;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sammelbox.TestExecuter;
import org.sammelbox.controller.filesystem.FileSystemAccessWrapper;
import org.sammelbox.controller.filesystem.FileSystemLocations;
import org.sammelbox.controller.filesystem.exporting.CSVExporter;
import org.sammelbox.controller.filesystem.exporting.HTMLExporter;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.model.album.AlbumItem;
import org.sammelbox.model.album.FieldType;
import org.sammelbox.model.album.ItemField;
import org.sammelbox.model.album.MetaItemField;
import org.sammelbox.model.album.OptionType;
import org.sammelbox.model.album.StarRating;
import org.sammelbox.model.database.DatabaseStringUtilities;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseOperations;

public class ExportTests {
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
	public void testHTMLExport() {
		try {
			Date dummyDate = new Date(System.currentTimeMillis());
			setupAlbum(dummyDate);
		
			File outputFile = new File(FileSystemLocations.TEMP_DIR + File.separatorChar + "test.html");
			if (outputFile.exists()) {
				outputFile.delete();
			}
			
			HTMLExporter.exportAlbum(
					DatabaseOperations.getAlbumItems("SELECT * FROM " + DatabaseStringUtilities.generateTableName("Test Table")),
					outputFile.toString());
			
			String htmlOutput = FileSystemAccessWrapper.readFileAsString(outputFile.getAbsolutePath());
			
			assertTrue(htmlOutput.contains("a"));
			assertTrue(htmlOutput.contains("b"));
			assertTrue(htmlOutput.contains("c"));
			assertTrue(htmlOutput.contains("d"));
			assertTrue(htmlOutput.contains("e"));
			assertTrue(htmlOutput.contains("f"));
			assertTrue(htmlOutput.contains("g"));
			
			assertTrue(htmlOutput.contains("Hello World 1"));
			assertTrue(htmlOutput.contains(Translator.get(DictKeys.BROWSER_YES)));
			assertTrue(htmlOutput.contains("333.333"));
			assertTrue(htmlOutput.contains("2"));
			assertTrue(htmlOutput.contains("1"));
			assertTrue(htmlOutput.contains(StarRating.toComboBoxArray()[1]));
			assertTrue(htmlOutput.contains(dummyDate.toString()));
			assertTrue(htmlOutput.contains("www.sammelbox.org"));
			
		} catch (DatabaseWrapperOperationException dwoe) {
			fail(dwoe.getMessage());
		}
	}
	
	@Test
	public void testCSVExport() {
		try {
			Date dummyDate = new Date(System.currentTimeMillis());
			setupAlbum(dummyDate);
			
			File outputFile = new File(FileSystemLocations.TEMP_DIR + File.separatorChar + "test.csv");
			if (outputFile.exists()) {
				outputFile.delete();
			}
			
			CSVExporter.exportAlbum(
					DatabaseOperations.getAlbumItems("SELECT * FROM " + DatabaseStringUtilities.generateTableName("Test Table")),
					outputFile.getAbsolutePath(), ";");
			
			String expectedOutput =
					"a;b;c;d;e;f;g" + System.lineSeparator() +
					dummyDate.toString() + ";111.111;1;" + Translator.get(DictKeys.BROWSER_UNKNOWN) + ";" + 
							StarRating.toComboBoxArray()[1] + ";Hello World 1;www.sammelbox.org" + System.lineSeparator() +
					dummyDate.toString() + ";222.222;2;" + Translator.get(DictKeys.BROWSER_YES) + ";" + 
							StarRating.toComboBoxArray()[2] + ";Hello World 2;www.sammelbox.org" + System.lineSeparator() +
					dummyDate.toString() + ";333.333;3;" + Translator.get(DictKeys.BROWSER_NO) + ";" + 
							StarRating.toComboBoxArray()[3] + ";Hello World 3;www.sammelbox.org" + System.lineSeparator();
			
			// Compare expected output with file	
			assertTrue(expectedOutput.equals(
					FileSystemAccessWrapper.readFileAsString(outputFile.getAbsolutePath())));
		} catch (DatabaseWrapperOperationException dwoe) {
			fail(dwoe.getMessage());
		}
	}
	
	private void setupAlbum(Date dummyDate) throws DatabaseWrapperOperationException {
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
		item1Fields.add(new ItemField("a", FieldType.DATE, dummyDate));
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
		item2Fields.add(new ItemField("a", FieldType.DATE, dummyDate));
		item2Fields.add(new ItemField("b", FieldType.DECIMAL, 222.222));
		item2Fields.add(new ItemField("c", FieldType.INTEGER, 2));
		item2Fields.add(new ItemField("d", FieldType.OPTION, OptionType.YES));
		item2Fields.add(new ItemField("e", FieldType.STAR_RATING, StarRating.TWO_STARS));
		item2Fields.add(new ItemField("f", FieldType.TEXT, "Hello World 2"));
		item2Fields.add(new ItemField("g", FieldType.URL, "www.sammelbox.org"));
		
		AlbumItem albumItem2 = new AlbumItem("Test Table");
		albumItem2.setFields(item2Fields);
		DatabaseOperations.addAlbumItem(albumItem2, true);
		
		// Add dummy items
		List<ItemField> item3Fields = new ArrayList<>();
		item3Fields.add(new ItemField("a", FieldType.DATE, dummyDate));
		item3Fields.add(new ItemField("b", FieldType.DECIMAL, 333.333));
		item3Fields.add(new ItemField("c", FieldType.INTEGER, 3));
		item3Fields.add(new ItemField("d", FieldType.OPTION, OptionType.NO));
		item3Fields.add(new ItemField("e", FieldType.STAR_RATING, StarRating.THREE_STARS));
		item3Fields.add(new ItemField("f", FieldType.TEXT, "Hello World 3"));
		item3Fields.add(new ItemField("g", FieldType.URL, "www.sammelbox.org"));
		
		AlbumItem albumItem3 = new AlbumItem("Test Table");
		albumItem3.setFields(item3Fields);
		DatabaseOperations.addAlbumItem(albumItem3, true);
	}
}
