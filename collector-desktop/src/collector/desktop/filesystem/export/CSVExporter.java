package collector.desktop.filesystem.export;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import collector.desktop.database.AlbumItem;
import collector.desktop.database.AlbumItemStore;
import collector.desktop.database.FieldType;
import collector.desktop.database.OptionType;
import collector.desktop.internationalization.DictKeys;
import collector.desktop.internationalization.Translator;

public class CSVExporter {
	public static void exportVisibleItems(String filepath) {
		List<AlbumItem> visibleAlbumItems = AlbumItemStore.getAllVisibleAlbumItems();
		
		StringBuilder headerBuilder = new StringBuilder();
		StringBuilder dataBuilder = new StringBuilder();
		boolean firstLine = true;

		for (AlbumItem albumItem : visibleAlbumItems) {			
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
							headerBuilder.append(albumItem.getField(i).getName());
							
							if (i < albumItem.getFields().size()) {
								headerBuilder.append("$");
							}
						}

						if (albumItem.getField(i).getValue() == OptionType.Yes) {
							dataBuilder.append(Translator.get(DictKeys.BROWSER_YES));
						} else if (albumItem.getField(i).getValue() == OptionType.No) {
							dataBuilder.append(Translator.get(DictKeys.BROWSER_NO));
						} else {
							dataBuilder.append(Translator.get(DictKeys.BROWSER_UNKNOWN));
						}
						
						if (i < albumItem.getFields().size()) {
							dataBuilder.append("$");
						}
					} else {
						if (firstLine) {
							headerBuilder.append(albumItem.getField(i).getName());
							
							if (i < albumItem.getFields().size()) {
								headerBuilder.append("$");
							}
						}

						dataBuilder.append(albumItem.getField(i).getValue());
						
						if (i < albumItem.getFields().size()) {
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
	}
}
