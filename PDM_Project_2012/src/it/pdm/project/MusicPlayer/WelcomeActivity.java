package it.pdm.project.MusicPlayer;

import it.pdm.project.MusicPlayer.objects.MusicPlayerDAO;
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
        
        MusicPlayerDAO mpDao = new MusicPlayerDAO(this);
        mpDao.open();
        //mpDao.insertStream("Radio DarkSin", "http://radio.darksin.ch:8000");
        mpDao.close();
    }
	
	@Override
	public void onClick(View arg0) {
		if (arg0.getId() == this.btntest.getId())
		{
			Intent newIntent = new Intent(WelcomeActivity.BROADCAST_ACTION);
			newIntent.putExtra("ACTION", "PLAY_STREAM");
			newIntent.putExtra("STREAM_NAME", "Radio DarkSin");
			newIntent.putExtra("STREAM_URL", "http://radio.darksin.ch:8000");
			this.sendBroadcast(newIntent);
			this.switchTabInActivity(0);
		}
	}
	
	public void switchTabInActivity(int indexTabToSwitch) {
		TabController thController = (TabController) this.getParent();
		
		thController.switchTab(indexTabToSwitch);
	}
}
