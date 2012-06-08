package it.pdm.project.MusicPlayer.objects;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Stack;


import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

public class MP3Player extends MediaPlayer {
	private boolean m_bIsReady;
	
	private boolean m_bIsStreaming;
	private String m_strStreamingName;
	private String m_strStreamingUrl;
	private String m_strStreamingStatus;
	
	private MP3Manager m_mp3Manager; //Manager responsabile del reperimento degli mp3
	
	private MP3Item m_mp3CurrentPlaying; //Mp3 in riproduzione
	
	private ArrayList<String> m_alCurrentPlaylist; //Playlist attuale
	private int m_iCursor; //Cursore della playlist
	
	//Hashtable che conterrà le canzoni
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
	
	/** PLAYER METHODS **/
	//Riproduco la canzone (se e solo se il player è pronto) che è stata impostata come dataSource
	public void playSong() {
		if (this.m_bIsReady) 
			this.start();	
	}
	
	//Riproduce lo stream passato come parametro
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
	
	/** GETTER AND SETTER **/
	
	public Hashtable<String, MP3Item> getAllMp3s() {
		return this.m_htMp3sSongs;
	}
	
	public MP3Item getMp3ElementById(String strKey) {
		return this.m_htMp3sSongs.get(strKey);
	}
	
	public MP3Item getCurrentPlaying() {
		return m_mp3CurrentPlaying;
	}

	public void setCurrentPlaying(MP3Item mp3CurrentPlaying) {
		this.m_mp3CurrentPlaying = mp3CurrentPlaying;
	}

	public ArrayList<String> getCurrentPlaylist() {
		return m_alCurrentPlaylist;
	}

	public void setCurrentPlaylist(ArrayList<String> m_alCurrentPlaylist) {
		this.m_alCurrentPlaylist = m_alCurrentPlaylist;
	}
	
	public boolean incrementPlaylistCursor(){
		if(m_iCursor >= m_alCurrentPlaylist.size()-1)
			return false;
		m_iCursor++;
		return true;
	}
	
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
	
	public void enableStreaming(){
		m_bIsStreaming = true;
	}
	
	public void disableStreaming(){
		m_bIsStreaming = false;
		this.m_strStreamingName = null;
		this.m_strStreamingUrl = null;
	}
	
	public void setStreamingStatus(String strStatus){
		this.m_strStreamingStatus = strStatus;
	}
	
	public String getStreamingStatus(){
		return this.m_strStreamingStatus;
	}
	
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
