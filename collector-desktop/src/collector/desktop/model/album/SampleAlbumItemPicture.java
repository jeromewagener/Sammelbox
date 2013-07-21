package collector.desktop.model.album;



// TODO explain
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
