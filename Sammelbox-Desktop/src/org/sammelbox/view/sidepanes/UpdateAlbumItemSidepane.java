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

package org.sammelbox.view.sidepanes;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.sammelbox.controller.GuiController;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.view.UIConstants;
import org.sammelbox.view.various.ComponentFactory;

public final class UpdateAlbumItemSidepane {
	private UpdateAlbumItemSidepane() {
		// use build method instead
	}
	
	/** Returns an "update album item" composite. This composite provides the fields (field names and value input fields)
	 *  needed by the update item composite.
	 * @param parentComposite the parent composite
	 * @param album the name of the album to which an item should be added
	 * @param albumItemId the id of the album item whose content is loaded into the fields
	 * @return a new "update album item" composite */
	public static Composite build(Composite parentComposite, final String album, final long albumItemId) {		
		Composite resizeComposite = new Composite(parentComposite, SWT.NONE);
		resizeComposite.setLayout(new GridLayout(1, false));
		resizeComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		// description (header) label
		ComponentFactory.getPanelHeaderComposite(resizeComposite, Translator.get(DictKeys.LABEL_UPDATE_ENTRY), Translator.toBeTranslated("Update current item"));

		// Setup ScrolledComposite containing an normal (basic) Composite
		ScrolledComposite scrolledComposite = new ScrolledComposite(resizeComposite,  SWT.NONE | SWT.H_SCROLL | SWT.V_SCROLL );
		scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		final Composite updateAlbumItemComposite = BasicAlbumItemSidepane.build(scrolledComposite, album, albumItemId);
		scrolledComposite.setContent(updateAlbumItemComposite);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.getHorizontalBar().setIncrement(scrolledComposite.getHorizontalBar().getIncrement() * UIConstants.SCROLL_SPEED_MULTIPLICATOR);

		// Add Button
		Button updateButton = new Button(updateAlbumItemComposite, SWT.PUSH);
		updateButton.setText(Translator.get(DictKeys.BUTTON_UPDATE_ITEM));
		updateButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		updateButton.addSelectionListener(
				BasicAlbumItemSidepane.getSelectionListenerForAddAndUpdateAlbumItemComposite(updateAlbumItemComposite, true, albumItemId));

		// Calculating the size of the scrolled composite at the end 
		// avoids having crushed buttons and text-fields..
		scrolledComposite.setMinSize(updateAlbumItemComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		GuiController.getGuiState().setCurrentAlbumItemSubComposite(updateAlbumItemComposite);
		GuiController.getGuiState().setIdOfAlbumItemInSidepane(albumItemId);
		
		return resizeComposite;
	}
}
