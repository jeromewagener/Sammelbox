package collector.desktop.tests.utilities;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import collector.desktop.model.database.utilities.ConnectionManager;

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
}
