package org.sammelbox.model.settings;

import org.sammelbox.controller.i18n.Language;
import org.sammelbox.view.SammelView;

public class ApplicationSettings {
	private Language userDefinedLanguage = Language.UNKNOWN;
	private String dateFormat = "dd/MM/yyyy";
	private SammelView defaultView = SammelView.DETAILED_VIEW;
	private boolean showDebugMenu = false;
	private boolean isFullSynchronizationEnabled = true;
	
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

	public SammelView getDefaultView() {
		return defaultView;
	}

	public void setDefaultView(SammelView defaultView) {
		this.defaultView = defaultView;
	}

	public boolean showDebugMenu() {
		return showDebugMenu;
	}

	public void setShowDebugMenu(boolean showDebugMenu) {
		this.showDebugMenu = showDebugMenu;
	}

	public void setFullSynchronizationEnabled(boolean isFullSynchronizationEnabled) {
		this.isFullSynchronizationEnabled = isFullSynchronizationEnabled; 
	}
	
	public boolean isFullSynchronizationEnabled() {
		return isFullSynchronizationEnabled;
	}
}