package collector.desktop.model.album;

import java.sql.Date;
import java.sql.Time;
import java.util.UUID;

public class ItemField extends MetaItemField {
	protected Object value; 
	
	/**
	 * Constructor.
	 * @param name The name of the item. Names must be unique within an album.
	 * @param type The type of the item.
	 * @param value The value which must be compliant to the specified type before.
	 * @param quickSearchable True indicates that the field will be taken into account for the quicksearch feature.
	 */
	public ItemField(String name, FieldType type, Object value, boolean quickSearchable)
	{
		super(name, type, quickSearchable);
		this.value = value;
	}
	
	/**
	 * Alternate constructor which automatically disables the quicksearch availability for this field if item is persisted into database.
	 * May be turned on later on.
	 * @param name The name of the item. Names must be unique within an album.
	 * @param type The type of the item.
	 * @param value The value which must be compliant to the specified type before.
	 */
	public ItemField(String name, FieldType type, Object value)
	{
		super(name, type, false);
		this.value = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ItemField other = (ItemField) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	/**
	 * Standard constructor.
	 */
	public ItemField() {
		this.name = "";
		this.type = FieldType.Text;
		this.value = null;
	}

	/**
	 * Gets the value and casts it into the specified type T. 
	 * @return The cast value of the field.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getValue()
	{
		T outValue = (T) value;
		
		return outValue;
	}	
	
	/**
	 * Sets the value for the field.
	 * @param value The value. It may be any supported types.
	 * @see {@link FieldType} for supported types.
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * Tests if the value is of the same type as the stored field type. The stored type may be converted
	 * into a another type to be conveniently stored in the DB afterwards, which is NOT checked here.
	 * @return True if item field is valid. False otherwise.
	 */
	public boolean isValid() {
		switch (type) {
		case ID:
			return (value instanceof Long);
			
		case Text:
			return (value instanceof String);

		case Number:
			return (value instanceof Double);

		case Date:
			return (value instanceof Date);
			
		case Time:
			return (value instanceof Time);
			
		case URL:
			return value instanceof String;
			
		case Integer:
			return value instanceof Integer;
			
		case Option:
			return value instanceof OptionType;
			
		case StarRating:
			return value instanceof StarRating;

		case UUID:
			return (value instanceof UUID);
			
		default:
			return (value instanceof String);
		}
		
	}
	
	@Override
	public String toString() {
		return getName() + ":" + getValue() + ":" + getType() + ":" + 
				(isQuickSearchable() ? " is quicksearchable" : " is not quicksearchable");
	}
}
