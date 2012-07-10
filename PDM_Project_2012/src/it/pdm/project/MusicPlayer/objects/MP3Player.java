package it.pdm.project.MusicPlayer.objects;
/**Classe oer l'oggetto MP3**/
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;


import android.media.MediaPlayer;
import android.util.Log;

public class MP3Player extends MediaPlayer {
	private boolean m_bIsReady;
	private boolean m_bIsStreaming;
	
	@SuppressWarnings("unused")
	private String m_strStreamingName;
	@SuppressWarnings("unused")
	private String m_strStreamingUrl;
	private String m_strStreamingStatus;
	
	private MP3Manager m_mp3Manager; //Manager responsabile del reperimento degli mp3
	
	private MP3Item m_mp3CurrentPlaying; //Mp3 in riproduzione
	
	private ArrayList<String> m_alCurrentPlaylist; //Playlist attuale
	private int m_iCursor; //Cursore della playlist
	
	//Hashtable che conterrˆ le canzoni
	private Hashtable<String, MP3Item> m_htMp3sSongs;
	
	public MP3Player() {		
		this.m_bIsReady = false;
		this.m_bIsStreaming = false;
		this.m_strStreamingName = null;
		this.m_strStreamingUrl = null;
		this.m_mp3Manager = new MP3Manager("/sdcard/Music/");
		this.m_mp3CurrentPlaying = null;
		this.setCurrentPlaylist(null);
		this.m_iCursor = -1;
		this.m_htMp3sSongs = this.m_mp3Manager.getMp3sTable();
		setStreamingStatus("DISABLED");
	}
	
	/**
	 * @return MP3Item prelevato random
	 */
	public MP3Item getRandomMp3() {
		//Ottengo le chiavi della tabella degli mp3
		Enumeration<String> keys = this.m_htMp3sSongs.keys();
		
		//Associo la lista di chiavi ad un array
		ArrayList<String> alKeys = Collections.list(keys);
		
		if (alKeys.size() > 0) {
			int randomInt = new Random().nextInt(alKeys.size());
			return this.m_htMp3sSongs.get(alKeys.get(randomInt));
		} else
			return null;
	}
	
	/* PLAYER METHODS */
	
	/**
	 * Riproduce la canzone - se il player è pronto - che è stata impostata come datasource
	 */
	public void playSong() {
		if (this.m_bIsReady) 
			this.start();	
	}
	
	/**
	 * Riproduce lo stream passato come parametro
	 * @param strName Nome dello stream
	 * @param strUrl Url dello stream
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	public void playStream(String strName, String strUrl) throws IllegalArgumentException, SecurityException, IllegalStateException, IOException{
		this.m_strStreamingName = strName;
		this.m_strStreamingUrl = strUrl;
		reset();
		setStreamingStatus("BUFFERING");
		setDataSource(strUrl);
		prepareAsync();
		setOnPreparedListener(new OnPreparedListener(){
			@Override
			public void onPrepared(MediaPlayer mp) {
				m_bIsReady = true;
				mp.start();
				setStreamingStatus("STREAMING");
			}
		});
		setOnBufferingUpdateListener(new OnBufferingUpdateListener(){
			@Override
			public void onBufferingUpdate(MediaPlayer mp, int arg1) {
				
			}
		});
		setOnErrorListener(new OnErrorListener(){
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				setStreamingStatus("ERROR");
				Log.d("STREAMING", "ERROR");
				return false;
			}
		});
	}
	
	/* GETTER AND SETTER */
	
	/**
	 * @return Hashtable di String e MP3Item contenente tutti i file mp3
	 */
	public Hashtable<String, MP3Item> getAllMp3s() {
		return this.m_htMp3sSongs;
	}
	
	/**
	 * Restituisce l'MP3Item corrispondente alla chiave selezionata
	 * @param strKey Chiave univoca rappresentata da path+filename
	 * @return MP3Item selezionato
	 */
	public MP3Item getMp3ElementById(String strKey) {
		return this.m_htMp3sSongs.get(strKey);
	}
	
	/**
	 * @return MP3Item attualmente in esecuzione
	 */
	public MP3Item getCurrentPlaying() {
		return m_mp3CurrentPlaying;
	}

	/**
	 * Imposta l'MP3Item in esecuzione
	 * @param mp3CurrentPlaying MP3Item da mettere in esecuzione
	 */
	public void setCurrentPlaying(MP3Item mp3CurrentPlaying) {
		this.m_mp3CurrentPlaying = mp3CurrentPlaying;
	}

	/**
	 * @return Playlist di esecuzione corrente
	 */
	public ArrayList<String> getCurrentPlaylist() {
		return m_alCurrentPlaylist;
	}

	/**
	 * Imposta la playlist d'esecuzione
	 * @param m_alCurrentPlaylist Playlist d'esecuzione
	 */
	public void setCurrentPlaylist(ArrayList<String> m_alCurrentPlaylist) {
		this.m_alCurrentPlaylist = m_alCurrentPlaylist;
	}
	
	/**
	 * Sposta in avanti di uno il cursore che indica quale elemento della playlist è attualmente in esecuzione
	 * @return false se siamo alla fine della playlist, true altrimenti
	 */
	public boolean incrementPlaylistCursor(){
		if(m_iCursor >= m_alCurrentPlaylist.size()-1)
			return false;
		m_iCursor++;
		return true;
	}
	
	/**
	 * Decrementa il cursore che indica quale elemento della playlist è attualmente in esecuzione
	 * @return false se non è più possibile decrementare (prima posizione), true altrimenti
	 */
	public boolean decrementPlaylistCursor(){
		if(m_iCursor <= 0)
			return false;
		m_iCursor--;
		return true;
	}
	
	public int getPlaylistCursor(){
		return m_iCursor;
	}
	
	public String getMp3sPath() {
		return this.m_mp3Manager.getMp3sPath();
	}
	
	public void resetPlaylistCursor(){
		m_iCursor = -1;
	}
	
	public boolean isStreaming(){
		return m_bIsStreaming;
	}
	
	/**
	 * Attiva la modalità streaming
	 */
	public void enableStreaming(){
		m_bIsStreaming = true;
	}
	
	/**
	 * Disabilita la modalità streaming
	 */
	public void disableStreaming(){
		m_bIsStreaming = false;
		this.m_strStreamingName = null;
		this.m_strStreamingUrl = null;
	}
	
	/**
	 * Imposta lo stato attuale dello streaming
	 * @param strStatus Stato dello streaming (BUFFERING, STREAMING, ERROR)
	 */
	public void setStreamingStatus(String strStatus){
		this.m_strStreamingStatus = strStatus;
	}
	
	/**
	 * @return Stato attuale dello streaming
	 */
	public String getStreamingStatus(){
		return this.m_strStreamingStatus;
	}
	
	/**
	 * Incrementa il cursore della playlist e prepara l'esecuzione della canzone successiva
	 */
	public void playNextSong(){
		if(this.incrementPlaylistCursor()){
			int iCursor = this.getPlaylistCursor();
			String strSrc = this.getCurrentPlaylist().get(iCursor);
			MP3Item miToPlay = this.getMp3ElementById(strSrc);
			
			try
			{
				this.reset();
				this.setDataSource(strSrc);
				this.setCurrentPlaying(miToPlay);
				this.prepare();
				this.m_bIsReady = true;
			}
			catch (IllegalArgumentException e) {e.printStackTrace();}
			catch (SecurityException e) {e.printStackTrace();}
			catch (IllegalStateException e) {e.printStackTrace();}
			catch (IOException e) {e.printStackTrace();}
		}
	}
	
	/**
	 * Decrementa il cursore della playlist e prepara l'esecuzione della canzone precedente
	 */
	public void playPreviousSong(){
		if(this.decrementPlaylistCursor()){
			int iCursor = this.getPlaylistCursor();
			String strSrc = this.getCurrentPlaylist().get(iCursor);
			MP3Item miToPlay = this.getMp3ElementById(strSrc);
			
			try
			{
				this.reset();
				this.setDataSource(strSrc);
				this.setCurrentPlaying(miToPlay);
				this.prepare();
				this.m_bIsReady = true;
			}
			catch (IllegalArgumentException e) {e.printStackTrace();}
			catch (SecurityException e) {e.printStackTrace();}
			catch (IllegalStateException e) {e.printStackTrace();}
			catch (IOException e) {e.printStackTrace();}
		}
	}
}
