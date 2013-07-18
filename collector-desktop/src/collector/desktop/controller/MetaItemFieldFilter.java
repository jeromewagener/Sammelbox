package collector.desktop.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import collector.desktop.model.album.FieldType;
import collector.desktop.model.album.MetaItemField;

public class MetaItemFieldFilter {
	
	/** Returns all valid MetaItemFields. Hereby valid means that only user editable fields are returned
	 * @param metaItemFields the list of all available meta item fields
	 * @return a list containing only valid meta item fields */
	public static List<MetaItemField> getValidMetaItemFields(List<MetaItemField> metaItemFields) {
		List<MetaItemField> validMetaItemFields = new ArrayList<MetaItemField>();
		List<String> validFieldTypes = Arrays.asList(FieldType.toUserTypeStringArray());

		for (MetaItemField metaItemField : metaItemFields) {
			if (validFieldTypes.contains(metaItemField.getType().toString())) {
				validMetaItemFields.add(metaItemField);
			}
		}

		return validMetaItemFields;
	}


	/** Returns all valid field names. Hereby valid means that only user editable fields are returned 
	 * @param metaItemFields the list of all available meta item fields
	 * @return a string array containing only valid field names */
	public static String[] getValidFieldNamesAsStringArray(List<MetaItemField> metaItemFields) {
		List<MetaItemField> validMetaItemFields = new ArrayList<MetaItemField>();
		List<String> validFieldTypes = Arrays.asList(FieldType.toUserTypeStringArray());

		for (MetaItemField metaItemField : metaItemFields) {
			if (validFieldTypes.contains(metaItemField.getType().toString())) {
				validMetaItemFields.add(metaItemField);
			}
		}

		String[] validMetaItemFieldsAsStringArray = new String[validMetaItemFields.size()];
		for(int i=0; i<validMetaItemFields.size(); i++) {
			validMetaItemFieldsAsStringArray[i] = validMetaItemFields.get(i).getName();
		}

		return validMetaItemFieldsAsStringArray;
	}
}
