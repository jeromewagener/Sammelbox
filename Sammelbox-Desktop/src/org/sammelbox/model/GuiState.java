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

package org.sammelbox.model;

import org.sammelbox.controller.settings.SettingsManager;

public class GuiState {
	public static final String NO_ALBUM_SELECTED = "NO_ALBUM_SELECTED"; 
	public static final String NO_VIEW_SELECTED = "NO_VIEW_SELECTED"; 
	
	private String selectedAlbum = null;
	private String selectedView = null;
	private String quickSearchTerms = null;
	private boolean isViewDetailed = SettingsManager.getSettings().isDetailedViewDefault();
	
	public GuiState() {
	}
	
	public GuiState(String selectedAlbum, String selectedView, String quickSearchTerms, boolean isViewDetailed) {
		this.selectedAlbum = selectedAlbum;
		this.selectedView = selectedView;
		this.quickSearchTerms = quickSearchTerms;
		this.isViewDetailed = isViewDetailed;
	}
	
	public String getSelectedAlbum() {
		return selectedAlbum;
	}

	public void setSelectedAlbum(String selectedAlbum) {
		this.selectedAlbum = selectedAlbum;
	}
		
	public boolean isAlbumSelected() {
		if (getSelectedAlbum() != null && !getSelectedAlbum().isEmpty() && !getSelectedAlbum().equals(NO_ALBUM_SELECTED)) {
			return true;
		}
		return false;
	}

	public boolean isViewSelected() {
		if (getSelectedView() != null && !getSelectedView().isEmpty() && !getSelectedView().equals(NO_VIEW_SELECTED)) {
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
