package it.pdm.project.MusicPlayer.objects;

import android.content.ContentValues;
import android.content.Context;
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
	 * operazioni sull'archivio musical
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
		values.put("bitrate", Float.valueOf(mp3.getLocalID3Field(MP3Item.BITRATE)));
		values.put("length", Long.valueOf(mp3.getLocalID3Field(MP3Item.LENGTH)));
		
		return m_sqliteDB.insert(MusicPlayerDBHelper.MUSIC_TABLE_NAME, null, values);
	}
	
	public long deteleteTrackById(int id){
		/* ritorna il numero di record eliminati */
		return m_sqliteDB.delete(MusicPlayerDBHelper.MUSIC_TABLE_NAME, "_id = "+id, null);
	}
	
	public long deteleteTrackByPathAndFilename(String strPath, String strFileName){
		/* ritorna il numero di record eliminati */
		return m_sqliteDB.delete(MusicPlayerDBHelper.MUSIC_TABLE_NAME, "path='"+strPath+"' AND filename='"+strFileName+"'", null);
	}
	
	public void deleteAllTracks(){
		m_sqliteDB.delete(MusicPlayerDBHelper.MUSIC_TABLE_NAME, null, null);
	}
	
	/**
	 * operazioni sulla cronologia
	 */
	
	public long insertHistoryItem(MP3Item mp3){
		/* ritorna l'id del record inserito oppure -1 in caso di errore */
		
		ContentValues values = new ContentValues();
		
		values.put("path", mp3.getPath());
		values.put("filename", mp3.getFileName());
		
		return m_sqliteDB.insert(MusicPlayerDBHelper.HISTORY_TABLE_NAME, null, values);
	}
	
	public long deteleteHistoryItemById(int id){
		/* ritorna il numero di record eliminati */
		return m_sqliteDB.delete(MusicPlayerDBHelper.HISTORY_TABLE_NAME, "_id = "+id, null);
	}
	
	public long deteleteHistoryItemByPathAndFilename(String strPath, String strFileName){
		/* ritorna il numero di record eliminati */
		return m_sqliteDB.delete(MusicPlayerDBHelper.HISTORY_TABLE_NAME, "path='"+strPath+"' AND filename='"+strFileName+"'", null);
	}
	
	public void flushHistory(){
		m_sqliteDB.delete(MusicPlayerDBHelper.HISTORY_TABLE_NAME, null, null);
	}

}
