package it.pdm.project.MusicPlayer;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

@SuppressWarnings("deprecation")
public class TabController extends TabActivity {
	private TabHost m_thController;
	private TabSpec m_tsMusicPlayer;
	private TabSpec m_tsSocialNetwork;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        /*
        actionBar = this.getActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        */
        
        m_thController = (TabHost)findViewById(android.R.id.tabhost);
        m_tsMusicPlayer = m_thController.newTabSpec("music_player");
        m_tsSocialNetwork = m_thController.newTabSpec("social_networking");
        
        m_tsMusicPlayer.setIndicator("Player Musicale").setContent(new Intent(this, MusicPlayerActivity.class));
        m_tsSocialNetwork.setIndicator("Dai tuoi amici").setContent(new Intent(this, WelcomeActivity.class));
        
        m_thController.addTab(m_tsSocialNetwork);
        m_thController.addTab(m_tsMusicPlayer);   
    }
    
    public void switchTab(int tab) {
    	this.m_thController.setCurrentTab(tab);
    }
}
