package org.sammelbox.controller.menu;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.sammelbox.view.ApplicationUI;
import org.sammelbox.view.sidepanes.SynchronizeSidepane;
import org.sammelbox.view.various.PanelType;

public final class SynchronizeMenuItemListener {
	private SynchronizeMenuItemListener() {
		// not needed
	}
	
	static SelectionAdapter getSynchronizeListener() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				ApplicationUI.changeRightCompositeTo(
						PanelType.SYNCHRONIZATION, SynchronizeSidepane.build(ApplicationUI.getThreePanelComposite()));
			}
		};
	}
}
