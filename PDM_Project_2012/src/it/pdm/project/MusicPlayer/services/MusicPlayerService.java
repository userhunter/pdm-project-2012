package it.pdm.project.MusicPlayer.services;

import it.pdm.project.MusicPlayer.objects.MP3Item;
import it.pdm.project.MusicPlayer.utils.MP3Player;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class MusicPlayerService extends Service {
	private static final String SERVICE_TAG = "MUSIC_PLAYER_SERVICE";
	public static final String BROADCAST_ACTION = "it.pdm.project.MusicPlayer.service.MusicPlayerService.displayevent";

	//Oggetto di tipo IBinder che verrà restituito nel momento in cui il binding tra activity e servizio sarà completato
	private final IBinder m_binderCurrent = new LocalBinder();
	private final MP3Player m_mpMP3Player = new MP3Player();
	
	@Override
	//Funzione richiamata nel momento in cui il bind è avvenuto con successo e sarà responsabile della callback onServiceConnected() dell'activity
	public IBinder onBind(Intent arg0) {
		return m_binderCurrent;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		//Se l'inizializzazione del player fallisce gestisco l'errore.
		if (!this.m_mpMP3Player.initPlayer())
			Log.d(SERVICE_TAG, "ERROR DURING INITIALIZATION OF PLAYER.");
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		Log.d(SERVICE_TAG, "SERVICE DESTROYED");
	}
	
	public class LocalBinder extends Binder {
		public MusicPlayerService getService() {
			//Return this instance of LocalService so clients can call public methods
			return MusicPlayerService.this;
		}
	}
	
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@SuppressWarnings("static-access")
		@Override
	    public void onReceive(Context context, Intent intent) {
			Log.d("SERVICE", "BROADCAST RECEIVED");
			
			if (intent.getStringExtra("ACTION").equals("PLAY_PLAYLIST"))
			{
				Log.d("BROADCAST FOR SERVICE", "RECEIVED");
				Toast.makeText(MusicPlayerService.this, "PLAY A PLAYLIST", Toast.LENGTH_LONG).show();
			}
		}
	};

	/** PLAYER METHODS **/
	public void playSong() {
		//Se il riproduttore non sta riproducendo, avvio la riproduzione
		if (!this.m_mpMP3Player.isPlaying()) {
			this.m_mpMP3Player.playSong();
			
			//Preparo l'intent per la notifica da inviare all'activity
			Intent intent = new Intent(MusicPlayerService.BROADCAST_ACTION);
			intent.putExtra("ACTION", "PLAY_SONG");
			intent.putExtra("CURRENT_FILE_PLAYING", this.m_mpMP3Player.getCurrentPlaying().getPath() + this.m_mpMP3Player.getCurrentPlaying().getFileName());
			this.sendBroadcast(intent);
		}
	}
	
	public int getCurrentPlayingPosition() {
		return this.m_mpMP3Player.getCurrentPosition();
	}
	
	public int getCurrentPlayingTotalDuration() {
		return this.m_mpMP3Player.getDuration();
	}
	
	public void setCurrentPlayingPosition(int pos){
		//Sposta la riproduzione a pos msec
		this.m_mpMP3Player.seekTo(pos);
	}
	
	public MP3Item getCurrentPlayingItem() {
		return this.m_mpMP3Player.getCurrentPlaying();
	}
	
	public void pausePlaying() {
		//Se il riproduttore sta riproducendo, metto in pausa
		if (this.m_mpMP3Player.isPlaying()) {
			this.m_mpMP3Player.pause();
		}
	}
	
	public MP3Item getItemFromFileName(String strKey) {
		return this.m_mpMP3Player.getMp3ElementById(strKey);
	}
}
