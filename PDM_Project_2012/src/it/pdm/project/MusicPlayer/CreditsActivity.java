package it.pdm.project.MusicPlayer;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

/**Activity che visualizza le informazioni relative all'applicazione**/

public class CreditsActivity extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	setContentView(R.layout.credits_activity_layout);
    	/**Funzione che visualizza la finestra con sfondo sfocato**/
    	getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
    }
}
