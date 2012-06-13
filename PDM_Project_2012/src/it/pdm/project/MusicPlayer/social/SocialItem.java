package it.pdm.project.MusicPlayer.social;


import android.graphics.drawable.Drawable;


public class SocialItem {
	public Drawable m_dAvatar;
	public String strUrl;
	public String strName;
	public String strSurname;
	public String strSongTitle;
	public String strSongArtist;
	public String strSongAlbum;
	
	private String strId;
	
	public SocialItem(String url, String title, String artist, String album, String name, String surname) {
		this.strUrl = url;
		this.strName = name;
		this.strSurname = surname;
		this.strSongTitle = title;
		this.strSongArtist = artist;
		this.strSongAlbum = album;
	}

	public String getId() {
		return strId;
	}

	public void setId(String strId) {
		this.strId = strId;
	}
}