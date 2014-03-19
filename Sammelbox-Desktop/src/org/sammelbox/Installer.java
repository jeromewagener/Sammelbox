package org.sammelbox;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.sammelbox.controller.filesystem.FileSystemAccessWrapper;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Language;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.controller.managers.BuildInformationManager;
import org.sammelbox.view.various.ScreenUtils;

public class Installer {
	private static final int INSTALLER_WIDTH = 650;
	private static final int INSTALLER_HEIGHT = 600;
	private static final int SEPARATOR_HEIGHT = 50;
	private static final int SEPARATOR_WIDTH = 610;
	private static final int DEFAULT_MARGIN = 10;
	private static final int DEFAULT_TOP_MARGIN = 20;
	
	private static Shell shell;
	private static Label welcomeLabelLine1;
	private static Label welcomeLabelLine2;
	private static Label installDirDescription;
	private static Label collectionDataDirDescription;
	private static Label changeLanguageDescription;
	private static Button cancelButton;
	private static Button installButton;
	
	public static void main(String[] args) throws ClassNotFoundException {
		Display display = new Display ();
		shell = new Shell(display);
		shell.setSize(INSTALLER_WIDTH, INSTALLER_HEIGHT);
		shell.setLocation(
				(new Double((ScreenUtils.getPrimaryScreenClientArea(display).width / 2.0) - (INSTALLER_WIDTH / 2.0))).intValue(),
				(new Double((ScreenUtils.getPrimaryScreenClientArea(display).height / 2.0) - (INSTALLER_HEIGHT / 2.0))).intValue());
		
		RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
		rowLayout.marginWidth = DEFAULT_MARGIN;
		rowLayout.marginTop = DEFAULT_TOP_MARGIN;
		shell.setLayout(rowLayout);
		
		Image installerLogo = FileSystemAccessWrapper.getImageFromResource("graphics/installer.png");
		Label installerLogoLabel = new Label(shell, SWT.NONE);
		installerLogoLabel.setImage(installerLogo);
		installerLogoLabel.setAlignment(SWT.TOP);
	 
		Label spacer = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		spacer.setLayoutData(new RowData(SEPARATOR_WIDTH, SEPARATOR_HEIGHT));
		
		welcomeLabelLine1 = new Label(shell, SWT.NONE);
		welcomeLabelLine2 = new Label(shell, SWT.NONE);
		
		Label spacer2 = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		spacer2.setLayoutData(new RowData(SEPARATOR_WIDTH, SEPARATOR_HEIGHT));
		
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(gridLayout);
		
		installDirDescription = new Label(composite, SWT.NULL);
		Label installDir = new Label(composite, SWT.BORDER);
		installDir.setText("/home/test/programs/Sammelbox");
		Button changeInstallDirButton = new Button(composite, SWT.PUSH);
		changeInstallDirButton.setText("...");
		Image infoIcon = FileSystemAccessWrapper.getImageFromResource("graphics/info.png");
		Label installInfoLabel = new Label(composite, SWT.NONE);
		installInfoLabel.setImage(infoIcon); 
		
		collectionDataDirDescription = new Label(composite, SWT.NULL);
		Label collectionDataDir = new Label(composite, SWT.BORDER);
		collectionDataDir.setText("/home/test/Sammelbox");
		Button changeDataDirButton = new Button(composite, SWT.PUSH);
		changeDataDirButton.setText("...");
		Label storageInfoLabel = new Label(composite, SWT.NONE);
		storageInfoLabel.setImage(infoIcon); 
		
		changeLanguageDescription = new Label(composite, SWT.NULL);
		final Combo changeLanguageCombo = new Combo(composite, SWT.READ_ONLY);
		String[] languages = new String[Language.valuesWithoutUnknown().length];
		for (int i=0; i<Language.valuesWithoutUnknown().length; i++) {
			languages[i] = Language.getTranslation(Language.valuesWithoutUnknown()[i]);
		}
		changeLanguageCombo.setItems(languages);
		changeLanguageCombo.setText(Language.getTranslation(Translator.getUsedLanguage()));
		new Label(composite, SWT.NONE);
		Label changeLanguageInfoLabel = new Label(composite, SWT.NONE);
		changeLanguageInfoLabel.setImage(infoIcon); 	
		
		Label spacer3 = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		spacer3.setLayoutData(new RowData(SEPARATOR_WIDTH, SEPARATOR_HEIGHT));
	    
		cancelButton = new Button(shell, SWT.PUSH);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				shell.close();
			}
		});
		cancelButton.setAlignment(SWT.RIGHT);
		
		installButton = new Button(shell, SWT.PUSH);
		installButton.setFocus();
		installButton.setAlignment(SWT.RIGHT);
		
		Translator.setLanguageFromSettingsOrSystem();
		initLabels();
		changeLanguageCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				//Translator.setLanguageManually(Language.byTranslation(changeLanguageCombo.getItem(changeLanguageCombo.getSelectionIndex())));
				//initLabels();
			}
		});
		
		shell.open ();
		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) {
				display.sleep ();
			}
		}
		display.dispose ();
	}
	
	private static void initLabels() {
		shell.setText(Translator.get(DictKeys.INSTALLER_WINDOW_TITLE, BuildInformationManager.instance().getApplicationName(), BuildInformationManager.instance().getVersion()));
		welcomeLabelLine1.setText(Translator.get(DictKeys.INSTALLER_INFO_LINE_1));
		welcomeLabelLine2.setText(Translator.get(DictKeys.INSTALLER_INFO_LINE_2));
		installDirDescription.setText(Translator.get(DictKeys.INSTALLER_INSTALL_DIR));			
		collectionDataDirDescription.setText(Translator.get(DictKeys.INSTALLER_STORAGE_DIR));
		changeLanguageDescription.setText(Translator.get(DictKeys.INSTALLER_LANGUAGE));
		cancelButton.setText(Translator.get(DictKeys.INSTALLER_CANCEL));
		installButton.setText(Translator.get(DictKeys.INSTALLER_INSTALL));
	}
}
