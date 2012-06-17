package it.pdm.project.MusicPlayer.social.facebook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.Util;

public class FacebookManager {
	//Utente che ha effettuato il login
	private User m_userMe;
	
	//Variabile che identifica l'app
	public static String APP_ID = "237120273069387";
	
	//Autorizzazioni necessarie per accedere alle informazioni dell'utente loggato
	public static String[] PERMISSION= { "offline_access", "publish_stream", "user_photos", "publish_checkins", "photo_upload", "read_stream", "read_insights" };
	
	//Variabile che fornisce i metodi per interagire con fb
	private Facebook mFacebook;
    
    //Necessaria per effettuare interrogazioni asincrone senza interropere il thread chiamante 
    private AsyncFacebookRunner mAsyncRunner;
    
    //Necessaria per avere l'activity del social networking
	private Activity mActivityChiamante;
	
	//Necessario renderla globale a causa delle risposte che vengono gestite nei listener
	private Hashtable<String, Post> mPostFriendApp;
	
	//ArrayList contenenti gli id degli utenti che usano l'applicazione
	private ArrayList<User> mUserFriendsApp;

    //Costruttore senza parametri
    public FacebookManager(Activity mActivityChimante){
    	this.m_userMe = null;
    	this.mFacebook = new Facebook(APP_ID);
    	this.mUserFriendsApp = new ArrayList<User>();
    	this.mAsyncRunner = new AsyncFacebookRunner(this.mFacebook);
    	this.mActivityChiamante = mActivityChimante;
    	this.mPostFriendApp = new Hashtable<String, Post>();
    }
    
    //Costruttore con parametri
    public FacebookManager(Facebook facebook){
    	this.m_userMe = new User();
    	this.mFacebook = facebook;
    	this.mUserFriendsApp = new ArrayList<User>();
    	this.mAsyncRunner = new AsyncFacebookRunner(this.mFacebook);
    	this.mPostFriendApp = new Hashtable<String, Post>();
    }
    
    /**Utility**/
 
    //Funzione che permette l'interrogazione delle info dell'utente data la query e il listener
    public void FQLQuery(String query, BaseRequestListener listener){
    	Bundle params = new Bundle();
		params.putString("method", "fql.query");
		params.putString("query", query);
		mAsyncRunner.request(params, listener);
    }
    
    public void FQLMultiQuery(Bundle params, BaseRequestListener listener) {
    	mAsyncRunner.request(params, listener);
    }
   
    //Funzione che restituisce la lista di amici che usano l'app (Da usare per parsare le risposte ottenute nel listener)
    public ArrayList<User> getFriendAppArray(String response) throws JSONException{
    	ArrayList<User> friendList = new ArrayList<User>();
    	String id = "";
    	String name = "";
    	String picture = "";
    	
    	response = "{\"data\":" + response + "}";
    	JSONObject json = Util.parseJson(response);
    	JSONArray jArray = json.getJSONArray("data");
    	
    	if(jArray.length() != 0){
    		for(int i=0; i<jArray.length(); i++){
    			id = jArray.getJSONObject(i).getString("uid");
    			name = jArray.getJSONObject(i).getString("name");
    			picture = jArray.getJSONObject(i).getString("pic_square");
    			friendList.add(new User(id, name, picture));
    		}
    	}
    	
    	return friendList;
    }

    public User getCurrentUser() {
    	if (this.m_userMe == null)
    		this.getUserInfo();

    	return this.m_userMe;
    }
    
    //Funzione che restituisce le info del'utente loggato (Da usare per parsare le risposte ottenute nel listener)
    public User getMyInfo(final String response) throws JSONException{
    	String id = "";
    	String name = "";
    	String picture = "";
    	JSONArray json = new JSONArray(response);
    	if(json.length() != 0){
    		id = json.getJSONObject(0).getString("uid");
    		name = json.getJSONObject(0).getString("name");
    		picture = json.getJSONObject(0).getString("pic_square");
    	}
    	return new User(id, name, picture);
    }
    /*
    //Funzione che restituisce una lista di post di un utente dell'app (Da usare per parsare le risposte ottenute nel listener)
    public void getInfoPost(String response) throws JSONException{
    	String idPost = "";
    	String message = "";
    	String idUser= "";
    	String name="";
    	String caption="";
    	String desc="";
    	int count =0;
    	
  
    	response = "{\"data\":" + response + "}";
    	JSONObject json = Util.parseJson(response);
    	JSONArray jArrayD = json.getJSONArray("data");
    	JSONArray jArray = jArrayD.getJSONObject(1).getJSONArray("fql_result_set");
    	
    	if (jArray.length() != 0) {
    		for(int i=0; i<jArray.length(); i++){
    			if (message.equals("")) message = " ";
    			
    			idPost = jArray.getJSONObject(i).getString("post_id");
    			message = jArray.getJSONObject(i).getString("message");
    			idUser = jArray.getJSONObject(i).getString("actor_id");
    			java.util.Date time = new java.util.Date((long)jArray.getJSONObject(i).getInt("created_time")*1000);
    			if(jArray.getJSONObject(i).getJSONObject("attachment").has("name"))
    				name = jArray.getJSONObject(i).getJSONObject("attachment").getString("name");
    			else
    				name= "";
    			if(jArray.getJSONObject(i).getJSONObject("attachment").has("caption"))
    				caption = jArray.getJSONObject(i).getJSONObject("attachment").getString("caption");
    			else
    				caption= "";
      			desc = jArray.getJSONObject(i).getJSONObject("attachment").getString("description");
      			count = jArray.getJSONObject(i).getJSONObject("likes").getInt("count");
      			   			
    			if(!this.mPostFriendApp.containsKey(idPost)){
	    			this.mPostFriendApp.put(idPost, new Post(idPost));
    			}

    			this.mPostFriendApp.get(idPost).setMessage(message);
    			this.mPostFriendApp.get(idPost).setUser(idUser);
    			this.mPostFriendApp.get(idPost).setCreatedPost(time);
    			if(!desc.equals(""))
      				this.mPostFriendApp.get(idPost).setAlbum(desc);
      			if(!name.equals(""))
      				this.mPostFriendApp.get(idPost).setTitle(name);
      			if(!caption.equals(""))
      				this.mPostFriendApp.get(idPost).setArtist(caption);
    			this.mPostFriendApp.get(idPost).setLikeUser(count);
    		}
    	}
	
	
    	if(this.mPostFriendApp.size()>0){
    		JSONArray jArrayN = jArrayD.getJSONObject(0).getJSONArray("fql_result_set");
    		if(jArrayN.length() != 0){
    			String str;
				Set set = getHashTablePostApp().keySet();
				Iterator itr = set.iterator(); 
				while(itr.hasNext()) { 
					str = (String) itr.next();
					for(int i=0; i<jArrayN.length(); i++){
						if(getHashTablePostApp().get(str).getUser().equals(jArrayN.getJSONObject(i).getString("uid"))){
							String id = jArrayN.getJSONObject(i).getString("uid");
							String nameU = jArrayN.getJSONObject(i).getString("name");
							String picture = jArrayN.getJSONObject(i).getString("pic_square");
							getHashTablePostApp().get(str).setUserPosted(new User(id, nameU, picture));
						}	
	    			}
				} 				
			}
    	}
    }
    */

    public void getInfoPost(String response) throws JSONException{
    	String idPost = "";
    	String message = "";
    	String idUser= "";
    	String name="";
    	String caption="";
    	String desc="";
    	int count =0;
    	
  
    	response = "{\"data\":" + response + "}";
    	JSONObject json = Util.parseJson(response);
    	JSONArray jArrayD = json.getJSONArray("data");
	
		for(int k=0; k<jArrayD.length(); k++){
	    		JSONArray jArray = jArrayD.getJSONObject(k).getJSONArray("fql_result_set");
	    	
	    		if (jArray.length() != 0) {
	    			for(int i=0; i<jArray.length(); i++){
	    				if (message.equals("")) message = " ";
	    			
	    				idPost = jArray.getJSONObject(i).getString("post_id");
	    				message = jArray.getJSONObject(i).getString("message");
	    				idUser = jArray.getJSONObject(i).getString("actor_id");
	    				java.util.Date time = new java.util.Date((long)jArray.getJSONObject(i).getInt("created_time")*1000);
	    				if(jArray.getJSONObject(i).getJSONObject("attachment").has("name"))
	    					name = jArray.getJSONObject(i).getJSONObject("attachment").getString("name");
	    				else
	    					name= "";
	    				if(jArray.getJSONObject(i).getJSONObject("attachment").has("caption"))
	    					caption = jArray.getJSONObject(i).getJSONObject("attachment").getString("caption");
	    				else
	    					caption= "";
	      				desc = jArray.getJSONObject(i).getJSONObject("attachment").getString("description");
	      				count = jArray.getJSONObject(i).getJSONObject("likes").getInt("count");
	      			   			
	    				if(!this.mPostFriendApp.containsKey(idPost)){
		    				this.mPostFriendApp.put(idPost, new Post(idPost));
	    				}
	
	    				this.mPostFriendApp.get(idPost).setMessage(message);
	    				this.mPostFriendApp.get(idPost).setUser(idUser);
	    				this.mPostFriendApp.get(idPost).setCreatedPost(time);
	    				if(!desc.equals(""))
	      					this.mPostFriendApp.get(idPost).setAlbum(desc);
	      				if(!name.equals(""))
	      					this.mPostFriendApp.get(idPost).setTitle(name);
	      				if(!caption.equals(""))
	      					this.mPostFriendApp.get(idPost).setArtist(caption);
	    				this.mPostFriendApp.get(idPost).setLikeUser(count);
	    			}
	    		}
		}
		
    	if(this.mPostFriendApp.size()>0){
    		if(mUserFriendsApp.size()!=0){
    			String str;
				Set set = getHashTablePostApp().keySet();
				Iterator itr = set.iterator(); 
				while(itr.hasNext()) { 
					str = (String) itr.next();
					for(int i=0; i<mUserFriendsApp.size(); i++){
						if(getHashTablePostApp().get(str).getUser().equals(mUserFriendsApp.get(i).getId())){
							String id = mUserFriendsApp.get(i).getId();
							String nameU = mUserFriendsApp.get(i).getName();
							String picture = mUserFriendsApp.get(i).getPicture();
							getHashTablePostApp().get(str).setUserPosted(new User(id, nameU, picture));
						}	
	    			}
				} 				
    		}
    	}
    }
    /**Function**/
    
    //Funzione che effettua il login con le autorizzazioni per la nostra app
    public void login(){
    	mFacebook.authorize(mActivityChiamante, PERMISSION, new LoginRequestListener());
    }
    
    //Funzione che effettua il login con le autorizzazioni di default dell'utente
    public void login(Activity activity){
    	 mFacebook.authorize(activity, new LoginRequestListener());
    }
    
    //Funzione che effettua il login con le autorizzazioni specifiche
    public void login(Activity activity, String[] permission){
    	 mFacebook.authorize(activity, permission, new LoginRequestListener());
    }
    
    //Funzione che effettua il logout dell'utente con listener generico
    public void logout(Context context, BaseRequestListener listener){
    	mAsyncRunner.logout(context ,listener);
    }
    
    //Funzione che effettua il logout dell'utente con listener della classe
    public void logout(Context context){
    	mAsyncRunner.logout(context ,new LogoutRequestListener());
    }
    
    public String getAllFriendsIds() {
    	return "SELECT uid, name, pic_square FROM user WHERE uid = me() OR uid IN (SELECT uid2 FROM friend WHERE uid1 = me()) AND is_app_user";
    }
    
    //Interrogazione FQL che richiede le info dell'utente loggato
    public void getUserInfo(){
    	String query = "SELECT uid, name, pic_square FROM user WHERE uid = me()";
    	this.FQLQuery(query, new CurrentUserRequestListener());
    }
    
    //Interrogazione FQL che richiede la social history
    public void getSocialHistory() {
    	String query = "SELECT actor_id, post_id, attachment.name, attachment.description, attachment.caption, created_time, message, likes.count FROM stream WHERE source_id IN (SELECT uid, name FROM user WHERE uid = me() OR uid IN (SELECT uid2 FROM friend WHERE uid1 = me()) AND is_app_user) AND app_id = 237120273069387 LIMIT 50";
    	this.FQLQuery(query, new GetGenericInfoPostRequestListener());
    }
    
    //Interrogazione FQL che posta sul profilo dell'utente loggato la canzone che sta ascoltando con le sue informazioni con un'immagine passata
    public void postOnWall(Activity activity, String imageAlbumUrl, String song, String album, String singer){
    	Bundle params = new Bundle();
    	params.putString("caption", singer);
        params.putString("description", album);
        params.putString("picture", imageAlbumUrl);
        params.putString("name", song);
        params.putString("link", "http://www.youtube.com/results?search_query=" + album + "+-+" + singer);
        mFacebook.dialog(activity, "feed", params, new PostDialogListener());
    }
    
    //Funzione che posta sul profilo dell'utente loggato la canzone che sta ascoltando con le sue informazione e l'immagine di default
    public void postOnWall(Activity activity, String song, String album, String singer){
    	Bundle params = new Bundle();
        params.putString("caption", singer);
        params.putString("description", album);
        params.putString("picture", "http://4.bp.blogspot.com/-Z57TwcYK41U/T7-LXXc9GSI/AAAAAAAAGBc/sxV4gzPJ_cg/s1600/musica+android.jpg");
        params.putString("name", song);
        params.putString("link", "http://www.youtube.com/results?search_query=" + album + "+-+" + singer);
        //params.putString("link", this.getDetailLink(album));
        
        mFacebook.dialog(activity, "feed", params, new PostDialogListener());
    }
    
    public void populateHashTable() {
    	if (this.mUserFriendsApp.size() == 0)
    		this.FQLQuery(getAllFriendsIds(), new GetUsersListener());
    	else
    		this.getFriendsPostsSorted();
    }
    
    public void getFriendsPostsSorted() {
    	/*
    	try {
	    	Bundle params = new Bundle();
	    	JSONObject jsonFQL = new JSONObject();
	    	
	    	//jsonFQL.put("query1", "SELECT uid, name, pic_square FROM user WHERE uid = me() OR uid IN (SELECT uid2 FROM friend WHERE uid1 = me()) AND is_app_user");
	    	jsonFQL.put("query1", "SELECT uid, name, pic_square FROM user WHERE uid = me() OR uid IN (SELECT uid2 FROM friend WHERE uid1 = me()) AND is_app_user");
	    	jsonFQL.put("query2", "SELECT actor_id, post_id, attachment.name, attachment.description, attachment.caption, created_time, message, likes.count FROM stream WHERE source_id IN (SELECT uid FROM #query1) AND app_id = 237120273069387");
	    	
	    	params.putString("method", "fql.multiquery");
	    	params.putString("queries", jsonFQL.toString());
	    	
	    	this.FQLMultiQuery(params, new GetGenericInfoPostRequestListener());
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	*/
    	try {
	    	Bundle params = new Bundle();
	    	JSONObject jsonFQL = new JSONObject();
	    	
	    	for (User user : this.mUserFriendsApp)
	    		jsonFQL.put(user.getId(), "SELECT actor_id, post_id, attachment.name, attachment.description, attachment.caption, created_time, message, likes.count FROM stream WHERE source_id = " + user.getId() + " AND app_id = 237120273069387");
	    	
	    	params.putString("method", "fql.multiquery");
	    	params.putString("queries", jsonFQL.toString());
	    	
	    	this.FQLMultiQuery(params, new GetGenericInfoPostRequestListener());
    	} catch (JSONException e) {
    		e.printStackTrace();
    	}
    }
    
    
    public String getDetailLink(String album) {
    	String searchQuery = "http://itunes.apple.com/search?term=" + album + "&entity=album&limit=1";
    	
		try {
			InputStream is = new URL(searchQuery).openStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			StringBuilder sb = new StringBuilder();
		    int cp;
		    
		    while ((cp = rd.read()) != -1)
		      sb.append((char) cp);
		    
		    String jsonText = "";
	    	JSONObject json = Util.parseJson(jsonText);
	    	JSONArray jArray = json.getJSONArray("results");
	    	
	    	if(jArray.length() != 0){
	    		for(int i=0; i<jArray.length(); i++){
	    			return jArray.getJSONObject(i).getString("collectionViewUrl");
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
		
		return "http://www.youtube.com/results?search_query=" + album;
    }
    
    //Funzione che restituisce vero se l'utente corrente è loggato altrimenti falso
    public boolean isLogged(){
    	return mFacebook.isSessionValid();
    }
    
    //Funzione che restituisce l'hashtable dei post 
    public Hashtable<String, Post> getHashTablePostApp(){
    	this.sortValue();
    	return this.mPostFriendApp;
    }
    
    public void sortValue(){
        //Transfer as List and sort it
        ArrayList<Map.Entry<String, Post>> l = new ArrayList(mPostFriendApp.entrySet());
        Collections.sort(l, new Comparator<Map.Entry<String, Post>>(){

			@Override
			public int compare(Entry<String, Post> arg0, Entry<String, Post> arg1) {
				return arg0.getValue().getCreatedPost().compareTo(arg1.getValue().getCreatedPost());
			}
        });
     }
    
  //Funzione che restituisce l'hashtable dei post 
    public Facebook getFacebook(){
    	return this.mFacebook;
    }
    
    /**Sarebbe meglio metterli nell'activity e mettere un listener generico a mio avviso, per ora li lasciamo tutti qui. 
      * Nei listener, nei casi in cui serve, serve utilizzare i parser per le risposte
     **/
    
    private class CurrentUserRequestListener extends BaseRequestListener {
		@Override
		public void onComplete(String response, Object state) {
			try {
				m_userMe = getMyInfo(response);
				
				Intent intent = new Intent("it.pdm.project.MusicPlayer.social.facebook.FacebookManager.displayevent");
				intent.putExtra("ACTION", "USER_SUCCESSFULLY_LOGGED");
				intent.putExtra("ID", m_userMe.getId());
				intent.putExtra("USERNAME", m_userMe.getName());
				intent.putExtra("AVATAR", m_userMe.getPicture());
				
				FacebookManager.this.mActivityChiamante.sendBroadcast(intent);
			} catch (Exception e) {}
		}
    }
    
    //Listener invocato alla conclusione della richiesta di logout
    private class LogoutRequestListener extends BaseRequestListener {
        @Override
        public void onComplete(String response, final Object state) {
        	//Inserire codice una volta che è avvenuto il logout

			Intent intent = new Intent("it.pdm.project.MusicPlayer.social.facebook.FacebookManager.displayevent");
			intent.putExtra("ACTION", "USER_SUCCESSFULLY_LOGGED_OUT");
			
			FacebookManager.this.mActivityChiamante.sendBroadcast(intent);
        }
    }
    
    //Listener invocato alla conclusione della richiesta per ottenere gli amici che usano l'app
    @SuppressWarnings("unused")
	private class FQLRequestListener extends BaseRequestListener {
    	@Override
    	public void onComplete(final String response, final Object state) {
    		//Inserire codice una volta che è avvenuto la richiesta degli amici, usare parser
    		try {
				User loggedUser = FacebookManager.this.getMyInfo(response);
				
				Intent intent = new Intent("it.pdm.project.MusicPlayer.social.facebook.FacebookManager.displayevent");
				intent.putExtra("ACTION", "USER_SUCCESSFULLY_LOGGED");
				intent.putExtra("ID", loggedUser.getId());
				intent.putExtra("USERNAME", loggedUser.getName());
				intent.putExtra("AVATAR", loggedUser.getPicture());
				
				FacebookManager.this.mActivityChiamante.sendBroadcast(intent);
			} catch (JSONException e) {}
		}
    }
    
    //Listener invocato alla conclusione della richiesta per le info di un utente dato l'id
    private class GetUsersListener extends BaseRequestListener {
    	@Override
    	public void onComplete(final String response, final Object state) {
    		try {
    			Log.d("RESPONSE GETUSERLISTENER", response);
				mUserFriendsApp = getFriendAppArray(response);
				getFriendsPostsSorted();
			} catch (JSONException e) {
				e.printStackTrace();
			}
    	}
    	@Override
        public void onFacebookError(FacebookError e, final Object state) {
            //Toast errore
        }
    		
    }
    
    //Listener invocato alla conclusione della richiesta per le generiche info di un post(Messaggio e id dell'utente, id del post, data di creazione del post)
    private class GetGenericInfoPostRequestListener extends BaseRequestListener {
    	@Override
    	public void onComplete(final String response, final Object state) {
    		try {
    			Log.d("RESPONSE", response);
				getInfoPost(response);
				
				Intent intent = new Intent("it.pdm.project.MusicPlayer.social.facebook.FacebookManager.displayevent");
				intent.putExtra("ACTION", "TABLE_SUCCESSFULLY_UPDATED");
				
				FacebookManager.this.mActivityChiamante.sendBroadcast(intent);
			} catch (JSONException e) {
				e.printStackTrace();
			}
    	}
    	@Override
        public void onFacebookError(FacebookError e, final Object state) {
            //Toast errore
        }
    }
    
    //Listener invocato alla fine dell'invio della richiesta di post sulla bacheca
    private class PostDialogListener implements DialogListener {
		@Override
		public void onComplete(Bundle values) {
			Intent intent = new Intent("it.pdm.project.MusicPlayer.social.facebook.FacebookManager.displayevent");
			intent.putExtra("ACTION", "SONG_SUCCESSFULLY_POSTED");
			
			FacebookManager.this.mActivityChiamante.sendBroadcast(intent);
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
    
    //Listener invocato alla fine dell'invio della richiesta di login
    private class LoginRequestListener implements DialogListener {
		@Override
		public void onComplete(Bundle values) {
			FacebookManager.this.getUserInfo();
		}

		@Override
		public void onFacebookError(FacebookError e) {
			Intent intent = new Intent("it.pdm.project.MusicPlayer.social.facebook.FacebookManager.displayevent");
	        intent.putExtra("ACTION", "USER_LOGIN_ABORT");
	           
	        FacebookManager.this.mActivityChiamante.sendBroadcast(intent);
		}

		@Override
		public void onError(DialogError e) {
			Intent intent = new Intent("it.pdm.project.MusicPlayer.social.facebook.FacebookManager.displayevent");
	        intent.putExtra("ACTION", "USER_LOGIN_ABORT");
	           
	        FacebookManager.this.mActivityChiamante.sendBroadcast(intent);
		}

		@Override
		public void onCancel() {
			Intent intent = new Intent("it.pdm.project.MusicPlayer.social.facebook.FacebookManager.displayevent");
	        intent.putExtra("ACTION", "USER_LOGIN_ABORT");
	           
	        FacebookManager.this.mActivityChiamante.sendBroadcast(intent);
		}
    }
}
