package collector.desktop.model.album;

/** The sample album item picture is a special kind of album item picture
 * which should only be used if temporary or sample pictures are needed
 * for a temporary or sample album item. The main difference is that
 * the originally specified path will be returned in all cases 
 * Do NOT store this item in the Database! */
public class SampleAlbumItemPicture extends AlbumItemPicture {

	public SampleAlbumItemPicture(String samplePicturePath) {
		super(Long.MAX_VALUE, samplePicturePath, samplePicturePath, "SAMPLE", Long.MAX_VALUE);
	}
	
	@Override
	public String getThumbnailPicturePath() {
		return getThumbnailPictureName();
	}

	@Override
	public String getOriginalPicturePath() {
		return getThumbnailPictureName();
	}
}
