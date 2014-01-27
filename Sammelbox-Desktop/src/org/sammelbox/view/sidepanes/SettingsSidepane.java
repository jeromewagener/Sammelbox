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

package org.sammelbox.view.sidepanes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Language;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.controller.managers.SettingsManager;
import org.sammelbox.model.settings.ApplicationSettings;
import org.sammelbox.view.various.ComponentFactory;

public final class SettingsSidepane {
	private static final String EUROPEAN_DOT = "16.01.1988";
	private static final String EUROPEAN_SLASH = "16/01/1988";
	private static final String AMERICAN_DOT = "01.16.1988";
	private static final String AMERICAN_SLASH = "01/16/1988";
	
	private static final Map<String, String> DATE_EXAMPLES_TO_FORMATS;
    static {
        Map<String, String> myDateExamplesToFormats = new HashMap<String, String>();
        myDateExamplesToFormats.put(EUROPEAN_DOT, "dd.MM.yyyy");
        myDateExamplesToFormats.put(EUROPEAN_SLASH, "dd/MM/yyyy");
        myDateExamplesToFormats.put(AMERICAN_DOT, "MM.dd.yyyy");
        myDateExamplesToFormats.put(AMERICAN_SLASH, "MM/dd/yyyy");
        DATE_EXAMPLES_TO_FORMATS = Collections.unmodifiableMap(myDateExamplesToFormats);
    }
    
    private static final int DEFAULT_COMPOSITE_HEIGHT_IN_PIXELS = 15;
	
    private SettingsSidepane() {
		// use build method instead
	}
    
	public static Composite build(Composite parentComposite) {		
		// setup settings composite
		Composite settingsComposite = new Composite(parentComposite, SWT.NONE);
		settingsComposite.setLayout(new GridLayout(1, false));
		settingsComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		// label header
		ComponentFactory.getPanelHeaderComposite(settingsComposite, Translator.get(DictKeys.LABEL_SETTINGS));

		Composite innerComposite = new Composite(settingsComposite, SWT.NONE);
		innerComposite.setLayout(new GridLayout(2, false));
		
		Label label = new Label(innerComposite, SWT.NONE);
		label.setText(Translator.get(DictKeys.LABEL_LANGUAGE));
		
		final Combo languageCombo = new Combo(innerComposite, SWT.READ_ONLY);
		languageCombo.setItems(Language.allLanguages());
		languageCombo.setText(Translator.getUsedLanguage().toString());
		
		Label defaultViewSelection = new Label(innerComposite, SWT.NONE);
		defaultViewSelection.setText(Translator.get(DictKeys.LABEL_DEFAULT_VIEW));
		
		final Combo viewSelectionCombo = new Combo(innerComposite, SWT.READ_ONLY);
		viewSelectionCombo.setItems(new String[] { Translator.get(DictKeys.LABEL_DETAILS_VIEW), Translator.get(DictKeys.LABEL_GALLERY_VIEW)});
		if (SettingsManager.getSettings().isDetailedViewDefault()) {
			viewSelectionCombo.setText(Translator.get(DictKeys.LABEL_DETAILS_VIEW));
		} else {
			viewSelectionCombo.setText(Translator.get(DictKeys.LABEL_GALLERY_VIEW));
		}
		
		Label dateFormatSelection = new Label(innerComposite, SWT.NONE);
		dateFormatSelection.setText(Translator.get(DictKeys.LABEL_DATE_FORMAT));
		
		final Combo dateFormatSelectionCombo = new Combo(innerComposite, SWT.READ_ONLY);
		dateFormatSelectionCombo.setItems(new String[] { EUROPEAN_DOT, EUROPEAN_SLASH, AMERICAN_DOT, AMERICAN_SLASH});
		
		String definedDateFormat = SettingsManager.getSettings().getDateFormat();
		for (String key : DATE_EXAMPLES_TO_FORMATS.keySet()) {
			if (DATE_EXAMPLES_TO_FORMATS.get(key).equals(definedDateFormat)) {
				dateFormatSelectionCombo.setText(key);
				break;
			}
		}
		
		if (dateFormatSelectionCombo.getText().isEmpty()) {
			dateFormatSelectionCombo.setText(EUROPEAN_DOT);
		}
		
		Label seperator = new Label(settingsComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gridDataForSeperator = new GridData(GridData.FILL_BOTH);
		gridDataForSeperator.heightHint = DEFAULT_COMPOSITE_HEIGHT_IN_PIXELS;
		seperator.setLayoutData(gridDataForSeperator);
		
		Button saveSettingsButton = new Button(settingsComposite, SWT.PUSH);
		saveSettingsButton.setText(Translator.get(DictKeys.BUTTON_SAVE_SETTINGS));
		saveSettingsButton.setLayoutData(new GridData(GridData.FILL_BOTH));
		saveSettingsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ApplicationSettings appSettings = SettingsManager.getSettings();
				
				appSettings.setUserDefinedLanguage(Language.valueOf(languageCombo.getItem(languageCombo.getSelectionIndex())));
				appSettings.setDetailedViewIsDefault(viewSelectionCombo.getSelectionIndex() == 0);
				appSettings.setDateFormat(DATE_EXAMPLES_TO_FORMATS.get(dateFormatSelectionCombo.getItem(dateFormatSelectionCombo.getSelectionIndex())));
				SettingsManager.setApplicationSettings(appSettings);
				
				Translator.setLanguageManually(Language.valueOf(languageCombo.getItem(languageCombo.getSelectionIndex())));
				ComponentFactory.getMessageBox(Translator.get(DictKeys.DIALOG_TITLE_RESTART_NEEDED_FOR_SETTINGS), 
						Translator.get(DictKeys.DIALOG_CONTENT_RESTART_NEEDED_FOR_SETTINGS), 
						SWT.ICON_INFORMATION).open();
			}
		});
		
		return settingsComposite;
	}
}
