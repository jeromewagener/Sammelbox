package org.sammelbox.view.composites;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.sammelbox.controller.GuiController;
import org.sammelbox.controller.filesystem.FileSystemAccessWrapper;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.view.SammelView;
import org.sammelbox.view.browser.BrowserFacade;

public class ChangeViewButton {
	private Button changeViewButton;
	private MenuItem galleryViewItem;
	
	public ChangeViewButton(Composite parentComposite, int style) {
		changeViewButton = new Button(parentComposite, style);
		
		changeViewButton.setText(Translator.get(DictKeys.BUTTON_CHANGE_VIEW));
		changeViewButton.setToolTipText(Translator.get(DictKeys.BUTTON_TOOLTIP_CHANGE_VIEW));
		changeViewButton.setMenu(createChangeViewButtonMenu(parentComposite.getShell()));
		changeViewButton.setImage(FileSystemAccessWrapper.getImageFromResource("graphics/changeview.png"));
		
		changeViewButton.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent e) {
				Rectangle buttonBounds = changeViewButton.getBounds();
				Point bottomLeftCorner = changeViewButton.toDisplay(buttonBounds.x, buttonBounds.y + buttonBounds.height);
				changeViewButton.getMenu().setLocation(bottomLeftCorner.x-buttonBounds.x, bottomLeftCorner.y-buttonBounds.y);                    		
				changeViewButton.getMenu().setVisible(true);					
			}

			@Override
			public void mouseDown(MouseEvent e) {}

			@Override
			public void mouseDoubleClick(MouseEvent e) {}
		});
	}

	private Menu createChangeViewButtonMenu(Shell parentShell) {
		Menu dropDownMenu = new Menu(parentShell, SWT.POP_UP);
		
		MenuItem detailedViewItem = new MenuItem (dropDownMenu, SWT.PUSH);
		detailedViewItem.setText(Translator.get(DictKeys.BUTTON_DETAILED_VIEW));
		detailedViewItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				GuiController.getGuiState().setSammelView(SammelView.DETAILED_VIEW);
				BrowserFacade.showAlbum();
			}
		});
		
		galleryViewItem = new MenuItem (dropDownMenu, SWT.PUSH);
		galleryViewItem.setText(Translator.get(DictKeys.BUTTON_GALLERY_VIEW));
		galleryViewItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				GuiController.getGuiState().setSammelView(SammelView.GALLERY_VIEW);
				BrowserFacade.showAlbum();
			}
		});

		MenuItem spreadSheetViewItem = new MenuItem (dropDownMenu, SWT.PUSH);
		spreadSheetViewItem.setText(Translator.get(DictKeys.BUTTON_SPREADSHEET_VIEW));
		spreadSheetViewItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				GuiController.getGuiState().setSammelView(SammelView.SPREADSHEET_VIEW);
				BrowserFacade.showAlbum();
			}
		});

		return dropDownMenu;
	}

	public void setEnabled(boolean isEnabled) {
		changeViewButton.setEnabled(isEnabled);
	}

	public void enabledGalleryMenu(boolean enabled) {
		galleryViewItem.setEnabled(enabled);
	}
}
