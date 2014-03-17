package org.sammelbox.view.various;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;

public class ScreenUtils {
	public static Rectangle getPrimaryScreenClientArea(Display display) {
		Monitor primaryMonitorBySwt = display.getPrimaryMonitor();
		Rectangle primaryMonitorClientAreaBySwt = primaryMonitorBySwt.getClientArea();
		GraphicsDevice[]screens =  GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
		  for (GraphicsDevice screen : screens) {
			  if (isPrimaryMonitor(screen)) {
				  // Cut off any excess area such as OS task-bars. 
				  Rectangle primaryScreenBoundsByJava = new Rectangle(	
						screen.getDefaultConfiguration().getBounds().x,
					   	screen.getDefaultConfiguration().getBounds().y,
					   	screen.getDefaultConfiguration().getBounds().width,
						screen.getDefaultConfiguration().getBounds().height);
				 
				  return primaryMonitorClientAreaBySwt.intersection(primaryScreenBoundsByJava);				 
			  } 			 
		  }
		  
		  // No primary screen has been found by java, use SWT to get clientArea of PrimaryScreen as fallback.
		  return primaryMonitorClientAreaBySwt;
	}
	
	public static boolean isPrimaryMonitor(GraphicsDevice screen) {
		java.awt.Rectangle screenBounds = screen.getDefaultConfiguration().getBounds();
		
		// If the top left corner of the screen is (0,0) that means we consider it the primary screen.
		// The x,y might be negative too depending on the virtual screens.
		if (screenBounds.getX() == 0 && screenBounds.getY() == 0) {
			return true;
		}
		return false;
	}
}
