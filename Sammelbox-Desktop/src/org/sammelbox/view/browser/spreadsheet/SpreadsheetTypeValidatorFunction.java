package org.sammelbox.view.browser.spreadsheet;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.sammelbox.controller.managers.SettingsManager;
import org.sammelbox.model.album.FieldType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpreadsheetTypeValidatorFunction extends BrowserFunction {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SpreadsheetTypeValidatorFunction.class);
	
	public SpreadsheetTypeValidatorFunction(Browser browser, String name) {
		super (browser, name);
	}
	
	@Override
	public Object function(Object[] arguments) {
		FieldType fieldType = FieldType.valueOf((String) arguments[0]);
		String valueAsString = (String) arguments[1];
		
		if (fieldType.equals(FieldType.DECIMAL)) {
	
			if (!valueAsString.isEmpty()) {
				try {
					Double.parseDouble(valueAsString);
				} catch (NumberFormatException nfe) {
					LOGGER.info("Decimal value not valid", nfe);
					return false;
				}
			}
		
		} else if (fieldType.equals(FieldType.INTEGER)) {	
			if (!valueAsString.isEmpty()) {
				try {
					Integer.parseInt(valueAsString);
				} catch (NumberFormatException nfe) {
					LOGGER.info("Integer value not valid", nfe);
					return false;
				}
			}
		
		} else if (fieldType.equals(FieldType.DATE)) {
			if (!valueAsString.isEmpty()) {
				try {
					SimpleDateFormat formatter = new SimpleDateFormat(SettingsManager.getSettings().getDateFormat());
			        formatter.parse(valueAsString);
			        
			        SimpleDateFormat dateFormater = new SimpleDateFormat(SettingsManager.getSettings().getDateFormat());
					dateFormater.parse(valueAsString);
				} catch (ParseException pe) {
					LOGGER.info("Date value not valid", pe);
					return false;
				}
			}
		}
		
		return true;
	}
}
