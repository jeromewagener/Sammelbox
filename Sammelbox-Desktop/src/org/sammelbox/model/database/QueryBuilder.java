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

package org.sammelbox.model.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.model.album.FieldType;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseOperations;
import org.sammelbox.view.browser.BrowserFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class QueryBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger(QueryBuilder.class);
	
	/** A private default constructor to forbid the creation of multiple instances */
	private QueryBuilder() {}
	
	private static final Map<String, String> SEARCH_TO_SQL_OPERATORS;
    static {
        Map<String, String> mySearchToSQLOperators = new HashMap<String, String>();

        mySearchToSQLOperators.put(Translator.get(DictKeys.QUERY_OPERATOR_EQUAL), QueryOperator.EQUALS.toSqlOperator());
        mySearchToSQLOperators.put(Translator.get(DictKeys.QUERY_OPERATOR_NOT_EQUAL), QueryOperator.NOT_EQUALS.toSqlOperator());
        mySearchToSQLOperators.put(Translator.get(DictKeys.QUERY_OPERATOR_CONTAINS), QueryOperator.CONTAINS.toSqlOperator());
        mySearchToSQLOperators.put(Translator.get(DictKeys.QUERY_OPERATOR_SMALLER_OR_EQUAL), QueryOperator.SMALLER_OR_EQUAL.toSqlOperator());
        mySearchToSQLOperators.put(Translator.get(DictKeys.QUERY_OPERATOR_SMALLER_THAN), QueryOperator.SMALLER.toSqlOperator());
        mySearchToSQLOperators.put(Translator.get(DictKeys.QUERY_OPERATOR_BIGGER_THAN), QueryOperator.BIGGER.toSqlOperator());
        mySearchToSQLOperators.put(Translator.get(DictKeys.QUERY_OPERATOR_BIGGER_OR_EQUAL), QueryOperator.BIGGER_OR_EQUAL.toSqlOperator());
        mySearchToSQLOperators.put(Translator.get(DictKeys.QUERY_OPERATOR_BEFORE), QueryOperator.DATE_BEFORE.toSqlOperator());
        mySearchToSQLOperators.put(Translator.get(DictKeys.QUERY_OPERATOR_BEFORE_OR_EQUAL), QueryOperator.DATE_BEFORE_OR_EQUAL.toSqlOperator());
        mySearchToSQLOperators.put(Translator.get(DictKeys.QUERY_OPERATOR_AFTER_OR_EQUAL), QueryOperator.DATE_AFTER_OR_EQUAL.toSqlOperator());
        mySearchToSQLOperators.put(Translator.get(DictKeys.QUERY_OPERATOR_AFTER), QueryOperator.DATE_AFTER.toSqlOperator());
        
        SEARCH_TO_SQL_OPERATORS = Collections.unmodifiableMap(mySearchToSQLOperators);
    }
	
    public static String getHumanReadableQueryOperator(QueryOperator queryOperator) {
    	for (Map.Entry<String, String> searchToSqlOperator : SEARCH_TO_SQL_OPERATORS.entrySet()) {
    		if (searchToSqlOperator.getValue().equals(queryOperator.toSqlOperator())) {
    			return searchToSqlOperator.getKey();
    		}
    	}
    	
    	LOGGER.error("Could not find a translation for " + queryOperator);
    	
    	return "ERROR";
    }
    
    public static QueryOperator getQueryOperator(String searchOperator) {
    	return QueryOperator.valueOfSQL(SEARCH_TO_SQL_OPERATORS.get(searchOperator));
    }
    
	/** Returns all natural language operators suited for text queries 
	 * @return a string array containing all natural language operators suited for text queries */
	public static String[] toTextOperatorStringArray() {
		return new String[] { 	
			Translator.get(DictKeys.QUERY_OPERATOR_EQUAL),
			Translator.get(DictKeys.QUERY_OPERATOR_NOT_EQUAL),
			Translator.get(DictKeys.QUERY_OPERATOR_CONTAINS)
		};
	}
		
	/** Returns all natural language operators suited for number queries 
	 * @return a string array containing all natural language operators suited for number queries */
	public static String[] toNumberOperatorStringArray() {
		return new String[] { 	
			Translator.get(DictKeys.QUERY_OPERATOR_EQUAL),
			Translator.get(DictKeys.QUERY_OPERATOR_SMALLER_THAN),
			Translator.get(DictKeys.QUERY_OPERATOR_SMALLER_OR_EQUAL),
			Translator.get(DictKeys.QUERY_OPERATOR_BIGGER_OR_EQUAL),
			Translator.get(DictKeys.QUERY_OPERATOR_BIGGER_THAN)
		};
	}
		
	/** Returns all operators suited for date queries 
	 * @return an string array containing all operators suited for date queries */
	public static String[] toDateOperatorStringArray() {			
		return new String[] { 	
			Translator.get(DictKeys.QUERY_OPERATOR_EQUAL),
			Translator.get(DictKeys.QUERY_OPERATOR_BEFORE),
			Translator.get(DictKeys.QUERY_OPERATOR_BEFORE_OR_EQUAL),
			Translator.get(DictKeys.QUERY_OPERATOR_AFTER_OR_EQUAL),
			Translator.get(DictKeys.QUERY_OPERATOR_AFTER)
		};
	}
		
	/** Returns all operators suited for yes/no queries 
	 * @return an string array containing all operators suited for yes/no queries */
	public static String[] toYesNoOperatorStringArray() {
		return new String[] { 	
				Translator.get(DictKeys.QUERY_OPERATOR_EQUAL)
		};
	}
		
	/** Transform a search operator to the corresponding SQL operator
	 * @param searchOperator the QueryOperator to be transformed
	 * @return a string with the corresponding SQL operator */
	public static String toSQLOperator(QueryOperator searchOperator) {
		return searchOperator.toSqlOperator();
	}
	
	/** This factory method returns a query component based on the provided parameters
	 * @param fieldName the name of the field respectively column 
	 * @param operator the operator to be used 
	 * @param value the value which should be used for querying
	 * @return a query component based on the provided parameters */
	public static QueryComponent getQueryComponent(String fieldName, QueryOperator operator, String value) {
		return new QueryComponent(fieldName, operator, value);
	}
	
	/** This method builds a SQL query string out of multiple query components 
	 * @param queryComponents a list of query components. Escapes all appearing quotes in the album fields.
	 * @param connectByAnd a boolean specifying whether the query components are connected by AND (connectedByAnd == true) 
	 * 						or by OR (connectedByAnd == false). 
	 * @param album the name of the album which should be queried. 
	 * @return a valid SQL query as a string. By default a 'SELECT *' is performed on the field/column names. 
	 * @throws QueryBuilderException */
	public static String buildQuery(List<QueryComponent> queryComponents, boolean connectByAnd, String albumName) throws QueryBuilderException {
		return buildQuery(queryComponents, connectByAnd, albumName, null, false);
	}
	
	/** This method builds a SQL query string out of multiple query components 
	 * @param queryComponents a list of query components. Escapes all appearing quotes in the album fields.
	 * @param connectByAnd a boolean specifying whether the query components are connected by AND (connectedByAnd == true) 
	 * 						or by OR (connectedByAnd == false). 
	 * @param albumName the name of the album which should be queried.
	 * @param sortField the field upon which the results should be sorted. Can be null or empty if not needed
	 * @param sortAscending only if a sortField is specified. In this case, true means that the results are sorted ascending, false means descending
	 * @return a valid SQL query as a string. By default a 'SELECT *' is performed on the field/column names. 
	 * @throws QueryBuilderException */
	public static String buildQuery(List<QueryComponent> queryComponents, boolean connectByAnd, String albumName, String sortField, boolean sortAscending) throws QueryBuilderException {
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
			if (fieldNameToFieldTypeMap.get(queryComponents.get(i).getFieldName()) == null) {
				throw new QueryBuilderException(Translator.toBeTranslated(
						"The following field seems to cause problems: " + queryComponents.get(i).getFieldName() + " " +
						"Is this a renamed or deleted field? If yes, please adapt the search using the edit functionality."));
			}
			
			if (fieldNameToFieldTypeMap.get(queryComponents.get(i).getFieldName()).equals(FieldType.OPTION) ||
					fieldNameToFieldTypeMap.get(queryComponents.get(i).getFieldName()).equals(FieldType.URL) ||
					fieldNameToFieldTypeMap.get(queryComponents.get(i).getFieldName()).equals(FieldType.TEXT)) {
				if (queryComponents.get(i).getOperator() == QueryOperator.CONTAINS) {
					query.append( "(" +
							"[" + queryComponents.get(i).getFieldName() + "] " + 
							toSQLOperator(queryComponents.get(i).getOperator()) + " " + 
							"'%" + DatabaseStringUtilities.sanitizeSingleQuotesInAlbumItemValues(queryComponents.get(i).getValue()) + "%')");
				} else {
					query.append( "(" +
							"[" + queryComponents.get(i).getFieldName() + "] " + 
							toSQLOperator(queryComponents.get(i).getOperator()) + " " + 
							"'" + DatabaseStringUtilities.sanitizeSingleQuotesInAlbumItemValues(queryComponents.get(i).getValue()) + "')");
				}
			} else {
				query.append( "(" +
						"[" + queryComponents.get(i).getFieldName() + "] " + 
						toSQLOperator(queryComponents.get(i).getOperator()) + " " + 
						queryComponents.get(i).getValue() + ")");
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
	 * @param album the name of the album which should be queried 
	 * @throws QueryBuilderException */
	public static void buildQueryAndExecute(ArrayList<QueryComponent> queryComponents, boolean connectByAnd, String album) throws QueryBuilderException {
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
	 * @param album the name of the album which should be queried 
	 * @throws QueryBuilderException */
	public static void buildQueryAndExecute(ArrayList<QueryComponent> queryComponents, boolean connectByAnd, String album, String sortField, boolean sortAscending) throws QueryBuilderException {
		String query = buildQuery(queryComponents, connectByAnd, album, sortField, sortAscending);
		BrowserFacade.performBrowserQueryAndShow(query);
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
