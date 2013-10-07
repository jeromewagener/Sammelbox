package org.sammelbox.model.database;

/** A helper class for building queries. A query component is the combination of a field name, an operator and a value */
public class QueryComponent {
	private String fieldName;
	private QueryOperator operator;
	private String value;

	public QueryComponent(String fieldName, QueryOperator operator, String value) {
		this.setFieldName(fieldName);
		this.setOperator(operator);
		this.setValue(value);
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public QueryOperator getOperator() {
		return operator;
	}

	public void setOperator(QueryOperator operator) {
		this.operator = operator;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
