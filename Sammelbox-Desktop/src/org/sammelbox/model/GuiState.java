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

package org.sammelbox.model;

import org.sammelbox.controller.settings.SettingsManager;


public class GuiState {
	
	private String selectedAlbum = null;
	private String selectedView = null;
	private String quickSearchTerms = null;
	private boolean isViewDetailed = SettingsManager.getSettings().isDetailedViewDefault();
	/** This string is used to deselect an album by passing it to {@link #setSelectedAlbum(String)}*/
	public final String NOALBUMSELECTED = null; 
	
	public String getSelectedAlbum() {
		return selectedAlbum;
	}

	public void setSelectedAlbum(String selectedAlbum) {
		this.selectedAlbum = selectedAlbum;
	}
		
	/**
	 * Determines if an album has been selected.
	 * @return True if the selectedAlbumName is not null and not empty. True if an album is selected.
	 */
	public boolean isAlbumSelected() {
		if (getSelectedAlbum() != null && !getSelectedAlbum().isEmpty()) {
			return true;
		}
		return false;
	}

	public String getSelectedView() {
		return selectedView;
	}

	public void setSelectedView(String selectedView) {
		this.selectedView = selectedView;
	}

	public String getQuickSearchTerms() {
		return quickSearchTerms;
	}

	public void setQuickSearchTerms(String quickSearchTerms) {
		this.quickSearchTerms = quickSearchTerms;
	}

	public boolean isViewDetailed() {
		return isViewDetailed;
	}

	public void setViewDetailed(boolean isViewDetailed) {
		this.isViewDetailed = isViewDetailed;
	}
}
