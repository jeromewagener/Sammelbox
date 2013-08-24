/** -----------------------------------------------------------------
 *    Sammelbox: Collection Manager - A free and open-source collection manager for Windows & Linux
 *    Copyright (C) 2011 Jérôme Wagener & Paul Bicheler
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

import org.sammelbox.controller.i18n.Translator;

public enum StarRating {
	ZERO_STARS  (0),
	ONE_STAR    (1),
	TWO_STARS   (2),
	THREE_STARS (3),
	FOUR_STARS  (4),
	FIVE_STARS  (5);
	
	private int numericValue;
	
	private StarRating(int numericValue) {
		this.numericValue = numericValue;
	}
	
	public int getIntegerValue() {
		return numericValue;
	} 

	public static String[] toComboBoxArray() {
		return new String[] {
				StarRating.ZERO_STARS.getIntegerValue() + Translator.toBeTranslated(" Stars"),
				StarRating.ONE_STAR.getIntegerValue() + Translator.toBeTranslated(" Stars"),
				StarRating.TWO_STARS.getIntegerValue() + Translator.toBeTranslated(" Stars"),
				StarRating.THREE_STARS.getIntegerValue() + Translator.toBeTranslated(" Stars"),
				StarRating.FOUR_STARS.getIntegerValue() + Translator.toBeTranslated(" Stars"),
				StarRating.FIVE_STARS.getIntegerValue() + Translator.toBeTranslated(" Stars")};
	}

	public static Object getByIntegerValue(int integerValue) {
		for (StarRating starRating : StarRating.values()) {
			if (starRating.getIntegerValue() == integerValue) {
				return starRating;
			}
		}
		
		return StarRating.ZERO_STARS;
	}
}
