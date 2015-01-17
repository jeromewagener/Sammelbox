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

import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.model.album.AlbumItem;
import org.sammelbox.model.album.FieldType;
import org.sammelbox.model.album.OptionType;
import org.sammelbox.model.album.StarRating;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

public final class HTMLExporter {
	private static final Logger LOGGER = LoggerFactory.getLogger(HTMLExporter.class);
	private static final String TABLE_STYLE = " style=\"border:1px solid black;\" ";
	
	private HTMLExporter() {
	}
	
	public static void exportAlbum(List<AlbumItem> albumItems, String filePath) {
		StringBuilder headerBuilder = new StringBuilder();
		StringBuilder dataBuilder = new StringBuilder();
		boolean firstLine = true;

		for (AlbumItem albumItem : albumItems) {	
			if (firstLine) {
				headerBuilder.append("<tr>");
			}
			dataBuilder.append("<tr>");
			
			for (int i=0; i<albumItem.getFields().size(); i++) {			
				if (albumItem.getField(i).getType().equals(FieldType.UUID) || albumItem.getField(i).getType().equals(FieldType.ID)) {
					// schema or content version UUID --> ignore
					// do not show ID either
					continue;
				}

				if (albumItem.getField(i).getType().equals(FieldType.OPTION)) {
					addHeaderIfFirstLine(firstLine, albumItem, headerBuilder, i);

					if (albumItem.getField(i).getValue() == OptionType.YES) {
						dataBuilder.append("<td" + TABLE_STYLE + ">").append(Translator.get(DictKeys.BROWSER_YES)).append("</td>");
					} else if (albumItem.getField(i).getValue() == OptionType.NO) {
						dataBuilder.append("<td" + TABLE_STYLE + ">").append(Translator.get(DictKeys.BROWSER_NO)).append("</td>");
					} else {
						dataBuilder.append("<td" + TABLE_STYLE + ">").append(Translator.get(DictKeys.BROWSER_UNKNOWN)).append("</td>");
					}
				} else if (albumItem.getField(i).getType().equals(FieldType.STAR_RATING)) {
					addHeaderIfFirstLine(firstLine, albumItem, headerBuilder, i);

					if (albumItem.getField(i).getValue() == null || albumItem.getField(i).getValue().equals("")) {
						dataBuilder.append("<td" + TABLE_STYLE + ">" + "-" + "</td>");
					} else {
						dataBuilder.append("<td " + TABLE_STYLE + ">").append(StarRating.toComboBoxArray()[((StarRating) albumItem.getField(i).getValue()).getIntegerValue()]).append("</td>");
					}
				} else {
					addHeaderIfFirstLine(firstLine, albumItem, headerBuilder, i);

					if (albumItem.getField(i).getValue() == null || albumItem.getField(i).getValue().equals("")) {
						dataBuilder.append("<td" + TABLE_STYLE + ">" + "-" + "</td>");
					} else {
						dataBuilder.append("<td" + TABLE_STYLE + ">").append(albumItem.getField(i).getValue()).append("</td>");
					}
				}

			}
			
			if (firstLine) {
				headerBuilder.append("</tr>");
			}
			
			dataBuilder.append("</tr>");
			
			firstLine = false;
		}

		try {
			BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8"));
			bufferedWriter.write("<html>" +
								 "	<head>" +
								 "		<meta charset=\"UTF-8\">" +
								 "	</head>" +
								 "	<body>" +
								 "		<table style=\"border:1px solid black; border-collapse:collapse;\">" + 
								 			headerBuilder.toString() + 
								 			dataBuilder.toString() + 
								 "		</table>" +
								 "	</body>" +
								 "</html>");
			bufferedWriter.close();
		} catch (IOException e) {
			LOGGER.error("An error occurred while writing the HTML to its destination", e);
		}
	}
	
	private static void addHeaderIfFirstLine(boolean firstLine, AlbumItem albumItem, StringBuilder builder, int fieldPosition) {
		if (firstLine) {
			builder.append("<th" + TABLE_STYLE + "\">").append(albumItem.getField(fieldPosition).getName()).append("</th>");
		}
	}
}
