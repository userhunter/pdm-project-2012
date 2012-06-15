package it.pdm.project.MusicPlayer.social.facebook;

/**
 * Classe per gli utenti
 */
public class User {
	private String mId;
	private String mName;
	private String mPicture;
	
	public User(){
		this.mId = "";
		this.mName = "";
		this.mPicture = "";
	}
	
	public User (String id, String name, String picture){
		this.mId = id;
		this.mName = name;
		this.mPicture = picture;
	}
	
	/**Set e Get**/
	public void setId(String id){
		this.mId = id;
	}

	public void setName(String name){
		this.mName = name;
	}

	public void setPicture(String picture){
		this.mPicture = picture;
	}
	
	public String getId(){
		return this.mId;
	}

	public String getName(){
		return this.mName;
	}

	public String getPicture(){
		return this.mPicture;
	}
}
