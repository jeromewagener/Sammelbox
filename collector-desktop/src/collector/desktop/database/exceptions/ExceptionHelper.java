package collector.desktop.database.exceptions;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionHelper {
	/** Returns a string containing the stack trace of the exception 
	 * @return the stack trace as string of the trowable passed as parameter */
	public static String toString(Throwable trowable) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		trowable.printStackTrace(printWriter);
		
		return stringWriter.toString();
	}
}
