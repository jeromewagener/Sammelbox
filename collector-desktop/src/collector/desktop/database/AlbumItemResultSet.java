package collector.desktop.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class AlbumItemResultSet {
	private ResultSet items;
	private String albumName = "";
	ResultSetMetaData metaData = null;
	private Map<Integer, MetaItemField> metaInfoMap = new HashMap<Integer, MetaItemField>();
	
	/**
	 * Constructor.
	 * @param connection The jdbc connection used to access the actual database.
	 * @param sqlStatement The SQL statement to yield the result set. Must be formatted properly.
	 * @throws SQLException Exception which will be thrown in case anything went wrong while creating the result set.
	 */
	public AlbumItemResultSet(Connection connection, String sqlStatement) throws SQLException {
		Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
		this.items = statement.executeQuery(sqlStatement);
		this.metaData = items.getMetaData();
		this.albumName = metaData.getTableName(1);	
		this.metaInfoMap = DatabaseWrapper.getAlbumItemMetaMap(albumName);
	}	
	
	/**
	 * Constructor. Convenience method which allows to specify the album name explicitly.
	 * @param connection The jdbc connection used to access the actual database.
	 * @param sqlStatement The SQL statement to yield the result set. Must be formatted properly.
	 * @param metaInfoMap A map containing all the metadata of the fields.
	 * @throws SQLException Exception which will be thrown in case anything went wrong while creating the result set.
	 */
	public AlbumItemResultSet(Connection connection, String sqlStatement, Map<Integer, MetaItemField> metaInfoMap) throws SQLException {
		this.metaInfoMap = metaInfoMap;
		Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
		this.items = statement.executeQuery(sqlStatement);
		this.metaData = items.getMetaData();
		this.albumName = metaData.getTableName(1);
	}	

	/**
	 * Getter for the album name to which this result set refers to.
	 * @return The name of the album.
	 */
	public String getAlbumName() {
		return albumName;
	}

	/**
	 * Setter for the album name to which this result set refers to.
	 * @param albumName The new album name.
	 */
	public void setAlbumName(String albumName) {
		this.albumName = albumName;
	}

	/**
	 * Moves the cursor to the next position and indicates whether such a position exits.
	 * @return True if an advance in position yields another element. False otherwise.
	 */
	public boolean moveToNext() {
		try {
			return items.next();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Gets the number of fields for the current item.
	 * @return An integer specifying the number of fields.
	 */
	public int getFieldCount() {
		// Retrieve table metadata
		try {
			if (metaData != null) {
				return metaData.getColumnCount();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return 0;
	}

	/**
	 * Get the value of the specified field. It is attempted to be cast into the specified type T. 
	 * @param fieldIndex The index undert which the field value is stored.
	 * @return The value of this field.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getFieldValue(int fieldIndex) {
		FieldType type =  metaInfoMap.get(fieldIndex).getType();
		Object outValue = null;
		try {
			outValue = DatabaseWrapper.fetchFieldItemValue(items, fieldIndex, type, albumName);
		} catch (SQLException e) {
			e.printStackTrace();														
		}

		return (T)outValue;
	}

	/**
	 * Getter for a field name. 
	 * @param fieldIndex The index position under which the item is stored.
	 * @return The name of the field or null if the index is invalid.
	 */
	public String getFieldName(int fieldIndex) {
		MetaItemField field = metaInfoMap.get(fieldIndex);
		if (field == null) {
			return null;
		}
		String name = field.getName();

		return name;
	}

	/**
	 * Getter for the field type.
	 * @param fieldIndex The index position under which the item is stored.
	 * @return The type of the field or null if the index is invalid.
	 */
	public FieldType getFieldType(int fieldIndex) {
		MetaItemField field = metaInfoMap.get(fieldIndex);
		if (field == null) {
			return null;
		}
		FieldType type =  field.getType();

		return type;
	}
	
	/**
	 * Indicates if the specified field index points to an Id field.
	 * @param fieldIndex The index to be checked.
	 * @return True if the field is of type FieldType.ID. False otherwise.
	 */
	public boolean isItemID(int fieldIndex) {
		if (getFieldName(fieldIndex).equals(DatabaseWrapper.TYPE_INFO_COLUMN_NAME)) {
			return false;
		}
		return true; 
	}
	
	public boolean isItemUUID(int fieldIndex) {
		if (getFieldType(fieldIndex).equals(FieldType.UUID)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Closes the result set and ensures that allocated resources are freed properly-
	 * @return True if the closing was successfully. False otherwise.
	 */
	public boolean close() {
		try {
			items.close();
		} catch (SQLException e) {			
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
