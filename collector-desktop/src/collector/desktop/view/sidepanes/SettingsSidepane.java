package collector.desktop.view.sidepanes;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import collector.desktop.controller.i18n.DictKeys;
import collector.desktop.controller.i18n.Language;
import collector.desktop.controller.i18n.Translator;
import collector.desktop.controller.settings.ApplicationSettingsManager;
import collector.desktop.view.ApplicationUI;
import collector.desktop.view.various.ComponentFactory;

public class SettingsSidepane {
	public static Composite build(Composite parentComposite) {		
		// setup settings composite
		Composite settingsComposite = new Composite(parentComposite, SWT.NONE);
		settingsComposite.setLayout(new GridLayout(1, false));
		settingsComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		// label header
		ComponentFactory.getPanelHeaderComposite(settingsComposite, Translator.get(DictKeys.LABEL_SETTINGS));

		Composite languageComposite = new Composite(settingsComposite, SWT.NONE);
		languageComposite.setLayout(new GridLayout(2, false));
		
		Label label = new Label(languageComposite, SWT.NONE);
		label.setText(Translator.get(DictKeys.LABEL_LANGUAGE));
		
		final Combo languageCombo = new Combo(languageComposite, SWT.READ_ONLY);
	    languageCombo.setBounds(50, 50, 150, 65);
		languageCombo.setItems(Language.allLanguages());
		languageCombo.setText(Translator.getUsedLanguage().toString());
		
		Label seperator = new Label(settingsComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gridDataForSeperator = new GridData(GridData.FILL_BOTH);
		gridDataForSeperator.heightHint = 15;
		seperator.setLayoutData(gridDataForSeperator);
		
		Button saveSettingsButton = new Button(settingsComposite, SWT.PUSH);
		saveSettingsButton.setText(Translator.get(DictKeys.BUTTON_SAVE_SETTINGS));
		saveSettingsButton.setLayoutData(new GridData(GridData.FILL_BOTH));
		saveSettingsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ApplicationSettingsManager.setUserDefinedLanguage(Language.valueOf(languageCombo.getItem(languageCombo.getSelectionIndex())));
				Translator.setLanguageManually(Language.valueOf(languageCombo.getItem(languageCombo.getSelectionIndex())));
				
				ApplicationSettingsManager.storeToSettingsFile();
				
				ComponentFactory.getMessageBox(ApplicationUI.getShell(), 
						Translator.get(DictKeys.DIALOG_TITLE_RESTART_NEEDED_FOR_SETTINGS), 
						Translator.get(DictKeys.DIALOG_CONTENT_RESTART_NEEDED_FOR_SETTINGS), 
						SWT.ICON_INFORMATION).open();
			}
		});
		
		return settingsComposite;
	}
}
