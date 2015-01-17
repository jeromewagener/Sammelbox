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

package org.sammelbox.controller.filesystem.exporting;

import org.sammelbox.model.album.AlbumItem;
import org.sammelbox.model.album.FieldType;
import org.sammelbox.model.album.OptionType;
import org.sammelbox.model.album.StarRating;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public final class CSVExporter {
	public static final String DEFAULT_SEPARATION_CHARACTER = ";";
	private static final Logger LOGGER = LoggerFactory.getLogger(CSVExporter.class);
	
	private CSVExporter() {
	}
	
	public static void exportAlbum(List<AlbumItem> albumItems, String filepath, String separationCharacter) {
		StringBuilder headerBuilder = new StringBuilder();
		StringBuilder dataBuilder = new StringBuilder();
		boolean firstLine = true;

		for (AlbumItem albumItem : albumItems) {			
			for (int i=0; i<albumItem.getFields().size(); i++) {				
				if (albumItem.getField(i).getType().equals(FieldType.UUID)) {
					// schema or content version UUID --> ignore 
				}
				else if (albumItem.getField(i).getType().equals(FieldType.ID)) {
					// do not show ID either
				} else {
					if (albumItem.getField(i).getType().equals(FieldType.OPTION)) {
						addHeaderIfFirstLine(firstLine, albumItem, headerBuilder, separationCharacter, i);
						dataBuilder.append(OptionType.getTranslation((OptionType) albumItem.getField(i).getValue()));
						
						addSeparationCharacterIfRequired(albumItem, dataBuilder, separationCharacter, i);
					} else if (albumItem.getField(i).getType().equals(FieldType.STAR_RATING)) {
						addHeaderIfFirstLine(firstLine, albumItem, headerBuilder, separationCharacter, i);
						dataBuilder.append(StarRating.toComboBoxArray()[((StarRating) albumItem.getField(i).getValue()).getIntegerValue()]);
						addSeparationCharacterIfRequired(albumItem, dataBuilder, separationCharacter, i);
					} else {
						addHeaderIfFirstLine(firstLine, albumItem, headerBuilder, separationCharacter, i);
						dataBuilder.append(albumItem.getField(i).getValue());
						addSeparationCharacterIfRequired(albumItem, dataBuilder, separationCharacter, i);
					}
				}
			}
			
			if (firstLine) {
				headerBuilder.append(System.lineSeparator());
			}
			
			dataBuilder.append(System.lineSeparator());
			firstLine = false;
		}

		try {
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filepath));
			bufferedWriter.write(headerBuilder.toString() + dataBuilder.toString());
			bufferedWriter.close();
		} catch (IOException e) {
			LOGGER.error("An error occurred while writing the export data to its destinatation (" + filepath + ")", e);
		}
	}
	
	private static void addHeaderIfFirstLine(boolean firstLine, AlbumItem albumItem, StringBuilder headerBuilder, String separationCharacter, int fieldPosition) {
		if (firstLine) {
			headerBuilder.append(albumItem.getField(fieldPosition).getName());
			addSeparationCharacterIfRequired(albumItem, headerBuilder, separationCharacter, fieldPosition);
		}
	}
	
	private static void addSeparationCharacterIfRequired(AlbumItem albumItem, StringBuilder builder, String separationCharacter, int fieldPosition) {
		if (fieldPosition < albumItem.getFields().size() - 1) {
			builder.append(separationCharacter);
		}
	}
}
