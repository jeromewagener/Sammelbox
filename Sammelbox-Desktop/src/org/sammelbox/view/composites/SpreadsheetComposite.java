package org.sammelbox.view.composites;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.sammelbox.controller.filters.ItemFieldFilter;
import org.sammelbox.model.album.AlbumItem;
import org.sammelbox.model.album.AlbumItemStore;
import org.sammelbox.model.album.ItemField;
import org.sammelbox.view.ApplicationUI;

public final class SpreadsheetComposite {
	private SpreadsheetComposite() {
		// use build method instead
	}
	
	public static Composite build(Composite parentComposite) {
		
		final int SPREADSHEEDMAXCOLUMNWIDTH = 250;
		
		// setup spreadsheet composite
		Composite spreadsheetComposite = new Composite(parentComposite, SWT.NONE);
		spreadsheetComposite.setLayout(new GridLayout());
		
		Table table = new Table (spreadsheetComposite, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		table.setLinesVisible (true);
		table.setHeaderVisible (true);
		
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		
		data.heightHint = 200;
		table.setLayoutData(data);
		
		if (!ApplicationUI.isAlbumSelectedAndShowMessageIfNot()) {
			return null;
		}

		ArrayList<String> columnTitlesArrayList = new ArrayList<>();
		ArrayList<String> contentArrayList = new ArrayList<>();
		
		Boolean firstRun = true;
		String[] titles = null;
		String[] content = null;
		
		// Add all available album items
		for (AlbumItem albumItem : AlbumItemStore.getAlbumItems(AlbumItemStore.getStopIndex())) {

			columnTitlesArrayList.clear();
			contentArrayList.clear();
			
			if(firstRun){
				for (ItemField fieldItem : ItemFieldFilter.getValidItemFields(albumItem.getFields())) {
				
					columnTitlesArrayList.add(fieldItem.getName());
					contentArrayList.add(fieldItem.getValue().toString());
					
				}
				
			    titles = new String[columnTitlesArrayList.size()];
			    titles = columnTitlesArrayList.toArray(titles);
			    
				for (int i=0; i<titles.length; i++) {
					TableColumn column = new TableColumn (table, SWT.NONE);
					column.setText (titles [i]);
				}	
				
				TableItem item = new TableItem (table, SWT.NONE);
				
			    content = new String[contentArrayList.size()];
			    content = contentArrayList.toArray(content);
				
				item.setText(content);
				
			}
			else{
				for (ItemField fieldItem : ItemFieldFilter.getValidItemFields(albumItem.getFields())) {
					if(fieldItem.getValue() != null)
						contentArrayList.add(fieldItem.getValue().toString());
					else
						contentArrayList.add("");
				}
				
				TableItem item = new TableItem (table, SWT.NONE);
				
			    content = new String[contentArrayList.size()];
			    content = contentArrayList.toArray(content);
				
				item.setText(content);
			}
			
			firstRun = false;
		}
		
		
		for (int i=0; i<titles.length; i++) {
			table.getColumn(i).pack ();
			if(table.getColumn(i).getWidth() > SPREADSHEEDMAXCOLUMNWIDTH)
				table.getColumn(i).setWidth(SPREADSHEEDMAXCOLUMNWIDTH);
		}	
		
		
		return spreadsheetComposite;
	}
}
