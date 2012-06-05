package it.pdm.project.MusicPlayer;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class TabController extends TabActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        /*
        actionBar = this.getActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        */
        
        TabHost tabHost = (TabHost)findViewById(android.R.id.tabhost);
        TabSpec musicPlayerTab = tabHost.newTabSpec("music_player");
        TabSpec socialNetworkTab = tabHost.newTabSpec("social_networking");
        
        musicPlayerTab.setIndicator("Player Musicale").setContent(new Intent(this, MusicPlayerActivity.class));
        socialNetworkTab.setIndicator("Dai tuoi amici").setContent(new Intent(this, WelcomeActivity.class));
        
        tabHost.addTab(socialNetworkTab);
        tabHost.addTab(musicPlayerTab);
    }
}
