package org.sammelbox.controller.menu;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.sammelbox.view.ApplicationUI;
import org.sammelbox.view.browser.BrowserFacade;
import org.sammelbox.view.sidepanes.SettingsSidepane;
import org.sammelbox.view.various.PanelType;

public final class SettingsMenuItemListener {
	private SettingsMenuItemListener() {
		// not needed
	}
	
	static SelectionAdapter getSettingsListener() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				BrowserFacade.showHtmlPage("settings");
				
				ApplicationUI.changeRightCompositeTo(
						PanelType.SETTINGS, SettingsSidepane.build(ApplicationUI.getThreePanelComposite()));
			}
		};
	}
}
