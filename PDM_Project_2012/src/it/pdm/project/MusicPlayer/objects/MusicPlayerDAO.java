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
	
	public MusicPlayerDAO(Context context){
		m_dbHelper = new MusicPlayerDBHelper(context);
		m_sqliteDB = null;
	}
	
	public void open() {
		if (this.m_sqliteDB == null || !this.m_sqliteDB.isOpen())
			m_sqliteDB = m_dbHelper.getWritableDatabase();
	}
	
	public void close(){
		if (m_sqliteDB.isOpen())
			m_sqliteDB.close();
	}
	
	public boolean isOpen() {
		return this.m_sqliteDB.isOpen();
	}
	
	public boolean isDbLockedByCurrentThread() {
		return this.m_sqliteDB.isDbLockedByCurrentThread();
	}
	
	public boolean isDbLockedByOtherThreads() {
		return this.m_sqliteDB.isDbLockedByOtherThreads();
	}
	
	
	/**
	 * OPERAZIONI SULL'ARCHIVIO MUSICALE
	 */

	public long insertTrack(MP3Item mp3){
		/* ritorna l'id del record inserito oppure -1 in caso di errore */
		
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
	
	public long insertTracksFromHT(Hashtable<String,MP3Item> ht){
		/* ritorna 0 se tutti gli elementi dell'hashtable sono stati inseriti nel db, -1 in caso di errore */
		for(MP3Item mp3 : ht.values()) {
			if(insertTrack(mp3) == -1)
				return -1;
		}
		return 0;
	}
	
	public long deleteTrackById(int id){
		/* ritorna il numero di record eliminati */
		return m_sqliteDB.delete(MusicPlayerDBHelper.MUSIC_TABLE_NAME, "_id = "+id, null);
	}
	
	public long deleteTrackByPathAndFilename(String strPath, String strFileName){
		/* ritorna il numero di record eliminati */
		return m_sqliteDB.delete(MusicPlayerDBHelper.MUSIC_TABLE_NAME, "path='"+strPath+"' AND filename='"+strFileName+"'", null);
	}
	
	public void deleteAllTracks(){
		m_sqliteDB.delete(MusicPlayerDBHelper.MUSIC_TABLE_NAME, null, null);
	}
	
	public Cursor getAllTracks(){
		return m_sqliteDB.query(MusicPlayerDBHelper.MUSIC_TABLE_NAME,	/* String table */
								null,									/* String[] columns */
								null,									/* String selection */
								null,									/* String[] selectionArgs */
								null,									/* String groupBy */
								null,									/* String having */
								null);									/* String orderBy */
	}
	
	public Cursor getAllTracks(String strFilterKey, String strFilterValue){
		return m_sqliteDB.query(MusicPlayerDBHelper.MUSIC_TABLE_NAME,				/* String table */
								null,												/* String[] columns */
								strFilterKey + " LIKE \"" + strFilterValue + "\"", 	/* String selection */
								null,												/* String[] selectionArgs */
								null,												/* String groupBy */
								null,												/* String having */
								null);												/* String orderBy */
	}
	
	/**
	 * OPERAZIONI SUGLI STREAMING
	 */
	
	public long insertStream(String strName, String strUrl){
		/* ritorna l'id del record inserito oppure -1 in caso di errore */
		
		ContentValues values = new ContentValues();
		values.put("name", strName);
		values.put("url", strUrl);
		
		return m_sqliteDB.insert(MusicPlayerDBHelper.STREAMS_TABLE_NAME, null, values);
	}
	
	public long deleteStreamById(int id){
		/* ritorna il numero di record eliminati */
		return m_sqliteDB.delete(MusicPlayerDBHelper.STREAMS_TABLE_NAME, "_id = "+id, null);
	}
	
	public void deleteAllStreams(){
		m_sqliteDB.delete(MusicPlayerDBHelper.STREAMS_TABLE_NAME, null, null);
	}
	
	public Cursor getAllStreams(){
		return m_sqliteDB.query(MusicPlayerDBHelper.STREAMS_TABLE_NAME, null, null, null, null, null, null);
	}
	
	public Cursor getAllStreams(String strFilterKey, String strFilterValue){
		return m_sqliteDB.query(MusicPlayerDBHelper.STREAMS_TABLE_NAME,
								null,
								strFilterKey + " LIKE \"" + strFilterValue + "\"",
								null, null, null, null);
	}

	/**
	 * OPERAZIONI SULLE UTILITY
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
	
	public String getUtilitiesValues(String label) {
		Cursor cursor = m_sqliteDB.query(MusicPlayerDBHelper.UTILITIES_TABLE_NAME, new String[] {"label", "value"}, 
				"label LIKE \"" + label + "\"", null, null, null, null);
		
		while (cursor.moveToNext())
			return cursor.getString(cursor.getColumnIndex("value"));
		
		return null;
	}
	
}
