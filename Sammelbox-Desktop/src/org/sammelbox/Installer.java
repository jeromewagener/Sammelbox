package org.sammelbox;


import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.sammelbox.controller.filesystem.FileSystemAccessWrapper;
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
	
	public static void main(String[] args) throws ClassNotFoundException {
		Display display = new Display ();
		Shell shell = new Shell(display);
		shell.setSize(INSTALLER_WIDTH, INSTALLER_HEIGHT);
		shell.setLocation(
				(new Double((ScreenUtils.getPrimaryScreenClientArea(display).width / 2.0) - (INSTALLER_WIDTH / 2.0))).intValue(),
				(new Double((ScreenUtils.getPrimaryScreenClientArea(display).height / 2.0) - (INSTALLER_HEIGHT / 2.0))).intValue());
		shell.setText(Translator.toBeTranslated(
				BuildInformationManager.instance().getApplicationName() + " " + 
				BuildInformationManager.instance().getVersion() + " Installer"));
		
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
		
		Label welcomeLabelLine1 = new Label(shell, SWT.NONE);
		Label welcomeLabelLine2 = new Label(shell, SWT.NONE);
		welcomeLabelLine1.setText(Translator.toBeTranslated(
				"Welcome to the Sammelbox installer. This installer allows to customize Sammelbox."));
		welcomeLabelLine2.setText(Translator.toBeTranslated(
				"Just click \"INSTALL\" in case you feel comfortable with the default settings. Have fun!"));
		
		Label spacer2 = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		spacer2.setLayoutData(new RowData(SEPARATOR_WIDTH, SEPARATOR_HEIGHT));
		
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(gridLayout);
		
		Label installDirDescription = new Label(composite, SWT.NULL);
		installDirDescription.setText(Translator.toBeTranslated("Installation Directory: "));
		Label installDir = new Label(composite, SWT.BORDER);
		installDir.setText("/home/test/programs/Sammelbox");
		Button changeInstallDirButton = new Button(composite, SWT.PUSH);
		changeInstallDirButton.setText("...");
		
		Label collectionDataDirDescription = new Label(composite, SWT.NULL);
		collectionDataDirDescription.setText(Translator.toBeTranslated("Storage Directory: "));
		Label collectionDataDir = new Label(composite, SWT.BORDER);
		collectionDataDir.setText("/home/test/Sammelbox");
		Button changeDataDirButton = new Button(composite, SWT.PUSH);
		changeDataDirButton.setText("...");
		
		Label spacer3 = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		spacer3.setLayoutData(new RowData(SEPARATOR_WIDTH, SEPARATOR_HEIGHT));
		
		Button cancelButton = new Button(shell, SWT.PUSH);
		cancelButton.setText(Translator.toBeTranslated("Cancel"));
		cancelButton.setFocus();
		
		Button installButton = new Button(shell, SWT.PUSH);
		installButton.setText(Translator.toBeTranslated("Install Now"));
		installButton.setFocus();
		
		shell.open ();
		
		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) {
				display.sleep ();
			}
		}
		
		display.dispose ();
	}
}
