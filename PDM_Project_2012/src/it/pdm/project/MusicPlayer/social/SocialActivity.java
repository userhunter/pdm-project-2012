package it.pdm.project.MusicPlayer.social;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import it.pdm.project.MusicPlayer.R;
import it.pdm.project.MusicPlayer.objects.MP3Item;
import it.pdm.project.MusicPlayer.services.MusicPlayerService;
import it.pdm.project.MusicPlayer.services.MusicPlayerService.LocalBinder;
import it.pdm.project.MusicPlayer.social.ImageThreadLoader.ImageLoadedListener;
import it.pdm.project.MusicPlayer.social.facebook.FacebookManager;
import it.pdm.project.MusicPlayer.social.facebook.User;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SocialActivity extends ListActivity implements OnClickListener {
	private User m_userMe;
	private ListView m_listView;
	private SocialItemAdapter m_lstAdapter;
	private ArrayList<SocialItem> m_strSource;
	private FacebookManager m_fbManager;
	private MusicPlayerService m_mpService;
	
	private ImageButton m_btnLogout, m_btnRefresh, m_btnShare;
	private Button m_btnLoginButton;
	private TextView m_txtUsername;
	private RelativeLayout m_lytLoginLayout;
	private ImageView m_imgAvatar;
	
	private ImageThreadLoader m_thImageLoader;
	
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
	    public void onReceive(Context context, Intent intent) {
			if (intent.getStringExtra("ACTION").equals("USER_SUCCESSFULLY_LOGGED")) {
				//m_txtUsername.setText(intent.getStringExtra("USERNAME"));
				if (m_userMe == null)
					m_userMe = new User();
				
				m_userMe.setId(intent.getStringExtra("ID"));
				m_userMe.setName(intent.getStringExtra("USERNAME"));
				m_userMe.setPicture(intent.getStringExtra("AVATAR"));
				
				updateAccountInfo(m_userMe.getPicture().replace("https://", "http://"), m_userMe.getName());
				saveTokens();// Salvo i token della nuova sessione
				m_lytLoginLayout.setVisibility(View.GONE);
			}
			else if (intent.getStringExtra("ACTION").equals("USER_SUCCESSFULLY_LOGGED_OUT"))
				m_lytLoginLayout.setVisibility(View.VISIBLE);
			else if (intent.getStringExtra("ACTION").equals("SONG_SUCCESSFULLY_POSTED")) 
				System.out.println("OK POST");
		}
	};
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		this.m_fbManager.getFacebook().authorizeCallback(requestCode, resultCode, data);
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
        if (!this.m_fbManager.isLogged())
        	this.m_fbManager.login();
        
        Thread child = new Thread(new Runnable() {
        	int i = 0;
        	
			@Override
			public void run() {
				while (i < 30) {
					updaterHandler.sendEmptyMessage(0);
					
					i++;
				
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
        });
        
        //child.start();
    }
    
    private void testPost() {
    	/*
    	Hashtable<String, Post> posts = new Hashtable<String, Post>();
    	
    	this.m_fbManager.getAllPost();
    	posts = this.m_fbManager.getHashTablePostApp();
    	
    	System.out.println("SIZE: " + posts.size());
    	
    	Hashtable ht = new Hashtable();
    	Enumeration keys = ht.keys();
    	
    	while (keys.hasMoreElements()) {
    		Object key = keys.nextElement();
    		Object value = ht.get(key);
    		
    		System.out.println("POST: key: " + key + " value: " + ht.get(key));
    	}
    	*/
    	
    	this.m_fbManager.getFriendsPostsSorted();
    }
    
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		//Callback richiamata nel momento in cui il bind tra questa activity e il service è avvenuto con successo.
	    public void onServiceConnected(ComponentName className, IBinder service) {
	        LocalBinder binder = (LocalBinder) service;
	        //Valorizzo m_mpService con il servizio a cui l'activity di è appena linkata in modo da poter richiamare metodi pubblici
	        m_mpService = binder.getService();
		}

	    @Override
	    //Callabck richiamata nel momento in cui il bind tra questa activity ed il service termina.
	    public void onServiceDisconnected(ComponentName arg0) { }
	};
    
	private Handler updaterHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int randomNumber = (int)Math.random()*100;
			
			SocialItem newSocialItem = new SocialItem("http://wecare.acmos.net/files/avatars/65/070519_124183_bokito-avatar1.jpg", "Rovine", "Clementino", "I.E.N.A", "Andrea Vitale", "14/06 @11:39", 5);
			m_strSource.add(newSocialItem);
			
			m_lstAdapter.notifyDataSetChanged();
		}
	};
	
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

	@Override
	public void onClick(View arg0) {
		if (arg0.getId() == this.m_btnLoginButton.getId())
			this.m_fbManager.login();
		else if (arg0.getId() == this.m_btnLogout.getId())
			this.m_fbManager.logout(this);
		else if (arg0.getId() == this.m_btnShare.getId()) {
			if (this.m_mpService != null) {
				MP3Item mp3Item = this.m_mpService.getCurrentPlayingItem();
				this.m_fbManager.postOnWall(this, mp3Item.getLocalID3Field(mp3Item.TITLE), mp3Item.getLocalID3Field(mp3Item.ALBUM), mp3Item.getLocalID3Field(mp3Item.ARTIST));
			}
		} else if (arg0.getId() == this.m_btnRefresh.getId()) {
			this.testPost();
		}
	}
	
	private void initMemberVars() {
        this.m_strSource = new ArrayList<SocialItem>();
        this.m_lstAdapter = new SocialItemAdapter(this, R.layout.music_player_social_row, this.m_strSource, this.getResources());
        this.m_fbManager = new FacebookManager(this);
        
        this.m_lytLoginLayout = (RelativeLayout)findViewById(R.id.facebook_login_layout);
        this.m_txtUsername = (TextView)findViewById(R.id.social_account_name);
        this.m_imgAvatar = (ImageView)findViewById(R.id.social_account_avatar);
        this.m_listView = (ListView)findViewById(android.R.id.list);
        
        this.m_btnLoginButton = (Button)findViewById(R.id.login_button);
        this.m_btnLogout = (ImageButton)findViewById(R.id.social_logout_btn);
        this.m_btnRefresh = (ImageButton)findViewById(R.id.social_refresh_btn);
        this.m_btnShare = (ImageButton)findViewById(R.id.social_share_btn);
        
        this.m_btnLoginButton.setOnClickListener(this);
        this.m_btnLogout.setOnClickListener(this);
        this.m_btnShare.setOnClickListener(this);
        this.m_btnRefresh.setOnClickListener(this);
        
        this.m_userMe = this.m_fbManager.getCurrentUser();
        this.m_thImageLoader = new ImageThreadLoader();
	}
	
	public void saveTokens() {
        /* Otteniamo il riferimento alle Preferences e vi salviamo i tokens appena ottenuti dopo il login */
        SharedPreferences prefs = getSharedPreferences("TOKENS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("ACCESS_TOKEN", this.m_fbManager.getFacebook().getAccessToken());
        editor.putLong("EXPIRES_TOKEN", this.m_fbManager.getFacebook().getAccessExpires());
        editor.commit();
	}
	
	public void restoreTokens(){
		/* Ripristiniamo eventuali token precedenti, in modo da verificare se si è loggati o no */
		SharedPreferences prefs = getSharedPreferences("TOKENS", Context.MODE_PRIVATE);
        String strACCESS_TOKEN = prefs.getString("ACCESS_TOKEN", null);
        long lEXPIRES_TOKEN = prefs.getLong("EXPIRES_TOKEN", 0);
        this.m_fbManager.getFacebook().setAccessToken(strACCESS_TOKEN);
        this.m_fbManager.getFacebook().setAccessExpires(lEXPIRES_TOKEN);
	}
}
