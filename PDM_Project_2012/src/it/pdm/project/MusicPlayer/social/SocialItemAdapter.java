package it.pdm.project.MusicPlayer.social;

import it.pdm.project.MusicPlayer.R;
import it.pdm.project.MusicPlayer.social.ImageThreadLoader.ImageLoadedListener;

import java.net.MalformedURLException;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SocialItemAdapter extends ArrayAdapter<SocialItem> {
	private final static String TAG = "MediaItemAdapter";
	private int resourceId = 0;
	private LayoutInflater inflater;
	private Context context;

	private ImageThreadLoader imageLoader = new ImageThreadLoader();

	public SocialItemAdapter(Context context, int resourceId, List<SocialItem> mediaItems) {
		super(context, 0, mediaItems);
		this.resourceId = resourceId;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		TextView textTitle, textAlbum, textArtist;
	    final ImageView image;
	
	    view = inflater.inflate(resourceId, parent, false);

	    try {
	    	textTitle = (TextView)view.findViewById(R.id.record_title);
	    	textAlbum = (TextView)view.findViewById(R.id.record_album);
	    	textArtist = (TextView)view.findViewById(R.id.record_artist);
	    	
	    	image = (ImageView)view.findViewById(R.id.img);
	    } catch( ClassCastException e ) {
	    	Log.e(TAG, "Your layout must provide an image and a text view with ID's icon and text.", e);
	    	throw e;
	    }

	    SocialItem item = getItem(position);
	    Bitmap cachedImage = null;
    
	    try {
	    	cachedImage = imageLoader.loadImage(item.strUrl, new ImageLoadedListener() {
	    		public void imageLoaded(Bitmap imageBitmap) {
	    			image.setImageBitmap(imageBitmap);
	    			notifyDataSetChanged();                
	    		}
	    	});
	    } catch (MalformedURLException e) {
	    	Log.e(TAG, "Bad remote image URL: " + item.strUrl, e);
	    }

	    textTitle.setText(item.strSongTitle);
	    textAlbum.setText(item.strSongAlbum);
	    textArtist.setText(item.strSongArtist);

	    if( cachedImage != null )
	    	image.setImageBitmap(cachedImage);

	    return view;
	}
}