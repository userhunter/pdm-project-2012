package it.pdm.project.MusicPlayer.social;
/**
 * Adapter per la popolazione delle righe dei post nella tab Social
 */
import it.pdm.project.MusicPlayer.R;
import it.pdm.project.MusicPlayer.social.ImageThreadLoader.ImageLoadedListener;
import it.pdm.project.MusicPlayer.social.facebook.Post;

import java.net.MalformedURLException;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SocialItemAdapter extends ArrayAdapter<Post> {
	private final static String TAG = "MediaItemAdapter";
	private int resourceId = 0;
	private LayoutInflater inflater;
	@SuppressWarnings("unused")
	private Context context;
	@SuppressWarnings("unused")
	private Resources m_res;

	private ImageThreadLoader imageLoader = new ImageThreadLoader();

	public SocialItemAdapter(Context context, int resourceId, List<Post> mediaItems, Resources res) {
		super(context, 0, mediaItems);
		this.resourceId = resourceId;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.context = context;
		this.m_res = res;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		TextView textTitle, textAlbum, textArtist, textName, textLikesCount, textDateTime;
	    final ImageView image;
	
	    view = inflater.inflate(resourceId, parent, false);
	    //Associa gli elementi dell'xml a quelli della classe stessa
	    try {
	    	textTitle = (TextView)view.findViewById(R.id.record_title);
	    	textAlbum = (TextView)view.findViewById(R.id.record_album);
	    	textArtist = (TextView)view.findViewById(R.id.record_artist);
	    	textName = (TextView)view.findViewById(R.id.record_name);
	    	textLikesCount = (TextView)view.findViewById(R.id.record_likes);
	    	textDateTime = (TextView)view.findViewById(R.id.record_datetime);
	    	
	    	image = (ImageView)view.findViewById(R.id.img);
	    } catch( ClassCastException e ) {
	    	Log.e(TAG, "Your layout must provide an image and a text view with ID's icon and text.", e);
	    	throw e;
	    }

	    Post item = getItem(position);
	    Bitmap cachedImage = null;
	    
	    /**
	     * Controlla se ha gi� le immagini bufferizzate e se ci sono le carica
	     */
	    try {
	    	cachedImage = imageLoader.loadImage(item.getUserPosted().getPicture().replace("https://", "http://"), new ImageLoadedListener() {
	    		public void imageLoaded(Bitmap imageBitmap) {
	    			image.setImageDrawable(addTransparentGradient(imageBitmap));
	    			notifyDataSetChanged();                
	    		}
	    	});
	    } catch (MalformedURLException e) {
	    	Log.e(TAG, "Bad remote image URL: " + item.getUserPosted().getPicture(), e);
	    }
	    
	    //Imposta i campi della lista
	    textTitle.setText(item.getTitle());
	    textAlbum.setText(item.getAlbum());
	    textArtist.setText(item.getArtist());
	    textName.setText(item.getUserPosted().getName());
	    textLikesCount.setText("" + item.getLikeUser());
	    textDateTime.setText("" + item.getCreatedPost());
	    
	    //Visualizza l'immagine
	    if( cachedImage != null )
	    	image.setImageDrawable(addTransparentGradient(cachedImage));

	    return view;
	}
	
	public Drawable addTransparentGradient(Bitmap bitmap){
        return new BitmapDrawable(bitmap);
	}
}