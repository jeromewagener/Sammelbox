/** -----------------------------------------------------------------
 *    Sammelbox: Collection Manager - A free and open-source collection manager for Windows & Linux
 *    Copyright (C) 2011 Jerome Wagener & Paul Bicheler
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ** ----------------------------------------------------------------- */

package org.sammelbox.view.composites;

import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.sammelbox.controller.GuiController;
import org.sammelbox.controller.events.EventObservable;
import org.sammelbox.controller.events.EventObserver;
import org.sammelbox.controller.events.SammelboxEvent;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.controller.managers.SettingsManager;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseOperations;
import org.sammelbox.view.ApplicationUI;
import org.sammelbox.view.various.PanelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ToolbarComposite extends Composite implements EventObserver {
	private static final Logger LOGGER = LoggerFactory.getLogger(ToolbarComposite.class);
	
	private static final int TOOLBAR_LEFT_MARGIN = 15;
	private static final int NUMBER_OF_TOOLBAR_BUTTONS = 7;
	
	private Image homeIcon = null, addNewAlbumIcon = null, addAlbumItemIcon = null, detailedViewIcon = null, 
			pictureViewIcon = null, advancedSearchIcon = null, synchronizeIcon = null, helpIcon = null;
	private Button homeButton = null, addNewAlbumButton = null, addAlbumItemButton = null,
			toggleViewButton = null, advancedSearchButton = null, synchronizeButton = null, helpButton = null;
	private PanelType lastSelectedPanelType = PanelType.EMPTY;

	void disableActiveButtons() {
		homeButton.setImage(homeIcon);
		addNewAlbumButton.setImage(addNewAlbumIcon);
		addAlbumItemButton.setImage(addAlbumItemIcon);
		toggleViewButton.setImage(detailedViewIcon);
		advancedSearchButton.setImage(advancedSearchIcon);
		synchronizeButton.setImage(synchronizeIcon);
		helpButton.setImage(helpIcon);
	}
	
	private void setButtonsWhenNoAlbumIsSelected() {
		homeButton.setEnabled(true);
		addNewAlbumButton.setEnabled(true);
		addAlbumItemButton.setEnabled(false);
		toggleViewButton.setEnabled(false);
		advancedSearchButton.setEnabled(false);
		synchronizeButton.setEnabled(true);
		helpButton.setEnabled(true);
	}

	public ToolbarComposite(final Composite parentComposite) {
		super(parentComposite, SWT.NONE);
		
		Image homeActiveIcon = null, addNewAlbumActiveIcon = null, addAlbumItemActiveIcon = null, 
		      advancedSearchActiveIcon = null, synchronizeActiveIcon = null, helpActiveIcon = null;
		Composite toolbarComposite = new Composite(parentComposite, SWT.NONE);

		EventObservable.registerObserver(this);
		
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginLeft = TOOLBAR_LEFT_MARGIN;

		toolbarComposite.setLayout(gridLayout);

		Composite innerComposite = new Composite(toolbarComposite, SWT.NONE);

		gridLayout = new GridLayout(NUMBER_OF_TOOLBAR_BUTTONS, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;

		innerComposite.setLayout(gridLayout);

		// retrieve button image
		homeIcon = getImageFromResource("graphics/home.png");
		homeActiveIcon = getImageFromResource("graphics/home-active.png");
		addNewAlbumIcon = getImageFromResource("graphics/add.png");
		addNewAlbumActiveIcon = getImageFromResource("graphics/add-active.png");
		addAlbumItemIcon = getImageFromResource("graphics/additem.png");
		addAlbumItemActiveIcon = getImageFromResource("graphics/additem-active.png");
		detailedViewIcon = getImageFromResource("graphics/detailedview.png");
		pictureViewIcon = getImageFromResource("graphics/pictureview.png");
		advancedSearchIcon = getImageFromResource("graphics/search.png");
		advancedSearchActiveIcon = getImageFromResource("graphics/search-active.png");
		synchronizeIcon = getImageFromResource("graphics/sync.png");
		synchronizeActiveIcon = getImageFromResource("graphics/sync-active.png");
		helpIcon = getImageFromResource("graphics/help.png");
		helpActiveIcon = getImageFromResource("graphics/help-active.png");

		// configure buttons
		homeButton = new Button(innerComposite, SWT.PUSH);
		homeButton.setImage(homeActiveIcon);
		homeButton.setText(Translator.get(DictKeys.BUTTON_HOME));
		homeButton.setToolTipText(Translator.get(DictKeys.BUTTON_TOOLTIP_HOME));

		addNewAlbumButton = new Button(innerComposite, SWT.PUSH);
		addNewAlbumButton.setImage(addNewAlbumIcon);
		addNewAlbumButton.setText(Translator.get(DictKeys.BUTTON_ADD_ALBUM));
		addNewAlbumButton.setToolTipText(Translator.get(DictKeys.BUTTON_TOOLTIP_ADD_ALBUM));

		addAlbumItemButton = new Button(innerComposite, SWT.PUSH);
		addAlbumItemButton.setImage(addAlbumItemIcon);
		addAlbumItemButton.setText(Translator.get(DictKeys.BUTTON_ADD_ENTRY));
		addAlbumItemButton.setToolTipText(Translator.get(DictKeys.BUTTON_TOOLTIP_ADD_ENTRY));
		addAlbumItemButton.setEnabled(false);

		toggleViewButton = new Button(innerComposite, SWT.PUSH);
		toggleViewButton.setImage(detailedViewIcon);
		toggleViewButton.setText(Translator.get(DictKeys.BUTTON_TOGGLE));
		toggleViewButton.setToolTipText(Translator.get(DictKeys.BUTTON_TOOLTIP_TOGGLE_TO_GALLERY));
		toggleViewButton.setEnabled(false);

		advancedSearchButton = new Button(innerComposite, SWT.PUSH);
		advancedSearchButton.setImage(advancedSearchIcon);
		advancedSearchButton.setText(Translator.get(DictKeys.BUTTON_SEARCH));
		advancedSearchButton.setToolTipText(Translator.get(DictKeys.BUTTON_TOOLTIP_SEARCH));
		advancedSearchButton.setEnabled(false);

		synchronizeButton = new Button(innerComposite, SWT.PUSH);
		synchronizeButton.setImage(synchronizeIcon);
		synchronizeButton.setText(Translator.get(DictKeys.BUTTON_SYNCHRONIZE));
		synchronizeButton.setToolTipText(Translator.get(DictKeys.BUTTON_TOOLTIP_SYNCHRONIZE));

		helpButton = new Button(innerComposite, SWT.PUSH);
		helpButton.setImage(helpIcon);
		helpButton.setText(Translator.get(DictKeys.BUTTON_HELP));
		helpButton.setToolTipText(Translator.get(DictKeys.BUTTON_TOOLTIP_HELP));

		// add button listeners
		homeButton.addMouseListener(ToolbarCompositeListener.getHomeButtonListener(
				homeButton, addAlbumItemButton, toggleViewButton, advancedSearchButton, detailedViewIcon, homeActiveIcon));

		addNewAlbumButton.addMouseListener(ToolbarCompositeListener.getAddNewAlbumButtonListener(
				addNewAlbumButton, addAlbumItemButton, toggleViewButton, advancedSearchButton, detailedViewIcon, addNewAlbumActiveIcon));

		addAlbumItemButton.addMouseListener(ToolbarCompositeListener.getAddAlbumItemButtonListener(
				addAlbumItemButton, addAlbumItemActiveIcon));

		toggleViewButton.addMouseListener(ToolbarCompositeListener.getToggleButtonListener(
				toggleViewButton, detailedViewIcon, pictureViewIcon));

		advancedSearchButton.addMouseListener(ToolbarCompositeListener.getAdvancedSearchButtonListener(
				advancedSearchButton, advancedSearchActiveIcon));

		synchronizeButton.addMouseListener(ToolbarCompositeListener.getSynchronizeButtonListener(
				synchronizeButton, synchronizeActiveIcon));

		helpButton.addMouseListener(ToolbarCompositeListener.getHelpButtonListener(
				helpButton, helpActiveIcon));

		// separator
		GridData seperatorGridData = new GridData(GridData.FILL_BOTH);
		seperatorGridData.minimumHeight = 0;
		new Label(toolbarComposite, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(seperatorGridData);
	}
	
	private Image getImageFromResource(String resourcePath) {
		InputStream istream = this.getClass().getClassLoader().getResourceAsStream(resourcePath);
		return new Image(Display.getCurrent(), istream);
	}
	
	@Override
	public void update(SammelboxEvent event) {
		if (event.equals(SammelboxEvent.RIGHT_SIDEPANE_CHANGED)) {
			if (getLastSelectedPanelType() != ApplicationUI.getCurrentRightPanelType()) {
				disableActiveButtons();
			}
		} else if (event.equals(SammelboxEvent.ALBUM_SELECTED)) {
			String currentlySelectedAlbum = ApplicationUI.getSelectedAlbum();
			enableAlbumButtons(currentlySelectedAlbum);
		} else if (event.equals(SammelboxEvent.ALBUM_LIST_UPDATED) && !GuiController.getGuiState().isAlbumSelected()) {				
			setButtonsWhenNoAlbumIsSelected();
		}
	}

	public void enableAlbumButtons(String albumName) {
		homeButton.setImage(homeIcon);
		addAlbumItemButton.setEnabled(true);

		try {
			if (DatabaseOperations.isPictureAlbum(albumName)) {
				if (SettingsManager.getSettings().isDetailedViewDefault()) {
					toggleViewButton.setImage(pictureViewIcon);
					toggleViewButton.setToolTipText(Translator.get(DictKeys.BUTTON_TOOLTIP_TOGGLE_TO_GALLERY));
				} else {
					toggleViewButton.setImage(detailedViewIcon);
					toggleViewButton.setToolTipText(Translator.get(DictKeys.BUTTON_TOOLTIP_TOGGLE_TO_DETAILS));
				}
				
				toggleViewButton.setEnabled(true);
			} else {
				toggleViewButton.setImage(detailedViewIcon);
				toggleViewButton.setEnabled(false);
			}
		} catch (DatabaseWrapperOperationException ex) {
			LOGGER.error("An error occured while checking whether the following album contains pictures: '" + albumName + "'", ex);
		}

		GuiController.getGuiState().setViewDetailed(SettingsManager.getSettings().isDetailedViewDefault());
		advancedSearchButton.setEnabled(true);
	}

	public PanelType getLastSelectedPanelType() {
		return lastSelectedPanelType;
	}

	public void setLastSelectedPanelType(PanelType lastSelectedPanelType) {
		this.lastSelectedPanelType = lastSelectedPanelType;
	}
}
