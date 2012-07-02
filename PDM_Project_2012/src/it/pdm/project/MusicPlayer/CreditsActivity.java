package it.pdm.project.MusicPlayer;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

public class CreditsActivity extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	setContentView(R.layout.credits_activity_layout);
    	getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
    	setContentView(R.layout.music_browser_layout);
    }
}
