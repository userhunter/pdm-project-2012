package it.pdm.project.MusicPlayer;

import android.app.Activity;
import android.os.Bundle;

public class PDM_Project_2012Activity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        System.out.println("CIAO");
    }
}