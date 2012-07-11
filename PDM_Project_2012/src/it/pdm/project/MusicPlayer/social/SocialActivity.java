package it.pdm.project.MusicPlayer.social;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

import it.pdm.project.MusicPlayer.R;
import it.pdm.project.MusicPlayer.objects.MP3Item;
import it.pdm.project.MusicPlayer.services.MusicPlayerService;
import it.pdm.project.MusicPlayer.services.MusicPlayerService.LocalBinder;
import it.pdm.project.MusicPlayer.social.ImageThreadLoader.ImageLoadedListener;
import it.pdm.project.MusicPlayer.social.facebook.FacebookManager;
import it.pdm.project.MusicPlayer.social.facebook.Post;
import it.pdm.project.MusicPlayer.social.facebook.User;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SocialActivity extends ListActivity implements OnClickListener {
	@SuppressWarnings("unused")
	private User m_userMe;
	@SuppressWarnings("unused")
	private ListView m_listView;
	/**
	 * Adapter per la lista
	 */
	private SocialItemAdapter m_lstAdapter;
	/**
	 * Array dei post
	 */
	private ArrayList<Post> m_strSource;
	/**
	 * Variabile per l'iterazione con Facebook
	 */
	private FacebookManager m_fbManager;
	/**
	 * Services
	 */
	private MusicPlayerService m_mpService;
	
	/**
	 * Variabili per gli oggetti grafici
	 * */
	private ImageButton m_btnLogout, m_btnRefresh, m_btnShare, m_btnLoginButton;
	private TextView m_txtUsername;
	private RelativeLayout m_lytLoginLayout;
	private ImageView m_imgAvatar;
	
	/**
	 * Variabile per immagazinare le immagini nella cache
	 */
	private ImageThreadLoader m_thImageLoader;
	
	/**
	 * Intercetta i messaggi etichettati nella regestrazione del servizio (OnCreate)
	 */
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
	    public void onReceive(Context context, Intent intent) {
			if (intent.getStringExtra("ACTION").equals("USER_SUCCESSFULLY_LOGGED")) {
				updateAccountInfo(m_fbManager.getCurrentUser().getPicture().replace("https://", "http://"), m_fbManager.getCurrentUser().getName());
				saveTokens(); // Salvo i token della nuova sessione
				m_fbManager.populateHashTable();
				showRefreshAnimation();
				m_lytLoginLayout.setVisibility(View.GONE);
				hideLoginSpinner();
			}
			else if (intent.getStringExtra("ACTION").equals("USER_SUCCESSFULLY_LOGGED_OUT")) {
				m_lytLoginLayout.setVisibility(View.VISIBLE);
				refreshSocialItems();
				clearTokens();
				saveTokens();
			}
			else if (intent.getStringExtra("ACTION").equals("SONG_SUCCESSFULLY_POSTED")) 
				Toast.makeText(SocialActivity.this, "Il messaggio sarà pubblicato sulla tua bacheca.", Toast.LENGTH_SHORT).show();
			else if (intent.getStringExtra("ACTION").equals("TABLE_SUCCESSFULLY_UPDATED")){
				refreshSocialItems();
			}
			else if (intent.getStringExtra("ACTION").equals("USER_LOGIN_ABORT")) {
				/* In caso di annullamento o fail del login, nascondiamo lo spinner di caricamento */
	            hideLoginSpinner();
	        }
			else if(intent.getStringExtra("ACTION").equals("CANCEL")){
				Toast.makeText(SocialActivity.this, "Azione annullata. Il post non sarà pubblicato.", Toast.LENGTH_SHORT).show();
			}
			else if(intent.getStringExtra("ACTION").equals("ERROR")){
				Toast.makeText(SocialActivity.this, "Errore nella richiesta. Riprovare.", Toast.LENGTH_SHORT).show();
			}
		}
	};
	/**
	 * Funzione che notifica se il token è stato ottenuto
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		this.m_fbManager.getFacebook().authorizeCallback(requestCode, resultCode, data);
	}
	
	@Override
	public void onBackPressed() {
	    //do nothing
		//Disabilitiamo il pulsante back (lasciamo attivo solo l'home button)
	}
			
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_player_social);
        
	    if (!this.getApplicationContext().bindService(new Intent(this, MusicPlayerService.class), mConnection, Context.BIND_AUTO_CREATE))
	    	Log.d("BINDSERVICE", "ERROR DURING BINDING");
       
	    this.initMemberVars();
        
        this.registerReceiver(broadcastReceiver, new IntentFilter("it.pdm.project.MusicPlayer.social.facebook.FacebookManager.displayevent"));
        this.setListAdapter(m_lstAdapter);
        
        restoreTokens(); // Ripristino i token dell'ultima sessione
        
        //Controllo se sono connesso ad internet per poter effettuare le operazioni relative al social networking
        if (!isOnline(this)) {
			Toast.makeText(getApplicationContext(), "Connessione Assente!", Toast.LENGTH_SHORT).show();
		}
        else{
	        if (this.m_fbManager.isLogged()){
	        	showLoginSpinner();
	        	this.m_fbManager.getUserInfo();
	        	this.m_fbManager.populateHashTable();
	        }
	        else
	        	this.m_fbManager.logout(this);
        }
    }
    
    /**
     * Funzione che controlla lo stato della rete
     * @param c Context in cui devo verificare se la connessione è presente
     * @return Boolen che indica se la connessione è presente(true) o assente (false)
     */
    public boolean isOnline(Context c) {
    	ConnectivityManager cm = (ConnectivityManager) c
    	.getSystemService(Context.CONNECTIVITY_SERVICE);
    	NetworkInfo ni = cm.getActiveNetworkInfo();

    	if (ni != null && ni.isConnected())
    	  return true;
    	else
    	  return false;
    }
    
    /**
     * Variabile che informa se il bind è avvenuto con successo o meno
     */
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		//Callback richiamata nel momento in cui il bind tra questa activity e il service è avvenuto con successo.
	    public void onServiceConnected(ComponentName className, IBinder service) {
	        LocalBinder binder = (LocalBinder) service;
	        //Valorizzo m_mpService con il servizio a cui l'activity si è appena linkata in modo da poter richiamare metodi pubblici
	        m_mpService = binder.getService();
		}

	    @Override
	    //Callabck richiamata nel momento in cui il bind tra questa activity ed il service termina.
	    public void onServiceDisconnected(ComponentName arg0) { }
	};
	
	/**
	 * Funzione che aggiorna le informazioni dell'utente corrente quali immagine e nome nell'action bar
	 * @param strAvatarURL URL dell'immagine profilo
	 * @param strName Nome e Cognome dell'utente loggato
	 */
	public void updateAccountInfo(String strAvatarURL, String strName){
		try {
	    	m_thImageLoader.loadImage(strAvatarURL, new ImageLoadedListener() {
	    		public void imageLoaded(Bitmap imageBitmap) {
	    			m_imgAvatar.setImageBitmap(imageBitmap);                
	    		}
	    	});
	    } catch (MalformedURLException e) {
	    	Log.d("UPDATE ACCOUNT INFO", "Bad remote image URL: " + strAvatarURL, e);
	    }
		
		m_txtUsername.setText(strName);
	}

	@SuppressWarnings("static-access")
	@Override
	public void onClick(View arg0) {
		if (!isOnline(this)) {
			Toast.makeText(getApplicationContext(), "Connessione Assente!", Toast.LENGTH_SHORT).show();
		}
		else {
			  //Internet available. Do what's required when internet is available.
			if (arg0.getId() == this.m_btnLoginButton.getId()){
				this.m_fbManager.login();
				showLoginSpinner();
			}
			else if (arg0.getId() == this.m_btnLogout.getId())
				this.m_fbManager.logout(this);
			else if (arg0.getId() == this.m_btnShare.getId()) {
				if (this.m_mpService != null && this.m_mpService.getCurrentPlayingItem() != null && !this.m_mpService.isStreaming()) {
					MP3Item mp3Item = this.m_mpService.getCurrentPlayingItem();
					this.m_fbManager.postOnWall(this, mp3Item.getLocalID3Field(mp3Item.TITLE), mp3Item.getLocalID3Field(mp3Item.ALBUM), mp3Item.getLocalID3Field(mp3Item.ARTIST));
				} else
					Toast.makeText(this, "Nessun brano attualmente in riproduzione", Toast.LENGTH_SHORT).show();
			} else if (arg0.getId() == this.m_btnRefresh.getId()) {
				showRefreshAnimation();
		    	this.m_fbManager.populateHashTable();
			}
		}
	}
	
	//Inizializzazione delle variabili
	private void initMemberVars() {
        this.m_strSource = new ArrayList<Post>();
        this.m_lstAdapter = new SocialItemAdapter(this, R.layout.music_player_social_row, this.m_strSource, this.getResources());
        this.m_fbManager = new FacebookManager(this);
        
        this.m_lytLoginLayout = (RelativeLayout)findViewById(R.id.facebook_login_layout);
        this.m_txtUsername = (TextView)findViewById(R.id.social_account_name);
        this.m_imgAvatar = (ImageView)findViewById(R.id.social_account_avatar);
        this.m_listView = (ListView)findViewById(android.R.id.list);
        
        this.m_btnLoginButton = (ImageButton)findViewById(R.id.login_button);
        this.m_btnLogout = (ImageButton)findViewById(R.id.social_logout_btn);
        this.m_btnRefresh = (ImageButton)findViewById(R.id.social_refresh_btn);
        this.m_btnShare = (ImageButton)findViewById(R.id.social_share_btn);
        
        this.m_btnLoginButton.setOnClickListener(this);
        this.m_btnLogout.setOnClickListener(this);
        this.m_btnShare.setOnClickListener(this);
        this.m_btnRefresh.setOnClickListener(this);
        
        this.m_thImageLoader = new ImageThreadLoader();
	}
	
	public void showLoginSpinner(){
	    ProgressBar spinner = (ProgressBar)findViewById(R.id.pb_login_live);
	    TextView txtLogin = (TextView)findViewById(R.id.txt_login_live);
	    spinner.setVisibility(View.VISIBLE);
	    txtLogin.setVisibility(View.VISIBLE);
	    this.m_btnLoginButton.setVisibility(View.GONE);
	}
	   
	public void hideLoginSpinner(){
		ProgressBar spinner = (ProgressBar)findViewById(R.id.pb_login_live);
		TextView txtLogin = (TextView)findViewById(R.id.txt_login_live);
		spinner.setVisibility(View.GONE);
		txtLogin.setVisibility(View.GONE);
		this.m_btnLoginButton.setVisibility(View.VISIBLE);
	}
	
	public void saveTokens() {
        /* Otteniamo il riferimento alle Preferences e vi salviamo i tokens appena ottenuti dopo il login */
        SharedPreferences prefs = getSharedPreferences("TOKENS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("ACCESS_TOKEN", this.m_fbManager.getFacebook().getAccessToken());
        editor.putLong("EXPIRES_TOKEN", this.m_fbManager.getFacebook().getAccessExpires());
        editor.commit();
	}
	
	public void clearTokens() {
        /* Otteniamo il riferimento alle Preferences e vi salviamo i tokens appena ottenuti dopo il login */
        SharedPreferences prefs = getSharedPreferences("TOKENS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("ACCESS_TOKEN", null);
        editor.putLong("EXPIRES_TOKEN", 0);
        editor.commit();
	}
	
	public void restoreTokens(){
		/* Ripristiniamo eventuali token precedenti, in modo da verificare se si � loggati o no */
		SharedPreferences prefs = getSharedPreferences("TOKENS", Context.MODE_PRIVATE);
        String strACCESS_TOKEN = prefs.getString("ACCESS_TOKEN", null);
        long lEXPIRES_TOKEN = prefs.getLong("EXPIRES_TOKEN", 0);
        this.m_fbManager.getFacebook().setAccessToken(strACCESS_TOKEN);
        this.m_fbManager.getFacebook().setAccessExpires(lEXPIRES_TOKEN);
	}
	
	/**
	 * Richiede la lista dei post dell'utente loggato e dei suoi amici che usano l'applicazione
	 */
	private void refreshSocialItems() {
		Hashtable<Long, Post> htCurrentPost = m_fbManager.getHashTablePostApp();
    	
    	ArrayList<Long> keys = new ArrayList<Long>(htCurrentPost.keySet());
        Collections.sort(keys);

        m_strSource.clear();
        
        for (int i = keys.size()-1; i >= 0; i--)
        	m_strSource.add(htCurrentPost.get(keys.get(i)));
        
		m_lstAdapter.notifyDataSetChanged();
		stopRefreshAnimation();
	}
	
	/**
	 * Visualizza l'animazione di refresh
	 */
	public void showRefreshAnimation(){
		Animation rotation = AnimationUtils.loadAnimation(this, R.anim.refresh_animation);
		rotation.setRepeatCount(Animation.INFINITE);
		this.m_btnRefresh.setAnimation(rotation);
		this.m_btnRefresh.startAnimation(rotation);
	}
	
	/**
	 * Interrompe l'animazione di refresh
	 */
	public void stopRefreshAnimation(){
		Animation rotation = AnimationUtils.loadAnimation(this, R.anim.refresh_animation);
		rotation.setRepeatCount(Animation.INFINITE);
		this.m_btnRefresh.clearAnimation();
	}
}
