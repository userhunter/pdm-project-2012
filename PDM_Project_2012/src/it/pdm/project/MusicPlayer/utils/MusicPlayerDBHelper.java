package it.pdm.project.MusicPlayer.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MusicPlayerDBHelper extends SQLiteOpenHelper {
	
	private static String DB_NAME = "MusicPlayer_db";
	private static int DB_VERSION = 1;
	
	public static String MUSIC_TABLE_NAME = "MusicArchive";
	public static String UTILITIES_TABLE_NAME = "Utilities";
	public static String HISTORY_TABLE_NAME = "History";
	
	private static String CREATE_MUSIC_TABLE_syntax = 
			"CREATE TABLE IF NOT EXISTS " + MUSIC_TABLE_NAME + "(" +
			"_id integer primary key autoincrement, " +
			"path text not null, " +
			"filename text not null, " +
			"title text, " +
			"artist text, " +
			"album text, " +
			"year text, " +
			"bitrate real, " +
			"length real not null ) ";
	
	
	private static String CREATE_HISTORY_TABLE_syntax = 
			"CREATE TABLE IF NOT EXISTS " + HISTORY_TABLE_NAME + "(" +
			"_id integer primary key autoincrement, " +
			"path text not null, " +
			"filename text not null, " +
			"timestamp datetime default current_timestamp) ";
	
	private static String CREATE_UTILITIES_TABLE_syntax = 
	        "CREATE TABLE IF NOT EXISTS " + UTILITIES_TABLE_NAME + "(" +
	        "label text not null, " +
	        "value text not null ) ";
	
	public MusicPlayerDBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_MUSIC_TABLE_syntax);
		db.execSQL(CREATE_HISTORY_TABLE_syntax);
		db.execSQL(CREATE_UTILITIES_TABLE_syntax);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	
	}

}
