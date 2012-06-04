package it.pdm.project.MusicPlayer.objects;


import java.io.File;
import java.io.FileFilter;
import java.util.Hashtable;

public class MP3Manager {
	private String m_strMp3sPath;
	
	public MP3Manager(String strPath) {
		this.m_strMp3sPath = strPath;
	}
	
	//Mappo dentro la hashtable, ogni singolo mp3.
	public Hashtable<String, MP3Item> getMp3sTable() {
		Hashtable<String, MP3Item> htMp3Table = new Hashtable<String, MP3Item>();
		File[] mp3sFiles = this.getMp3Files();
		
		if (mp3sFiles != null)
			for (File mp3File : mp3sFiles) {
				String strFileName = mp3File.getName();
				String strFilePath = mp3File.getPath().substring(0, mp3File.getPath().lastIndexOf("/")+1);
				htMp3Table.put(strFilePath + strFileName, new MP3Item(strFilePath, strFileName));
			}
		
		return htMp3Table;
	}
	
	//Ottengo la lista di mp3 interne alla cartella passata al costruttore
	private File[] getMp3Files() {
		File mp3sDirectory = new File(this.m_strMp3sPath);
		File[] mp3sFiles   = null;
		
		if (mp3sDirectory.isDirectory()) {
			
			FileFilter dumpsFilter = new FileFilter() {
			    public boolean accept(File file) {
			    	return (file.getName().endsWith(".mp3"));
				}
			};
			
			mp3sFiles = mp3sDirectory.listFiles(dumpsFilter);
		}
		
		return mp3sFiles;
	}
	
	public String getMp3sPath() {
		return this.m_strMp3sPath;
	}
}
