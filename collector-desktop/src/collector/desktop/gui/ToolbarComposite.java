package collector.desktop.gui;

import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import collector.desktop.Collector;
import collector.desktop.database.DatabaseWrapper;
import collector.desktop.interfaces.UIObserver;

public class ToolbarComposite implements UIObserver {
	private static ToolbarComposite instance = null;
	private Composite toolbarComposite = null;
	private Image home = null, addAlbum = null, addEntry = null,
			detailedView = null, pictureView = null, search = null,
			sync = null, help = null;
	private Image homeActive = null, addAlbumActive = null,
			addEntryActive = null, searchActive = null, syncActive = null,
			helpActive = null;
	private Button homeBtn = null, addAlbumBtn = null, addEntryBtn = null,
			viewBtn = null, searchBtn = null, syncBtn = null, helpBtn = null;
	private PanelType lastSelectedPanelType = PanelType.Empty;

	private void disableActiveButtons() {
		homeBtn.setImage(home);
		addAlbumBtn.setImage(addAlbum);
		addEntryBtn.setImage(addEntry);
		viewBtn.setImage(detailedView);
		searchBtn.setImage(search);
		syncBtn.setImage(sync);
		helpBtn.setImage(help);
	}

	private ToolbarComposite(final Composite parentComposite) {
		toolbarComposite = new Composite(parentComposite, SWT.NONE);

		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;

		toolbarComposite.setLayout(gridLayout);

		Composite innerComposite = new Composite(toolbarComposite, SWT.NONE);

		gridLayout = new GridLayout(7, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;

		innerComposite.setLayout(gridLayout);

		InputStream istream = this.getClass().getClassLoader()
				.getResourceAsStream("graphics/home.png");
		home = new Image(Display.getCurrent(), istream);
		istream = this.getClass().getClassLoader()
				.getResourceAsStream("graphics/home-active.png");
		homeActive = new Image(Display.getCurrent(), istream);

		istream = this.getClass().getClassLoader()
				.getResourceAsStream("graphics/add.png");
		addAlbum = new Image(Display.getCurrent(), istream);
		istream = this.getClass().getClassLoader()
				.getResourceAsStream("graphics/add-active.png");
		addAlbumActive = new Image(Display.getCurrent(), istream);

		istream = this.getClass().getClassLoader()
				.getResourceAsStream("graphics/additem.png");
		addEntry = new Image(Display.getCurrent(), istream);
		istream = this.getClass().getClassLoader()
				.getResourceAsStream("graphics/additem-active.png");
		addEntryActive = new Image(Display.getCurrent(), istream);

		istream = this.getClass().getClassLoader()
				.getResourceAsStream("graphics/detailedview.png");
		detailedView = new Image(Display.getCurrent(), istream);
		istream = this.getClass().getClassLoader()
				.getResourceAsStream("graphics/pictureview.png");
		pictureView = new Image(Display.getCurrent(), istream);

		istream = this.getClass().getClassLoader()
				.getResourceAsStream("graphics/search.png");
		search = new Image(Display.getCurrent(), istream);
		istream = this.getClass().getClassLoader()
				.getResourceAsStream("graphics/search-active.png");
		searchActive = new Image(Display.getCurrent(), istream);

		istream = this.getClass().getClassLoader()
				.getResourceAsStream("graphics/sync.png");
		sync = new Image(Display.getCurrent(), istream);
		istream = this.getClass().getClassLoader()
				.getResourceAsStream("graphics/sync-active.png");
		syncActive = new Image(Display.getCurrent(), istream);

		istream = this.getClass().getClassLoader()
				.getResourceAsStream("graphics/help.png");
		help = new Image(Display.getCurrent(), istream);
		istream = this.getClass().getClassLoader()
				.getResourceAsStream("graphics/help-active.png");
		helpActive = new Image(Display.getCurrent(), istream);

		homeBtn = new Button(innerComposite, SWT.PUSH);
		homeBtn.setImage(homeActive);
		homeBtn.setText("Home");
		homeBtn.setToolTipText("Go to the welcome screen");

		addAlbumBtn = new Button(innerComposite, SWT.PUSH);
		addAlbumBtn.setImage(addAlbum);
		addAlbumBtn.setText("Add Album");
		addAlbumBtn.setToolTipText("Create a new album");

		addEntryBtn = new Button(innerComposite, SWT.PUSH);
		addEntryBtn.setImage(addEntry);
		addEntryBtn.setText("Add Entry");
		addEntryBtn
				.setToolTipText("Add an entry to the currently active album");
		addEntryBtn.setEnabled(false);

		viewBtn = new Button(innerComposite, SWT.PUSH);
		viewBtn.setImage(detailedView);
		viewBtn.setText("Toggle Views");
		viewBtn.setToolTipText("Toggle to gallery mode");
		viewBtn.setEnabled(false);

		searchBtn = new Button(innerComposite, SWT.PUSH);
		searchBtn.setImage(search);
		searchBtn.setText("Search");
		searchBtn.setToolTipText("Open the advanced search panel");
		searchBtn.setEnabled(false);

		syncBtn = new Button(innerComposite, SWT.PUSH);
		syncBtn.setImage(sync);
		syncBtn.setText("Synchronize");
		syncBtn.setToolTipText("Synchronize with mobile device");

		helpBtn = new Button(innerComposite, SWT.PUSH);
		helpBtn.setImage(help);
		helpBtn.setText("Help");
		helpBtn.setToolTipText("Open the program's internal help");

		// ---------- Add Mouse Listeners ----------

		homeBtn.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				disableActiveButtons();

				homeBtn.setImage(homeActive);

				BrowserContent.loadWelcomePage();

				addEntryBtn.setEnabled(false);

				viewBtn.setEnabled(false);
				viewBtn.setImage(detailedView);
				viewBtn.setToolTipText("Toggle to gallery mode");

				searchBtn.setEnabled(false);
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
			}
		});

		addAlbumBtn.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				if (Collector.getCurrentRightPanelType() != PanelType.AddAlbum) {
					Collector.changeRightCompositeTo(PanelType.AddAlbum,
							CompositeFactory
									.getCreateNewAlbumComposite(Collector
											.getThreePanelComposite()));
					StatusBarComposite.getInstance(parentComposite.getShell())
							.writeStatus("You can now add a new album...");

					disableActiveButtons();
					addAlbumBtn.setImage(addAlbumActive);
					Collector.setCurrentRightPanelType(PanelType.AddAlbum);
					lastSelectedPanelType = PanelType.AddAlbum;
				} else {
					Collector.changeRightCompositeTo(PanelType.Empty,
							CompositeFactory.getEmptyComposite(Collector
									.getThreePanelComposite()));
					StatusBarComposite.getInstance(parentComposite.getShell())
							.writeStatus("Collector started...");

					disableActiveButtons();
					Collector.setCurrentRightPanelType(PanelType.Empty);
				}
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
			}
		});

		addEntryBtn.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				if (!Collector.hasSelectedAlbum()) {
					Collector
							.showErrorDialog("No album has been selected",
									"Please select an album from the list or create one first.");
					return;
				}
				if (Collector.getCurrentRightPanelType() != PanelType.AddEntry) {
					Collector.changeRightCompositeTo(PanelType.AddAlbum,
							CompositeFactory.getAddAlbumItemComposite(
									Collector.getThreePanelComposite(),
									Collector.getSelectedAlbum()));
					StatusBarComposite
							.getInstance(parentComposite.getShell())
							.writeStatus(
									"You can now add an item to the currently active album...");

					disableActiveButtons();
					addEntryBtn.setImage(addEntryActive);
					Collector.setCurrentRightPanelType(PanelType.AddEntry);
					lastSelectedPanelType = PanelType.AddEntry;
				} else {
					Collector.changeRightCompositeTo(PanelType.Empty,
							CompositeFactory.getEmptyComposite(Collector
									.getThreePanelComposite()));
					StatusBarComposite.getInstance(parentComposite.getShell())
							.writeStatus("Collector started...");

					disableActiveButtons();
					Collector.setCurrentRightPanelType(PanelType.Empty);
				}
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
			}
		});

		viewBtn.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				if (!Collector.isViewDetailed()) {
					viewBtn.setImage(pictureView);
					viewBtn.setToolTipText("Toggle to gallery mode");

					Collector.setViewIsDetailed(true);

					BrowserContent.performLastQuery(Collector
							.getAlbumItemSWTBrowser());
				} else {
					viewBtn.setImage(detailedView);
					viewBtn.setToolTipText("Toggle to details view");

					Collector.setViewIsDetailed(false);

					BrowserContent.performLastQuery(Collector
							.getAlbumItemSWTBrowser());
				}
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
			}
		});

		searchBtn.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				if (!Collector.hasSelectedAlbum()) {
					Collector
							.showErrorDialog("No album has been selected",
									"Please select an album from the list or create one first.");
					return;
				}
				if (Collector.getCurrentRightPanelType() != PanelType.AdvancedSearch) {
					Collector.changeRightCompositeTo(PanelType.AdvancedSearch,
							CompositeFactory.getAdvancedSearchComposite(
									Collector.getThreePanelComposite(),
									Collector.getSelectedAlbum()));
					StatusBarComposite
							.getInstance(parentComposite.getShell())
							.writeStatus(
									"You can now search for different items...");

					disableActiveButtons();
					searchBtn.setImage(searchActive);
					Collector
							.setCurrentRightPanelType(PanelType.AdvancedSearch);
					lastSelectedPanelType = PanelType.AdvancedSearch;
				} else {
					Collector.changeRightCompositeTo(PanelType.Empty,
							CompositeFactory.getEmptyComposite(Collector
									.getThreePanelComposite()));
					StatusBarComposite.getInstance(parentComposite.getShell())
							.writeStatus("Collector started...");

					disableActiveButtons();
					Collector.setCurrentRightPanelType(PanelType.Empty);
				}
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
			}
		});

		syncBtn.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				if (Collector.getCurrentRightPanelType() != PanelType.Synchronization) {
					Collector.changeRightCompositeTo(PanelType.Synchronization,
							CompositeFactory.getSynchronizeComposite(Collector
									.getThreePanelComposite()));
					StatusBarComposite
							.getInstance(parentComposite.getShell())
							.writeStatus(
									"You can now synchronize with your mobile device...");

					disableActiveButtons();
					syncBtn.setImage(syncActive);
					Collector
							.setCurrentRightPanelType(PanelType.Synchronization);
					lastSelectedPanelType = PanelType.Synchronization;
				} else {
					Collector.changeRightCompositeTo(PanelType.Empty,
							CompositeFactory.getEmptyComposite(Collector
									.getThreePanelComposite()));
					StatusBarComposite.getInstance(parentComposite.getShell())
							.writeStatus("Collector started...");

					disableActiveButtons();
					Collector.setCurrentRightPanelType(PanelType.Empty);
				}
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
			}
		});

		helpBtn.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				if (Collector.getCurrentRightPanelType() != PanelType.Help) {
					BrowserContent.loadHelpPage();
					Collector.changeRightCompositeTo(PanelType.Help,
							CompositeFactory.getEmptyComposite(Collector
									.getThreePanelComposite()));
					StatusBarComposite.getInstance(parentComposite.getShell())
							.writeStatus("You can now browse the help...");

					disableActiveButtons();
					helpBtn.setImage(helpActive);
					Collector.setCurrentRightPanelType(PanelType.Empty);
					lastSelectedPanelType = PanelType.Empty;
				} else {
					Collector.changeRightCompositeTo(PanelType.Empty,
							CompositeFactory.getEmptyComposite(Collector
									.getThreePanelComposite()));
					StatusBarComposite.getInstance(parentComposite.getShell())
							.writeStatus("Collector started...");

					disableActiveButtons();
					Collector.setCurrentRightPanelType(PanelType.Empty);
				}
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
			}
		});

		GridData seperatorGridData = new GridData(GridData.FILL_BOTH);
		seperatorGridData.minimumHeight = 0;

		// separator
		new Label(toolbarComposite, SWT.SEPARATOR | SWT.HORIZONTAL)
				.setLayoutData(seperatorGridData);
	}

	public static ToolbarComposite getInstance(Composite parentComposite) {
		if (instance == null) {
			instance = new ToolbarComposite(parentComposite);
		}

		return instance;
	}

	public Composite getToolBarComposite() {
		return toolbarComposite;
	}

	@Override
	public void update(Class<?> origin) {
		if (lastSelectedPanelType != Collector.getCurrentRightPanelType()) {
			disableActiveButtons();
		}
	}

	public void registerAsObserverToCollectorUpdates() {
		Collector.getInstance().registerObserver(instance);
	}

	public void enableAlbumButtons(String albumName) {
		homeBtn.setImage(home);

		addEntryBtn.setEnabled(true);
		viewBtn.setEnabled(true);

		if (DatabaseWrapper.albumHasPictureField(albumName)) {
			viewBtn.setImage(pictureView);
			viewBtn.setToolTipText("Toggle to gallery mode");
			viewBtn.setEnabled(true);

			Collector.setViewIsDetailed(true);

			BrowserContent.performLastQuery(Collector.getAlbumItemSWTBrowser());
		} else {
			viewBtn.setImage(detailedView);
			viewBtn.setEnabled(false);

			Collector.setViewIsDetailed(true);

			BrowserContent.performLastQuery(Collector.getAlbumItemSWTBrowser());
		}

		searchBtn.setEnabled(true);
	}
}
