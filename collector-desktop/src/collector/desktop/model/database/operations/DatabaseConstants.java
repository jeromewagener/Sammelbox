package collector.desktop.model.database.operations;

public class DatabaseConstants {
	/** The default name for ID columns */
	public static final String ID_COLUMN_NAME = "id";
	/** The final name of the column containing the type information foreign key in the main table.*/
	public static final String TYPE_INFO_COLUMN_NAME = "typeinfo";
	/** The suffix to the table containing all picture information for a single album */
	public static final String PICTURE_TABLE_SUFFIX = "_pictures";
	/** The suffix used to append to the main table name to obtain the typeInfo table name.*/
	public static final String TYPE_INFO_SUFFIX = "_typeinfo";
	/** Suffix used to append to the name of the main table to obtain the index name during index creation.*/
	public static final String INDEX_NAME_SUFFIX = "_index";
	/** The suffix used to append to the main table to obtain the temporary table name.*/
	public static final String TEMP_TABLE_SUFFIX = "_temptable";
	
	
	/** The name of the picture table column that stores the filename of the original picture */
	static final String ORIGINAL_PICTURE_FILE_NAME_IN_PICTURE_TABLE = "original_picture_filename";
	/** The name of the picture table column that stores the filename of the thumbnail picture */
	static final String THUMBNAIL_PICTURE_FILE_NAME_IN_PICTURE_TABLE = "thumbnail_picture_filename";
	/** The reference to the album item which is associated with the current picture */
	static final String ALBUM_ITEM_ID_REFERENCE_IN_PICTURE_TABLE = "album_item_foreign_key";
	/** The final foreign key of all main table entries to their type information entry */
	static final int TYPE_INFO_FOREIGN_KEY = 1;
	/** The final name of the schema version column. Updated at each structural change of an album */
	static final String SCHEMA_VERSION_COLUMN_NAME = "schema_version";
	/** The final name of the content version column. Updated at each change of the content of the field */
	static final String CONTENT_VERSION_COLUMN_NAME = "content_version";
	
	
	/** The name of the album master table containing all stored album table names and their type table names */
	static final String ALBUM_MASTER_TABLE_NAME = "album_master_table";
	/** The column name for the album name */
	static final String ALBUMNAME_IN_ALBUM_MASTER_TABLE = "album_name";
	/** The column name for the album table */
	static final String ALBUM_TABLENAME_IN_ALBUM_MASTER_TABLE= "album_table_name";
	/** The final name of the picture column. Currently only a single column is supported, this is its name */
	static final String HAS_PICTURES_COLUMN_IN_ALBUM_MASTER_TABLE = "has_pictures";
}
