/** -----------------------------------------------------------------
 *    Sammelbox: Collection Manager - A free and open-source collection manager for Windows & Linux
 *    Copyright (C) 2011 Jerome Wagener & Paul Bicheler
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

package org.sammelbox.model.database.exceptions;

public class DatabaseWrapperOperationException extends Exception {
	/** An enumeration of the different errors that can occur during database operations.
	 * ERROR_CLEAN_STATE means that there are absolutely no side effects to the db  */
	public static enum DBErrorState {
		/** ERROR_DIRTY_STATE means that there can be lingering side effects in the db and/or involved variable. */
		ERROR_DIRTY_STATE,
		/** ERROR_CLEAN_STATE means that there are absolutely no side effects to the db */
		ERROR_CLEAN_STATE
	}
	
	private static final long serialVersionUID = 1L;	
	/** When this exception is caught this state can be used to determine which type of error the database has encountered.*/
	private DBErrorState errorState;
	
	public DatabaseWrapperOperationException(DBErrorState errorState, String message) {
		super(message);
		this.errorState = errorState;
	}
	
	public DatabaseWrapperOperationException(DBErrorState errorState) {
		super();
		this.errorState = errorState;
	}
	
	public DatabaseWrapperOperationException(DBErrorState errorState, Throwable cause) {
		super(cause);
		this.errorState = errorState;
	}

	public DBErrorState getErrorState() {
		return errorState;
	}

	public void setErrorState(DBErrorState errorState) {
		this.errorState = errorState;
	}
}
