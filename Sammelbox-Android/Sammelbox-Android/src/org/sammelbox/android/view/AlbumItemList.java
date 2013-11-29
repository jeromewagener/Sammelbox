package org.sammelbox.android.view;

import org.sammelbox.R;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/** A list container to show album items */
public class AlbumItemList extends ArrayAdapter<String>{
	private final Activity context;
	private final Drawable[] images;
	private final String[] data;
	
	public AlbumItemList(Activity context, Drawable[] images, String[] data) {
		super(context, R.layout.album_item, data);
		this.context = context;
		this.data = data;
		this.images = images;

	}
	
	@Override
	public View getView(int position, View view, ViewGroup parent) {
		LayoutInflater inflater = context.getLayoutInflater();
		View rowView = inflater.inflate(R.layout.album_item, null, true);
		TextView albumItemData = (TextView) rowView.findViewById(R.id.albumItemData);

		ImageView imageView = (ImageView) rowView.findViewById(R.id.mainAlbumItemPicture);
		albumItemData.setText(data[position]);

		imageView.setImageDrawable(images[position]);
		return rowView;
	}
}
