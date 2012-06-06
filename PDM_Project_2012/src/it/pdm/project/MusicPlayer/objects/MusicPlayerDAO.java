package it.pdm.project.MusicPlayer.objects;

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
	}
	
	public void open(){
		m_sqliteDB = m_dbHelper.getWritableDatabase();
	}
	
	public void close(){
		m_sqliteDB.close();
	}
	
	/**
	 * OPERAZIONI SULL'ARCHIVIO MUSICALE
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
	
	public long insertTrack(MP3Item mp3){
		/* ritorna l'id del record inserito oppure -1 in caso di errore */
		
		ContentValues values = new ContentValues();
		
		values.put("path", mp3.getPath());
		values.put("filename", mp3.getFileName());
		values.put("title", mp3.getLocalID3Field(MP3Item.TITLE));
		values.put("artist", mp3.getLocalID3Field(MP3Item.ARTIST));
		values.put("album", mp3.getLocalID3Field(MP3Item.ALBUM));
		values.put("year", mp3.getLocalID3Field(MP3Item.YEAR));
		values.put("bitrate", Float.valueOf(mp3.getLocalID3Field(MP3Item.BITRATE)));
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
	 * OPERAZIONI SULLA CRONOLOGIA
	 */
	
	public long insertHistoryItem(MP3Item mp3){
		/* ritorna l'id del record inserito oppure -1 in caso di errore */
		
		ContentValues values = new ContentValues();
		
		values.put("path", mp3.getPath());
		values.put("filename", mp3.getFileName());
		
		return m_sqliteDB.insert(MusicPlayerDBHelper.HISTORY_TABLE_NAME, null, values);
	}
	
	public long deleteHistoryItemById(int id){
		/* ritorna il numero di record eliminati */
		return m_sqliteDB.delete(MusicPlayerDBHelper.HISTORY_TABLE_NAME, "_id = "+id, null);
	}
	
	public long deleteHistoryItemByPathAndFilename(String strPath, String strFileName){
		/* ritorna il numero di record eliminati */
		return m_sqliteDB.delete(MusicPlayerDBHelper.HISTORY_TABLE_NAME, "path='"+strPath+"' AND filename='"+strFileName+"'", null);
	}
	
	public void flushHistory(){
		m_sqliteDB.delete(MusicPlayerDBHelper.HISTORY_TABLE_NAME, null, null);
	}
	
	public Cursor getHistory(){
		return m_sqliteDB.query(MusicPlayerDBHelper.HISTORY_TABLE_NAME,	/* String table */
								null,									/* String[] columns */
								null,									/* String selection */
								null,									/* String[] selectionArgs */
								null,									/* String groupBy */
								null,									/* String having */
								"timestamp DESC");						/* String orderBy */
	}
	
	public String[] getFirstHistoryItemPathAndFilename(){
		/* ritorna un array di stringhe con path e filename del primo elemento della cronologia */
		Cursor tempCursor;
		tempCursor = m_sqliteDB.query(MusicPlayerDBHelper.HISTORY_TABLE_NAME,
								null,
								null,
								null,
								null,
								null,
								"timestamp DESC",
								"LIMIT 1");
		String[] res = {"",""};							/* se non c'è alcun elemento, ritorna array vuoto */
		if(tempCursor.isFirst()){
			res[0] = tempCursor.getString(1);			/* path */
			res[1] = tempCursor.getString(2);			/* filename */
		}
		return res;
	}

}
