package org.sammelbox.android.model.querybuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sammelbox.android.controller.DatabaseQueryOperation;
import org.sammelbox.android.controller.DatabaseWrapper;
import org.sammelbox.android.model.FieldType;

import android.content.Context;

public final class QueryBuilder {
	/** A private default constructor to forbid the creation of multiple instances */
	private QueryBuilder() {}
	
	private static final Map<String, String> SEARCH_TO_SQL_OPERATORS;
    static {
        Map<String, String> mySearchToSQLOperators = new HashMap<String, String>();

        mySearchToSQLOperators.put("equal", QueryOperator.EQUALS.toSqlOperator());
        mySearchToSQLOperators.put("not equal", QueryOperator.NOT_EQUALS.toSqlOperator());
        mySearchToSQLOperators.put("contains", QueryOperator.CONTAINS.toSqlOperator());
        mySearchToSQLOperators.put("smaller or equal to", QueryOperator.SMALLER_OR_EQUAL.toSqlOperator());
        mySearchToSQLOperators.put("smaller than", QueryOperator.SMALLER.toSqlOperator());
        mySearchToSQLOperators.put("bigger than", QueryOperator.BIGGER.toSqlOperator());
        mySearchToSQLOperators.put("bigger or equal to", QueryOperator.BIGGER_OR_EQUAL.toSqlOperator());
        mySearchToSQLOperators.put("before", QueryOperator.DATE_BEFORE.toSqlOperator());
        mySearchToSQLOperators.put("before or equal to", QueryOperator.DATE_BEFORE_OR_EQUAL.toSqlOperator());
        mySearchToSQLOperators.put("after or equal to", QueryOperator.DATE_AFTER_OR_EQUAL.toSqlOperator());
        mySearchToSQLOperators.put("after", QueryOperator.DATE_AFTER.toSqlOperator());
        
        SEARCH_TO_SQL_OPERATORS = Collections.unmodifiableMap(mySearchToSQLOperators);
    }
	
    public static QueryOperator getQueryOperator(String searchOperator) {
    	return QueryOperator.valueOfSQL(SEARCH_TO_SQL_OPERATORS.get(searchOperator));
    }
    
	/** Returns all natural language operators suited for text queries 
	 * @return a string array containing all natural language operators suited for text queries */
	public static String[] toTextOperatorStringArray() {
		return new String[] { 	
			"equal",
			"not equal",
			"contains"
		};
	}
		
	/** Returns all natural language operators suited for number queries 
	 * @return a string array containing all natural language operators suited for number queries */
	public static String[] toNumberOperatorStringArray() {
		return new String[] {
			"equal",
			"smaller than",
			"smaller or equal to",
			"bigger than",
			"bigger or equal to"
		};
	}
		
	/** Returns all operators suited for date queries 
	 * @return an string array containing all operators suited for date queries */
	public static String[] toDateOperatorStringArray() {			
		return new String[] {
			"equal",
			"before",
			"before or equal to",
			"after or equal to",
			"after"
		};
	}
		
	/** Returns all operators suited for yes/no queries 
	 * @return an string array containing all operators suited for yes/no queries */
	public static String[] toYesNoOperatorStringArray() {
		return new String[] { 	
				"equal"
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
	 * @return a valid SQL query as a string. By default a 'SELECT *' is performed on the field/column names. */
	public static String buildQuery(List<QueryComponent> queryComponents, boolean connectByAnd, String album, Context context) {
		return buildQuery(queryComponents, connectByAnd, album, null, false, context);
	}
	
	/** This method builds a SQL query string out of multiple query components 
	 * @param queryComponents a list of query components. Escapes all appearing quotes in the album fields.
	 * @param connectByAnd a boolean specifying whether the query components are connected by AND (connectedByAnd == true) 
	 * 						or by OR (connectedByAnd == false). 
	 * @param albumName the name of the album which should be queried.
	 * @param sortField the field upon which the results should be sorted. Can be null or empty if not needed
	 * @param sortAscending only if a sortField is specified. In this case, true means that the results are sorted ascending, false means descending
	 * @return a valid SQL query as a string. By default a 'SELECT *' is performed on the field/column names. */
	public static String buildQuery(List<QueryComponent> queryComponents, boolean connectByAnd, String albumName, String sortField, boolean sortAscending, Context context) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM " + DatabaseStringUtilities.encloseNameWithQuotes(DatabaseStringUtilities.generateTableName(albumName)));
		
		if (!queryComponents.isEmpty()) {
			query.append(" WHERE ");
		}

		Map<String, FieldType> fieldNameToFieldTypeMap = 
				DatabaseQueryOperation.retrieveFieldnameToFieldTypeMapping(
						DatabaseWrapper.getSQLiteDatabase(context), context, DatabaseStringUtilities.generateTableName(albumName));
			
		for (int i=0; i<queryComponents.size(); i++) {	
			
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
	
	/** This method builds a SQL query out of multiple query components and returns the resulting query string.
	 * @param queryComponents a list of query components
	 * @param connectByAnd a boolean specifying whether the query components are connected by AND (connectedByAnd == true) 
	 * 						or by OR (connectedByAnd == false) 
	 * @param album the name of the album which should be queried 
	 * @return a valid SQL query as a string. By default a 'SELECT *' is performed on the field/column names */
	public static String buildQueryString(ArrayList<QueryComponent> queryComponents, boolean connectByAnd, String album, Context context) {
		return buildQuery(queryComponents, connectByAnd, album, null, false, context);
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
