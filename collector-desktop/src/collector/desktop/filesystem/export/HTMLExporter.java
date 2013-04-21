package collector.desktop.filesystem.export;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import collector.desktop.database.AlbumItem;
import collector.desktop.database.AlbumItemStore;
import collector.desktop.database.FieldType;
import collector.desktop.database.OptionType;

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
				else if (albumItem.getField(i).getType().equals(FieldType.Picture)) {
					// not possible in CSV
				}
				else {
					if (albumItem.getField(i).getType().equals(FieldType.Option)) {
						if (firstLine) {
							headerBuilder.append("<th style=\"border:1px solid black;\">" + albumItem.getField(i).getName() + "</th>");
						}

						if (albumItem.getField(i).getValue() == OptionType.Yes) {
							dataBuilder.append("<td style=\"border:1px solid black;\">" + "Yes" + "</td>");
						} else if (albumItem.getField(i).getValue() == OptionType.No) {
							dataBuilder.append("<td style=\"border:1px solid black;\">" + "No" + "</td>");
						} else {
							dataBuilder.append("<td style=\"border:1px solid black;\">" + "Unknown" + "</td>");
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
