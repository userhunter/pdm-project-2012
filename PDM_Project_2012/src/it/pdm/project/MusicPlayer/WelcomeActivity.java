package it.pdm.project.MusicPlayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class WelcomeActivity extends Activity implements OnClickListener {
	private Button btntest;
	public static final String BROADCAST_ACTION = "it.pdm.project.MusicPlayer.WelcomeActivity.displayevent";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.welcome_layout);
        
        btntest = (Button)this.findViewById(R.id.test_button);
        
        btntest.setOnClickListener(this);
    }
	
	@Override
	public void onClick(View arg0) {
		if (arg0.getId() == this.btntest.getId())
		{
			String[] playlistContent = {"/sdcard/Music/Oceana_-_Endless_Summer.mp3", "/sdcard/Music/guetta.mp3", "/sdcard/Music/Oceana_-_Endless_Summer.mp3"};
			Intent newIntent = new Intent(WelcomeActivity.BROADCAST_ACTION);
			newIntent.putExtra("ACTION", "PLAY_PLAYLIST");
			newIntent.putExtra("PLAYLIST", playlistContent);
			this.sendBroadcast(newIntent);
			Log.d("WELCOMEACTIVITY", "SENDING INTENT");
			this.switchTabInActivity(0);
		}
	}
	
	public void switchTabInActivity(int indexTabToSwitch) {
		TabController thController = (TabController) this.getParent();
		
		thController.switchTab(indexTabToSwitch);
	}
}
