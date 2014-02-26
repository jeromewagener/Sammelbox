package org.sammelbox.view.browser.spreadsheet;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.sammelbox.model.album.AlbumItemStore;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseOperations;
import org.sammelbox.view.browser.BrowserFacade;

public class SpreadsheetUpdateFunction extends BrowserFunction {
	public SpreadsheetUpdateFunction (Browser browser, String name) {
		super (browser, name);
	}
	
	@Override
	public Object function (Object[] arguments) {
		System.out.println ("theJavaFunction() called from javascript with args:");
		
		for (Object o : (Object[]) arguments[0]) {
			//JavaScript has only one type of numbers. Numbers can be written with, or without decimals:
			long id = ((Double)o).longValue();
			
			System.out.println("Delete " + id);
			
			try {
				DatabaseOperations.deleteAlbumItem(AlbumItemStore.getAlbumItem(id));
			} catch (DatabaseWrapperOperationException e) {
				// TODO Log me
				e.printStackTrace();
			}			
		}
		
		String[] myArray = new String[100];
		myArray = (String[]) arguments[3];
		
		System.out.println(myArray[1]);
		
		BrowserFacade.showAlbum();
		
		return null;
	}
}