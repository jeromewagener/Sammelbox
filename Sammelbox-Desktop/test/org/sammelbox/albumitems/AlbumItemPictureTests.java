package org.sammelbox.albumitems;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sammelbox.TestExecuter;
import org.sammelbox.controller.managers.DatabaseIntegrityManager;
import org.sammelbox.model.album.AlbumItem;
import org.sammelbox.model.album.FieldType;
import org.sammelbox.model.album.MetaItemField;
import org.sammelbox.model.database.QueryBuilder;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseOperations;

public class AlbumItemPictureTests {
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		TestExecuter.resetTestHome();
	}

	@Before
	public void setUp() throws Exception {
		TestExecuter.resetTestHome();
	}

	@After
	public void tearDown() throws Exception {
		TestExecuter.resetTestHome();
	}
	
	@Test
	public void testAlbumModificationAfterItemDeletion() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestExecuter.PATH_TO_TEST_CBK);
			Map<Long, String> albumItemIdToPictureName = new HashMap<>();
			
			for (AlbumItem albumItem : DatabaseOperations.getAlbumItems(QueryBuilder.createSelectStarQuery("DVDs"))) {
				albumItemIdToPictureName.put(albumItem.getItemId(), albumItem.getFirstPicture().getOriginalPictureName());
			}
			
			// remove the sixth item
			AlbumItem thirdItem = DatabaseOperations.getAlbumItem("DVDs", 3);
			DatabaseOperations.deleteAlbumItem(thirdItem);
			
			// modify the album structure (this causes a table regeneration + copy of items)
			MetaItemField titleField = new MetaItemField("Title", FieldType.TEXT, true);
			MetaItemField movieTitleField = new MetaItemField("Movie Title", FieldType.TEXT, true);
			
			DatabaseOperations.renameAlbumItemField("DVDs", titleField, movieTitleField);
			
			// after the modification the pictures should remain linked to the correct items
			for (AlbumItem albumItem : DatabaseOperations.getAlbumItems(QueryBuilder.createSelectStarQuery("DVDs"))) {
				assertTrue("Every album item should still have a picture associated",
						albumItem.getFirstPicture() != null);
				assertTrue("The pictures should remain linked to the correct items", 
						albumItem.getFirstPicture().getOriginalPictureName().equals(albumItemIdToPictureName.get(albumItem.getItemId())));
			}
			
		} catch (DatabaseWrapperOperationException e) {
			fail(e.toString());
		}
	}
	
	@Test
	public void testAlbumModificationAfterMultiItemDeletion() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestExecuter.PATH_TO_TEST_CBK);
			Map<Long, String> albumItemIdToPictureName = new HashMap<>();
			
			for (AlbumItem albumItem : DatabaseOperations.getAlbumItems(QueryBuilder.createSelectStarQuery("DVDs"))) {
				albumItemIdToPictureName.put(albumItem.getItemId(), albumItem.getFirstPicture().getOriginalPictureName());
			}
			
			// remove items
			AlbumItem secondItem = DatabaseOperations.getAlbumItem("DVDs", 2);
			DatabaseOperations.deleteAlbumItem(secondItem);
			AlbumItem thirdItem = DatabaseOperations.getAlbumItem("DVDs", 3);
			DatabaseOperations.deleteAlbumItem(thirdItem);
			AlbumItem fourthItem = DatabaseOperations.getAlbumItem("DVDs", 4);
			DatabaseOperations.deleteAlbumItem(fourthItem);
			AlbumItem fifthItem = DatabaseOperations.getAlbumItem("DVDs", 5);
			DatabaseOperations.deleteAlbumItem(fifthItem);
			
			// modify the album structure (this causes a table regeneration + copy of items)
			MetaItemField titleField = new MetaItemField("Title", FieldType.TEXT, true);
			MetaItemField movieTitleField = new MetaItemField("Movie Title", FieldType.TEXT, true);
			
			DatabaseOperations.renameAlbumItemField("DVDs", titleField, movieTitleField);
			
			DatabaseOperations.reorderAlbumItemField("DVDs", new MetaItemField("Actors", FieldType.TEXT, true), null);
			
			// after the modification the pictures should remain linked to the correct items
			for (AlbumItem albumItem : DatabaseOperations.getAlbumItems(QueryBuilder.createSelectStarQuery("DVDs"))) {
				assertTrue("Every album item should still have a picture associated",
						albumItem.getFirstPicture() != null);
				assertTrue("The pictures should remain linked to the correct items", 
						albumItem.getFirstPicture().getOriginalPictureName().equals(albumItemIdToPictureName.get(albumItem.getItemId())));
			}
			
		} catch (DatabaseWrapperOperationException e) {
			fail(e.toString());
		}
	}
	
	@Test
	public void testAlbumModificationAfterFirstLastItemDeletion() {
		try {
			DatabaseIntegrityManager.restoreFromFile(TestExecuter.PATH_TO_TEST_CBK);
			Map<Long, String> albumItemIdToPictureName = new HashMap<>();
			
			for (AlbumItem albumItem : DatabaseOperations.getAlbumItems(QueryBuilder.createSelectStarQuery("DVDs"))) {
				albumItemIdToPictureName.put(albumItem.getItemId(), albumItem.getFirstPicture().getOriginalPictureName());
			}
			
			// remove items
			AlbumItem firstItem = DatabaseOperations.getAlbumItem("DVDs", 1);
			DatabaseOperations.deleteAlbumItem(firstItem);
			AlbumItem lastItem = DatabaseOperations.getAlbumItem("DVDs", 11);
			DatabaseOperations.deleteAlbumItem(lastItem);
			
			// modify the album structure (this causes a table regeneration + copy of items)			
			MetaItemField actorsField = new MetaItemField("Actors", FieldType.TEXT, true);
			MetaItemField movieActorsField = new MetaItemField("Movie Actors", FieldType.TEXT, true);
			DatabaseOperations.renameAlbumItemField("DVDs", actorsField, movieActorsField);
			
			// after the modification the pictures should remain linked to the correct items
			for (AlbumItem albumItem : DatabaseOperations.getAlbumItems(QueryBuilder.createSelectStarQuery("DVDs"))) {
				assertTrue("Every album item should still have a picture associated",
						albumItem.getFirstPicture() != null);
				assertTrue("The pictures should remain linked to the correct items", 
						albumItem.getFirstPicture().getOriginalPictureName().equals(albumItemIdToPictureName.get(albumItem.getItemId())));
			}
			
		} catch (DatabaseWrapperOperationException e) {
			fail(e.toString());
		}
	}
}