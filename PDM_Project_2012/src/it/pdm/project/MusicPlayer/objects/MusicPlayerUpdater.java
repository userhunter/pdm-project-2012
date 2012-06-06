package it.pdm.project.MusicPlayer.objects;

import java.io.File;
import java.util.Hashtable;

import android.os.Handler;
import android.os.Message;

public class MusicPlayerUpdater implements Runnable {
	private String m_strMusicPath;
	private Hashtable<String, MP3Item> m_htMp3Items;
	
	public MusicPlayerUpdater(String strMusicPath, Hashtable<String, MP3Item> htMp3Items) {
		this.m_strMusicPath = strMusicPath;
		this.m_htMp3Items = htMp3Items;
	}
	
	@Override
	public void run() {
		File fMusicDirectory = new File(m_strMusicPath);
		
		long lLastModified = fMusicDirectory.lastModified();
	}
}
