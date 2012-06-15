package it.pdm.project.MusicPlayer.social.facebook;

import java.util.Date;

/**
 * Classe per i post
 */

public class Post {
	private String mPost_id;
	private String mMessage;
	private String mUserId;
	private int mLikes;
	private String mAlbum;
	private String mArtist;
	private String mTitle;
	private Date mCreatedPost;
	
	public Post(){
		this.mPost_id = "";
		this.mMessage = "";
		this.mUserId = "";
		this.mLikes = 0;
		this.mAlbum = "";
		this.mArtist = "";
		this.mTitle = "";
		this.mCreatedPost = new Date();
	}
	
	public Post(String post_id){
		this.mPost_id = post_id;
		this.mMessage = "";
		this.mUserId = "";
		this.mLikes = 0;
		this.mAlbum = "";
		this.mArtist = "";
		this.mTitle = "";
		this.mCreatedPost = new Date();
	}
	
	
	/**Set e Get**/
	public String getPostID(){
		return this.mPost_id;
	}
	
	public String getMessage(){
		return this.mMessage;
	}
	
	public String getUser(){
		return this.mUserId;
	}
	
	public int getLikeUser(){
		return this.mLikes;
	}
	
	public String getAlbum(){
		return this.mAlbum;
	}
	
	public String getArtist(){
		return this.mArtist;
	}
	
	public String getTitle(){
		return this.mTitle;
	}
	
	public Date getCreatedPost(){
		return this.mCreatedPost;
	}
	
	public void setMessage(String message){
		this.mMessage = message;
	}
	
	public void setUser(String user){
		this.mUserId = user;
	}
	
	public void setLikeUser(int likeUser){
		this.mLikes = likeUser;
	}
	
	public void setAlbum(String album){
		this.mAlbum = album;
	}
	
	public void setTitle(String title){
		this.mTitle = title;
	}
	
	public void setArtist(String artist){
		this.mArtist = artist;
	}
	
	public void setCreatedPost(Date date){
		this.mCreatedPost = date;
	}
}
