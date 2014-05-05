package org.sammelbox.view.browser.spreadsheet;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.sammelbox.view.ApplicationUI;
import org.sammelbox.view.various.ComponentFactory;

public class YesNoDialogFunction extends BrowserFunction {
	private String titleText;
	private String messageText;
	
	public YesNoDialogFunction(Browser browser, String name, String titleText, String messageText) {
		super (browser, name);
		
		this.titleText = titleText;
		this.messageText = messageText;
	}
	
	@Override
	public Object function(Object[] arguments) {
		return ComponentFactory.showYesNoDialog(
				ApplicationUI.getShell(), titleText, messageText);
	}
}
