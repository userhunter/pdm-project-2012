package it.pdm.project.MusicPlayer.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

import it.pdm.project.MusicPlayer.objects.MP3Item;
import it.pdm.project.MusicPlayer.objects.MP3Player;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class MusicPlayerService extends Service {
	private static final String SERVICE_TAG = "MUSIC_PLAYER_SERVICE";
	public static final String BROADCAST_ACTION = "it.pdm.project.MusicPlayer.service.MusicPlayerService.displayevent";

	//Oggetto di tipo IBinder che verr� restituito nel momento in cui il binding tra activity e servizio sar� completato
	private final IBinder m_binderCurrent = new LocalBinder();
	private  MP3Player m_mpMP3Player;
	
	@Override
	//Funzione richiamata nel momento in cui il bind � avvenuto con successo e sar� responsabile della callback onServiceConnected() dell'activity
	public IBinder onBind(Intent arg0) {
		return m_binderCurrent;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		m_mpMP3Player = new MP3Player();
		
		//Registro il servizio abilitandolo alla ricezione di Broadcast da parte di SearchActivity
		registerReceiver(broadcastReceiver, new IntentFilter("it.pdm.project.MusicPlayer.playerevents"));

	}
	
	@Override
	  public int onStartCommand(Intent intent, int flags, int startId) {
	  Log.i("LocalService", "Received start id " + startId + ": " + intent);
	  // We want this service to continue running until it is explicitly
	  // stopped, so return sticky.
	  this.m_mpMP3Player = new MP3Player();
	  return START_STICKY;
	  }
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(SERVICE_TAG, "SERVICE DESTROYED");
	}
	
	public class LocalBinder extends Binder {
		/**
		 * Utilizzato dalle activity per ottenere il controllo sul servizio stesso.
		 * @return L'oggetto MusicPlayerService all'oggetto che ha richiesto il bind.
		 */
		public MusicPlayerService getService() {
			//Ritorna il service in modo da poterne utilizzare i metodi pubblici
			return MusicPlayerService.this;
		}
	}
	
	//Oggetto che gestisce la ricezione di messaggi di tipo Broadcast
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@SuppressWarnings("unchecked")
		@Override
	    public void onReceive(Context context, Intent intent) {
			
			if (intent.getStringExtra("ACTION").equals("PLAY_PLAYLIST")){
				//Disabilito lo streaming, se attivo
				disableStreaming();
				//Imposto la playlist che ricevo come playlist del mediaplayer e avvio la riproduzione del primo elemento
				String[] playlistContent = intent.getStringArrayExtra("PLAYLIST");
				@SuppressWarnings("rawtypes")
				ArrayList<String> alNewPlaylist  = new ArrayList(Arrays.asList(playlistContent));
				m_mpMP3Player.setCurrentPlaylist(alNewPlaylist);
				//Resetto il cursore
				m_mpMP3Player.resetPlaylistCursor();
				playNextSong();
			}
			else if (intent.getStringExtra("ACTION").equals("PLAY_STREAM")){
				//Abilito lo streaming
				enableStreaming();
				String strStreamingName = intent.getStringExtra("STREAM_NAME");
				String strStreamingUrl = intent.getStringExtra("STREAM_URL");
				playStream(strStreamingName, strStreamingUrl);
			}
		}
	};
	
	/**
	 * Avvia la riproduzione della canzone ed invia una notifica, ad azione avvenuta, tramite messaggio in broadcast.
	 */
	public void playSong() {
		//Disabilito lo streaming, se attivo
		disableStreaming();
		
		//Se il riproduttore non sta riproducendo, avvio la riproduzione
		if (!this.m_mpMP3Player.isPlaying()) {
			this.m_mpMP3Player.playSong();
			
			m_mpMP3Player.setOnCompletionListener(new OnCompletionListener() {
						@Override
						public void onCompletion(MediaPlayer arg0) {
							if (!isStreaming())
								playNextSong();
						}});
			
			//Preparo l'intent per la notifica da inviare all'activity
			Intent intent = new Intent(MusicPlayerService.BROADCAST_ACTION);
			intent.putExtra("ACTION", "PLAY_SONG");
			//intent.putExtra("CURRENT_FILE_PLAYING", this.m_mpMP3Player.getCurrentPlaying().getPath() + this.m_mpMP3Player.getCurrentPlaying().getFileName());
			this.getApplicationContext().sendBroadcast(intent);
		}
	}
	
	/**
	 * Riproduce la canzone successiva.
	 */
	public void playNextSong(){
		if(this.m_mpMP3Player.getPlaylistCursor() != this.m_mpMP3Player.getCurrentPlaylist().size()-1){
			this.m_mpMP3Player.playNextSong();
			this.playSong();
		}
	}
	
	/**
	 * Riproduce la canzone precedente.
	 */
	public void playPreviousSong(){
		this.m_mpMP3Player.playPreviousSong();
		this.playSong();
	}
	
	/**
	 * Restituisce l'avanzamento del brano.
	 * @return Intero rappresentante l'avanzamento del brano.
	 */
	public int getCurrentPlayingPosition() {
		return this.m_mpMP3Player.getCurrentPosition();
	}
	
	/**
	 * Restituisce la durata totale del brano in riproduzione.
	 * @return durata totale del brano in riproduzione.
	 */
	public int getCurrentPlayingTotalDuration() {
		return Integer.parseInt(this.m_mpMP3Player.getCurrentPlaying().getLocalID3Field(MP3Item.LENGTH))*1000;
	}
	
	/**
	 * Imposta l'attuale posizione di riproduzione. Richiamata nel momento in cui l'utente interagisce con la progress bar di riproduzione.
	 * @param pos Posizione desiderata sotto forma di intero.
	 */
	public void setCurrentPlayingPosition(int pos){
		this.m_mpMP3Player.seekTo(pos);
	}
	
	/**
	 * Restituisce l'oggetto attualmente in riproduzione.
	 * @return MP3Item attualmente in riproduzione. 
	 */
	public MP3Item getCurrentPlayingItem() {
		return this.m_mpMP3Player.getCurrentPlaying();
	}
	
	/**
	 * Restituisce il path dell'MP3 in riproduzione.
	 * @return Path dell'MP3 come oggetto String.
	 */
	public String getMp3sPath() {
		return this.m_mpMP3Player.getMp3sPath();
	}
	
	/**
	 * Mette in pausa la riproduzione.
	 */
	public void pausePlaying() {
		//Se il riproduttore sta riproducendo, metto in pausa
		if (this.m_mpMP3Player.isPlaying()) {
			this.m_mpMP3Player.pause();
		}
	}
	
	/**
	 * Restituisce l'oggetto MP3Item in base ai valori passati.
	 * @param strKey Path dell'mp3 + Filename dell'mp3.
	 * @return MP3Item corrispondende alla chiave strKey
	 */
	public MP3Item getItemFromFileName(String strKey) {
		return this.m_mpMP3Player.getMp3ElementById(strKey);
	}
	
	/**
	 * Restituisce tutti gli MP3Items
	 * @return Restituisce tutti gli MP3Items
	 */
	public Hashtable<String, MP3Item> getAllMp3s() {
		return this.m_mpMP3Player.getAllMp3s();
	}
	
	/*
	 * 	STREAMING
	 */
	
	public boolean isStreaming(){
		return this.m_mpMP3Player.isStreaming();
	}
	
	public void enableStreaming(){
		this.m_mpMP3Player.enableStreaming();
	}
	
	public void disableStreaming(){
		this.m_mpMP3Player.disableStreaming();
	}
	
	public String getStreamingStatus(){
		return this.m_mpMP3Player.getStreamingStatus();
	}
	
	/**
	 * Avvia la riproduzione dello stream e ne notifica il suo avvio tramite messaggio in broadcast.
	 * @param strName Nome dello streaming.
	 * @param strUrl URL relativo allo streaming.
	 */
	public void playStream(String strName, String strUrl){
		try
		{
			this.m_mpMP3Player.playStream(strName, strUrl);
		} 	
			catch (IllegalArgumentException e) {e.printStackTrace();}
			catch (SecurityException e) {e.printStackTrace();}
			catch (IllegalStateException e) {e.printStackTrace();}
			catch (IOException e) {e.printStackTrace();}
		
		//Preparo l'intent per la notifica da inviare all'activity
		Intent intent = new Intent(MusicPlayerService.BROADCAST_ACTION);
		intent.putExtra("ACTION", "PLAY_STREAM");
		intent.putExtra("STREAM_NAME", strName);
		intent.putExtra("STREAM_URL", strUrl);
		this.getApplicationContext().sendBroadcast(intent);
		
	}
	
	public void resumeStream(){
		this.m_mpMP3Player.playSong();
	}
	
	public boolean isPlaying(){
		return this.m_mpMP3Player.isPlaying();
	}
	
}
