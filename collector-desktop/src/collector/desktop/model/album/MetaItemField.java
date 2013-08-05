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

package collector.desktop.model.album;

import collector.desktop.model.database.operations.DatabaseOperations;

public class MetaItemField {

	protected String name;
	protected FieldType type;
	protected boolean quickSearchable;
	
	/**
	 * Standard constructor. For internal use by inheriting classes only. 
	 */
	protected MetaItemField() {
		super();
	}
	
	/**
	 *  Alternative constructor. Disables the quicksearch support by default if item is inserted into database.
	 * Recommended for internal use by the {@link DatabaseOperations} only.
	 * @param name The name of the field.
	 * @param type The type of the field. All values inserted must be of the specified type.
	 */
	public MetaItemField(String name, FieldType type) {
		this.name = name;
		this.type = type;
		this.quickSearchable = false;
	}
	
	/**
	 * Constructor with explicit quicksearch switch. For any retrieval, insertion of deletion query through
	 * the {@link DatabaseOperations} it is recommended to use this constructor. More specifically since the equals
	 * takes all fields (including the quicksearch) into account to correctly identify a album item field. 
	 * @param name The name of the field.
	 * @param type The type of the field. All values inserted must be of the specified type.
	 * @param quickSearchable True enables quicksearch support, false disables it.
	 */
	public MetaItemField(String name, FieldType type, boolean quickSearchable) {
		this.name = name;
		this.type = type;
		this.quickSearchable = quickSearchable;
	}

	/**
	 * Getter for the name.
	 * @return The current name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Setter for the name.
	 * @param name The new name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Getter for the type.
	 * @return The current field type.
	 */
	public FieldType getType() {
		return type;
	}

	/**
	 * Setter for the type.
	 * @param type The new type.
	 */
	public void setType(FieldType type) {
		this.type = type;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (quickSearchable ? 1231 : 1237);
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MetaItemField other = (MetaItemField) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (quickSearchable != other.quickSearchable)
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getName() + ":" + getType();
	}
	
	/**
	 * Getter for quicksearch feature status for this field.
	 * @return
	 */
	public boolean isQuickSearchable() {
		return quickSearchable;
	}

	/**
	 * Setter for quicksearch feature status for this field. Only valid if item is persisted into database.
	 * @param quickSearchable The ne state of the Setter for quicksearch feature status.
	 */
	public void setQuickSearchable(boolean quickSearchable) {
		this.quickSearchable = quickSearchable;
	}
}