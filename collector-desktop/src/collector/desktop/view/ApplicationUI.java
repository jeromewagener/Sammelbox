package collector.desktop.view;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import collector.desktop.controller.filesystem.FileSystemAccessWrapper;
import collector.desktop.controller.interfaces.UIObservable;
import collector.desktop.controller.interfaces.UIObserver;
import collector.desktop.model.database.DatabaseIntegrityManager;
import collector.desktop.model.database.DatabaseWrapper;
import collector.desktop.model.database.QueryBuilder;
import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException;
import collector.desktop.model.database.exceptions.ExceptionHelper;
import collector.desktop.view.browser.BrowserFacade;
import collector.desktop.view.composites.BrowserComposite;
import collector.desktop.view.composites.StatusBarComposite;
import collector.desktop.view.composites.ToolbarComposite;
import collector.desktop.view.internationalization.DictKeys;
import collector.desktop.view.internationalization.Translator;
import collector.desktop.view.listeners.BrowserListener;
import collector.desktop.view.managers.AlbumManager;
import collector.desktop.view.managers.AlbumViewManager;
import collector.desktop.view.managers.AlbumViewManager.AlbumView;
import collector.desktop.view.managers.MenuManager;
import collector.desktop.view.sidepanes.EmptySidepane;
import collector.desktop.view.sidepanes.QuickControlSidepane;
import collector.desktop.view.various.ComponentFactory;
import collector.desktop.view.various.LoadingOverlayShell;
import collector.desktop.view.various.PanelType;

public class ApplicationUI implements UIObservable, UIObserver {
	private static final int RIGHT_PANEL_LARGE_WIDTH = 320;
	private static final int RIGHT_PANEL_MEDIUM_WIDTH = 225;
	private static final int RIGHT_PANEL_SMALL_WIDTH = 150;
	private static final int RIGHT_PANEL_NO_WIDTH = 0;

	/** The minimum width of the shell in pixels. The shell can never have a smaller width than this. */
	private static final int MIN_SHELL_WIDTH = 1110;
	/** The minimum height of the shell in pixels. The shell can never have a smaller height than this. */
	private static final int MIN_SHELL_HEIGHT = 700;
	/** A reference to the main display */
	private final static Display display = new Display();
	/** A reference to the main shell */
	private final static Shell shell = new Shell(display);
	/** A reference to a composite being part of the general user interface */
	private static Composite threePanelComposite = null, upperLeftSubComposite = null, lowerLeftSubComposite = null, 
			leftComposite = null, rightComposite = null, centerComposite = null, statusComposite = null, toolbarComposite = null;
	/** The currently selected album. The selected album changes via selections within the album list */
	private static String selectedAlbum = "";
	/** A reference to the SWT list containing all available albums */
	private static List albumSWTList;
	/** A reference to the SWT Text representing the quickSearch field*/
	private static Text quickSearchTextField;
	/** A reference to the SWT list containing all available views */
	private static List viewSWTList;
	/** True if the current view is list based, false if item based (picture based) */
	private static boolean viewIsDetailed = true;	
	/** A reference to the SWT browser in charge of presenting album items */
	private static Browser albumItemSWTBrowser;
	/** A reference to the SWT album item browser listener*/
	private static BrowserListener albumItemSWTBrowserListener;
	/** The panel type that is currently visible on the right of the main three panel composite */
	private static PanelType currentRightPanelType = PanelType.Empty;
	/** A list of observers, waiting for certain global changes */
	private static ArrayList<UIObserver> observers = new ArrayList<UIObserver>();
	/** An instance to the main collector */
	private static ApplicationUI instance = null;
	/** This flag indicates if an error (e.g. corrupt db) was encountered during startup*/
	private static boolean normalStartup = true;
	/** The normal logger for all info, debug, error and warning in the collector class */
	private final static Logger LOGGER = LoggerFactory.getLogger(ApplicationUI.class);

	private ApplicationUI() {
	}

	/** This method creates the main user interface. This involves the creation of different sub-composites 
	 * using the CompositeFactory 
	 * @param shell the shell used to create the user interface */
	public static void createCollectorShell(final Shell shell) {				
		// set program icon
		shell.setImage(new Image(shell.getDisplay(), FileSystemAccessWrapper.LOGO_SMALL));
		
		// setup the Layout for the shell
		GridLayout shellGridLayout = new GridLayout(1, false);
		shellGridLayout.marginHeight = 0;
		shellGridLayout.marginWidth = 0;		
		shell.setMinimumSize(MIN_SHELL_WIDTH, MIN_SHELL_HEIGHT);

		// setup the Shell
		shell.setText(Translator.get(DictKeys.TITLE_MAIN_WINDOW));				
		shell.setLayout(shellGridLayout);

		// center the shell to primary screen
		Monitor primaryMonitor = display.getPrimaryMonitor();
		Rectangle primaryMonitorBounds = primaryMonitor.getClientArea();
		Rectangle shellBounds = shell.getBounds();
		int xCoordinateForShell = primaryMonitorBounds.x + (primaryMonitorBounds.width - shellBounds.width) / 2;
		int yCoordinateForShell = primaryMonitorBounds.y + (primaryMonitorBounds.height - shellBounds.height) / 2;
		shell.setLocation(xCoordinateForShell, yCoordinateForShell);

		// define toolbar composite layout data
		GridData gridDataForToolbarComposite = new GridData(GridData.FILL_BOTH);
		gridDataForToolbarComposite.grabExcessHorizontalSpace = true;
		gridDataForToolbarComposite.grabExcessVerticalSpace = false;

		// define three panel composite layout data
		GridData gridDataForThreePanelComposite = new GridData(GridData.FILL_BOTH);
		gridDataForThreePanelComposite.grabExcessHorizontalSpace = true;
		gridDataForThreePanelComposite.grabExcessVerticalSpace = true;
		gridDataForThreePanelComposite.verticalAlignment = GridData.FILL;
		gridDataForThreePanelComposite.horizontalAlignment = GridData.FILL;
		GridLayout mainGridLayout = new GridLayout(3, false);

		// define left (upper & lower) composite layout data
		GridData gridDataForLeftComposite = new GridData(GridData.FILL_BOTH);
		gridDataForLeftComposite.grabExcessHorizontalSpace = false;
		GridData gridDataForUpperLeftComposite = new GridData(GridData.FILL_BOTH);
		gridDataForUpperLeftComposite.verticalAlignment = GridData.BEGINNING;
		GridData gridDataForLowerLeftComposite = new GridData(GridData.FILL_BOTH);
		gridDataForLowerLeftComposite.verticalAlignment = GridData.END;

		// define center composite layout data
		GridData gridDataForCenterComposite = new GridData(SWT.FILL, SWT.FILL, true, true);

		// define right composite layout data
		GridData gridDataForRightComposite = new GridData(GridData.FILL_BOTH);
		gridDataForRightComposite.grabExcessHorizontalSpace = false;
		gridDataForRightComposite.verticalAlignment = GridData.BEGINNING;

		// define statusbar composite layout data
		GridData gridDataForStatusBarComposite = new GridData(GridData.FILL_BOTH);
		gridDataForStatusBarComposite.grabExcessHorizontalSpace = true;
		gridDataForStatusBarComposite.grabExcessVerticalSpace = false;

		// Setup composites using layout definitions from before
		toolbarComposite = ToolbarComposite.getInstance(shell).getToolBarComposite();
		toolbarComposite.setLayout(new GridLayout(1, false));
		toolbarComposite.setLayoutData(gridDataForToolbarComposite);

		threePanelComposite = new Composite(shell, SWT.NONE);
		threePanelComposite.setLayout(mainGridLayout);
		threePanelComposite.setLayoutData(gridDataForThreePanelComposite);

		leftComposite = new Composite(threePanelComposite, SWT.NONE);
		leftComposite.setLayout(new GridLayout(1, false));
		leftComposite.setLayoutData(gridDataForLeftComposite);
		upperLeftSubComposite = QuickControlSidepane.build(leftComposite);
		upperLeftSubComposite.setLayoutData(gridDataForUpperLeftComposite);
		lowerLeftSubComposite = EmptySidepane.build(leftComposite);		
		lowerLeftSubComposite.setLayoutData(gridDataForLowerLeftComposite);
		albumItemSWTBrowserListener = new BrowserListener(threePanelComposite);
		centerComposite = BrowserComposite.getBrowserComposite(threePanelComposite, albumItemSWTBrowserListener);
		centerComposite.setLayout(new GridLayout(1, false));
		centerComposite.setLayoutData(gridDataForCenterComposite);
		rightComposite = EmptySidepane.build(threePanelComposite);
		rightComposite.setLayout(new GridLayout(1, false));
		rightComposite.setLayoutData(gridDataForRightComposite);

		statusComposite = StatusBarComposite.getInstance(shell).getStatusbarComposite();
		statusComposite.setLayout(new GridLayout(1, false));
		statusComposite.setLayoutData(gridDataForStatusBarComposite);

		// Create the menu bar
		MenuManager.createAndInitializeMenuBar(shell);
		
		// Register the toolbar as an observer for collector updates
		AlbumManager.getInstance().registerObserver(instance);
		ToolbarComposite.getInstance(ApplicationUI.getShell()).registerAsObserverToCollectorUpdates();
		AlbumViewManager.getInstance().registerObserver(instance);
		
		// SWT display management
		shell.pack();

		Rectangle displayClientArea = display.getPrimaryMonitor().getClientArea();
		if (maximizeShellOnStartUp(displayClientArea.width, displayClientArea.height)){
			shell.setMaximized(true);
		}
		
		createAutosaveOverlay();		
		
		shell.open();
		
		if (!normalStartup) {
			ComponentFactory.getMessageBox(shell, 
					Translator.toBeTranslated("Fatal error occured during startup"), 
					Translator.toBeTranslated("The database is corrupt and was removed. A snapshot can be found in the program folder"),
					SWT.ICON_INFORMATION)
				.open();
		}

		selectDefaultAndShowWelcomePage();		

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		display.dispose();
	}

	public static Composite getThreePanelComposite() {
		return threePanelComposite;
	}

	public static void selectDefaultAndShowWelcomePage() {
		if (albumSWTList.getItemCount() > 0) {
			albumSWTList.setSelection(-1);
		}

		BrowserFacade.loadWelcomePage();
	}
	
	/** This method exchanges the center composite with a composite provided as parameter. Hereby, the previous composite is disposed. 
	 * @param newCenterComposite the new composite for the center element of the user interface */
	public static void changeCenterCompositeTo(Composite newCenterComposite) {
		Layout layout = centerComposite.getLayout();
		GridData layoutData = (GridData) centerComposite.getLayoutData();

		centerComposite.dispose();
		newCenterComposite.setLayout(layout);
		newCenterComposite.setLayoutData(layoutData);

		centerComposite = newCenterComposite;
		centerComposite.moveAbove(rightComposite);
		centerComposite.getParent().layout();
	}

	/** This method exchanges the lower left composite with a composite provided as parameter. Hereby, the previous composite is disposed. 
	 * @param newLowerLeftComposite the new composite for the lower left element of the user interface */
	public static void changeLowerLeftCompositeTo(Composite newLowerLeftComposite) {
		Layout layout = lowerLeftSubComposite.getLayout();
		GridData layoutData = (GridData) lowerLeftSubComposite.getLayoutData();

		lowerLeftSubComposite.dispose();
		newLowerLeftComposite.setLayout(layout);
		newLowerLeftComposite.setLayoutData(layoutData);

		lowerLeftSubComposite = newLowerLeftComposite;
		lowerLeftSubComposite.moveBelow(upperLeftSubComposite);
		lowerLeftSubComposite.getParent().layout();
	}

	public static HashMap<PanelType, Integer> panelTypeToPixelSize = new HashMap<PanelType, Integer>() {
		private static final long serialVersionUID = 1L;	{
			put(PanelType.Empty, RIGHT_PANEL_NO_WIDTH);
			put(PanelType.AddAlbum, RIGHT_PANEL_LARGE_WIDTH);
			put(PanelType.AddEntry, RIGHT_PANEL_LARGE_WIDTH);
			put(PanelType.AdvancedSearch, RIGHT_PANEL_LARGE_WIDTH);
			put(PanelType.AlterAlbum, RIGHT_PANEL_LARGE_WIDTH);
			put(PanelType.Synchronization, RIGHT_PANEL_MEDIUM_WIDTH);
			put(PanelType.UpdateEntry, RIGHT_PANEL_LARGE_WIDTH);
			put(PanelType.Help, RIGHT_PANEL_SMALL_WIDTH);
		}
	};

	public static void resizeRightCompositeTo(int pixels) {
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.grabExcessHorizontalSpace = false;
		layoutData.grabExcessVerticalSpace = true;
		layoutData.verticalAlignment = GridData.BEGINNING;
		layoutData.widthHint = pixels;

		rightComposite.setLayoutData(layoutData);
		rightComposite.getParent().layout();
	}

	/** This method exchanges the right composite with a composite provided as parameter. Hereby, the previous composite is disposed. 
	 * @param newRightComposite the new composite for the right element of the user interface */
	public static void changeRightCompositeTo(PanelType panelType, Composite newRightComposite) {
		currentRightPanelType = panelType;

		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.grabExcessHorizontalSpace = false;
		layoutData.grabExcessVerticalSpace = true;
		layoutData.verticalAlignment = GridData.BEGINNING;

		if (panelTypeToPixelSize.containsKey(panelType)) {
			if (ScrolledComposite.class.isInstance(newRightComposite)) {
				ScrolledComposite sc = (ScrolledComposite) newRightComposite;
				layoutData.widthHint = panelTypeToPixelSize.get(panelType) - sc.getVerticalBar().getSize().x;
			} else {		
				layoutData.widthHint = panelTypeToPixelSize.get(panelType);
			}
		} else {
			if (ScrolledComposite.class.isInstance(newRightComposite)) {
				ScrolledComposite sc = (ScrolledComposite) newRightComposite;
				layoutData.widthHint = RIGHT_PANEL_MEDIUM_WIDTH - sc.getVerticalBar().getSize().x;
			} else {		
				layoutData.widthHint = RIGHT_PANEL_MEDIUM_WIDTH;
			}
		}

		newRightComposite.setLayoutData(layoutData);

		rightComposite.dispose();
		rightComposite = newRightComposite;
		rightComposite.moveBelow(centerComposite);
		rightComposite.getParent().layout();

		instance.notifyObservers();
	}

	/** Returns the currently selected/active album or view
	 * @return the currently selected/active album or view */
	public static String getSelectedAlbum() {
		return selectedAlbum;
	}

	/**
	 * Determines is an album has been selected.
	 * @return True if the selectedAlbumName is not null and not empty. True if an album is selected.
	 */
	public static boolean hasSelectedAlbum() {
		if (selectedAlbum != null && !selectedAlbum.isEmpty()) {
			return true;
		}
		return false;
	}
	
	public static void setQuickSearchTextField(Text quickSearchTextField) {
		ApplicationUI.quickSearchTextField = quickSearchTextField;
	}
	
	public static Text getQuickSearchTextField() {
		return ApplicationUI.quickSearchTextField;
	}

	/** Sets the currently selected/active album
	 * @param albumName The name of the now selected/active album. If the albumName is null or empty then all albums are deselected.  
	 * @return True if the album is selected internally and in the SWT Album list. If all albums were successfully deselected then true is also returned. 
	 * False otherwise.*/
	public static boolean setSelectedAlbum(String albumName) {
		// Set the album name and verify that it is in the list
		ApplicationUI.selectedAlbum = albumName;
		if (albumName== null || albumName.isEmpty()) {
			ApplicationUI.albumSWTList.deselectAll();
			return true;
		}
		
		int albumListItemCount = ApplicationUI.albumSWTList.getItemCount();
		boolean albumSelectionIsInSync = false;
		for (int itemIndex = 0; itemIndex<albumListItemCount; itemIndex++) {
			 if ( ApplicationUI.albumSWTList.getItem(itemIndex).equals(albumName) ) {
				 ApplicationUI.albumSWTList.setSelection(itemIndex);
				 albumSelectionIsInSync = true;
				 break;
			 }
		}
		if (!albumSelectionIsInSync){
			LOGGER.error("The album list does not contain the album that is supposed to be selected");
			return false;
		}
	
		ApplicationUI.getQuickSearchTextField().setText("");
		try {
			ApplicationUI.getQuickSearchTextField().setEnabled(DatabaseWrapper.isAlbumQuicksearchable(albumName));
		} catch (DatabaseWrapperOperationException ex) {
			LOGGER.error("An error occured while enabling the quick search field \n Stacktrace: " + ExceptionHelper.toString(ex));
		}

		BrowserFacade.performBrowserQueryAndShow(QueryBuilder.createSelectStarQuery(albumName));
		
		ApplicationUI.getViewSWTList().setEnabled(AlbumViewManager.hasAlbumViewsAttached(albumName));
		AlbumViewManager.getInstance().notifyObservers();
		ToolbarComposite.getInstance(ApplicationUI.getThreePanelComposite()).enableAlbumButtons(albumName);
		
		return true;
	}
	
	/** After adding/removing albums, this method should be used to refresh the SWT album list with the current album names thus leaving no album selected.*/
	public static void refreshSWTAlbumList() {
		instance.update(AlbumManager.class);
		ApplicationUI.getQuickSearchTextField().setEnabled(false);
	}

	/** Sets the the list of albums
	 * @param albumSWTList the list of albums */ 
	public static void setAlbumSWTList(List albumSWTList) {
		ApplicationUI.albumSWTList = albumSWTList;
	}

	/** Returns the list of albums 
	 * @return the album SWT list */
	public static List getAlbumSWTList() {
		return albumSWTList;
	}

	/** Sets the the list of views
	 * @param albumSWTList the list of albums */ 
	public static void setViewSWTList(List viewSWTList) {
		ApplicationUI.viewSWTList = viewSWTList;
	}

	/** Returns the list of views 
	 * @return the album SWT list */
	public static List getViewSWTList() {
		return viewSWTList;
	}

	/** Sets the album item SWT browser
	 * @param browser the reference to the albumItemSWTBrowser */
	public static void setAlbumItemSWTBrowser(Browser browser) {
		ApplicationUI.albumItemSWTBrowser = browser;
	}

	/** Returns the album item SWT browser
	 * @return the album item SWT browser */
	public static Browser getAlbumItemSWTBrowser() {
		return albumItemSWTBrowser;
	}
	
	public static PanelType getCurrentRightPanelType() {
		return currentRightPanelType;
	}

	public static void setCurrentRightPanelType(PanelType currentRightPanel) {
		ApplicationUI.currentRightPanelType = currentRightPanel;
	}

	public void registerObserver(UIObserver observer) {
		observers.add(observer);
	}

	public void unregisterObserver(UIObserver observer) {
		observers.remove(observer);
	}

	public void unregisterAllObservers() {
		observers.clear();
	}

	public void notifyObservers() {
		for (UIObserver observer : observers) {
			observer.update(this.getClass());
		}
	}

	public static ApplicationUI getInstance() {
		if (instance == null) {
			instance = new ApplicationUI();
		}
		
		return instance;
	}

	public static boolean isViewDetailed() {
		return viewIsDetailed;
	}

	public static void setViewIsDetailed(boolean viewIsDetailed) {
		ApplicationUI.viewIsDetailed = viewIsDetailed;
	}

	public static Shell getShell() {
		return shell;
	}

	@Override
	public void update(Class<?> origin) {
		if (origin == AlbumManager.class) {
			albumSWTList.removeAll();
			
			for (String album : AlbumManager.getInstance().getAlbums()) {
				albumSWTList.add(album);
			}
		} else if (origin == AlbumViewManager.class) {
			viewSWTList.removeAll();

			for (AlbumView albumView : AlbumViewManager.getAlbumViews(selectedAlbum)) {
				viewSWTList.add(albumView.getName());				
			}
			
			if (viewSWTList.isEnabled() == false && viewSWTList.getItemCount() != 0) {
				viewSWTList.setEnabled(true);
			}
		}
	}
	
	/**
	 * When the horizontal or vertical screen resolution is smaller than their respective thresholds
	 * {@value #MIN_DISPLAY_WIDTH_BEFORE_MAXIMIZE} and {@link #MIN_DISPLAY_HEIGHT_BEFORE_MAXIMIZE} then
	 * it returns true, False otherwise.
	 */
	public static boolean maximizeShellOnStartUp(int screenWidth, int screenHeight) {
		if (MIN_SHELL_WIDTH >= screenWidth || MIN_SHELL_HEIGHT >= screenHeight){
			return true;
		}
		return false;
	}
	
	private static LoadingOverlayShell createAutosaveOverlay () {
		final LoadingOverlayShell loadingOverlayShell = new LoadingOverlayShell(shell, Translator.toBeTranslated("Autosaving the database"));
		loadingOverlayShell.setCloseParentWhenDone(true);
		shell.addListener(SWT.Close, new Listener() {			
			@Override
			public void handleEvent(Event event) {
				// Show the loading overlay while creating a database autosave. 
				if (!loadingOverlayShell.isDone()) {
					launchLoadingOverlayShell(loadingOverlayShell, false);					
					event.doit =  false;
				}
			}
		});		
		return loadingOverlayShell;
	}
	
	private static void launchLoadingOverlayShell(final LoadingOverlayShell shell, boolean useWorkerThread) {
		shell.start();
		if (useWorkerThread){
			final Thread performer = new Thread(new Runnable() {
				@Override
				public void run() {
					createAutoSaveOfDatabase(shell);
				}
			});
			performer.start();
		}else {
			createAutoSaveOfDatabase(shell);
		}
	}

	private static void createAutoSaveOfDatabase(LoadingOverlayShell shell) {
		// Backup the database in a Thread running in parallel to the SWT UI Thread. 
		try {
			DatabaseIntegrityManager.backupAutoSave();
		} catch (DatabaseWrapperOperationException ex) {
			LOGGER.error("Couldn't backup the auto save \n Stacktrace:" + ExceptionHelper.toString(ex));
		}
		shell.stop();
	}	
}
