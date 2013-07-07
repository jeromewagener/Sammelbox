package collector.desktop.model;

public class GuiState {
	public static final String NO_ALBUM_SELECTED = "NO_ALBUM_SELECTED";
	
	private String selectedAlbum = NO_ALBUM_SELECTED;
	private String selectedView = null;
	private String quickSearchTerms = null;
	private boolean isViewDetailed = true;	
	
	public String getSelectedAlbum() {
		return selectedAlbum;
	}

	public void setSelectedAlbum(String selectedAlbum) {
		this.selectedAlbum = selectedAlbum;
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
