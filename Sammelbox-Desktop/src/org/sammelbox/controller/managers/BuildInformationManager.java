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

package org.sammelbox.controller.managers;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BuildInformationManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(BuildInformationManager.class);
	
	public enum BuildType {
		/** 
		 * A SNAPSHOT build has no restrictions and everything is allowed. Nothing is guaranteed within such a build.
		 */
		SNAPSHOT,
		
		/**
		 * A DEVELOPMENT build generally works but might contain minor bugs and issues
		 */
	    DEVELOPMENT,
	    
	    /**
	     * A TESTING build is similar to a DEVELOPMENT build, but must have passed all Unit tests
	     */
	    TESTING,
	    
	    /**
	     * A RELEASE build is similar to a TESTING build, but all known issues and/or bugs (with regard to the current release) must have been fixed
	     */
	    RELEASE			
	}
	
	private static ResourceBundle buildInfoBundle = null;
	private static BuildInformationManager instance;
	
	private BuildInformationManager() {		
		try {
			buildInfoBundle = ResourceBundle.getBundle("information/buildinfo");
		} catch (MissingResourceException mre) {
			LOGGER.error("The properties file with the build information could not be found");
		}
	}
	
	public static BuildInformationManager instance() {
		if (instance == null) {
			instance = new BuildInformationManager();
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
