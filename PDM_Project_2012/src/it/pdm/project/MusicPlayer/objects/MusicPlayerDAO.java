package it.pdm.project.MusicPlayer.objects;
/**DAO per i db**/
import java.util.Hashtable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import it.pdm.project.MusicPlayer.utils.MusicPlayerDBHelper;

public class MusicPlayerDAO {
	
	private MusicPlayerDBHelper m_dbHelper;
	private SQLiteDatabase m_sqliteDB;
	
	/**
	 * Oggetto per la gestione delle operazioni su database
	 * @param context Contesto d'esecuzione
	 */
	public MusicPlayerDAO(Context context){
		m_dbHelper = new MusicPlayerDBHelper(context);
		m_sqliteDB = null;
	}
	
	/**
	 * Apre la connessione con il database
	 */
	public void open() {
		if (this.m_sqliteDB == null || !this.m_sqliteDB.isOpen())
			m_sqliteDB = m_dbHelper.getWritableDatabase();
	}
	
	/**
	 * Chiude la connessione con il database
	 */
	public void close(){
		if (m_sqliteDB.isOpen())
			m_sqliteDB.close();
	}
	
	/**
	 * Verifica l'apertura della connessione con il database
	 * @return true se la connessione è aperta, false altrimenti
	 */
	public boolean isOpen() {
		return this.m_sqliteDB.isOpen();
	}
	
	/**
	 * @return true se il database è locked (non utilizzabile) dal thread corrente, false altrimenti
	 */
	public boolean isDbLockedByCurrentThread() {
		return this.m_sqliteDB.isDbLockedByCurrentThread();
	}
	
	/**
	 * @return true se il database è locked (non utilizzabile) da altri thread, false altrimenti
	 */
	public boolean isDbLockedByOtherThreads() {
		return this.m_sqliteDB.isDbLockedByOtherThreads();
	}
	
	
	/*
	 * OPERAZIONI SULL'ARCHIVIO MUSICALE
	 */

	/**
	 * Inserisce l'oggetto MP3Item all'interno del database
	 * @param mp3 Oggetto MP3Item da inserire
	 * @return Id del record appena inserito, -1 in caso di errore
	 */
	public long insertTrack(MP3Item mp3){
		ContentValues values = new ContentValues();
		
		values.put("path", mp3.getPath());
		values.put("filename", mp3.getFileName());
		values.put("title", mp3.getLocalID3Field(MP3Item.TITLE));
		values.put("artist", mp3.getLocalID3Field(MP3Item.ARTIST));
		values.put("album", mp3.getLocalID3Field(MP3Item.ALBUM));
		values.put("year", mp3.getLocalID3Field(MP3Item.YEAR));
		values.put("bitrate", Long.valueOf(mp3.getLocalID3Field(MP3Item.BITRATE).replace("~", "")));
		values.put("length", Long.valueOf(mp3.getLocalID3Field(MP3Item.LENGTH)));
		
		return m_sqliteDB.insert(MusicPlayerDBHelper.MUSIC_TABLE_NAME, null, values);
	}
	
	/**
	 * Inserisce nel database tutti i brani prelevati dall'Hashtable di String e MP3Item
	 * @param ht Hashtable da cui prelevare i brani
	 * @return 0 se tutti gli elementi dell'hashtable sono stati inseriti nel database, -1 in caso di errore
	 */
	public long insertTracksFromHT(Hashtable<String,MP3Item> ht){
		for(MP3Item mp3 : ht.values()) {
			if(insertTrack(mp3) == -1)
				return -1;
		}
		return 0;
	}
	
	/**
	 * Elimina un brano nel database 
	 * @param id Id del brano da eliminare
	 * @return Il numero di record eliminati
	 */
	public long deleteTrackById(int id){
		return m_sqliteDB.delete(MusicPlayerDBHelper.MUSIC_TABLE_NAME, "_id = "+id, null);
	}
	
	/**
	 * Elimina un brano nel database
	 * @param strPath Path del brano da eliminare
	 * @param strFileName Filename del brano da eliminare
	 * @return Il numero di record eliminati
	 */
	public long deleteTrackByPathAndFilename(String strPath, String strFileName){
		return m_sqliteDB.delete(MusicPlayerDBHelper.MUSIC_TABLE_NAME, "path='"+strPath+"' AND filename='"+strFileName+"'", null);
	}
	
	/**
	 * Elimina tutti i brani
	 */
	public void deleteAllTracks(){
		m_sqliteDB.delete(MusicPlayerDBHelper.MUSIC_TABLE_NAME, null, null);
	}
	
	/**
	 * Restituisce tutti i brani presenti nel database
	 * @return Oggetto Cursor contenente tutti i brani
	 */
	public Cursor getAllTracks(){
		return m_sqliteDB.query(MusicPlayerDBHelper.MUSIC_TABLE_NAME,	/* String table */
								null,									/* String[] columns */
								null,									/* String selection */
								null,									/* String[] selectionArgs */
								null,									/* String groupBy */
								null,									/* String having */
								null);									/* String orderBy */
	}
	
	/**
	 * Restituisce tutti i brani presenti nel database, in base ad un particolare filtro
	 * @param strFilterKey Nome della colonna da filtrare
	 * @param strFilterValue Valore filtro
	 * @return Oggetto Cursor contenente tutti i brani filtrati
	 */
	public Cursor getAllTracks(String strFilterKey, String strFilterValue){
		return m_sqliteDB.query(MusicPlayerDBHelper.MUSIC_TABLE_NAME,				/* String table */
								null,												/* String[] columns */
								strFilterKey + " LIKE \"" + strFilterValue + "\"", 	/* String selection */
								null,												/* String[] selectionArgs */
								null,												/* String groupBy */
								null,												/* String having */
								null);												/* String orderBy */
	}
	
	/*
	 * OPERAZIONI SUGLI STREAMING
	 */
	
	/**
	 * Inserisce uno stream nel database
	 * @param strName Nome dello stream
	 * @param strUrl Url dello stream
	 * @return Id del record inserito, -1 in caso di errore
	 */
	public long insertStream(String strName, String strUrl){
		ContentValues values = new ContentValues();
		values.put("name", strName);
		values.put("url", strUrl);
		
		return m_sqliteDB.insert(MusicPlayerDBHelper.STREAMS_TABLE_NAME, null, values);
	}
	
	/**
	 * Cancella uno stream dal database
	 * @param id Id dello stream da cancellare
	 * @return Numero di record eliminati
	 */
	public long deleteStreamById(int id){
		return m_sqliteDB.delete(MusicPlayerDBHelper.STREAMS_TABLE_NAME, "_id = "+id, null);
	}
	
	/**
	 * Cancella tutti gli stream nel database
	 */
	public void deleteAllStreams(){
		m_sqliteDB.delete(MusicPlayerDBHelper.STREAMS_TABLE_NAME, null, null);
	}
	
	/**
	 * Restituisce tutti gli stream presenti nel database
	 * @return Oggetto Cursor contenente tutti gli stream
	 */
	public Cursor getAllStreams(){
		return m_sqliteDB.query(MusicPlayerDBHelper.STREAMS_TABLE_NAME, null, null, null, null, null, null);
	}
	
	/**
	 * Restituisce tutti gli stream presenti nel database in base ad un filtro
	 * @param strFilterKey Nome della colonna filtro
	 * @param strFilterValue Valore del filtro
	 * @return Oggetto Cursor contenente tutti gli stream filtrati
	 */
	public Cursor getAllStreams(String strFilterKey, String strFilterValue){
		return m_sqliteDB.query(MusicPlayerDBHelper.STREAMS_TABLE_NAME,
								null,
								strFilterKey + " LIKE \"" + strFilterValue + "\"",
								null, null, null, null);
	}

	/*
	 * OPERAZIONI SULLE UTILITY
	 */
	
	/**
	 * Inserisce una variabile globale di utility nel database
	 * @param label Nome della variabile
	 * @param value Valore della variabile
	 */
	public void insertUtilityValue(String label, String value){
		ContentValues cv = new ContentValues();
		cv.put("label", label);
		cv.put("value", value);
		
		if (getUtilitiesValues(label) == null)
			m_sqliteDB.insert(MusicPlayerDBHelper.UTILITIES_TABLE_NAME, null, cv);
		else
			m_sqliteDB.update(MusicPlayerDBHelper.UTILITIES_TABLE_NAME, cv, "label LIKE \""+label+"\"", null);
	}
	
	/**
	 * Restituisce il valore di una variabile globale salvata nel database
	 * @param label Nome della variabile globale da restituire
	 * @return Valore della variabile globale selezionata
	 */
	public String getUtilitiesValues(String label) {
		Cursor cursor = m_sqliteDB.query(MusicPlayerDBHelper.UTILITIES_TABLE_NAME, new String[] {"label", "value"}, 
				"label LIKE \"" + label + "\"", null, null, null, null);
		
		while (cursor.moveToNext())
			return cursor.getString(cursor.getColumnIndex("value"));
		
		return null;
	}
	
}
