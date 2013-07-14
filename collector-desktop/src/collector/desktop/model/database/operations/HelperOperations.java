package collector.desktop.model.database.operations;

import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import collector.desktop.model.album.FieldType;
import collector.desktop.model.album.ItemField;
import collector.desktop.model.album.OptionType;
import collector.desktop.model.album.StarRating;
import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException;
import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException.DBErrorState;
import collector.desktop.model.database.utilities.ConnectionManager;
import collector.desktop.model.database.utilities.DatabaseStringUtilities;
import collector.desktop.model.database.utilities.QueryBuilder;

public class HelperOperations {
	private static final Logger LOGGER = LoggerFactory.getLogger(HelperOperations.class);
	
	/**
	 * This helper method sets a value based on type to a preparedStatement at the specified position
	 * @param preparedStatement The JDBC prepared statement to which the value is to be set.
	 * @param parameterIndex The index of the parameter to be set.
	 * @param field The field containing the value to be set as well as the according meta-data.
	 * @param albumName The name of the album to which the item of the field belongs.
	 * @throws DatabaseWrapperOperationException Exception thrown if any part of the operation fails. 
	 */
	static void setValueToPreparedStatement(PreparedStatement preparedStatement, int parameterIndex,  ItemField field, String albumName) throws DatabaseWrapperOperationException {
		try {
			switch (field.getType()) {
			case Text: 
				String text = field.getValue();
				preparedStatement.setString(parameterIndex, text);		
				break;
			case Number: 
				Double real = field.getValue();
				preparedStatement.setDouble(parameterIndex, real);		
				break;
			case Date: 
				Date date = field.getValue();
				preparedStatement.setDate(parameterIndex, date);		
				break;
			case Time: 
				Time time = field.getValue();
				preparedStatement.setTime(parameterIndex, time);		
				break;
			case Option: 
				OptionType option = field.getValue();
				// Textual representation of the enum is stored in the DB. 
				preparedStatement.setString(parameterIndex, option.toString());		
				break;
			case URL: 
				String	url = field.getValue();
				preparedStatement.setString(parameterIndex, url);		
				break;
			case StarRating: 
				StarRating	starRating = field.getValue();
				preparedStatement.setString(parameterIndex, starRating.toString());		
				break;
			case Integer: 
				Integer	integer = field.getValue();
				preparedStatement.setString(parameterIndex, integer.toString());		
				break;
			default:
				break;
			}			
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState, e);
		}
	}
	
	/**
	 * A helper method to detect the collector FieldType using the type information in the separate typeInfo table.
	 * @param tableName The name of the table to which the column belongs to.
	 * @param columnName The name of the column whose type should be determined.
	 * @return A FieldType expressing the type of the specified column in the resultSet.
	 * @throws DatabaseWrapperOperationException 
	 */
	static FieldType detectDataType(String tableName, String columnName) throws DatabaseWrapperOperationException {
		DatabaseMetaData dbmetadata = null;
		try {
			dbmetadata = ConnectionManager.getConnection().getMetaData();
		} catch (SQLException e1) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState);
		}
		try (ResultSet dbmetars = dbmetadata.getImportedKeys(null, null, tableName)) {
			// Get the primary and foreign keys
			String primaryKey = dbmetars.getString(4);
			String foreignKey = dbmetars.getString(8);
			
			if (columnName.equalsIgnoreCase(primaryKey) || columnName.equalsIgnoreCase(foreignKey)) {
				return FieldType.ID; 
			}
			
		} catch (SQLException sqlException) {
			return FieldType.Text;
		}	

		String dbtypeInfoTableName = DatabaseStringUtilities.encloseNameWithQuotes(QueryOperations.getTypeInfoTableName(tableName));
		try (
				Statement statement = ConnectionManager.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
				ResultSet typeResultSet = statement.executeQuery(QueryBuilder.createSelectColumnQuery(dbtypeInfoTableName, columnName));) {			
			return FieldType.valueOf(typeResultSet.getString(1));
			
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState, e);
		} catch (IllegalArgumentException e) {
			return FieldType.Text;
		}	
		
	}

	// TODO comment
	static Object fetchFieldItemValue(ResultSet results, int columnIndex, FieldType type, String albumName) throws DatabaseWrapperOperationException {
		Object value = null;
		try {
			switch (type) {
			case ID:
				value = results.getLong(columnIndex);
				break;
			case Text:
				value = results.getString(columnIndex);
				break;
			case Number:
				value = results.getDouble(columnIndex);
				break;
			case Integer:
				value = results.getInt(columnIndex);
				break;
			case Date:
				value = results.getDate(columnIndex);
				break;
			case Time:
				value = results.getTime(columnIndex);
				break;
			case Option:
				String optionValue = results.getString(columnIndex);
				if (optionValue != null && !optionValue.isEmpty()) {
					value =  OptionType.valueOf(optionValue);
				} else {
					LOGGER.error("Fetching option type for item field failed. Url string is unexpectantly null or empty");
				}
				break;
			case URL:
				String urlString = results.getString(columnIndex);
				value =  urlString;
				break;
			case StarRating:
				String rating = results.getString(columnIndex);
				if (rating != null && !rating.isEmpty()) {
					value =  StarRating.valueOf(rating);
				} else {
					LOGGER.error("Fetching star rating for item field failed. Star rating string is unexpectantly null or empty");
				}
				break;
			case UUID:
				value  = UUID.fromString(results.getString(columnIndex));
				break;
			default:
				value = null;
				break;
			}
			return value;
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState, e);
		}
	}
	
	static String buildTypeInfoTableName(String albumTableName) {
		if (albumTableName == null || albumTableName.isEmpty()) {
			return "";
		}
		
		return albumTableName + DatabaseConstants.TYPE_INFO_SUFFIX;
	}

	static String buildPictureTableName(String albumTableName) {
		if (albumTableName == null || albumTableName.isEmpty()) {
			return "";
		}
		
		return albumTableName + DatabaseConstants.PICTURE_TABLE_SUFFIX;
	}
}
