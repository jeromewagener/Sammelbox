package collector.desktop.gui.composites;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import collector.desktop.Collector;
import collector.desktop.gui.browser.BrowserListener;

public class BrowserComposite {
	
	/** Returns a browser composite which is used to render HTML.
	 * @param parentComposite the parent composite
	 * @param browserListener a class of various listeners for the browser
	 * @return a new browser composite */
	public static Composite getBrowserComposite(Composite parentComposite, BrowserListener browserListener) {
		// setup SWT browser composite
		Composite browserComposite = new Composite(parentComposite, SWT.NONE);
		browserComposite.setLayout(new GridLayout());

		// the browser itself
		Browser browser = new Browser(browserComposite, SWT.NONE);
		browser.setDragDetect(false);

		// setup layout data for the browser
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		browser.setLayoutData(gridData);

		// store browser reference in the main shell & register location listener with the browser
		Collector.setAlbumItemSWTBrowser(browser);
		browser.addLocationListener(browserListener);
		browser.addProgressListener(browserListener);
		browser.addMenuDetectListener(browserListener);

		return browserComposite;
	}
}
