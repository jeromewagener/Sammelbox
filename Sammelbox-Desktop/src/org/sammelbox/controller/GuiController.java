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

package org.sammelbox.controller;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.model.GuiState;

public final class GuiController {
	private static GuiState guiState = new GuiState();
	
	private GuiController() {
	}
	
	public static GuiState getGuiState() {
		return guiState;
	}
	
	public static void setGuiState(GuiState guiState) {
		GuiController.guiState = guiState;
	}
	
	public static boolean continueWithUnsavedModifications(Shell shell) {
		if (GuiController.getGuiState().hasUnsavedAlbumItem()) {
			MessageBox dialog = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES| SWT.NO);
			dialog.setText(Translator.get(DictKeys.DIALOG_TITLE_CONTINUE_UNSAVED_CHANGES));
			dialog.setMessage(Translator.get(DictKeys.DIALOG_CONTENT_CONTINUE_UNSAVED_CHANGES));

			if (dialog.open() == SWT.NO) {
				return false;
			}
		}
		
		return true;
	}
}