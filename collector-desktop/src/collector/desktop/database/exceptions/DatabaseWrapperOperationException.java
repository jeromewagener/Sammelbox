package collector.desktop.database.exceptions;

import collector.desktop.database.DBErrorState;

public class DatabaseWrapperOperationException extends Exception {

	
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
