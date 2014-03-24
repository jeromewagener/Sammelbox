package org.sammelbox;


import java.io.File;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.sammelbox.controller.filesystem.FileSystemAccessWrapper;
import org.sammelbox.controller.filesystem.FileSystemLocations;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Language;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.controller.managers.BuildInformationManager;
import org.sammelbox.controller.managers.SettingsManager;
import org.sammelbox.model.settings.ApplicationSettings;
import org.sammelbox.view.sidepanes.SettingsSidepane;
import org.sammelbox.view.various.ScreenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.sammelbox.view.sidepanes.SettingsSidepane.*;

// TODO when hovering over the info icons, some additional information should be shown to the user
public class Configurator {
	private static final Logger LOGGER = LoggerFactory.getLogger(Configurator.class);
	
	private static final int CONFIGURATOR_WIDTH = 650;
	private static final int CONFIGURATOR_HEIGHT = 600;
	private static final int DEFAULT_MARGIN = 20;
	private static final int SEPARATOR_HEIGHT = 25;
	
	private static Shell shell;
	private static Label welcomeLabelLine1;
	private static Label welcomeLabelLine2;
	private static Label lblSammelBoxHomeDir;
	private static Label lblChooseLanguage;
	private static Label lblChooseDateFormat;
	private static Button cancelButton;
	private static Button startButton;
	
	public static void launch() {
		Display display = new Display ();
		shell = new Shell(display);
		shell.setSize(CONFIGURATOR_WIDTH, CONFIGURATOR_HEIGHT);
		shell.setLocation(
				(new Double((ScreenUtils.getPrimaryScreenClientArea(display).width / 2.0) - (CONFIGURATOR_WIDTH / 2.0))).intValue(),
				(new Double((ScreenUtils.getPrimaryScreenClientArea(display).height / 2.0) - (CONFIGURATOR_HEIGHT / 2.0))).intValue());

		// Shell layout
		GridData shellLayoutData = new GridData();
		shellLayoutData.grabExcessHorizontalSpace = true;
		GridLayout shellLayout = new GridLayout();
		shellLayout.marginWidth = DEFAULT_MARGIN;
		shellLayout.marginHeight = DEFAULT_MARGIN;
		shell.setLayout(shellLayout);
		shell.setLayoutData(shellLayoutData);
		
		// Logo
		Image installerLogo = FileSystemAccessWrapper.getImageFromResource("graphics/installer.png");
		Label installerLogoLabel = new Label(shell, SWT.NONE);
		installerLogoLabel.setImage(installerLogo);
		installerLogoLabel.setAlignment(SWT.TOP);
	 
		// First spacer (horizontal line)
		createSpacer();		
		
		welcomeLabelLine1 = new Label(shell, SWT.NONE);
		welcomeLabelLine2 = new Label(shell, SWT.NONE);
		
		// Second spacer
		createSpacer();

		// Layout data and inner composite for the settings (home directory etc...)
		GridLayout settingsLayout = new GridLayout();
		settingsLayout.numColumns = 4;
		GridData settingsLayoutData = new GridData();
		settingsLayoutData.grabExcessHorizontalSpace = true;
		settingsLayoutData.horizontalAlignment = GridData.FILL;
		Composite settingsComposite = new Composite(shell, SWT.NONE);
		settingsComposite.setLayout(settingsLayout);
		settingsComposite.setLayoutData(settingsLayoutData);
		
		// Define the home directory
		lblSammelBoxHomeDir = new Label(settingsComposite, SWT.NULL);
		
		GridData gridDataForHomeDirLabel = new GridData ();
		gridDataForHomeDirLabel.widthHint = 350;
		final Label homeDirLabel = new Label(settingsComposite, SWT.BORDER);
		homeDirLabel.setText(FileSystemLocations.DEFAULT_SAMMELBOX_HOME);
		homeDirLabel.setToolTipText(FileSystemLocations.DEFAULT_SAMMELBOX_HOME);
		homeDirLabel.setLayoutData(gridDataForHomeDirLabel);
		
		Button changeHomeDirButton = new Button(settingsComposite, SWT.PUSH);
		changeHomeDirButton.setText("...");
		changeHomeDirButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				DirectoryDialog dialog = new DirectoryDialog(shell);
				String directory = dialog.open();
				
				if (directory != null && !directory.isEmpty()) {
					homeDirLabel.setText(directory);
					homeDirLabel.setToolTipText(directory);
				}
			}
		});
		
		Label storageInfoLabel = new Label(settingsComposite, SWT.NONE);
		storageInfoLabel.setImage(FileSystemAccessWrapper.getImageFromResource("graphics/info.png"));
		
		// Create shortcut?
		new Label(settingsComposite, SWT.NULL);
		final Button createDesktopShortCut = new Button(settingsComposite, SWT.CHECK);
		createDesktopShortCut.setText(Translator.get(DictKeys.CONFIGURATOR_CREATE_DESKTOP_SHORTCUT));
		createDesktopShortCut.setSelection(true);		
		new Label(settingsComposite, SWT.NULL);
		Label shortcutInfoLabel = new Label(settingsComposite, SWT.NONE);
		shortcutInfoLabel.setImage(FileSystemAccessWrapper.getImageFromResource("graphics/info.png"));
		
		if (!isWindows()) {
			createDesktopShortCut.setEnabled(false);
			createDesktopShortCut.setSelection(false);
		}
		
		// Choose the language
		lblChooseLanguage = new Label(settingsComposite, SWT.NULL);
		
		final Combo chooseLanguageCombo = new Combo(settingsComposite, SWT.READ_ONLY);
		String[] languages = new String[Language.valuesWithoutUnknown().length];
		for (int i=0; i<Language.valuesWithoutUnknown().length; i++) {
			languages[i] = Language.getTranslation(Language.valuesWithoutUnknown()[i]);
		}
		chooseLanguageCombo.setItems(languages);
		chooseLanguageCombo.setText(Language.getTranslation(Translator.getUsedLanguage()));
		new Label(settingsComposite, SWT.NULL);
		new Label(settingsComposite, SWT.NULL);
		
		// Choose the date format
		lblChooseDateFormat = new Label(settingsComposite, SWT.NULL);
		
		final Combo dateFormatSelectionCombo = new Combo(settingsComposite, SWT.READ_ONLY|SWT.BORDER|SWT.H_SCROLL);
		dateFormatSelectionCombo.setItems(new String[] { EUROPEAN_DOT, EUROPEAN_SLASH, AMERICAN_DOT, AMERICAN_SLASH });
		dateFormatSelectionCombo.select(0);
		
		// Third spacer
		createSpacer();
		
		// Create a composite for the cancel and install/run buttons
		Composite buttonComposite = new Composite(shell, SWT.NONE);
		GridLayout buttonGridLayout = new GridLayout();
		buttonGridLayout.numColumns = 2;
		buttonComposite.setLayout(buttonGridLayout);
		buttonComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, true, 1, 1));
		
		cancelButton = new Button(buttonComposite, SWT.PUSH);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				shell.close();
			}
		});
		
		startButton = new Button(buttonComposite, SWT.PUSH);
		startButton.setFocus();
		startButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				if (homeDirLabel.getText().equals(FileSystemLocations.DEFAULT_SAMMELBOX_HOME)) {
					// create default home
					FileSystemLocations.setActiveHomeDir(FileSystemLocations.DEFAULT_SAMMELBOX_HOME);
					FileSystemAccessWrapper.updateSammelboxFileStructure();
				} else {
					// create alternative home with redirection link
					FileSystemLocations.setActiveHomeDir(homeDirLabel.getText());
					FileSystemAccessWrapper.updateSammelboxFileStructure();
					
					// Create redirection
					FileSystemAccessWrapper.writeToFile(homeDirLabel.getText(), FileSystemLocations.HOME_REDIRECTION_FILE);
				}
				
				// Map all settings (the home directory should be initialized first!)
				ApplicationSettings applicationSettings = new ApplicationSettings();
				applicationSettings.setUserDefinedLanguage(Language.byTranslation(
						chooseLanguageCombo.getItem(chooseLanguageCombo.getSelectionIndex())));
				applicationSettings.setDateFormat(SettingsSidepane.DATE_EXAMPLES_TO_FORMATS.get(
						dateFormatSelectionCombo.getItem(dateFormatSelectionCombo.getSelectionIndex())));
				
				// Store settings
				SettingsManager.setApplicationSettings(applicationSettings);
				SettingsManager.storeToSettingsFile();	
				
				// Copy executable and create starters
				copyExecutableToActiveHomeDir();
				if (createDesktopShortCut.getSelection()) {
					createShortcut();
				}
				
				// Close configurator and start sammelbox
				shell.close();
				Sammelbox.launch();
			}

			private void copyExecutableToActiveHomeDir() {
				// search for sammelbox exe in current directory
				for (File file : new File(FileSystemLocations.INITIALIZATION_DIR).listFiles()) {
					if (file.getName().toLowerCase().contains("sammelbox") && 
					        (file.getName().toLowerCase().endsWith(".exe") || file.getName().toLowerCase().endsWith(".jar"))) {
						try {
							FileSystemAccessWrapper.copyFile(file, new File(
									FileSystemLocations.DEFAULT_SAMMELBOX_HOME + File.separatorChar + file.getName()));
						} catch (IOException ioe) {
							LOGGER.error("An error occurred while copying the Sammelbox executable", ioe);
						}
						break;
					};
				}
			}
		});
		
		// Initialize labels with correct translation
		Translator.setLanguageFromSettingsOrSystem();
		initLabels();
		
		// Default SWT stuff
		shell.open ();
		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) {
				display.sleep ();
			}
		}
		display.dispose ();
	}
	
	private static void initLabels() {
		shell.setText(Translator.get(DictKeys.CONFIGURATOR_WINDOW_TITLE, BuildInformationManager.instance().getPublicVersionString()));
		welcomeLabelLine1.setText(Translator.get(DictKeys.CONFIGURATOR_INFO_LINE_1));
		welcomeLabelLine2.setText(Translator.get(DictKeys.CONFIGURATOR_INFO_LINE_2));	
		lblSammelBoxHomeDir.setText(Translator.get(DictKeys.CONFIGURATOR_STORAGE_DIR));
		lblChooseLanguage.setText(Translator.get(DictKeys.CONFIGURATOR_LANGUAGE));
		lblChooseDateFormat.setText(Translator.get(DictKeys.CONFIGURATOR_DATE_FORMAT));
		cancelButton.setText(Translator.get(DictKeys.CONFIGURATOR_CANCEL));
		startButton.setText(Translator.get(DictKeys.CONFIGURATOR_START_SAMMELBOX));
	}
	
	private static void createSpacer() {
		GridData seperaratorGridData = new GridData(GridData.FILL_BOTH);
		seperaratorGridData.minimumHeight = SEPARATOR_HEIGHT;
		
		Label spacer = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		spacer.setLayoutData(seperaratorGridData);
	}
	
	private static void createShortcut() {
		if (isWindows()) {
			String vbsScriptForShortCutCreation = FileSystemLocations.TEMP_DIR + File.separatorChar + "createSammelboxShortcut.vbs";
			StringBuilder vbsScriptForShortcutCreationBuilder = new StringBuilder();
			
			vbsScriptForShortcutCreationBuilder.append("set WshShell = WScript.CreateObject(\"WScript.Shell\")\n"); 
			vbsScriptForShortcutCreationBuilder.append("strDesktop = WshShell.SpecialFolders(\"Desktop\")\n");
			vbsScriptForShortcutCreationBuilder.append("set oShellLink = WshShell.CreateShortcut(strDesktop & \"\\Sammelbox.lnk\")\n");
			vbsScriptForShortcutCreationBuilder.append("oShellLink.TargetPath = \"" + FileSystemLocations.DEFAULT_SAMMELBOX_HOME + File.separatorChar + "Sammelbox.exe\"\n");
			vbsScriptForShortcutCreationBuilder.append("oShellLink.WindowStyle = 1\n");
			vbsScriptForShortcutCreationBuilder.append("oShellLink.Description = \"Sammelbox - Collection Manager\"\n");
			vbsScriptForShortcutCreationBuilder.append("oShellLink.WorkingDirectory = \"" + FileSystemLocations.DEFAULT_SAMMELBOX_HOME + "\"\n");
			vbsScriptForShortcutCreationBuilder.append("oShellLink.Save\n");
			
			FileSystemAccessWrapper.writeToFile(
					vbsScriptForShortcutCreationBuilder.toString(), vbsScriptForShortCutCreation);
			
			try {
		         Runtime.getRuntime().exec("wscript " + vbsScriptForShortCutCreation);
		    } catch (IOException e) {
		    	LOGGER.error("An error occurred while creating the Windows desktop shortcut");
		    }
		}
	}
	
	private static boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().contains("windows");
	}
}
