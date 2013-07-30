package collector.desktop.tests.utilities;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import collector.desktop.controller.managers.ConnectionManager;
import collector.desktop.model.database.DatabaseStringUtilities;
import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException;
import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException.DBErrorState;

public class TestQueries {
	public static boolean isDatabaseTableAvailable(String tableName) {
		String query = "SELECT * FROM " + tableName;
	
		try (Statement statement = ConnectionManager.getConnection().createStatement();
			 ResultSet resultSet = statement.executeQuery(query)) {
		} catch (SQLException e) {
			return false;
		}
		
		return true;
	}
	
	public static long getNumberOfRecordsInTable(String tableName) throws DatabaseWrapperOperationException {
		String countQuery = " SELECT COUNT(*) AS 'numberOfItems' FROM " + DatabaseStringUtilities.encloseNameWithQuotes(tableName); 
	
		try (Statement statement = ConnectionManager.getConnection().createStatement();
			 ResultSet resultSet = statement.executeQuery(countQuery);){			
			
			if (resultSet.next()) {
				return resultSet.getLong("numberOfItems");
			}

			throw new DatabaseWrapperOperationException(
					DBErrorState.ErrorWithCleanState, "The number of items could not be fetched for the table " + tableName);
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState, e);
		}
	}
}
