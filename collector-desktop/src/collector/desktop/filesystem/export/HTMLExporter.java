package collector.desktop.filesystem.export;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import collector.desktop.database.AlbumItemResultSet;
import collector.desktop.database.DatabaseWrapper;
import collector.desktop.database.FieldType;
import collector.desktop.database.OptionType;
import collector.desktop.gui.BrowserContent;

public class HTMLExporter {
	public static void exportVisibleItems(String filepath) {
		String lastSQLQuery = BrowserContent.getLastSqlQuery();

		AlbumItemResultSet albumItemResultSet = DatabaseWrapper.executeSQLQuery(lastSQLQuery);

		StringBuilder headerBuilder = new StringBuilder();
		StringBuilder dataBuilder = new StringBuilder();
		boolean firstLine = true;

		while (albumItemResultSet.moveToNext()) {
			if (firstLine) {
				headerBuilder.append("<tr>");
			}
			dataBuilder.append("<tr>");
			
			for (int i=1; i<=albumItemResultSet.getFieldCount(); i++) {				
				if (albumItemResultSet.getFieldType(i).equals(FieldType.UUID)) {
					// schema or content version UUID --> ignore 
				}
				else if (albumItemResultSet.getFieldType(i).equals(FieldType.ID)) {
					if (albumItemResultSet.isItemID(i)) {
						// do not show
					} else {
						// its a trap :-) (Probably just the typeinfo foreign key..)
					}
				}
				else if (albumItemResultSet.getFieldType(i).equals(FieldType.Picture)) {
					// not possible in CSV
				}
				else {
					if (albumItemResultSet.getFieldType(i).equals(FieldType.Option)) {
						if (firstLine) {
							headerBuilder.append("<th>" + albumItemResultSet.getFieldName(i) + "</th>");
						}

						if (albumItemResultSet.getFieldValue(i) == OptionType.Yes) {
							dataBuilder.append("<td>" + "Yes" + "</td>");
						} else if (albumItemResultSet.getFieldValue(i) == OptionType.No) {
							dataBuilder.append("<td>" + "No" + "</td>");
						} else {
							dataBuilder.append("<td>" + "Unknown" + "</td>");
						}
					} else {
						if (firstLine) {
							headerBuilder.append("<th>" + albumItemResultSet.getFieldName(i) + "</th>");
						}

						if (albumItemResultSet.getFieldValue(i) == null || albumItemResultSet.getFieldValue(i).equals("")) {
							dataBuilder.append("<td>" + "-" + "</td>");
						} else {
							dataBuilder.append("<td>" + albumItemResultSet.getFieldValue(i) + "</td>");
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
			bufferedWriter.write("<html><body><table border=\"1\">" + headerBuilder.toString() + dataBuilder.toString() + "</table></body></html>");
			bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		albumItemResultSet.close();
	}
}
