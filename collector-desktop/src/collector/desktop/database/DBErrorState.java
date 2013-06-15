package collector.desktop.database;

/** 
 * An enumeration of the different errors that can occur during database operations.
 * ErrorWithDirtyState means that there can be lingering side effects in the db and/or involved variable. 
 * ErrorWithCleanState means that there are absolutely no side effects to the db  */
public enum DBErrorState {
	ErrorWithDirtyState,
	ErrorWithCleanState
}
