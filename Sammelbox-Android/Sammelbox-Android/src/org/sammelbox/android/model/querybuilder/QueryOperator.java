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

package org.sammelbox.android.model.querybuilder;

public enum QueryOperator {
		EQUALS 					("="),
		NOT_EQUALS 				("!="),
		CONTAINS     			("like"),
		SMALLER 				("<"), 
		SMALLER_OR_EQUAL 		("<="), 
		BIGGER 					(">"),
		BIGGER_OR_EQUAL 		(">="),
		DATE_EQUALS 			("="),
		DATE_BEFORE 			("<"),
		DATE_BEFORE_OR_EQUAL 	("<="),
		DATE_AFTER_OR_EQUAL 	(">="),
		DATE_AFTER 				(">");
		
		private final String sqlOperator;       

	    private QueryOperator(String sqlOperator) {
	    	this.sqlOperator = sqlOperator;
	    }
	    
	    public String toSqlOperator() {
	        return sqlOperator;
	    }
	    
	    public static QueryOperator valueOfSQL(String searchOperator) {
	        for (QueryOperator operator : values()) {
	            if (operator.toSqlOperator().equals(searchOperator)) {
	                return operator;
	            }
	        }
	        return null;
	    }
	}