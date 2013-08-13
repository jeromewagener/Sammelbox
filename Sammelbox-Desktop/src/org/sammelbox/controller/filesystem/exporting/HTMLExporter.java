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

package org.sammelbox.controller.filesystem.exporting;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.model.album.AlbumItem;
import org.sammelbox.model.album.AlbumItemStore;
import org.sammelbox.model.album.FieldType;
import org.sammelbox.model.album.OptionType;

public class HTMLExporter {
	public static void exportVisibleItems(String filepath) {
		List<AlbumItem> visibleAlbumItems = AlbumItemStore.getAllVisibleAlbumItems();

		StringBuilder headerBuilder = new StringBuilder();
		StringBuilder dataBuilder = new StringBuilder();
		boolean firstLine = true;

		for (AlbumItem albumItem : visibleAlbumItems) {	
			if (firstLine) {
				headerBuilder.append("<tr>");
			}
			dataBuilder.append("<tr>");
			
			for (int i=0; i<albumItem.getFields().size(); i++) {			
				if (albumItem.getField(i).getType().equals(FieldType.UUID)) {
					// schema or content version UUID --> ignore 
				}
				else if (albumItem.getField(i).getType().equals(FieldType.ID)) {
					// do not show ID either
				}
				else {
					if (albumItem.getField(i).getType().equals(FieldType.Option)) {
						if (firstLine) {
							headerBuilder.append("<th style=\"border:1px solid black;\">" + albumItem.getField(i).getName() + "</th>");
						}

						if (albumItem.getField(i).getValue() == OptionType.YES) {
							dataBuilder.append("<td style=\"border:1px solid black;\">" + Translator.get(DictKeys.BROWSER_YES) + "</td>");
						} else if (albumItem.getField(i).getValue() == OptionType.NO) {
							dataBuilder.append("<td style=\"border:1px solid black;\">" + Translator.get(DictKeys.BROWSER_NO) + "</td>");
						} else {
							dataBuilder.append("<td style=\"border:1px solid black;\">" + Translator.get(DictKeys.BROWSER_UNKNOWN) + "</td>");
						}
					} else {
						if (firstLine) {
							headerBuilder.append("<th style=\"border:1px solid black;\">" + albumItem.getField(i).getName() + "</th>");
						}

						if (albumItem.getField(i).getValue() == null || albumItem.getField(i).getValue().equals("")) {
							dataBuilder.append("<td style=\"border:1px solid black;\">" + "-" + "</td>");
						} else {
							dataBuilder.append("<td style=\"border:1px solid black;\">" + albumItem.getField(i).getValue() + "</td>");
						}
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
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filepath));
			bufferedWriter.write("<html><head><meta charset=\"utf-8\"></head><body><table style=\"border:1px solid black; border-collapse:collapse;\">" + headerBuilder.toString() + dataBuilder.toString() + "</table></body></html>");
			bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
