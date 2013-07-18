package collector.desktop;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import collector.desktop.model.album.AlbumItemPicture;
import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException;
import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException.DBErrorState;
import collector.desktop.model.database.utilities.ConnectionManager;

public class RemoveColumn {
	public static void main(String[] args) throws DatabaseWrapperOperationException, ClassNotFoundException {
			
		Class.forName("org.sqlite.JDBC");
		
		try {
			ConnectionManager.openConnection();
		} catch (DatabaseWrapperOperationException ex){
			try {
				ConnectionManager.openCleanConnection();				
			} catch (DatabaseWrapperOperationException ex2) {
			}			
		}
		
		String query = "CREATE TABLE postkarten_pictures_2 ( id INTEGER PRIMARY KEY , 'original_picture_filename' TEXT , 'thumbnail_picture_filename' TEXT , 'album_item_foreign_key' TEXT)";
	
		try (Statement statement = ConnectionManager.getConnection().createStatement();) {		
			statement.execute(query);
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState, e);
		}				
		
		String query2 = "SELECT original_picture_filename, thumbnail_picture_filename, album_item_foreign_key FROM postkarten_pictures";
		
		List<AlbumItemPicture> pictures = new ArrayList<>();
		
		try (Statement statement = ConnectionManager.getConnection().createStatement();		
			ResultSet resultSet = statement.executeQuery(query2);) {
			
			while (resultSet.next()) {
				pictures.add(new AlbumItemPicture(resultSet.getString(2), resultSet.getString(1), "", resultSet.getLong(3)));
			}
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState, e);
		}
		
		for (AlbumItemPicture picture : pictures) {
			String query3 = "INSERT INTO postkarten_pictures_2 (original_picture_filename, thumbnail_picture_filename, album_item_foreign_key) VALUES " +
					"('" + picture.getOriginalPictureName() + "', '" + picture.getThumbnailPictureName() + "', '" + picture.getAlbumItemID() + "')";
			
			try (Statement statement = ConnectionManager.getConnection().createStatement();) {		
				statement.executeUpdate(query3);
			} catch (SQLException e) {
				throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState, e);
			}
		}
		
		ConnectionManager.closeConnection();
	}
}
