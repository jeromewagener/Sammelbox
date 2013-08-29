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

package org.sammelbox.utilities;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.sammelbox.controller.managers.ConnectionManager;
import org.sammelbox.model.database.DatabaseStringUtilities;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException.DBErrorState;

public class TestQueries {
	public static boolean isDatabaseTablePresent(String tableName) throws DatabaseWrapperOperationException {
		String query = "SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "';";
	
		try (Statement statement = ConnectionManager.getConnection().createStatement();
			 ResultSet resultSet = statement.executeQuery(query)) {
			
			while (resultSet.next()) {
				return true;
			}
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE, e);
		}
		
		return false;
	}
	
	public static long getNumberOfRecordsInTable(String tableName) throws DatabaseWrapperOperationException {
		String countQuery = " SELECT COUNT(*) AS 'numberOfItems' FROM " + DatabaseStringUtilities.encloseNameWithQuotes(tableName); 
	
		try (Statement statement = ConnectionManager.getConnection().createStatement();
			 ResultSet resultSet = statement.executeQuery(countQuery);){			
			
			if (resultSet.next()) {
				return resultSet.getLong("numberOfItems");
			}

			throw new DatabaseWrapperOperationException(
					DBErrorState.ERROR_CLEAN_STATE, "The number of items could not be fetched for the table " + tableName);
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE, e);
		}
	}
}
