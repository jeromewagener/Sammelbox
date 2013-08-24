package org.sammelbox.model.settings;

import org.sammelbox.controller.i18n.Language;

public class ApplicationSettings {
	private Language userDefinedLanguage = Language.UNKNOWN;
	private String dateFormat = "dd/MM/yyyy";
	private boolean detailedViewIsDefault = true;
	
	public ApplicationSettings() {}
	
	public Language getUserDefinedLanguage() {
		return userDefinedLanguage;
	}

	public void setUserDefinedLanguage(Language userDefinedLanguage) {
		this.userDefinedLanguage = userDefinedLanguage;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public boolean isDetailedViewDefault() {
		return detailedViewIsDefault;
	}

	public void setDetailedViewIsDefault(boolean detailedViewIsDefault) {
		this.detailedViewIsDefault = detailedViewIsDefault;
	}
}