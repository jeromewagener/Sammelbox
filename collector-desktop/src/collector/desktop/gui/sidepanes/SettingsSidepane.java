package collector.desktop.gui.sidepanes;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;

import collector.desktop.Collector;
import collector.desktop.gui.various.ComponentFactory;
import collector.desktop.internationalization.DictKeys;
import collector.desktop.internationalization.Language;
import collector.desktop.internationalization.Translator;
import collector.desktop.settings.ApplicationSettingsManager;

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
				
				MessageBox messageBox = new MessageBox(Collector.getShell(), SWT.ICON_INFORMATION);
				messageBox.setText(Translator.get(DictKeys.DIALOG_TITLE_RESTART_NEEDED_FOR_SETTINGS));
				messageBox.setMessage(Translator.get(DictKeys.DIALOG_CONTENT_RESTART_NEEDED_FOR_SETTINGS));
				
				messageBox.open();				
			}
		});
		
		return settingsComposite;
	}
}
