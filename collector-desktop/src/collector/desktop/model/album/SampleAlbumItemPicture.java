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
