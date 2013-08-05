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

package collector.desktop.model.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import collector.desktop.model.album.FieldType;
import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException;
import collector.desktop.model.database.operations.DatabaseOperations;
import collector.desktop.view.browser.BrowserFacade;

public class QueryBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger(QueryBuilder.class);
	
	/** A singleton instance of the QueryBuilder class */
	private static QueryBuilder instance = null;
	
	/** This method either creates & returns the singleton instance, or if already created, simply returns it */
	private static QueryBuilder getInstance() {
		if (instance == null) {
			instance = new QueryBuilder();
		}
		return instance;
	}
	
	/** A private default constructor to forbid the creation of multiple instances */
	private QueryBuilder() {}
	
	/** All available query operators together with some utility functions */
	public enum QueryOperator {
		equals,
		notEquals,
		like,
		smallerThan, 
		smallerOrEqualThan, 
		biggerThan,
		biggerOrEqualThan,
		dateEquals,
		dateBefore,
		dateBeforeEquals,
		dateAfterEquals,
		dateAfter;
		
		/** Returns all operators suited for text queries 
		 * @return an string array containing all operators suited for text queries */
		public static String[] toTextOperatorStringArray() {
			return new String[] { 	
					"=",
					"!=",
					"like"
			};
		}
		
		/** Returns all operators suited for number queries 
		 * @return an string array containing all operators suited for number queries */
		public static String[] toNumberOperatorStringArray() {
			return new String[] { 	
					"=",
					"<",
					"<=",
					">",
					">="
			};
		}
		
		/** Returns all operators suited for date queries 
		 * @return an string array containing all operators suited for date queries */
		public static String[] toDateOperatorStringArray() {
			return new String[] { 	
					"equals",
					"before",
					"before or equals",
					"after or equals",
					"after"
			};
		}
		
		/** Returns all operators suited for yes/no queries 
		 * @return an string array containing all operators suited for yes/no queries */
		public static String[] toYesNoOperatorStringArray() {
			return new String[] { 	
					"="
			};
		}
		
		/** Transform a QueryOperator to the corresponding SQL operator
		 * @param queryOperator the QueryOperator to be transformed
		 * @return a string with the corresponding SQL operator */
		public static String toSQLOperator(QueryOperator queryOperator) {
			if (queryOperator.equals(QueryOperator.equals)) {
				return "=";
			}
			else if (queryOperator.equals(QueryOperator.notEquals)) {
				return "!=";
			}
			else if (queryOperator.equals(QueryOperator.like)) {
				return "like";
			}
			else if (queryOperator.equals(QueryOperator.smallerThan)) {
				return "<";
			}
			else if (queryOperator.equals(QueryOperator.smallerOrEqualThan)) {
				return "<=";
			}
			else if (queryOperator.equals(QueryOperator.biggerThan)) {
				return ">";
			}
			else if (queryOperator.equals(QueryOperator.biggerOrEqualThan)) {
				return ">=";
			} 
			else if (queryOperator.equals(QueryOperator.dateEquals)) {
				return "=";
			} 
			else if (queryOperator.equals(QueryOperator.dateBefore)) {
				return "<";
			} 
			else if (queryOperator.equals(QueryOperator.dateBeforeEquals)) {
				return "<=";
			} 
			else if (queryOperator.equals(QueryOperator.dateAfterEquals)) {
				return ">";
			}

			return null;
		}

		/** Transform a SQL operator string into a QueryOperator
		 * @param fieldOperator a string containing a field operator
		 * @return a QueryOperator transformation of the field operator */
		public static QueryOperator toQueryOperator(String fieldOperator) {
			if (fieldOperator.equals("=")) {
				return QueryOperator.equals;
			}
			else if (fieldOperator.equals("!=")) {
				return QueryOperator.notEquals;
			}
			else if (fieldOperator.equals("like")) {
				return QueryOperator.like;
			}
			else if (fieldOperator.equals("<")) {
				return QueryOperator.smallerThan;
			}
			else if (fieldOperator.equals("<=")) {
				return QueryOperator.smallerOrEqualThan;
			}
			else if (fieldOperator.equals(">")) {
				return QueryOperator.biggerThan;
			}
			else if (fieldOperator.equals(">=")) {
				return QueryOperator.biggerOrEqualThan;
			}
			else if (fieldOperator.equals("equals")) {
				return QueryOperator.dateEquals;
			}
			else if (fieldOperator.equals("before")) {
				return QueryOperator.dateBefore;
			}
			else if (fieldOperator.equals("before or equals")) {
				return QueryOperator.dateBeforeEquals;
			}
			else if (fieldOperator.equals("after or equals")) {
				return QueryOperator.dateAfterEquals;
			}
			else if (fieldOperator.equals("after")) {
				return QueryOperator.dateAfter;
			}
			else if (fieldOperator.equals("after")) {
				return QueryOperator.dateAfter;
			}
			else {
				return null;
			}
		}
	}

	/** A helper (data) class for building queries. A query component is the combination of a field name, an operator and a value */
	public class QueryComponent {
		public String fieldName;
		public QueryOperator operator;
		public String value;

		public QueryComponent(String fieldName, QueryOperator operator, String value) {
			this.fieldName = fieldName;
			this.operator = operator;
			this.value = value;
		}
	}
	
	/** This factory method returns a query component based on the provided parameters
	 * @param fieldName the name of the field respectively column 
	 * @param operator the operator to be used 
	 * @param value the value which should be used for querying
	 * @return a query component based on the provided parameters */
	public static QueryComponent getQueryComponent(String fieldName, QueryOperator operator, String value) {
		return getInstance().new QueryComponent(fieldName, operator, value);
	}
	
	/** This method builds a SQL query string out of multiple query components 
	 * @param queryComponents a list of query components. Escapes all appearing quotes in the album fields.
	 * @param connectByAnd a boolean specifying whether the query components are connected by AND (connectedByAnd == true) 
	 * 						or by OR (connectedByAnd == false). 
	 * @param album the name of the album which should be queried. 
	 * @return a valid SQL query as a string. By default a 'SELECT *' is performed on the field/column names. */
	public static String buildQuery(ArrayList<QueryComponent> queryComponents, boolean connectByAnd, String album) {
		return buildQuery(queryComponents, connectByAnd, album, null, false);
	}
	
	/** This method builds a SQL query string out of multiple query components 
	 * @param queryComponents a list of query components. Escapes all appearing quotes in the album fields.
	 * @param connectByAnd a boolean specifying whether the query components are connected by AND (connectedByAnd == true) 
	 * 						or by OR (connectedByAnd == false). 
	 * @param albumName the name of the album which should be queried.
	 * @param sortField the field upon which the results should be sorted. Can be null or empty if not needed
	 * @param sortAscending only if a sortField is specified. In this case, true means that the results are sorted ascending, false means descending
	 * @return a valid SQL query as a string. By default a 'SELECT *' is performed on the field/column names. */
	public static String buildQuery(ArrayList<QueryComponent> queryComponents, boolean connectByAnd, String albumName, String sortField, boolean sortAscending) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM " + DatabaseStringUtilities.encloseNameWithQuotes(DatabaseStringUtilities.generateTableName(albumName)));
		
		if (!queryComponents.isEmpty()) {
			query.append(" WHERE ");
		}

		Map<String, FieldType> fieldNameToFieldTypeMap = new HashMap<String, FieldType>();
		try {
			fieldNameToFieldTypeMap = DatabaseOperations.getAlbumItemFieldNameToTypeMap(albumName);
		} catch (DatabaseWrapperOperationException ex) {
			LOGGER.error("Couldn't determine field types for album " + albumName, ex);
		}
			
		for (int i=0; i<queryComponents.size(); i++) {	
			
			if (fieldNameToFieldTypeMap.get(queryComponents.get(i).fieldName).equals(FieldType.Option) ||
					fieldNameToFieldTypeMap.get(queryComponents.get(i).fieldName).equals(FieldType.URL) ||
					fieldNameToFieldTypeMap.get(queryComponents.get(i).fieldName).equals(FieldType.Text)) {
				if (queryComponents.get(i).operator == QueryOperator.like) {
					query.append( "(" +
							"[" + queryComponents.get(i).fieldName + "] " + 
							QueryOperator.toSQLOperator(queryComponents.get(i).operator) + " " + 
							"'%" + DatabaseStringUtilities.sanitizeSingleQuotesInAlbumItemValues(queryComponents.get(i).value) + "%')");
				} else {
					query.append( "(" +
							"[" + queryComponents.get(i).fieldName + "] " + 
							QueryOperator.toSQLOperator(queryComponents.get(i).operator) + " " + 
							"'" + DatabaseStringUtilities.sanitizeSingleQuotesInAlbumItemValues(queryComponents.get(i).value) + "')");
				}
			} else {
				query.append( "(" +
						"[" + queryComponents.get(i).fieldName + "] " + 
						QueryOperator.toSQLOperator(queryComponents.get(i).operator) + " " + 
						queryComponents.get(i).value + ")");
			}

			if (i+1 != queryComponents.size()) {
				if (connectByAnd) {
					query.append(" AND ");
				} else {
					query.append(" OR ");
				}
			}
		}
		
		if (sortField != null && !sortField.isEmpty()) {
			query.append(" ORDER BY [" + sortField + "]");
			
			if (sortAscending) {
				query.append(" ASC");
			} else {
				query.append(" DESC");
			}
		}
		
		return query.toString();
	}

	/** This method builds a SQL query out of multiple query components and executes the resulting query. The result set is presented
	 * using the BrowserContent class.
	 * @param queryComponents a list of query components
	 * @param connectByAnd a boolean specifying whether the query components are connected by AND (connectedByAnd == true) 
	 * 						or by OR (connectedByAnd == false) 
	 * @param album the name of the album which should be queried */
	public static void buildQueryAndExecute(ArrayList<QueryComponent> queryComponents, boolean connectByAnd, String album) {
		String query = buildQuery(queryComponents, connectByAnd, album, null, false);		
		BrowserFacade.performBrowserQueryAndShow(query);
	}

	/** This method builds a SQL query out of multiple query components and executes the resulting query. The result set is presented
	 * using the BrowserContent class.
	 * @param queryComponents a list of query components
	 * @param connectByAnd a boolean specifying whether the query components are connected by AND (connectedByAnd == true) 
	 * 						or by OR (connectedByAnd == false) 
	 * @param sortField the field upon which the results should be sorted. Can be null or empty if not needed
	 * @param sortAscending only if a sortField is specified. In this case, true means that the results are sorted ascending, false means descending
	 * @param album the name of the album which should be queried */
	public static void buildQueryAndExecute(ArrayList<QueryComponent> queryComponents, boolean connectByAnd, String album, String sortField, boolean sortAscending) {
		String query = buildQuery(queryComponents, connectByAnd, album, sortField, sortAscending);
		BrowserFacade.performBrowserQueryAndShow(query);
	}
	
	/** This method builds a SQL query out of multiple query components and returns the resulting query string.
	 * @param queryComponents a list of query components
	 * @param connectByAnd a boolean specifying whether the query components are connected by AND (connectedByAnd == true) 
	 * 						or by OR (connectedByAnd == false) 
	 * @param album the name of the album which should be queried 
	 * @return a valid SQL query as a string. By default a 'SELECT *' is performed on the field/column names */
	public static String buildQueryString(ArrayList<QueryComponent> queryComponents, boolean connectByAnd, String album) {
		return buildQuery(queryComponents, connectByAnd, album, null, false);
	}
	
	/**
	 * Creates a simple select * from albumName with a properly formatted albumName
	 * @param albumName The album on which the query should be performed.
	 * @return A string containing the proper SQL string.
	 */
	public static String createSelectStarQuery(String albumName) {
		return "SELECT * FROM " + DatabaseStringUtilities.encloseNameWithQuotes(DatabaseStringUtilities.generateTableName(albumName));
	}
	
	/**
	 * Creates a simple select * from albumName with a properly formatted albumName and columnName
	 * @param albumName The name of the album to which this query refers to
	 * @param columnName The name of the column to be queried
	 * @param whereColumn The name of the column which is referenced in the where clause
	 * @return The properly formatted select query containing a wildcard as the value for in the where clause
	 */
	public static String createSelectColumnQuery(String albumName, String columnName) {

		return " SELECT " + DatabaseStringUtilities.transformColumnNameToSelectQueryName(columnName)+ 
			   " FROM " + DatabaseStringUtilities.encloseNameWithQuotes(DatabaseStringUtilities.generateTableName(albumName)); 
	}
	
	/**
	 *  Creates a query in the form of "SELECT COUNT(*) AS alias FROM albumName
	 * @param albumName the album to be counted
	 * @param alias the alias of the count field
	 * @return a string of the corresponding SQL query
	 */
	public static String createCountAsAliasStarWhere(String albumName, String alias) {
		return " SELECT COUNT(*) AS " + alias + 
			   " FROM " +  DatabaseStringUtilities.encloseNameWithQuotes(DatabaseStringUtilities.generateTableName(albumName));
		
	}
}
