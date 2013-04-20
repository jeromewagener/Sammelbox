package collector.desktop.filesystem.export;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import collector.desktop.database.AlbumItemResultSet;
import collector.desktop.database.DatabaseWrapper;
import collector.desktop.database.FieldType;
import collector.desktop.database.OptionType;
import collector.desktop.gui.BrowserContent;

public class CSVExporter {
	public static void exportVisibleItems(String filepath) {
		String lastSQLQuery = BrowserContent.getLastSqlQuery();

		AlbumItemResultSet albumItemResultSet = DatabaseWrapper.executeSQLQuery(lastSQLQuery);

		StringBuilder headerBuilder = new StringBuilder();
		StringBuilder dataBuilder = new StringBuilder();
		boolean firstLine = true;

		while (albumItemResultSet.moveToNext()) {
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
							headerBuilder.append(albumItemResultSet.getFieldName(i));
							
							if (i < albumItemResultSet.getFieldCount()) {
								headerBuilder.append("$");
							}
						}

						if (albumItemResultSet.getFieldValue(i) == OptionType.Yes) {
							dataBuilder.append("Yes");
						} else if (albumItemResultSet.getFieldValue(i) == OptionType.No) {
							dataBuilder.append("No");
						} else {
							dataBuilder.append("Unknown");
						}
						
						if (i < albumItemResultSet.getFieldCount()) {
							dataBuilder.append("$");
						}
					} else {
						if (firstLine) {
							headerBuilder.append(albumItemResultSet.getFieldName(i));
							
							if (i < albumItemResultSet.getFieldCount()) {
								headerBuilder.append("$");
							}
						}

						dataBuilder.append(albumItemResultSet.getFieldValue(i));
						
						if (i < albumItemResultSet.getFieldCount()) {
							dataBuilder.append("$");
						}
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
			e.printStackTrace();
		}

		albumItemResultSet.close();
	}
}
