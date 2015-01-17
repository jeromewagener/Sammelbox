package org.sammelbox.controller.filesystem.importing;

import org.sammelbox.controller.events.EventObservable;
import org.sammelbox.controller.events.SammelboxEvent;
import org.sammelbox.model.album.FieldType;
import org.sammelbox.model.album.MetaItemField;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseOperations;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CSVAppender {
	private CSVAppender() {}
	
	public static void appendItemsFromCSV(String albumName, String csvFilePath, String separationCharacter, 
			boolean isSimulation) throws ImportException {
		appendItemsFromCSV(albumName, csvFilePath, separationCharacter, 
				CSVImporter.NO_PICTURE_COLUMN_NAME, CSVImporter.NO_PICTURE_SEPARATION_CHARACTER, isSimulation);
	}
	
	public static void appendItemsFromCSV(String albumName, String csvFilePath, String separationCharacter, 
			String pictureColumnName, String pictureSeparationCharacter, boolean isSimulation) throws ImportException {
				
		EventObservable.addEventToQueue(SammelboxEvent.DISABLE_SAMMELBOX);
		
		try (BufferedReader br = new BufferedReader(new FileReader(new File(csvFilePath)));) {
			Map<Integer, MetaItemField> positionToMetaItemFieldMap = DatabaseOperations.getAlbumItemMetaMap(albumName);
			int pictureColumnIndex = CSVImporter.NO_PICTURE_INDEX;
			
			List<MetaItemField> metaItemFields = new ArrayList<>();
			
			String line = br.readLine();
			pictureColumnIndex = handleFirstLine(line, albumName, separationCharacter, metaItemFields, pictureColumnName, pictureSeparationCharacter, positionToMetaItemFieldMap);
			
			long lineCounter = 2;
			while ((line = br.readLine()) != null) {
				CSVImporter.handleData(line, lineCounter, albumName, separationCharacter, metaItemFields, pictureSeparationCharacter, pictureColumnIndex, isSimulation);
				lineCounter++;
			}
		} catch (FileNotFoundException fnfe) {
			throw new ImportException("An error occurred while handling the file", fnfe);
		} catch (IOException ioe) {
			throw new ImportException("An error occurred while handling the file", ioe);
		} catch (NumberFormatException nfe) {
			throw new ImportException("An error occurred while trying to interpret a decimal or integer value", nfe);
		} catch (DatabaseWrapperOperationException dwoe) {
			throw new ImportException("An internal error occurred", dwoe);
		}
		
		EventObservable.addEventToQueue(SammelboxEvent.ENABLE_SAMMELBOX);
	}
	
	private static int handleFirstLine(String line, String albumName, String separationCharacter, List<MetaItemField> metaItemFields, 
			String pictureColumnName, String pictureSeparationCharacter, Map<Integer, MetaItemField> positionToMetaItemFieldMap) throws ImportException, DatabaseWrapperOperationException {
		
		if (line == null || line.isEmpty()) {
			throw new ImportException("There seems to be no header data");
		}
		
		int pictureColumnIndex = CSVImporter.NO_PICTURE_INDEX;
		
		// Using the first line, we should be able to define the fields, and possibly there types
		String[] fieldHeader = line.split(separationCharacter);
		
		for (int i=0; i<fieldHeader.length; i++) {
			// Check if the type had been added to the field name
			String[] fieldNameAndPossiblyType = fieldHeader[i].split("~");
			
			if (fieldNameAndPossiblyType.length == 1) {
				if (fieldNameAndPossiblyType[0].equals(pictureColumnName)) {
					pictureColumnIndex = i;
				} else {
					metaItemFields.add(new MetaItemField(fieldNameAndPossiblyType[0], FieldType.TEXT));
				}
			} else if (fieldNameAndPossiblyType.length == 2) {
				metaItemFields.add(new MetaItemField(fieldNameAndPossiblyType[0], FieldType.valueOf(fieldNameAndPossiblyType[1])));
			} else {
				throw new ImportException("Could not interpret: " + fieldHeader[i]);
			}
		}
		
		if (CSVImporter.NO_PICTURE_COLUMN_NAME.equals(pictureColumnName) && CSVImporter.NO_PICTURE_SEPARATION_CHARACTER.equals(pictureSeparationCharacter)) {
			for (MetaItemField importMetaItemField : positionToMetaItemFieldMap.values()) {
				boolean foundField = false;

				for (MetaItemField albumMetaItemField : positionToMetaItemFieldMap.values()) {
					if (importMetaItemField.equals(albumMetaItemField)) {
						foundField = true;
						break;
					}
				}

				if (!foundField) {
					throw new ImportException("Could not match field: " + importMetaItemField.getName() + " within: " + albumName);
				}
			}
		}
		
		return pictureColumnIndex;
	}
}
