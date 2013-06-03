package collector.desktop.gui.sidepanes;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class EmptySidepane {
	
	/** Returns an empty composite
	 * @param parentComposite the parent composite
	 * @return a new empty composite */
	public static Composite build(Composite parentComposite) {
		// Setup empty composite
		Composite emptyComposite = new Composite(parentComposite, SWT.NONE);

		return emptyComposite;
	}
}
