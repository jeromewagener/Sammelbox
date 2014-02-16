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

import org.eclipse.swt.widgets.Composite;
import org.sammelbox.view.SammelView;
import org.sammelbox.view.various.PanelType;

public class GuiState {
	/** Default value if no album is selected */
	public static final String NO_ALBUM_SELECTED = "NO_ALBUM_SELECTED"; 
	/** Default value if no saved search is selected */
	public static final String NO_VIEW_SELECTED = "NO_VIEW_SELECTED"; 
	
	/** The currently selected album */
	private String selectedAlbum = null;
	/** The currently selected saved search */
	private String selectedSavedSearch = null;
	/** The quick search terms string that is currently displayed */
	private String quickSearchTerms = null;
	/** The currently selected view */
	private SammelView sammelView = SammelView.DETAILED_VIEW; 
	// TODO adapt SettingsManager.getSettings().isDetailedViewDefault();
	
	/** The panel type of the right sidepane */
	private PanelType currentRightSidepaneType = PanelType.EMPTY;
	/** A reference to the composite of the current right sidepane */	
	private Composite currentRightSidepaneComposite = null;
	/** A reference to the sub-composite (BasicAlbumItemSidePane) of the current right sidepane */
	private Composite currentRightAlbumItemSubComposite = null;
	/** The id of the album item currently displayed via the right sidepane */
	private long idOfAlbumItemInSidepane = -1;
	/** True if the sidepane contains unsaved modifications, false otherwise */
	private boolean unsavedAlbumItem = false;
	
	public GuiState() {
	}
	
	public GuiState(String selectedAlbum, String selectedView, String quickSearchTerms, SammelView sammelView) {
		this.selectedAlbum = selectedAlbum;
		this.selectedSavedSearch = selectedView;
		this.quickSearchTerms = quickSearchTerms;
		this.sammelView = sammelView;
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
		if (getSelectedSavedSearch() != null && !getSelectedSavedSearch().isEmpty() && !getSelectedSavedSearch().equals(NO_VIEW_SELECTED)) {
			return true;
		}
		
		return false;
	}
	
	public String getSelectedSavedSearch() {
		return selectedSavedSearch;
	}

	public void setSelectedView(String selectedView) {
		this.selectedSavedSearch = selectedView;
	}

	public String getQuickSearchTerms() {
		return quickSearchTerms;
	}

	public void setQuickSearchTerms(String quickSearchTerms) {
		this.quickSearchTerms = quickSearchTerms;
	}

	public SammelView getSammelView() {
		return sammelView;
	}

	public void setSammelView(SammelView sammelView) {
		this.sammelView = sammelView;
	}

	public PanelType getCurrentSidepaneType() {
		return currentRightSidepaneType;
	}

	public void setCurrentSidepaneType(PanelType currentSidepaneType) {
		this.currentRightSidepaneType = currentSidepaneType;
	}

	public Composite getCurrentSidepaneComposite() {
		return currentRightSidepaneComposite;
	}

	public void setCurrentSidepaneComposite(Composite currentSidepaneComposite) {
		this.currentRightSidepaneComposite = currentSidepaneComposite;
	}

	public long getIdOfAlbumItemInSidepane() {
		return idOfAlbumItemInSidepane;
	}

	public void setIdOfAlbumItemInSidepane(long idOfAlbumItemInSidepane) {
		this.idOfAlbumItemInSidepane = idOfAlbumItemInSidepane;
	}

	public Composite getCurrentAlbumItemSubComposite() {
		return currentRightAlbumItemSubComposite;
	}

	public void setCurrentAlbumItemSubComposite(Composite currentAlbumItemSubComposite) {
		this.currentRightAlbumItemSubComposite = currentAlbumItemSubComposite;
	}

	public boolean hasUnsavedAlbumItem() {
		return unsavedAlbumItem;
	}

	public void setUnsavedAlbumItem(boolean unsavedAlbumItem) {
		this.unsavedAlbumItem = unsavedAlbumItem;
	}
}
