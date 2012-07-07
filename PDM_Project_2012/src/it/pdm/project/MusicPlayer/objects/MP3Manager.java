package it.pdm.project.MusicPlayer.objects;
/**Classe che si occupa dell'aggiornamento degli mp3**/
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class MP3Manager {
	private String m_strMp3sPath;
	
	public MP3Manager(String strPath) {
		this.m_strMp3sPath = strPath;
	}
	
	//Mappo dentro la hashtable, ogni singolo mp3.
	public Hashtable<String, MP3Item> getMp3sTable() {
		Hashtable<String, MP3Item> htMp3Table = new Hashtable<String, MP3Item>();
		List<File> listFiles = new ArrayList<File>();
		this.getMp3Files(listFiles, m_strMp3sPath);
		File[] mp3sFiles = listFiles.toArray(new File[listFiles.size()]);
		
		if (mp3sFiles != null)
			for (File mp3File : mp3sFiles) {
				String strFileName = mp3File.getName();
				String strFilePath = mp3File.getPath().substring(0, mp3File.getPath().lastIndexOf("/")+1);
				htMp3Table.put(strFilePath + strFileName, new MP3Item(strFilePath, strFileName));
			}
		
		return htMp3Table;
	}
	
	/**
	 * Prelievo ricorsivo di tutti gli mp3 presenti nella sdcard.
	 * @param listFiles
	 * @param listDirectory
	 */
	private void getMp3Files(List<File> listFiles, String listDirectory) {
		File mp3sDirectory = new File(listDirectory);
		File[] mp3sFiles   = mp3sDirectory.listFiles();
		
		for (File fileItem : mp3sFiles) {
			if (fileItem.isDirectory())
				this.getMp3Files(listFiles, listDirectory + fileItem.getName());
			else
				if (fileItem.getName().endsWith(".mp3"))
					listFiles.add(fileItem);
		}
	}
	
	public String getMp3sPath() {
		return this.m_strMp3sPath;
	}
}
