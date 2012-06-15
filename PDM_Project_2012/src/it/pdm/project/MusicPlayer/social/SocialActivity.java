package it.pdm.project.MusicPlayer.social;

import java.util.ArrayList;

import it.pdm.project.MusicPlayer.R;
import it.pdm.project.MusicPlayer.services.MusicPlayerService;
import it.pdm.project.MusicPlayer.social.facebook.FacebookManager;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.MatrixCursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SocialActivity extends ListActivity {
    /** Called when the activity is first created. */
	private ListView m_listView;
	private SocialItemAdapter m_lstAdapter;
	private ArrayList<SocialItem> m_strSource;
	private FacebookManager m_fbManager;
	
	private TextView m_txtUsername;
	private RelativeLayout m_lytLoginLayout;
	
	
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
	    public void onReceive(Context context, Intent intent) {
			if (intent.getStringExtra("ACTION").equals("USER_SUCCESSFULLY_LOGGED"))
			{
				m_txtUsername.setText(intent.getStringExtra("USERNAME"));
				m_lytLoginLayout.setVisibility(View.GONE);
			}
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
        
        this.m_strSource = new ArrayList<SocialItem>();
        this.m_listView = (ListView)findViewById(android.R.id.list);
        this.m_lstAdapter = new SocialItemAdapter(this, R.layout.music_player_social_row, this.m_strSource, this.getResources());
        this.m_fbManager = new FacebookManager(this);
        
        this.m_lytLoginLayout = (RelativeLayout)findViewById(R.id.facebook_login_layout);
        this.m_txtUsername = (TextView)findViewById(R.id.social_account_name);
        
        this.registerReceiver(broadcastReceiver, new IntentFilter("it.pdm.project.MusicPlayer.social.facebook.FacebookManager.displayevent"));
        this.setListAdapter(m_lstAdapter);
        
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
        
        child.start();
    }
    
	private Handler updaterHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int randomNumber = (int)Math.random()*100;
			
			SocialItem newSocialItem = new SocialItem("http://wecare.acmos.net/files/avatars/65/070519_124183_bokito-avatar1.jpg", "Rovine", "Clementino", "I.E.N.A", "Andrea Vitale", "14/06 @11:39", 5);
			m_strSource.add(newSocialItem);
			
			m_lstAdapter.notifyDataSetChanged();
		}
	};
}
