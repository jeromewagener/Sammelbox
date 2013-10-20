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

package org.sammelbox.model.album;

import org.sammelbox.controller.i18n.DictKeys;

public enum OptionType {
	YES,
	NO,
	UNKNOWN;

	/**
	 * Retrieves the option type string value which is stored in the database, based on a given dictionary key
	 * @param dictKey the dictionary key for which the string database value should be retrieved
	 * @return the string database value for a given dictionary option type key
	 * */
	public static String getDatabaseOptionValue(String dictKey) {
		switch (dictKey) {
		case DictKeys.BROWSER_YES:
			return OptionType.YES.toString();

		case DictKeys.BROWSER_NO:
			return OptionType.NO.toString();

		case DictKeys.BROWSER_UNKNOWN:
		default:
			return OptionType.UNKNOWN.toString();
		}
	} 
}
