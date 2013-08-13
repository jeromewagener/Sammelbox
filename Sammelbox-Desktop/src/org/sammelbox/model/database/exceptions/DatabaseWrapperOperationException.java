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

package org.sammelbox.model.database.exceptions;

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
