package it.pdm.project.MusicPlayer;

import it.pdm.project.MusicPlayer.services.MusicPlayerService;
import it.pdm.project.MusicPlayer.social.SocialActivity;
import android.app.ActionBar;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
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
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //Avvio del servizio in background
        this.startService(new Intent(this, MusicPlayerService.class));
        
        //Impostazione dell'ActionBar
        m_abActionBar = this.getActionBar();
        m_abActionBar.setHomeButtonEnabled(false);
        m_abActionBar.setDisplayShowTitleEnabled(false);
        
        m_thController = (TabHost)findViewById(android.R.id.tabhost);
        
        //Associazione degli indicatori della tab
        m_tsMusicPlayer = m_thController.newTabSpec("music_player");
        m_tsSocialNetwork = m_thController.newTabSpec("social_networking");
        m_tsMusicLibrary = m_thController.newTabSpec("music_library");
        
        //Assegna ad ogni scheda l'activity
        m_tsMusicPlayer.setIndicator("Player").setContent(new Intent(this, MusicPlayerActivity.class));
        m_tsSocialNetwork.setIndicator("Amici").setContent(new Intent(this, SocialActivity.class));
        m_tsMusicLibrary.setIndicator("Libreria").setContent(new Intent(this, MusicBrowserActivity.class));
        
        //Aggiunge i tab alla tabhost
        m_thController.addTab(m_tsMusicPlayer);
        m_thController.addTab(m_tsSocialNetwork);
        m_thController.addTab(m_tsMusicLibrary);
        
        //Imposta la tab Social come la prima visualizzata
        m_thController.setCurrentTab(1);
    }
    
    /**Funzione che al touch della ActionBar esegue le azioni indicate**/
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.settings_item:
				//Visualizza i Credits
				showDialogInfo();
			default: 
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /**Lettura XML per la compilazione dei pulsanti dell'actionbar**/
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar_layout, menu);
        
        return true;
    }
    
    /**Funzione che permette di cambiare tad**/
	public void switchTab(int tab) {
    	this.m_thController.setCurrentTab(tab);
    }
    
	/**Funzione che visualizza i credits dell'app**/
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
