package it.pdm.project.MusicPlayer.social.facebook;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.Util;



public class FacebookManager {
	
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

    //Costruttore senza parametri
    public FacebookManager(Activity mActivityChimante){
    	this.mFacebook = new Facebook(APP_ID);
    	this.mAsyncRunner = new AsyncFacebookRunner(this.mFacebook);
    	this.mActivityChiamante = mActivityChimante;
    	this.mPostFriendApp = new Hashtable<String, Post>();
    	login();
    }
    
    //Costruttore con parametri
    public FacebookManager(Facebook facebook){
    	this.mFacebook = facebook;
    	this.mAsyncRunner = new AsyncFacebookRunner(this.mFacebook);
    	this.mPostFriendApp = new Hashtable<String, Post>();
    	login();
    }
    
    /**Utility**/
 
    //Funzione che permette l'interrogazione delle info dell'utente data la query e il listener
    public void FQLQuery(String query, BaseRequestListener listener){
    	Bundle params = new Bundle();
		params.putString("method", "fql.query");
		params.putString("query", query);
		mAsyncRunner.request(params, listener);
    }
   
    //Funzione che restituisce la lista di amici che usano l'app (Da usare per parsare le risposte ottenute nel listener)
    public ArrayList<User> getFriendAppArray(final String response) throws JSONException{
    	ArrayList<User> friendList = new ArrayList<User>();
    	String id = "";
    	String name = "";
    	String picture = "";
    	JSONArray json = new JSONArray(response);
    	if(json.length() != 0){
    		for(int i=0; i<json.length(); i++){
    			id = json.getJSONObject(i).getString("uid");
    			name = json.getJSONObject(i).getString("name");
    			picture = json.getJSONObject(i).getString("pic_square");
    			friendList.add(new User(id, name, picture));
    		}
    	}
    	return friendList;
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
    
    //Funzione che restituisce una lista di post di un utente dell'app (Da usare per parsare le risposte ottenute nel listener)
    public void getGenericInfoPost(String response) throws JSONException{
    	String idPost = "";
    	String message = "";
    	String idUser= "";
  
    	response = "{\"data\":" + response + "}";
    	JSONObject json = Util.parseJson(response);
    	JSONArray jArray = json.getJSONArray("data");
    	
    	if(jArray.length() != 0){
    		for(int i=0; i<jArray.length(); i++){
    			idPost = jArray.getJSONObject(i).getString("post_id");
    			message = jArray.getJSONObject(i).getString("message");
    			idUser = jArray.getJSONObject(i).getString("actor_id");
    			java.util.Date time = new java.util.Date((long)jArray.getJSONObject(i).getInt("created_time")*1000);
    			
    			if(!this.mPostFriendApp.containsKey(idPost)){
	    			this.mPostFriendApp.put(idPost, new Post(idPost));
    			}

    			this.mPostFriendApp.get(idPost).setMessage(message);
    			this.mPostFriendApp.get(idPost).setUser(idUser);
    			this.mPostFriendApp.get(idPost).setCreatedPost(time);
    		}
    	}
    }
    
    //Funzione che Album, Titolo e Artista di un post (Da usare per parsare le risposte ottenute nel listener)
    public void getAttachementPost(String response) throws JSONException{
    	String name = "";
    	
    	response = "{\"data\":" + response + "}";
    	JSONObject json = Util.parseJson(response);
    	JSONArray jArray = json.optJSONArray("data");

    	if(jArray.length() != 0){
    		for(int i=0; i<jArray.length(); i++){
    			if(jArray.getJSONObject(i).getJSONObject("attachment").has("name"))
    				name = jArray.getJSONObject(i).getJSONObject("attachment").getString("name");
    			else
    				name= "";
      			String desc = jArray.getJSONObject(i).getJSONObject("attachment").getString("description");
      			String index = jArray.getJSONObject(i).getString("post_id");
      			if(!this.mPostFriendApp.containsKey(index)){
	    			this.mPostFriendApp.put(index, new Post(index));
    			}
      			if(!desc.equals("")){
      				this.mPostFriendApp.get(index).setAlbum(parseForAlbum(desc));
      				this.mPostFriendApp.get(index).setArtist(parseForArtist(desc));
      			}
      			if(!name.equals(""))
      				this.mPostFriendApp.get(index).setTitle(parseForTitle(name));
    		}
    	}
    }
    
    //Funzione che ottiene il numero di likes (Da usare per parsare le risposte ottenute nel listener)
    public void getLikesPost(String response) throws JSONException{
    	response = "{\"data\":" + response + "}";
    	JSONObject json = Util.parseJson(response);
    	JSONArray jArray = json.getJSONArray("data");

    	if(jArray.length() != 0){
    		for(int i=0; i<jArray.length(); i++){
      			int count = jArray.getJSONObject(i).getJSONObject("likes").getInt("count");
      			String index = jArray.getJSONObject(i).getString("post_id");
      			
      			if(!this.mPostFriendApp.containsKey(index)){
	    			this.mPostFriendApp.put(index, new Post(index));
    			}
      			this.mPostFriendApp.get(index).setLikeUser(count);
    		}
    	}
    }
    
    //Parser per ottenere album
    public String parseForAlbum(String desc){
    	desc = desc.substring(7, desc.length());
    	return desc.substring(0, desc.indexOf(':')-7);
    }
    
    //Parser per ottenere l'artista
    public String parseForArtist(String desc){
    	desc = desc.substring(7, desc.length());
    	return desc.substring(desc.indexOf(':')+2, desc.length());
    }
    
    //Parser per ottenere il titolo
    public String parseForTitle(String name){
    	return name.substring(15, name.length());
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
    
    //Interrogazione FQL che richiede la lista di amici che usano l'app
    public void getFriendApp(){
    	String query = "SELECT uid, name, pic_square FROM user WHERE uid IN (SELECT uid2 FROM friend WHERE uid1 = me()) AND is_app_user = 1";
    	this.FQLQuery(query, new FQLRequestListener());
    }
    
    //Interrogazione FQL che richiede la lista di amici che usano l'app dato il listener
    public void getFriendApp(BaseRequestListener listener){
    	String query = "SELECT uid, name, pic_square FROM user WHERE uid IN (SELECT uid2 FROM friend WHERE uid1 = me()) AND is_app_user = 1";
    	this.FQLQuery(query, listener);
    }
    
    //Interrogazione FQL che richiede le info di un utente dato l'id
    public void getUserById(String uid){
    	String query = "SELECT name, pic_square FROM user WHERE uid = " + uid;
    	this.FQLQuery(query, new GetUserByIDListener());
    }
    
    //Interrogazione FQL che richiede le info di un post dato l'id
    public void getInfoPostAppById(String uid){
    	String query = "SELECT post_id, actor_id, created_time, message FROM stream WHERE app_id = 237120273069387 AND source_id = "+ uid + " AND actor_id = "+ uid;
    	this.FQLQuery(query, new GetGenericInfoPostRequestListener());
    }
    
    //Interrogazione FQL che richiede la description(contenuta negli attachment) di un post dato l'id utente
    public void getAttachmentPostAppById(String uid){
    	String query = "SELECT post_id, attachment.name, attachment.description FROM stream WHERE app_id = 237120273069387 AND source_id = "+ uid + " AND actor_id = "+ uid;
    	this.FQLQuery(query, new AttachmentPostAppRequestListener());
    }
    
    //Interrogazione FQL che richiede i likes di un post dato l'id utente
    public void getLikesPostAppById(String uid){
    	String query = "SELECT post_id, likes.count FROM stream WHERE app_id = 237120273069387 AND source_id = "+ uid + " AND actor_id = "+ uid;
    	this.FQLQuery(query, new LikesPostAppRequestListener());
    }
    
    //Interrogazione FQL che richiede le info dell'utente loggato
    public void getUserInfo(){
    	String query = "SELECT uid, name, pic_square FROM user WHERE uid = me()";
    	this.FQLQuery(query, new FQLRequestListener());
    }
    
    //Interrogazione FQL che posta sul profilo dell'utente loggato la canzone che sta ascoltando con le sue informazioni con un'immagine passata
    public void postOnWall(Activity activity, String imageAlbumUrl, String song, String album, String singer){
    	Bundle params = new Bundle();
        params.putString("caption", "SocialMediaPlayer for Andorid");
        params.putString("description", "Album: "+album+" Artist: "+singer);
        params.putString("picture", imageAlbumUrl);
        params.putString("name", "I am listening "+song);
        mFacebook.dialog(activity, "feed", params, new PostDialogListener());
    }
    
    //Funzione che posta sul profilo dell'utente loggato la canzone che sta ascoltando con le sue informazione e l'immagine di default
    public void postOnWall(Activity activity, String song, String album, String singer){
    	Bundle params = new Bundle();
        params.putString("caption", "SocialMediaPlayer for Android");
        params.putString("description", "Album: "+album+" Artist: "+singer);
        params.putString("picture", "http://4.bp.blogspot.com/-Z57TwcYK41U/T7-LXXc9GSI/AAAAAAAAGBc/sxV4gzPJ_cg/s1600/musica+android.jpg");
        params.putString("name", "I am listening "+song);
        
        mFacebook.dialog(activity, "feed", params, new PostDialogListener());
    }
    
    //Funzione che posta sul profilo dell'utente loggato (Implementata anche se non usata)
    public void inviteFriends(Activity activity){
    	Bundle lparameters = new Bundle();
	    lparameters.putString("message","Friend request");
	    mFacebook.dialog(activity, "apprequests", lparameters ,new SendRequestDialogListener()); 
    }
    
    //Funzione che restituisce vero se l'utente corrente è loggato altrimenti falso
    public boolean isLogged(){
    	return mFacebook.isSessionValid();
    }
    
    //Funzione che restituisce l'hashtable dei post 
    public Hashtable<String, Post> getHashTablePostApp(){
    	return this.mPostFriendApp;
    }
    
  //Funzione che restituisce l'hashtable dei post 
    public Facebook getFacebook(){
    	return this.mFacebook;
    }
    
    /**Sarebbe meglio metterli nell'activity e mettere un listener generico a mio avviso, per ora li lasciamo tutti qui. 
      * Nei listener, nei casi in cui serve, serve utilizzare i parser per le risposte
     **/
    
    //Listener invocato alla conclusione della richiesta di logout
    private class LogoutRequestListener extends BaseRequestListener {
        @Override
        public void onComplete(String response, final Object state) {
        	//Inserire codice una volta che è avvenuto il logout
        }
    }
    
    //Listener invocato alla conclusione della richiesta per ottenere gli amici che usano l'app
    public class FQLRequestListener extends BaseRequestListener {
    	@Override
    	public void onComplete(final String response, final Object state) {	
    		//Inserire codice una volta che è avvenuto la richiesta degli amici, usare parser
    		try {
				User loggedUser = FacebookManager.this.getMyInfo(response);
				
				Intent intent = new Intent();
				intent.putExtra("ACTION", "USER_SUCCESSFULLY_LOGGED");
				intent.putExtra("ID", loggedUser.getId());
				intent.putExtra("USERNAME", loggedUser.getName());
				intent.putExtra("AVATAR", loggedUser.getPicture());
				
				FacebookManager.this.mActivityChiamante.sendBroadcast(intent);
			} catch (JSONException e) {}
		}
    }
   
    //Listener invocato alla conclusione della richiesta per le info di un utente dato l'id
    public class GetUserByIDListener extends BaseRequestListener {
    	@Override
    	public void onComplete(final String response, final Object state) {
    		//Inserire codice una volta che è si è ottenuta l'info, usare parser
    	}
    	@Override
        public void onFacebookError(FacebookError e, final Object state) {
            //Toast errore
        }
    		
    }
    
    //Listener invocato alla conclusione della richiesta per le generiche info di un post(Messaggio e id dell'utente, id del post, data di creazione del post)
    public class GetGenericInfoPostRequestListener extends BaseRequestListener {
    	@Override
    	public void onComplete(final String response, final Object state) {
    		//Da gestire la risposta in base a ciÃ² che si vuole fare
    		try {
    			getGenericInfoPost(response);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	@Override
        public void onFacebookError(FacebookError e, final Object state) {
            //Toast errore
        }
    }
    
    //Listener invocato alla conclusione della richiesta per titolo, autore e brano
    public class AttachmentPostAppRequestListener extends BaseRequestListener {
    	@Override
    	public void onComplete(final String response, final Object state) {
    		try {
    			getAttachementPost(response);
    		} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	@Override
        public void onFacebookError(FacebookError e, final Object state) {
            //Toast errore
        }
    }
    
    //Listener invocato alla conclusione della richiesta dei likes dei post
    public class LikesPostAppRequestListener extends BaseRequestListener {
    	@Override
    	public void onComplete(final String response, final Object state) {
    		try {
    			getLikesPost(response);
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	@Override
        public void onFacebookError(FacebookError e, final Object state) {
            //Toast errore
        }
    }
    
    //Listener invocato alla fine dell'invio della richiesta di utilizzo dell'app
    private class SendRequestDialogListener implements DialogListener {
        public void onComplete(String response, final Object state) {
            //Toast.makeText(getApplicationContext(), "La richiesta ï¿½ stata inviata agli amici selezionati!", Toast.LENGTH_SHORT).show();
        }

		@Override
		public void onComplete(Bundle values) {
			// TODO Auto-generated method stub
			
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
    
    //Listener invocato alla fine dell'invio della richiesta di post sulla bacheca
    private class PostDialogListener implements DialogListener {

        public void onComplete(String response, final Object state) {
            //Toast.makeText(getApplicationContext(), "Il messaggio Ã¨ stato postato sulla bacheca!", Toast.LENGTH_SHORT).show();
        }

		@Override
		public void onComplete(Bundle values) {
			// TODO Auto-generated method stub
			
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
