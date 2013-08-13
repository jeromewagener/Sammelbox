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

public enum StarRating {
	ZeroStars  (0),
	OneStar    (1),
	TwoStars   (2),
	ThreeStars (3),
	FourStars  (4),
	FiveStars  (5);
	
	private int numericValue;
	
	private StarRating(int numericValue) {
		this.numericValue = numericValue;
	}
	
	public int getIntegerValue() {
		return numericValue;
	} 

	public static String[] toComboBoxArray() {
		return new String[] {
				StarRating.ZeroStars.getIntegerValue() + " Stars", // TODO translate
				StarRating.OneStar.getIntegerValue() + " Star",
				StarRating.TwoStars.getIntegerValue() + " Stars",
				StarRating.ThreeStars.getIntegerValue() + " Stars",
				StarRating.FourStars.getIntegerValue() + " Stars",
				StarRating.FiveStars.getIntegerValue() + " Stars"};
	}

	public static Object getByIntegerValue(int integerValue) {
		for (StarRating starRating : StarRating.values()) {
			if (starRating.getIntegerValue() == integerValue) {
				return starRating;
			}
		}
		
		return StarRating.ZeroStars;
	}
}
