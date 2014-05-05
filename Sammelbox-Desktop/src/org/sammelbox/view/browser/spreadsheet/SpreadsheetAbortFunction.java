package org.sammelbox.view.browser.spreadsheet;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.sammelbox.view.browser.BrowserFacade;

public class SpreadsheetAbortFunction extends BrowserFunction {

	public SpreadsheetAbortFunction(Browser browser, String name) {
		super (browser, name);
	}
	
	@Override
	public Object function(Object[] arguments) {
		BrowserFacade.showAlbum();
		return null;
	}
}