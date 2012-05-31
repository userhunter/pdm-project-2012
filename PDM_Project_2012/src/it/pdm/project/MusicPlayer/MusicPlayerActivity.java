package it.pdm.project.MusicPlayer;

import it.pdm.project.MusicPlayer.services.MusicPlayerService;
import it.pdm.project.MusicPlayer.services.MusicPlayerService.LocalBinder;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MusicPlayerActivity extends Activity implements OnClickListener {
	//Servizio per la gestione del mediaplayer
	private MusicPlayerService m_mpService;
	  
	private TextView twResult;
	private Button buttonStart, buttonStop;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.music_player_layout);
	    
	    //BindService sarà responsabile del linking tra questa activity e il servizio. True se il bind è avvenuto con successo.
	    if (bindService(new Intent(this, MusicPlayerService.class), mConnection, Context.BIND_AUTO_CREATE)) {
	    	//Abilito questa activity per ricevere notifiche dal servizio MusicPlayerService
	    	registerReceiver(broadcastReceiver, new IntentFilter(MusicPlayerService.BROADCAST_ACTION));
	    } else {
	    	Log.d("BINDSERVICE", "ERROR DURING BINDING");
	    }

	    buttonStart = (Button) findViewById(R.id.play_button);
	    buttonStop = (Button) findViewById(R.id.pause_button);
	    twResult = (TextView) findViewById(R.id.mp3_title);

	    buttonStart.setOnClickListener(this);
	    buttonStop.setOnClickListener(this);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		//con UnregisterReceiver, richiedo di non voler più ricevere notifiche da parte del Service
		//con UnbindService, elimino il link tra questa activity e il servizio.
		unregisterReceiver(broadcastReceiver);
		unbindService(mConnection);
	}
	  
	@Override
	public void onClick(View src) {
		switch (src.getId()) {
		case R.id.play_button:
			m_mpService.playSong();
			break;
		case R.id.pause_button:
			m_mpService.pausePlaying();
			break;
		}
	}
	  
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		//Callback richiamata nel momento in cui il bind tra questa activity e il service è avvenuto con successo.
	    public void onServiceConnected(ComponentName className, IBinder service) {
	        LocalBinder binder = (LocalBinder) service;
	        //Valorizzo m_mpService con il servizio a cui l'activity di è appena linkata in modo da poter richiamare metodi pubblici
	        m_mpService = binder.getService();
		}

	    @Override
	    //Callabck richiamata nel momento in cui il bind tra questa activity ed il service termina.
	    public void onServiceDisconnected(ComponentName arg0) {
	    	Log.d("SERVICES", "UNBOUNDED");
	    }
	};
	  
	//Oggetto responsabile della gestione delle notifiche inviate dal Service.
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
	    public void onReceive(Context context, Intent intent) {
			if (intent.getStringExtra("ACTION").equals("PLAY_SONG")) {
				//Setting param  
			}
	    }
	};
}
