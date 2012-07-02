package it.pdm.project.MusicPlayer;

import it.pdm.project.MusicPlayer.services.MusicPlayerService;
import it.pdm.project.MusicPlayer.social.SocialActivity;
import android.app.ActionBar;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

@SuppressWarnings("deprecation")
public class TabController extends TabActivity {
	private TabHost m_thController;
	private TabSpec m_tsMusicPlayer;
	private TabSpec m_tsSocialNetwork;
	private TabSpec m_tsMusicLibrary;
	private ActionBar m_abActionBar;
	private Dialog m_dlgCredits;
	
	private Handler m_hndSplashScreen = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			//RelativeLayout rlSplashLayout = (RelativeLayout)findViewById(R.id.splash_screen_layout);
			//rlSplashLayout.setVisibility(View.GONE);
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        /*
        @SuppressWarnings("unused")
		Thread thSplashScreen = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(5000);
					
					m_hndSplashScreen.sendEmptyMessage(0);
				} catch (InterruptedException e) {}
			}
        });
        
        thSplashScreen.start();
        */
        
        //Avvio del servizio in background
        this.startService(new Intent(this, MusicPlayerService.class));
        
        m_abActionBar = this.getActionBar();
        m_abActionBar.setHomeButtonEnabled(false);
        m_abActionBar.setDisplayShowTitleEnabled(false);
        
        m_thController = (TabHost)findViewById(android.R.id.tabhost);
        
        m_tsMusicPlayer = m_thController.newTabSpec("music_player");
        m_tsSocialNetwork = m_thController.newTabSpec("social_networking");
        m_tsMusicLibrary = m_thController.newTabSpec("music_library");
        
        m_tsMusicPlayer.setIndicator("Player").setContent(new Intent(this, MusicPlayerActivity.class));
        m_tsSocialNetwork.setIndicator("Amici").setContent(new Intent(this, SocialActivity.class));
        m_tsMusicLibrary.setIndicator("Libreria").setContent(new Intent(this, MusicBrowserActivity.class));
        
        m_thController.addTab(m_tsMusicPlayer);
        m_thController.addTab(m_tsSocialNetwork);
        m_thController.addTab(m_tsMusicLibrary);
        
        m_thController.setCurrentTab(1);
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.settings_item:
				showDialogInfo();
			default: 
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar_layout, menu);

        /*
        //Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.abSearch).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        */
        
        return true;
    }
    
    public void switchTab(int tab) {
    	this.m_thController.setCurrentTab(tab);
    }
    
    private void showDialogInfo() {
    	m_dlgCredits = new Dialog(this);
    	m_dlgCredits.requestWindowFeature(Window.FEATURE_NO_TITLE);
    	m_dlgCredits.setContentView(R.layout.credits_activity_layout);
    	
    	Button dialogButton = (Button) m_dlgCredits.findViewById(R.id.close_button);
		dialogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				m_dlgCredits.dismiss();
			}
		});
    	
    	m_dlgCredits.show();
    }
}
