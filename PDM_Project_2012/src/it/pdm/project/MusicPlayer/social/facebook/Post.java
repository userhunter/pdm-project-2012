package it.pdm.project.MusicPlayer.social.facebook;

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
	private String mCreatedPost;
	private User mUserPosted;
	
	public Post(){
		this.mPost_id = "";
		this.mMessage = "";
		this.mUserId = "";
		this.mLikes = 0;
		this.mAlbum = "";
		this.mArtist = "";
		this.mTitle = "";
		this.mCreatedPost = "";
		this.mUserPosted = new User();
	}
	
	public Post(String post_id){
		this.mPost_id = post_id;
		this.mMessage = "";
		this.mUserId = "";
		this.mLikes = 0;
		this.mAlbum = "";
		this.mArtist = "";
		this.mTitle = "";
		this.mCreatedPost = "";
		this.mUserPosted = new User();
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

	public User getUserPosted(){
		return this.mUserPosted;
	}
	
	public String getCreatedPost(){
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
	
	public void setCreatedPost(String date){
		this.mCreatedPost = date;
	}
	
	public void setUserPosted(User user){
		this.mUserPosted = user;
	}
}