package it.pdm.project.MusicPlayer.objects;
/**Classe per l'aggiornamento del db**/
import java.io.File;
import java.util.Hashtable;

import android.content.Context;

public class MusicPlayerUpdater implements Runnable {
	private MusicPlayerDAO m_daoDatabase;
	private String m_strMusicPath;
	private Hashtable<String, MP3Item> m_htMp3Items;
	
	/**
	 * Si occupa di aggiornare il database dei brani nel caso in cui siano stati aggiunti o rimossi dei brani 
	 * @param context Contesto d'esecuzione
	 * @param strMusicPath Path dove risiedono gli mp3
	 * @param htMp3Items Hashtable contenente tutti gli MP3Item
	 */
	public MusicPlayerUpdater(Context context, String strMusicPath, Hashtable<String, MP3Item> htMp3Items) {
		this.m_daoDatabase = new MusicPlayerDAO(context);	//Database utilizzato per le operazione di aggiornamento
		this.m_strMusicPath = strMusicPath;					//Path dove risiedono tutti gli mp3
		this.m_htMp3Items = htMp3Items;						//HashTable contenente tutti gli mp3
	}
	
	@Override
	public void run() {
		File fMusicDirectory = new File(m_strMusicPath);
		this.m_daoDatabase.open();
		
		//Data di modifica della directory sotto forma di long
		long lLastModified = fMusicDirectory.lastModified();
		
		//Numero di brani presenti nel db
		int nTracksCount = this.m_daoDatabase.getAllTracks().getCount();
			
		//Se DbUpdatedAt ha come valore null (non è stato mai popolato) oppure, se la directory che contiene la musica è stata modificata recentemente, allora aggiorno. 
		if (this.m_daoDatabase.getUtilitiesValues("DbUpdatedAt") == null || nTracksCount == 0 || lLastModified > Long.parseLong(this.m_daoDatabase.getUtilitiesValues("DbUpdatedAt")))
		{
			this.m_daoDatabase.insertUtilityValue("DbIsUpdating", "true");
			this.m_daoDatabase.deleteAllTracks();
			this.m_daoDatabase.insertTracksFromHT(m_htMp3Items);
			this.m_daoDatabase.insertUtilityValue("DbIsUpdating", "false");
			this.m_daoDatabase.insertUtilityValue("DbUpdatedAt", Long.toString(lLastModified));
		}
		
		//Dopo aver aggiornato il database, inserisco la data di modifica in quest'ultimo.
		this.m_daoDatabase.close();
	}
}
