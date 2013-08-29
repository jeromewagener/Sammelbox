package org.sammelbox.controller.filesystem.importing;

public class ImportException extends Exception {
	private static final long serialVersionUID = 8009279995814569681L;

	public ImportException(String message) {
		super(message);
	}

	public ImportException(String message, Throwable cause) {
		super(message, cause);
	}
}
