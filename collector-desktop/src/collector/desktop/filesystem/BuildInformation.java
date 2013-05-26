package collector.desktop.filesystem;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class BuildInformation {
	public enum BuildType {
	    DEVELOPMENT,
	    TESTING,
	    RELEASE
	}
	
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
		return buildInfoBundle.getString("buildVersion");
	}
	
	public String getBuildTimeStamp() {
		return buildInfoBundle.getString("buildTime");
	}
	
	public BuildType getBuildType() {
		return BuildType.valueOf(buildInfoBundle.getString("buildType"));
	}
}
