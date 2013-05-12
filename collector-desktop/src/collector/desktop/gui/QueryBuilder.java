package collector.desktop.gui;

import java.util.ArrayList;

import collector.desktop.Collector;
import collector.desktop.database.DatabaseWrapper;

public class QueryBuilder {
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
					"equals", // TODO translate
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
			else if (queryOperator.equals(QueryOperator.dateAfter)) {
				return ">=";
			}

			return null;
		}

		/** Transform a SQL operator string into a QueryOperator
		 * @param sqlOperator a string containing a SQL operator
		 * @return a QueryOperator transformation of the SQL operator */
		public static QueryOperator toQueryOperator(String sqlOperator) {
			if (sqlOperator.equals("=")) {
				return QueryOperator.equals;
			}
			else if (sqlOperator.equals("!=")) {
				return QueryOperator.notEquals;
			}
			else if (sqlOperator.equals("like")) {
				return QueryOperator.like;
			}
			else if (sqlOperator.equals("<")) {
				return QueryOperator.smallerThan;
			}
			else if (sqlOperator.equals("<=")) {
				return QueryOperator.smallerOrEqualThan;
			}
			else if (sqlOperator.equals(">")) {
				return QueryOperator.biggerThan;
			}
			else if (sqlOperator.equals(">=")) {
				return QueryOperator.biggerOrEqualThan;
			}
			else if (sqlOperator.equals("equals")) {
				return QueryOperator.dateEquals;
			}
			else if (sqlOperator.equals("before")) {
				return QueryOperator.dateBefore;
			}
			else if (sqlOperator.equals("before or equals")) {
				return QueryOperator.dateBeforeEquals;
			}
			else if (sqlOperator.equals("after or equals")) {
				return QueryOperator.dateAfterEquals;
			}
			else if (sqlOperator.equals("after")) {
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
	 * @param album the name of the album which should be queried.
	 * @param sortField the field upon which the results should be sorted. Can be null or empty if not needed
	 * @param sortAscending only if a sortField is specified. In this case, true means that the results are sorted ascending, false means descending
	 * @return a valid SQL query as a string. By default a 'SELECT *' is performed on the field/column names. */
	public static String buildQuery(ArrayList<QueryComponent> queryComponents, boolean connectByAnd, String album, String sortField, boolean sortAscending) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM '" + album + "'");
		
		if (!queryComponents.isEmpty()) {
			query.append(" WHERE ");
		}

		for (int i=0; i<queryComponents.size(); i++) {			
			if (queryComponents.get(i).operator == QueryOperator.like) {
				query.append( "(" +
						"[" + queryComponents.get(i).fieldName + "] " + 
						QueryOperator.toSQLOperator(queryComponents.get(i).operator) + " " + 
						"'%" + DatabaseWrapper.sanitizeSingleQuotesInAlbumItemValues(queryComponents.get(i).value) + "%')");
			} else {
				query.append( "(" +
						"[" + queryComponents.get(i).fieldName + "] " + 
						QueryOperator.toSQLOperator(queryComponents.get(i).operator) + " " + 
						"'" + DatabaseWrapper.sanitizeSingleQuotesInAlbumItemValues(queryComponents.get(i).value) + "')");
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
		
		System.out.println("Advanced Search: " + query.toString()); // TODO log
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
		BrowserContent.performBrowserQueryAndShow(Collector.getAlbumItemSWTBrowser(), query);
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
		BrowserContent.performBrowserQueryAndShow(Collector.getAlbumItemSWTBrowser(), query);
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
}
