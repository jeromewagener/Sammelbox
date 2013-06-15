package collector.desktop.database.exceptions;

import collector.desktop.database.DBErrorState;
//TODO: rename to failedCollectorDBOperationException
public class FailedDatabaseWrapperOperationException extends Exception {

	
	private static final long serialVersionUID = 1L;	
	/** When this exception is caught this state can be used to determine which type of error the database has encountered.*/
	public DBErrorState ErrorState;
	
	public FailedDatabaseWrapperOperationException(DBErrorState errorState, String message) {
		super(message);
		this.ErrorState = errorState;
	}
	
	public FailedDatabaseWrapperOperationException(DBErrorState errorState) {
		super();
		this.ErrorState = errorState;
	}
	
	public FailedDatabaseWrapperOperationException(DBErrorState errorState, Throwable cause) {
		super(cause);
		this.ErrorState = errorState;
	}
}
