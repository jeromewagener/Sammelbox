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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.sammelbox.model.album.FieldType;
import org.sammelbox.model.album.ItemField;

// TODO this class together with MetaItemFieldFilter needs to be refactored
public final class ItemFieldFilterPlusID {
	private ItemFieldFilterPlusID() {
	}
	
	/** Returns all valid ItemFields. Hereby valid means that only user editable fields are returned
	 * @param ItemFields the list of all available  item fields
	 * @return a list containing only valid  item fields */
	public static List<ItemField> getValidItemFields(Collection<ItemField> ItemFields) {
		List<ItemField> validItemFields = new ArrayList<ItemField>();
		List<FieldType> validFieldTypes = new LinkedList<FieldType>(Arrays.asList(FieldType.values()));

		// TODO implement or delete for future releases
		validFieldTypes.remove(FieldType.TIME);
		validFieldTypes.remove(FieldType.UUID);
		
		for (ItemField ItemField : ItemFields) {
			if (validFieldTypes.contains(ItemField.getType())) {
				validItemFields.add(ItemField);
			}
		}

		return validItemFields;
	}
}
