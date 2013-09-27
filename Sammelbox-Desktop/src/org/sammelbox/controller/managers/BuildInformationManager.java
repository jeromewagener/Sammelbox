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

package org.sammelbox.controller.managers;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BuildInformationManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(BuildInformationManager.class);
		
	private static ResourceBundle buildInfoBundle = null;
	private static BuildInformationManager instance;
	
	private BuildInformationManager() {		
		try {
			buildInfoBundle = ResourceBundle.getBundle("dynamic/build");
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
	
	public String getApplicationName() {
		return buildInfoBundle.getString("app.name");
	}
	
	public String getVersion() {
		return buildInfoBundle.getString("app.version");
	}
	
	public String getBuildTimeStamp() {
		return buildInfoBundle.getString("build.time");
	}
}
