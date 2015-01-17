package org.sammelbox.view.composites;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.model.GuiState;
import org.sammelbox.view.ApplicationUI;
import org.sammelbox.view.browser.BrowserFacade;
import org.sammelbox.view.sidepanes.*;
import org.sammelbox.view.various.PanelType;

public final class ToolbarCompositeListener {
	private ToolbarCompositeListener() {
		// not needed
	}
	
	static MouseListener getHomeButtonListener(final Button homeButton, final Button addAlbumItemButton, 
			final ChangeViewButton changeViewButton, final Button advancedSearchButton, final Image homeActiveIcon) {
		return new MouseListener() {
			@Override
			public void mouseUp(MouseEvent mouseEvent) {
				BrowserFacade.loadWelcomePage();
				
				ApplicationUI.getToolbarComposite().disableActiveButtons();
				ApplicationUI.changeRightCompositeTo(PanelType.EMPTY, EmptySidepane.build(ApplicationUI.getThreePanelComposite()));
				ApplicationUI.getToolbarComposite().setLastSelectedPanelType(PanelType.EMPTY);

				addAlbumItemButton.setEnabled(false);
				advancedSearchButton.setEnabled(false);
				changeViewButton.setEnabled(false);
				
				homeButton.setImage(homeActiveIcon);

				ApplicationUI.setSelectedAlbumAndReload(GuiState.NO_ALBUM_SELECTED);
				ApplicationUI.getSavedSearchesListBox().removeAll();
			}

			@Override
			public void mouseDown(MouseEvent mouseEvent) {}

			@Override
			public void mouseDoubleClick(MouseEvent mouseEvent) {}
		};
	}
	
	static MouseListener getAddNewAlbumButtonListener(final Button addNewAlbumButton, final Button addAlbumItemButton, 
			final Button advancedSearchButton, final Image addNewAlbumActiveIcon) {
		return new MouseListener() {
			@Override
			public void mouseUp(MouseEvent mouseEvent) {
				
				BrowserFacade.clearAlterationList();
				
				if (ApplicationUI.getCurrentRightPanelType() != PanelType.ADD_ALBUM) {
					ApplicationUI.changeRightCompositeTo(PanelType.ADD_ALBUM, CreateAlbumSidepane.build(ApplicationUI.getThreePanelComposite()));
					StatusBarComposite.getInstance(ApplicationUI.getShell()).writeStatus(Translator.get(DictKeys.STATUSBAR_ADD_ALBUM_OPENED));

					addAlbumItemButton.setEnabled(false);
					advancedSearchButton.setEnabled(false);
					
					ApplicationUI.getToolbarComposite().disableActiveButtons();
					addNewAlbumButton.setImage(addNewAlbumActiveIcon);
					ApplicationUI.setCurrentRightPanelType(PanelType.ADD_ALBUM);
					ApplicationUI.getToolbarComposite().setLastSelectedPanelType(PanelType.ADD_ALBUM);
				} else {
					ApplicationUI.changeRightCompositeTo(PanelType.EMPTY, EmptySidepane.build(ApplicationUI.getThreePanelComposite()));
					StatusBarComposite.getInstance(ApplicationUI.getShell()).writeStatus(Translator.get(DictKeys.STATUSBAR_PROGRAM_STARTED));

					ApplicationUI.getToolbarComposite().disableActiveButtons();
					ApplicationUI.setCurrentRightPanelType(PanelType.EMPTY);
				}
			}

			@Override
			public void mouseDown(MouseEvent mouseEvent) {}

			@Override
			public void mouseDoubleClick(MouseEvent mouseEvent) {}
		};
	}
	
	static MouseListener getAddAlbumItemButtonListener(final Button addAlbumItemIconButton, final Image addAlbumItemActiveIcon) {
		return new MouseListener() {
			@Override
			public void mouseUp(MouseEvent mouseEvent) {				
				if (!ApplicationUI.isAlbumSelectedAndShowMessageIfNot()) {
					return;
				}
				
				if (ApplicationUI.getCurrentRightPanelType() != PanelType.ADD_ENTRY) {					
					ApplicationUI.changeRightCompositeTo(PanelType.ADD_ENTRY, AddAlbumItemSidepane.build(ApplicationUI.getThreePanelComposite(), ApplicationUI.getSelectedAlbum()));
					StatusBarComposite.getInstance(ApplicationUI.getShell()).writeStatus(Translator.get(DictKeys.STATUSBAR_ADD_ITEM_OPENED));

					ApplicationUI.getToolbarComposite().disableActiveButtons();
					addAlbumItemIconButton.setImage(addAlbumItemActiveIcon);
					ApplicationUI.setCurrentRightPanelType(PanelType.ADD_ENTRY);
					ApplicationUI.getToolbarComposite().setLastSelectedPanelType(PanelType.ADD_ENTRY);
				} else {					
					ApplicationUI.changeRightCompositeTo(PanelType.EMPTY, EmptySidepane.build(ApplicationUI.getThreePanelComposite()));
					StatusBarComposite.getInstance(ApplicationUI.getShell()).writeStatus(Translator.get(DictKeys.STATUSBAR_PROGRAM_STARTED));

					ApplicationUI.getToolbarComposite().disableActiveButtons();
					ApplicationUI.setCurrentRightPanelType(PanelType.EMPTY);
				}
			}

			@Override
			public void mouseDown(MouseEvent mouseEvent) {}

			@Override
			public void mouseDoubleClick(MouseEvent mouseEvent) {}
		};
	}
		
	static MouseListener getAdvancedSearchButtonListener(final Button advancedSearchButton, final Image advancedSearchActiveIcon) {
		return new MouseListener() {
			@Override
			public void mouseUp(MouseEvent mouseEvent) {
				if (!ApplicationUI.isAlbumSelectedAndShowMessageIfNot()) {
					return;
				}
				
				if (ApplicationUI.getCurrentRightPanelType() != PanelType.ADVANCED_SEARCH) {
					ApplicationUI.changeRightCompositeTo(PanelType.ADVANCED_SEARCH, AdvancedSearchSidepane.build(ApplicationUI.getThreePanelComposite(),ApplicationUI.getSelectedAlbum()));
					StatusBarComposite.getInstance(ApplicationUI.getShell()).writeStatus(Translator.get(DictKeys.STATUSBAR_SEARCH_OPENED));

					ApplicationUI.getToolbarComposite().disableActiveButtons();
					advancedSearchButton.setImage(advancedSearchActiveIcon);
					ApplicationUI.setCurrentRightPanelType(PanelType.ADVANCED_SEARCH);
					ApplicationUI.getToolbarComposite().setLastSelectedPanelType(PanelType.ADVANCED_SEARCH);
				} else {
					ApplicationUI.changeRightCompositeTo(PanelType.EMPTY, EmptySidepane.build(ApplicationUI.getThreePanelComposite()));
					StatusBarComposite.getInstance(ApplicationUI.getShell()).writeStatus(Translator.get(DictKeys.STATUSBAR_PROGRAM_STARTED));

					ApplicationUI.getToolbarComposite().disableActiveButtons();
					ApplicationUI.setCurrentRightPanelType(PanelType.EMPTY);
				}
			}

			@Override
			public void mouseDown(MouseEvent mouseEvent) {}

			@Override
			public void mouseDoubleClick(MouseEvent mouseEvent) {}
		};
	}
	
	static MouseListener getSynchronizeButtonListener(final Button synchronizeButton, final Image synchronizeActiveIcon) {
		return new MouseListener() {
			@Override
			public void mouseUp(MouseEvent mouseEvent) {
				BrowserFacade.showHtmlPage("synchronize");
				
				if (ApplicationUI.getCurrentRightPanelType() != PanelType.SYNCHRONIZATION) {
					ApplicationUI.changeRightCompositeTo(PanelType.SYNCHRONIZATION, SynchronizeSidepane.build(ApplicationUI.getThreePanelComposite()));
					StatusBarComposite.getInstance(ApplicationUI.getShell()).writeStatus(Translator.get(DictKeys.STATUSBAR_SYNCHRONIZE_OPENED));

					ApplicationUI.getToolbarComposite().disableActiveButtons();
					synchronizeButton.setImage(synchronizeActiveIcon);
					ApplicationUI.setCurrentRightPanelType(PanelType.SYNCHRONIZATION);
					ApplicationUI.getToolbarComposite().setLastSelectedPanelType(PanelType.SYNCHRONIZATION);
				} else {
					ApplicationUI.changeRightCompositeTo(PanelType.EMPTY, EmptySidepane.build(ApplicationUI.getThreePanelComposite()));
					StatusBarComposite.getInstance(ApplicationUI.getShell()).writeStatus(Translator.get(DictKeys.STATUSBAR_PROGRAM_STARTED));

					ApplicationUI.getToolbarComposite().disableActiveButtons();
					ApplicationUI.setCurrentRightPanelType(PanelType.EMPTY);
				}
			}

			@Override
			public void mouseDown(MouseEvent mouseEvent) {}

			@Override
			public void mouseDoubleClick(MouseEvent mouseEvent) {}
		};
	}
	
	static MouseListener getHelpButtonListener(final Button helpButton, final Image helpActiveIcon) {
		return new MouseListener() {
			@Override
			public void mouseUp(MouseEvent mouseEvent) {
				if (ApplicationUI.getCurrentRightPanelType() != PanelType.HELP) {
					BrowserFacade.showHtmlPage("help");
					ApplicationUI.changeRightCompositeTo(PanelType.HELP, EmptySidepane.build(ApplicationUI.getThreePanelComposite()));
					StatusBarComposite.getInstance(ApplicationUI.getShell()).writeStatus(Translator.get(DictKeys.STATUSBAR_HELP_OPENED));

					ApplicationUI.getToolbarComposite().disableActiveButtons();
					helpButton.setImage(helpActiveIcon);
					ApplicationUI.setCurrentRightPanelType(PanelType.EMPTY);
					ApplicationUI.getToolbarComposite().setLastSelectedPanelType(PanelType.EMPTY);
				} else {
					ApplicationUI.changeRightCompositeTo(PanelType.EMPTY, EmptySidepane.build(ApplicationUI.getThreePanelComposite()));
					StatusBarComposite.getInstance(ApplicationUI.getShell()).writeStatus(Translator.get(DictKeys.STATUSBAR_PROGRAM_STARTED));

					ApplicationUI.getToolbarComposite().disableActiveButtons();
					ApplicationUI.setCurrentRightPanelType(PanelType.EMPTY);
				}
			}

			@Override
			public void mouseDown(MouseEvent mouseEvent) {}

			@Override
			public void mouseDoubleClick(MouseEvent mouseEvent) {}
		};
	}
}
