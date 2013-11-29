package org.sammelbox.android.controller;

public class DatabaseQueryHelper {
	static boolean isSpecialField(String fieldName) {
		return fieldName.equals("id") || fieldName.equals("content_version") ||
				fieldName.equals("schema_version") || fieldName.equals("typeinfo");
	}
}
