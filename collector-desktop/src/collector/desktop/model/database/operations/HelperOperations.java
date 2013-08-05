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

import collector.desktop.controller.managers.ConnectionManager;
import collector.desktop.model.album.FieldType;
import collector.desktop.model.album.ItemField;
import collector.desktop.model.album.OptionType;
import collector.desktop.model.album.StarRating;
import collector.desktop.model.database.DatabaseStringUtilities;
import collector.desktop.model.database.QueryBuilder;
import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException;
import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException.DBErrorState;

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
			case Decimal: 
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
				StarRating starRating = field.getValue();
				preparedStatement.setInt(parameterIndex, starRating.getIntegerValue());		
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
	 * @param tableName The name of the table to which the column belongs to. Do NOT escape!
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
			LOGGER.error("Could not detect fieldtype for column [" + columnName + "] in table [" + tableName + "]", sqlException);
			return FieldType.Text;
		}	

		String dbtypeInfoTableName = DatabaseStringUtilities.generateTypeInfoTableName(tableName);
		try (
				Statement statement = ConnectionManager.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
				ResultSet typeResultSet = statement.executeQuery(QueryBuilder.createSelectColumnQuery(dbtypeInfoTableName, columnName));) {			
			return FieldType.valueOf(typeResultSet.getString(1));
			
		} catch (SQLException e) {
			LOGGER.error("Could not detect fieldtype for column [" + columnName + "] in table [" + tableName + "]", e);
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState, e);
		} catch (IllegalArgumentException e) {
			LOGGER.error("Could not detect fieldtype for column [" + columnName + "] in table [" + tableName + "]", e);
			return FieldType.Text;
		}	
		
	}

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
			case Decimal:
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
				value = StarRating.getByIntegerValue(results.getInt(columnIndex));
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
}
