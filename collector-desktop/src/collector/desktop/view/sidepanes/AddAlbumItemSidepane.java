package collector.desktop.view.sidepanes;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import collector.desktop.controller.i18n.DictKeys;
import collector.desktop.controller.i18n.Translator;
import collector.desktop.view.UIConstants;
import collector.desktop.view.various.ComponentFactory;

public class AddAlbumItemSidepane {

	/** Returns an "add album item" composite. This composite provides the fields (field names and value input fields)
	 *  needed by the add item composite.
	 * @param parentComposite the parent composite
	 * @param album the name of the album to which an item should be added
	 * @return a new "add album item" composite */
	public static Composite build(Composite parentComposite, final String album) {
		Composite resizeComposite = new Composite(parentComposite, SWT.NONE);
		resizeComposite.setLayout(new GridLayout(1, false));
		resizeComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		// description (header) label
		ComponentFactory.getPanelHeaderComposite(resizeComposite, Translator.get(DictKeys.LABEL_ADD_ENTRY));

		// Setup ScrolledComposite containing an normal (basic) Composite
		ScrolledComposite scrolledComposite = new ScrolledComposite(resizeComposite,  SWT.NONE | SWT.H_SCROLL | SWT.V_SCROLL );
		scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		final Composite addAlbumItemComposite = BasicAlbumItemSidepane.build(scrolledComposite, album);
		scrolledComposite.setContent(addAlbumItemComposite);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.getHorizontalBar().setIncrement(scrolledComposite.getHorizontalBar().getIncrement() * UIConstants.SCROLL_SPEED_MULTIPLICATOR);

		// Add Button
		Button addButton = new Button(addAlbumItemComposite, SWT.PUSH);
		addButton.setText(Translator.get(DictKeys.BUTTON_ADD_ENTRY));
		addButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		addButton.addSelectionListener(BasicAlbumItemSidepane.getSelectionListenerForAddAndUpdateAlbumItemComposite(addAlbumItemComposite, false, 0));

		// Calculating the size of the scrolled composite at the end 
		// avoids having crushed buttons and text-fields..
		scrolledComposite.setMinSize(addAlbumItemComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		return resizeComposite;
	}
}
