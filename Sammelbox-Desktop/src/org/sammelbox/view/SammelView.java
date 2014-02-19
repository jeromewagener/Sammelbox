package org.sammelbox.view;

import org.sammelbox.controller.i18n.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum SammelView {
	DETAILED_VIEW,
	GALLERY_VIEW,
	SPREADSHEET_VIEW;
    
	private static final Logger LOGGER = LoggerFactory.getLogger(SammelView.class);
	
    public static String getTranslation(SammelView sammelView) {
    	if (DETAILED_VIEW.equals(sammelView)) {
    		return Translator.toBeTranslated("Detailed View");
    	} else if (GALLERY_VIEW.equals(sammelView)) {
    		return Translator.toBeTranslated("Gallery View");
    	} else if (SPREADSHEET_VIEW.equals(sammelView)) {
    		return Translator.toBeTranslated("Spreadsheet View");
    	}
    	
    	LOGGER.error("A translation for an unknown view was requested");
    	return "";
    }
    
    public static SammelView byTranslation(String sammelView) {
    	if ((Translator.toBeTranslated("Detailed View").equals(sammelView))) {
    		return DETAILED_VIEW;
    	} else if ((Translator.toBeTranslated("Gallery View").equals(sammelView))) {
    		return GALLERY_VIEW;
    	} else if ((Translator.toBeTranslated("Spreadsheet View").equals(sammelView))) {
    		return SPREADSHEET_VIEW;
    	}
    	
    	LOGGER.error("A SammelView for an unknown translation was requested");
    	return null;
    }
}
