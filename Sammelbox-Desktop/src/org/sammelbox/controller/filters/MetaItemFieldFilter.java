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

package org.sammelbox.controller.filters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.sammelbox.model.album.FieldType;
import org.sammelbox.model.album.MetaItemField;

public final class MetaItemFieldFilter {
	private MetaItemFieldFilter() {
	}
	
	/** Returns all valid MetaItemFields. Hereby valid means that only user editable fields are returned
	 * @param metaItemFields the list of all available meta item fields
	 * @return a list containing only valid meta item fields */
	public static List<MetaItemField> getValidMetaItemFields(List<MetaItemField> metaItemFields) {
		List<MetaItemField> validMetaItemFields = new ArrayList<MetaItemField>();
		List<FieldType> validFieldTypes = new LinkedList<FieldType>(Arrays.asList(FieldType.values()));

		validFieldTypes.remove(FieldType.ID);
		validFieldTypes.remove(FieldType.UUID);
		
		for (MetaItemField metaItemField : metaItemFields) {
			if (validFieldTypes.contains(metaItemField.getType())) {
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
		List<FieldType> validFieldTypes = new LinkedList<FieldType>(Arrays.asList(FieldType.values()));	
		
		validFieldTypes.remove(FieldType.ID);
		validFieldTypes.remove(FieldType.UUID);
		
		for (MetaItemField metaItemField : metaItemFields) {
			if (validFieldTypes.contains(metaItemField.getType())) {
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
