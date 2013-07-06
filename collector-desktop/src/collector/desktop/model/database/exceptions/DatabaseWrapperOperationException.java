package collector.desktop.model.database.exceptions;

public class DatabaseWrapperOperationException extends Exception {
	/** 
	 * An enumeration of the different errors that can occur during database operations.
	 * ErrorWithDirtyState means that there can be lingering side effects in the db and/or involved variable. 
	 * ErrorWithCleanState means that there are absolutely no side effects to the db  */
	public static enum DBErrorState {
		ErrorWithDirtyState,
		ErrorWithCleanState
	}
	
	private static final long serialVersionUID = 1L;	
	/** When this exception is caught this state can be used to determine which type of error the database has encountered.*/
	public DBErrorState ErrorState;
	
	public DatabaseWrapperOperationException(DBErrorState errorState, String message) {
		super(message);
		this.ErrorState = errorState;
	}
	
	public DatabaseWrapperOperationException(DBErrorState errorState) {
		super();
		this.ErrorState = errorState;
	}
	
	public DatabaseWrapperOperationException(DBErrorState errorState, Throwable cause) {
		super(cause);
		this.ErrorState = errorState;
	}
}
