package it.pdm.project.MusicPlayer.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Stack;

import it.pdm.project.MusicPlayer.objects.MP3Item;
import it.pdm.project.MusicPlayer.objects.MP3Manager;

import android.media.MediaPlayer;

public class MP3Player extends MediaPlayer {
	private boolean m_bIsReady;
	private MP3Manager m_mp3Manager; //Manager responsabile del reperimento degli mp3
	
	private MP3Item m_mp3CurrentPlaying; //Mp3 in riproduzione
	private Stack m_nextQueue; //Coda dei brani successivi
	private MP3Item m_mp3Previous; //Mp3 precedente
	
	//Hashtable che conterrà le canzoni
	private Hashtable<String, MP3Item> m_htMp3sSongs;
	
	public MP3Player() {
		this.m_bIsReady = false;
		this.m_mp3Manager = new MP3Manager("/sdcard/Music/");
		this.m_mp3CurrentPlaying = null;
		this.m_nextQueue = null;
		this.m_mp3Previous = null;
		this.m_htMp3sSongs = this.m_mp3Manager.getMp3sTable();
	}
	
	public boolean initPlayer() {
    	try {
    		//Se non c'è nessun mp3 in riproduzione, ne scelgo uno a caso. Se è di nuovo null ritorno false
    		if (this.m_mp3CurrentPlaying == null)
    			this.m_mp3CurrentPlaying = this.getRandomMp3();
    		
    		//Inizializzo il player dicendo di riprodurre come primo mp3 m_mp3CurrentPlaying. 
			this.reset();
			this.setDataSource(this.m_mp3CurrentPlaying.getPath() + this.m_mp3CurrentPlaying.getFileName());
			this.prepare();
			this.m_bIsReady = true;
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
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
	
	/** GETTER AND SETTER **/
	public MP3Item getMp3ElementById(String strKey) {
		return this.m_htMp3sSongs.get(strKey);
	}
	
	public MP3Item getCurrentPlaying() {
		return m_mp3CurrentPlaying;
	}

	public void setCurrentPlaying(MP3Item mp3CurrentPlaying) {
		this.m_mp3CurrentPlaying = mp3CurrentPlaying;
	}
}
