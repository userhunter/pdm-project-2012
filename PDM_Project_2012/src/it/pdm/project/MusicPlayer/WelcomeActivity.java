package it.pdm.project.MusicPlayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
			String[] playlistContent = {"Canzone1", "Canzone2", "Canzone3"};
			Intent newIntent = new Intent(this.BROADCAST_ACTION);
			newIntent.putExtra("ACTION", "PLAY_PLAYLIST");
			newIntent.putExtra("PLAYLIST", playlistContent);
			
			this.sendBroadcast(newIntent);
		}
	}
}
