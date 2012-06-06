package it.pdm.project.MusicPlayer;

import it.pdm.project.MusicPlayer.objects.MusicPlayerDAO;
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
			String[] playlistContent = {"/sdcard/Music/01 Rovine (ft. Mama Marjas).mp3", "/sdcard/Music/05 What's My Age Again_.mp3" };
			Intent newIntent = new Intent(WelcomeActivity.BROADCAST_ACTION);
			newIntent.putExtra("ACTION", "PLAY_PLAYLIST");
			newIntent.putExtra("PLAYLIST", playlistContent);
			this.sendBroadcast(newIntent);
			this.switchTabInActivity(0);
		}
	}
	
	public void switchTabInActivity(int indexTabToSwitch) {
		TabController thController = (TabController) this.getParent();
		
		thController.switchTab(indexTabToSwitch);
	}
}
