/**
 * Classe che offre tutte le funzionalità per lo sviluppo della parte social
 */

package it.pdm.project.MusicPlayer.social.facebook;

import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
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
	/**
	 * Utente che ha effettuato il login
	 */
	private User m_userMe;
	
	/**
	 * Variabile che identifica l'app
	 */
	public static String APP_ID = "237120273069387";
	
	/**
	 * Autorizzazioni necessarie per accedere alle informazioni dell'utente loggato
	 */
	public static String[] PERMISSION= { "offline_access", "publish_stream", "user_photos", "publish_checkins", "photo_upload", "read_stream", "read_insights" };
	
	/**
	 * Variabile che fornisce i metodi per interagire con fb
	 */
	private Facebook mFacebook;
    
    /**
     * Necessaria per effettuare interrogazioni asincrone senza interropere il thread chiamante 
     */
    private AsyncFacebookRunner mAsyncRunner;
    
    /**
     * Necessaria per avere l'activity del social networking
     */
	private Activity mActivityChiamante;
	
	/**
	 * Necessario renderla globale a causa delle risposte che vengono gestite nei listener
	 */
	private Hashtable<Long, Post> mPostFriendApp;
	
	/**
	 * ArrayList contenenti gli id degli utenti che usano l'applicazione
	 */
	private ArrayList<User> mUserFriendsApp;

    /**
     * Costruttore che richiede solo una activity come paramentro
     * 
     * @param mActivityChimante Activity che richiede le funzionalità social
     */
    public FacebookManager(Activity mActivityChimante){
    	this.m_userMe = null;
    	this.mFacebook = new Facebook(APP_ID);
    	this.mUserFriendsApp = new ArrayList<User>();
    	this.mAsyncRunner = new AsyncFacebookRunner(this.mFacebook);
    	this.mActivityChiamante = mActivityChimante;
    	this.mPostFriendApp = new Hashtable<Long, Post>();
    }
    
    /**
     * Costruttore che richiede una variabile di tipo facebook
     * @param facebook Variabile di tipo facebook che esegue per l'esecuzione delle richieste
     */
    public FacebookManager(Facebook facebook){
    	this.m_userMe = new User();
    	this.mFacebook = facebook;
    	this.mUserFriendsApp = new ArrayList<User>();
    	this.mAsyncRunner = new AsyncFacebookRunner(this.mFacebook);
    	this.mPostFriendApp = new Hashtable<Long, Post>();
    }
    
    /**Utility**/
 
    /**
     * Funzione che permette l'interrogazione delle info dell'utente data la query e il listener
     * @param query Query che si vuole eseguire
     * @param listener Listener per l'interfaccia di callback richiamata alla fine dell'esecuzione 
     * 				   della query	
     */
    public void FQLQuery(String query, BaseRequestListener listener){
    	Bundle params = new Bundle();
		params.putString("method", "fql.query");
		params.putString("query", query);
		mAsyncRunner.request(params, listener);
    }
    
    /**
     * Funzione che esegue una multiquery
     * @param params Bundle che contiene le query da eseguire
     * @param listener Listener richiamato alla fine dell'esecuzione della multiquery
     */
    public void FQLMultiQuery(Bundle params, BaseRequestListener listener) {
    	mAsyncRunner.request(params, listener);
    }
   
    /**
     * Funzione che restituisce la lista di amici che usano l'app (Da usare per parsare le risposte ottenute nel listener)
     * @param response Risposta alla query ottenuta nel listener
     * @return ArrayList di utenti che usano l'app e sono amici dell'utente loggato
     * @throws JSONException Si verifica nel caso in cui non si riesca a convertire la risposta in JSON
     */
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
    
    /**
     * Funzione che restituisce l'utente corrente
     * @return Utente
     */
    public User getCurrentUser() {
    	if (this.m_userMe == null)
    		this.getUserInfo();

    	return this.m_userMe;
    }
    
    /**
     * Funzione che restituisce le info del'utente loggato (Da usare per parsare le risposte ottenute nel listener)
     * @param response Risposta alla query ottenuta nel listener
     * @return Variabile di tipo user che contiene l'utente loggato
     * @throws JSONException Si verifica nel caso in cui non si riesca a convertire la risposta in JSON
     */
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

    /**
     * Funzione che parsa la risposta ottenuta ad una richiesta di post degli utenti amici dell'utente 
     * loggato
     * Usata per popolare l'hashtable
     * @param response Risposta alla query ottenuta nel listener
     * @throws JSONException Si verifica nel caso in cui non si riesca a convertire la risposta in JSON
     */
    @SuppressWarnings("rawtypes")
	public void getInfoPost(String response) throws JSONException{
    	Format formatter = new SimpleDateFormat("dd/MM HH:mm");
    	
    	String idPost = "";
    	String message = "";
    	String idUser = "";
    	String name = "";
    	String caption = "";
    	String desc = "";
    	int count = 0;
    	
  
    	response = "{\"data\":" + response + "}";
    	JSONObject json = Util.parseJson(response);
    	JSONArray jArrayD = json.getJSONArray("data");
    	
    	/**
    	 * Ottiene le informazioni dalla risposta
    	 */
		for(int k=0; k<jArrayD.length(); k++){
	    	JSONArray jArray = jArrayD.getJSONObject(k).getJSONArray("fql_result_set");
    	
    		if (jArray.length() != 0) {
    			for(int i=0; i<jArray.length(); i++){
    				
    				idPost = jArray.getJSONObject(i).getString("post_id");
    				message = jArray.getJSONObject(i).getString("message");
    				if (message.equals("")) message = " ";
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
      				
      				this.mPostFriendApp.put(time.getTime(), new Post(idPost));

    				this.mPostFriendApp.get(time.getTime()).setMessage(message);
    				this.mPostFriendApp.get(time.getTime()).setUser(idUser);
    				this.mPostFriendApp.get(time.getTime()).setCreatedPost(formatter.format(time));
    				
    				if(!desc.equals(""))
      					this.mPostFriendApp.get(time.getTime()).setAlbum(desc);
      				if(!name.equals(""))
      					this.mPostFriendApp.get(time.getTime()).setTitle(name);
      				if(!caption.equals(""))
      					this.mPostFriendApp.get(time.getTime()).setArtist(caption);
      				
    				this.mPostFriendApp.get(time.getTime()).setLikeUser(count);
    			}
    		}
		}
		
		/**
		 * Associa ad ogni post le informazioni dell'utente che l'ha pubblicato
		 */
    	if (this.mPostFriendApp.size()>0){
    		if(mUserFriendsApp.size()!=0){
    			Long str;
				Set set = this.mPostFriendApp.keySet();
				Iterator itr = set.iterator(); 
				while(itr.hasNext()) { 
					str = (Long) itr.next();
					for(int i=0; i<mUserFriendsApp.size(); i++){
						if(this.mPostFriendApp.get(str).getUser().equals(mUserFriendsApp.get(i).getId())){
							String id = mUserFriendsApp.get(i).getId();
							String nameU = mUserFriendsApp.get(i).getName();
							String picture = mUserFriendsApp.get(i).getPicture();
							this.mPostFriendApp.get(str).setUserPosted(new User(id, nameU, picture));
						}	
	    			}
				}			
    		}
    	}
    }
    
    /**Function**/
    
    /**
     * Funzione che effettua il login con le autorizzazioni per la nostra app, al suo completamento
     * richiama LoginRequestListener
     */
    public void login(){
    	mFacebook.authorize(mActivityChiamante, PERMISSION, new LoginRequestListener());
    }
    
    /**
     * Funzione che effettua il login con le autorizzazioni di default dell'utente
     * Al suo completamento viene richiamato il LoginRequestListener
     * @param activity Activity che richiede il login
     */
    public void login(Activity activity){
    	 mFacebook.authorize(activity, new LoginRequestListener());
    }
    
    /**
     * Funzione che effettua il login con le autorizzazioni specifiche
     * @param activity Activity che richiede il login
     * @param permission Elenco dei permessi necessari
     */
    public void login(Activity activity, String[] permission){
    	 mFacebook.authorize(activity, permission, new LoginRequestListener());
    }
    
    /**
     * Funzione che effettua il logout dell'utente con listener generico
     * @param context Context nel quale è stato richiesto il login della sessione
     * @param listener Interfaccia di callback richiamata a fine richiesta
     */
    public void logout(Context context, BaseRequestListener listener){
    	mAsyncRunner.logout(context ,listener);
    }
    
    /**
     * Funzione che effettua il logout dell'utente con listener della classe
     * @param context Context nel quale è stato richiesto il login della sessione
     */
    public void logout(Context context){
    	mAsyncRunner.logout(context ,new LogoutRequestListener());
    }
    
    /**
     * Funzione che restituisce tutti gli amici che utilizzano l'app compreso l'utente loggato
     * @return Stringa contenente la query
     */
    public String getAllFriendsIds() {
    	return "SELECT uid, name, pic_square FROM user WHERE uid = me() OR uid IN (SELECT uid2 FROM friend WHERE uid1 = me()) AND is_app_user";
    }
    
    /**
     * Interrogazione FQL che richiede le info dell'utente loggato
     * Al suo completamento viene richiamato CurrentUserRequestListener
     */
    public void getUserInfo(){
    	String query = "SELECT uid, name, pic_square FROM user WHERE uid = me()";
    	this.FQLQuery(query, new CurrentUserRequestListener());
    }
    
    /**
     * Funzione che posta sul profilo dell'utente loggato la canzone che sta ascoltando con le sue informazione e l'immagine di default
     * @param activity Activity a cui è stato affidato il compito di postare il messaggio sulla bacheca
     * @param song Titolo della canzone
     * @param album Nome dell'album 
     * @param singer Nome del cantante
     */
    public void postOnWall(Activity activity, String song, String album, String singer) {
    	PostCreator postCreator = new PostCreator(this.mActivityChiamante, this, song, album, singer);
    	Thread child = new Thread(postCreator);
    	child.start();
    }
    
    /**
     * Funzione che popola l'hashtable degli amici
     */
    public void populateHashTable() {
    	if (this.mUserFriendsApp.size() == 0)
    		this.FQLQuery(getAllFriendsIds(), new GetUsersListener());
    	else
    		this.getFriendsPostsSorted();
    }
    
    /**
     * Funzione che richiede i post per ogni amico che usa l'app e per l'utente stesso
     */
    public void getFriendsPostsSorted() {
    	try {
	    	Bundle params = new Bundle();
	    	JSONObject jsonFQL = new JSONObject();
	    	
	    	/**
	    	 * Per ogni id fai la richiesta delle info sui post
	    	 */
	    	for (User user : this.mUserFriendsApp)
	    		jsonFQL.put(user.getId(), "SELECT actor_id, post_id, attachment.name, attachment.description, attachment.caption, created_time, message, likes.count FROM stream WHERE source_id = " + user.getId() + " AND app_id = 237120273069387 LIMIT 200");
	    	
	    	params.putString("method", "fql.multiquery");
	    	params.putString("queries", jsonFQL.toString());
	    	
	    	this.FQLMultiQuery(params, new GetGenericInfoPostRequestListener());
    	} 
    	catch (JSONException e) {
    		Intent intent = new Intent("it.pdm.project.MusicPlayer.social.facebook.FacebookManager.displayevent");
	        intent.putExtra("ACTION", "ERROR");
	           
	        FacebookManager.this.mActivityChiamante.sendBroadcast(intent);
    	}
    }
    
    /**
     * Funzione che restituisce vero se l'utente corrente è loggato altrimenti falso
     * @return Booleano che indica se esiste una sessione valida(esiste un utente loggato)
     */
    public boolean isLogged(){
    	return mFacebook.isSessionValid();
    }
    
    /**
     * Funzione che restituisce l'hashtable dei post 
     * @return Hashtable con i post
     */
    public Hashtable<Long, Post> getHashTablePostApp(){ 
    	return this.mPostFriendApp;
    }
    
    /**
     * Funzione che ordina cronologicamente i post in base alla data di creazione
     * @return Hashtable dei post ordinata
     */
    public Hashtable<Long, Post> sortValue(){
    	ArrayList<Long> keys = new ArrayList<Long>(this.mPostFriendApp.keySet());
        Collections.sort(keys);
        
        Hashtable<Long, Post> newHtable = new Hashtable<Long, Post>();
        
        for (int i = keys.size()-1; i >= 0; i--)
        	newHtable.put(keys.get(i), this.mPostFriendApp.get(keys.get(i)));
        
        return newHtable;
     }
    
    
    public Facebook getFacebook(){
    	return this.mFacebook;
    }
    
    //Funzione che restituisce l'activity chiamante 
    public Activity getActivity(){
    	return this.mActivityChiamante;
    }
    
    /**
     * Listener per le richieste
     **/
    /**
     * Listener richiamato alla conclusione della richiesta delle informazioni sull'utente corrente
     * @author ChronicDev
     *
     */
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
			} 
			catch (Exception e) {
				Intent intent = new Intent("it.pdm.project.MusicPlayer.social.facebook.FacebookManager.displayevent");
		        intent.putExtra("ACTION", "ERROR");
		           
		        FacebookManager.this.mActivityChiamante.sendBroadcast(intent);
			}
		}
		
		@Override
        public void onFacebookError(FacebookError e, final Object state) {
    		Intent intent = new Intent("it.pdm.project.MusicPlayer.social.facebook.FacebookManager.displayevent");
	        intent.putExtra("ACTION", "ERROR");
	           
	        FacebookManager.this.mActivityChiamante.sendBroadcast(intent);
        }
		
		@Override
	    public void onIOException(IOException e, final Object state) {
			Intent intent = new Intent("it.pdm.project.MusicPlayer.social.facebook.FacebookManager.displayevent");
	        intent.putExtra("ACTION", "ERROR");
	           
	        FacebookManager.this.mActivityChiamante.sendBroadcast(intent);
	    }
    }
    
    /**
     * Listener invocato alla conclusione della richiesta di logout
     * @author ChronicDev
     *
     */
    private class LogoutRequestListener extends BaseRequestListener {
        @Override
        public void onComplete(String response, final Object state) {

			Intent intent = new Intent("it.pdm.project.MusicPlayer.social.facebook.FacebookManager.displayevent");
			intent.putExtra("ACTION", "USER_SUCCESSFULLY_LOGGED_OUT");
			
			FacebookManager.this.mActivityChiamante.sendBroadcast(intent);
        }
        
        @Override
        public void onFacebookError(FacebookError e, final Object state) {
    		Intent intent = new Intent("it.pdm.project.MusicPlayer.social.facebook.FacebookManager.displayevent");
	        intent.putExtra("ACTION", "ERROR");
	           
	        FacebookManager.this.mActivityChiamante.sendBroadcast(intent);
        }
		
		@Override
	    public void onIOException(IOException e, final Object state) {
			Intent intent = new Intent("it.pdm.project.MusicPlayer.social.facebook.FacebookManager.displayevent");
	        intent.putExtra("ACTION", "ERROR");
	           
	        FacebookManager.this.mActivityChiamante.sendBroadcast(intent);
	    }
    }
    
    /**
     * Listener invocato alla conclusione della richiesta per le info di un utente dato l'id
     * @author ChronicDev
     *
     */
    private class GetUsersListener extends BaseRequestListener {
    	@Override
    	public void onComplete(final String response, final Object state) {
    		try {
    			//ottieni l'array degli amici
				mUserFriendsApp = getFriendAppArray(response);
				//richiedi i post per ognuno
				getFriendsPostsSorted();
			} 
    		catch (JSONException e) {
    			Intent intent = new Intent("it.pdm.project.MusicPlayer.social.facebook.FacebookManager.displayevent");
		        intent.putExtra("ACTION", "ERROR");
		           
		        FacebookManager.this.mActivityChiamante.sendBroadcast(intent);
			}
    	}
    	
    	@Override
        public void onFacebookError(FacebookError e, final Object state) {
    		Intent intent = new Intent("it.pdm.project.MusicPlayer.social.facebook.FacebookManager.displayevent");
	        intent.putExtra("ACTION", "ERROR");
	           
	        FacebookManager.this.mActivityChiamante.sendBroadcast(intent);
        }
    	
    	@Override
	    public void onIOException(IOException e, final Object state) {
			Intent intent = new Intent("it.pdm.project.MusicPlayer.social.facebook.FacebookManager.displayevent");
	        intent.putExtra("ACTION", "ERROR");
	           
	        FacebookManager.this.mActivityChiamante.sendBroadcast(intent);
	    }
    		
    }
    
    /**
     * Listener invocato alla conclusione della richiesta per le generiche info di un post(Messaggio e id dell'utente, id del post, data di creazione del post)
     * @author ChronicDev
     *
     */
    private class GetGenericInfoPostRequestListener extends BaseRequestListener {
    	@Override
    	public void onComplete(final String response, final Object state) {
    		try {
    			//parsa la risposta
				getInfoPost(response);
				
				Intent intent = new Intent("it.pdm.project.MusicPlayer.social.facebook.FacebookManager.displayevent");
				intent.putExtra("ACTION", "TABLE_SUCCESSFULLY_UPDATED");
				
				FacebookManager.this.mActivityChiamante.sendBroadcast(intent);
			} 
    		catch (JSONException e) {
    			Intent intent = new Intent("it.pdm.project.MusicPlayer.social.facebook.FacebookManager.displayevent");
    	        intent.putExtra("ACTION", "ERROR");
    	           
    	        FacebookManager.this.mActivityChiamante.sendBroadcast(intent);
			}
    	}
    	
    	@Override
        public void onFacebookError(FacebookError e, final Object state) {
    		Intent intent = new Intent("it.pdm.project.MusicPlayer.social.facebook.FacebookManager.displayevent");
	        intent.putExtra("ACTION", "ERROR");
	           
	        FacebookManager.this.mActivityChiamante.sendBroadcast(intent);
        }
    	
    	@Override
	    public void onIOException(IOException e, final Object state) {
			Intent intent = new Intent("it.pdm.project.MusicPlayer.social.facebook.FacebookManager.displayevent");
	        intent.putExtra("ACTION", "ERROR");
	           
	        FacebookManager.this.mActivityChiamante.sendBroadcast(intent);
	    }
    }
    
    /**
     * Listener invocato alla fine dell'invio della richiesta di login
     * @author ChronicDev
     *
     */
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
