package collector.desktop.filesystem;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class BuildInformation {
	private static ResourceBundle buildInfoBundle = null;
	private static BuildInformation instance;
	
	private BuildInformation() {
		try {
			buildInfoBundle = ResourceBundle.getBundle("information/buildinfo");
		} catch (MissingResourceException mre) {
			System.err.println("properties not found"); // TODO log me
		}
	}
	
	public static BuildInformation instance() {
		if (instance == null) {
			instance = new BuildInformation();
		}
		
		return instance;
	}
	
	public String getVersion() {
		return buildInfoBundle.getString("version");
	}
	
	public String getBuildTimeStamp() {
		return buildInfoBundle.getString("buildtime");
	}
}
