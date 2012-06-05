package it.pdm.project.MusicPlayer.objects;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.images.Artwork;

public class MP3Item {
	private String m_strPath;
	private String m_strFileName;
	private Hashtable<String, String> m_htID3Fields = new Hashtable<String, String>();
	
	public static String TITLE = "TITLE";
	public static String ALBUM = "ALBUM";
	public static String ARTIST = "ARTIST";
	public static String YEAR = "YEAR";
	public static String BITRATE = "BITRATE";
	public static String LENGTH = "LENGTH";
	
	/*
	 * Costruttore dell'oggetto MP3Item
	 * tramite libreria jaudiotagger, parsa i campi ID3 del relativo mp3
	 */
	public MP3Item(String strPath, String strFileName) {
		this.setPath(strPath);
		this.setFileName(strFileName);
		
		File tempFile = new File(getPath()+getFileName());
		AudioFile f = null;
		try {
			f = AudioFileIO.read(tempFile);
			} 	catch (CannotReadException e) {e.printStackTrace();}
				catch (IOException e) {e.printStackTrace();}
				catch (TagException e) {e.printStackTrace();}
				catch (ReadOnlyFileException e) {e.printStackTrace();}
				catch (InvalidAudioFrameException e) {e.printStackTrace();}
		
		Tag tag = f.getTag();
		
		//Aggiungo le informazioni prelevate dal tag ID3 all'hashtable m_htID3Fields
		setLocalID3Field("TITLE", tag.getFirst(FieldKey.TITLE));
		setLocalID3Field("ALBUM", tag.getFirst(FieldKey.ALBUM));
		setLocalID3Field("ARTIST", tag.getFirst(FieldKey.ARTIST));
		setLocalID3Field("YEAR", tag.getFirst(FieldKey.YEAR));
		setLocalID3Field("LENGTH", ""+f.getAudioHeader().getTrackLength());
		setLocalID3Field("BITRATE", ""+f.getAudioHeader().getBitRate());
		

	}

	public String getPath() {
		return m_strPath;
	}

	public void setPath(String m_strPath) {
		this.m_strPath = m_strPath;
	}

	public String getFileName() {
		return m_strFileName;
	}

	public void setFileName(String m_strFileName) {
		this.m_strFileName = m_strFileName;
	}
	
	public void setLocalID3Field(String strFieldName, String strValue){
		m_htID3Fields.put(strFieldName, strValue);
	}
	
	public String getLocalID3Field(String strFieldName){
		return m_htID3Fields.get(strFieldName);
	}
	
	public Artwork getCover(){
		/* restituisce null se nessuna cover è presente */
		File tempFile = new File(getPath()+getFileName());
		AudioFile f = null;
		try {
			f = AudioFileIO.read(tempFile);
			} 	catch (CannotReadException e) {e.printStackTrace();}
				catch (IOException e) {e.printStackTrace();}
				catch (TagException e) {e.printStackTrace();}
				catch (ReadOnlyFileException e) {e.printStackTrace();}
				catch (InvalidAudioFrameException e) {e.printStackTrace();}
		
		Tag tag = f.getTag();
		Artwork coverArt = tag.getFirstArtwork();
		return coverArt;
	}
}
