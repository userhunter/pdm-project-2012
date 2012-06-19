package it.pdm.project.MusicPlayer.social.facebook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;

import com.facebook.android.DialogError;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;
import com.facebook.android.Facebook.DialogListener;

public class PostCreator implements Runnable {
	private Activity m_aCaller;
	private String m_strAlbum, m_strArtist, m_strTitle;
	private FacebookManager m_fbManager;
	
	public PostCreator(Activity callerActivity, FacebookManager fbManager, String strTitle, String strAlbum, String strArtist) {
		this.m_aCaller = callerActivity;
		this.m_fbManager = fbManager;
		this.m_strAlbum = strAlbum;
		this.m_strArtist = strArtist;
		this.m_strTitle = strTitle;
	}
	
	@Override
	public void run() {
		String[] result = this.getLinkDetails();
		
    	Bundle params = new Bundle();
        params.putString("caption", this.m_strArtist);
        params.putString("description", this.m_strAlbum);
        params.putString("picture", result[1]);
        params.putString("name", this.m_strTitle);
        params.putString("link", result[0]);
        
        Looper.prepare();
        this.m_fbManager.getFacebook().dialog(this.m_aCaller, "feed", params, new PostDialogListener());
        Looper.loop();
	}
	
	private String[] getLinkDetails() {
		String[] resultInfo;
		String searchQuery = "http://itunes.apple.com/search?term=" + this.m_strAlbum.replaceAll(" ", "+") + "&term=" + this.m_strArtist.replaceAll(" ", "+") + "&media=music&entity=album";
		System.out.println("QUERY: " + searchQuery);
		
		try {
			InputStream is = new URL(searchQuery).openStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			StringBuilder sb = new StringBuilder();
		    int cp;
		    
		    while ((cp = rd.read()) != -1)
		      sb.append((char) cp);
		    
		    String jsonText = sb.toString();
	    	JSONObject json = Util.parseJson(jsonText);
	    	JSONArray jArray = json.getJSONArray("results");
	    	
	    	if(jArray.length() != 0){
	    		for(int i=0; i<jArray.length(); i++){
	    			if (jArray.getJSONObject(i).getString("artistName").toLowerCase().contains(this.m_strArtist.toLowerCase())) {
	    				resultInfo = new String[] { jArray.getJSONObject(i).getString("collectionViewUrl"), jArray.getJSONObject(i).getString("artworkUrl100") };
	    			
	    				return resultInfo;
	    			}
	    		}
	    	}
		} catch (MalformedURLException e) {
			System.out.println("MALFORMED");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IOEXC");
			e.printStackTrace();
		} catch (FacebookError e) {
			System.out.println("FBERR");
			e.printStackTrace();
		} catch (JSONException e) {
			System.out.println("JSONEXC");
			e.printStackTrace();
		}
		
		resultInfo = new String[] { "http://www.youtube.com/results?search_query=" + this.m_strAlbum + "-" + this.m_strArtist, "http://4.bp.blogspot.com/-Z57TwcYK41U/T7-LXXc9GSI/AAAAAAAAGBc/sxV4gzPJ_cg/s1600/musica+android.jpg" };
		
		return resultInfo;
    }
	
    //Listener invocato alla fine dell'invio della richiesta di post sulla bacheca
    private class PostDialogListener implements DialogListener {
		@Override
		public void onComplete(Bundle values) {
			Intent intent = new Intent("it.pdm.project.MusicPlayer.social.facebook.FacebookManager.displayevent");
			intent.putExtra("ACTION", "SONG_SUCCESSFULLY_POSTED");
			
			m_aCaller.sendBroadcast(intent);
		}

		@Override
		public void onFacebookError(FacebookError e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onError(DialogError e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onCancel() {
			// TODO Auto-generated method stub
			
		}
    }
}
