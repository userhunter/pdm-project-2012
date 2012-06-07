package it.pdm.project.MusicPlayer;

import it.pdm.project.MusicPlayer.objects.MusicPlayerDAO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import android.app.ExpandableListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;

public class MusicBrowserActivity extends ExpandableListActivity implements OnClickListener {
	private Button m_btnFilterAll, m_btnFilterAlbum, m_btnFilterArtists, m_btnFilterRadio;
	private ExpandableListView m_expListView;
	private SimpleExpandableListAdapter m_expListAdapter;
	private MusicPlayerDAO m_daoDatabase;
	private ArrayList<HashMap<String, String>> m_alRootElements;
	private ArrayList<ArrayList<HashMap<String, String>>> m_alChildElements;
	
	private Thread m_thUpdateChecker = new Thread(new Runnable() {
    	@Override
    	public void run() {
    		boolean bIsUpdating = true;
    		
    		while (bIsUpdating) {
    			bIsUpdating = checkIfIsUpdating();
    			
				Bundle data = new Bundle();
				Message msg = new Message();
				data.putBoolean("CURRENT_STATUS", bIsUpdating);
				msg.setData(data);
				
				m_hndUpdateChecker.sendMessage(msg);

    			try {
    				Thread.sleep(1000);
    			} catch (InterruptedException e) {}
    		}
    	}
    });
	
	private Handler m_hndUpdateChecker = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			if (!msg.getData().getBoolean("CURRENT_STATUS")) {
				findViewById(R.id.loading_popup).setVisibility(View.GONE);
				applyFilter("all_tracks");
				m_expListAdapter.notifyDataSetChanged();
			}
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	this.m_daoDatabase = new MusicPlayerDAO(this.getApplicationContext());
    	
    	setContentView(R.layout.music_browser_layout);
    	initMemberVars();
    	this.m_thUpdateChecker.start();
   
	    this.m_expListAdapter = new SimpleExpandableListAdapter(
	    	this,
	    	this.m_alRootElements,			 					//ArrayList contenente le root
	        R.layout.music_browser_root,     					//XML relativo al layout delle root
	        new String[] { "key" },  		 					//Chiave delle hashtable relative alle root
	        new int[] { R.id.element_title },     				//Nome della text view a cui associare il valore della root
	        this.m_alChildElements,			 					//ArrayList contenente i child per le root
	        R.layout.music_browser_child,    					//XML relativo al layout dei child
	        new String[] { "item_title", "item_album" },       	//Chiave delle hashtable relative ai childs
	        new int[] { R.id.item_title, R.id.item_album }     	//Nome della text view a cui associare il valore della root
		);
		
		this.setListAdapter(this.m_expListAdapter);
    }
    
    private void initMemberVars() {
    	this.m_expListView = this.getExpandableListView();
    	
    	this.m_alRootElements = new ArrayList<HashMap<String, String>>();
    	this.m_alChildElements = new ArrayList<ArrayList<HashMap<String, String>>>();
    	
    	this.m_btnFilterAll = (Button)findViewById(R.id.btnAll);
    	this.m_btnFilterAlbum = (Button)findViewById(R.id.btnAlbum);
    	this.m_btnFilterArtists = (Button)findViewById(R.id.btnArtist);
    	this.m_btnFilterRadio = (Button)findViewById(R.id.btnWebRadio);
    	
    	this.m_btnFilterAll.setOnClickListener(this);
    	this.m_btnFilterAlbum.setOnClickListener(this);
    	this.m_btnFilterArtists.setOnClickListener(this);
    	//this.m_btnFilterRadio.setOnClickListener(this);
    }
    
    @Override
    public void onClick(View arg0) {
    	
    	if (arg0.getId() == this.m_btnFilterAll.getId())
    		this.applyFilter("all_tracks");
    	else if (arg0.getId() == this.m_btnFilterAlbum.getId())
    		this.applyFilter("albums");
    	else if (arg0.getId() == this.m_btnFilterArtists.getId())
    		this.applyFilter("artists");
    	else if (arg0.getId() == this.m_btnFilterRadio.getId())
    		this.createListFromFilter("all_tracks");
    	
    }
    
    private void applyFilter(String strFilter) {
    	this.m_daoDatabase.open();
    	
    	this.m_alRootElements.clear();
    	this.m_alChildElements.clear();
    	
    	this.createListFromFilter(strFilter);
    	this.m_expListAdapter.notifyDataSetChanged();
    	
		if (this.m_expListAdapter.getGroupCount() > 0)
		{
			if (strFilter.equalsIgnoreCase("all_tracks"))
				m_expListView.expandGroup(0);
			else
				m_expListView.collapseGroup(0);
		}
    	
    	this.m_daoDatabase.close();
    }
    
    private void createListFromFilter(String strFilter) {
    	if (strFilter.equals("all_tracks")) {
    		this.allTracksCase();
    	} else if (strFilter.equals("artists"))
    		this.artistsOrAlbumCase("artist");
    	else if (strFilter.equals("albums"))
    		this.artistsOrAlbumCase("album");
    }
	
	private void artistsOrAlbumCase(String strKey) {
		Cursor cursor = this.m_daoDatabase.getAllTracks();
		while (cursor.moveToNext()) {
			HashMap<String, String> hmNewArtist = new HashMap<String, String>();
			hmNewArtist.put("key", cursor.getString(cursor.getColumnIndex(strKey)));
			
			if (!this.m_alRootElements.contains(hmNewArtist))
				this.m_alRootElements.add(hmNewArtist);
		}
		
		for (int rootCounter = 0; rootCounter < this.m_alRootElements.size(); rootCounter++) {
			Set<String> keys = this.m_alRootElements.get(rootCounter).keySet();
			Iterator<String> iterator = keys.iterator();
			
			String element = "";
			
			while (iterator.hasNext()) {
				String key = iterator.next();
				element = this.m_alRootElements.get(rootCounter).get(key);
			}
			
			ArrayList<HashMap<String, String>> childs = new ArrayList<HashMap<String, String>>();
			Cursor tracksCursor = this.m_daoDatabase.getAllTracks(strKey, "%"+element+"%");
			
			while (tracksCursor.moveToNext()) {
				HashMap<String, String> hmNewTrack = new HashMap<String, String>();
				hmNewTrack.put("item_id", tracksCursor.getString(tracksCursor.getColumnIndex("path")) + tracksCursor.getString(tracksCursor.getColumnIndex("filename")));
				hmNewTrack.put("item_title", tracksCursor.getString(tracksCursor.getColumnIndex("title")));
				hmNewTrack.put("item_album", tracksCursor.getString(tracksCursor.getColumnIndex("album")));
				
				childs.add(hmNewTrack);
			}
			
			this.m_alChildElements.add(childs);
		}
	}
	
	private void allTracksCase() {
		//Creazione dell'unica root (tutti i brani)
		HashMap<String, String> hmAllTracksNode = new HashMap<String, String>();
		hmAllTracksNode.put("key", "Tutti i brani");
		
		this.m_alRootElements.add(hmAllTracksNode);
		
		//Creazione dei childs
		Cursor cursor = this.m_daoDatabase.getAllTracks();
		ArrayList<HashMap<String, String>> alAllTracksChilds = new ArrayList<HashMap<String, String>>();
		
		while (cursor.moveToNext()) {
			HashMap<String, String> hmNewTrack = new HashMap<String, String>();
			hmNewTrack.put("item_id", cursor.getString(cursor.getColumnIndex("path")) + cursor.getString(cursor.getColumnIndex("filename")));
			hmNewTrack.put("item_title", cursor.getString(cursor.getColumnIndex("title")));
			hmNewTrack.put("item_album", cursor.getString(cursor.getColumnIndex("album")));
			
			alAllTracksChilds.add(hmNewTrack);
		}
		
		if (alAllTracksChilds.size() == 0)
			this.m_alRootElements.remove(0);
		
		this.m_alChildElements.add(alAllTracksChilds);
	}
	
    /* This function is called on each child click */
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		String[] playlistContent = this.getPlaylistFromClick(groupPosition, childPosition);
		
		if (playlistContent.length > 0) {
			Intent newIntent = new Intent(WelcomeActivity.BROADCAST_ACTION);
			newIntent.putExtra("ACTION", "PLAY_PLAYLIST");
			newIntent.putExtra("PLAYLIST", playlistContent);
			this.sendBroadcast(newIntent);
			this.switchTabInActivity(0);
		}
		
        return true;
    }
    
    
    private String[] getPlaylistFromClick(int group, int clickedChild) {
    	ArrayList<HashMap<String, String>> clickedGroupChilds = this.m_alChildElements.get(group);
    	ArrayList<String> alResultList = new ArrayList<String>();
    	
    	for (int i = clickedChild; i < clickedGroupChilds.size(); i++) {
    		alResultList.add(clickedGroupChilds.get(i).get("item_id"));
    		System.out.println("Adding: " + i + " -> " + clickedGroupChilds.get(i).get("item_id"));
    	}
    	
    	for (int i = 0; i < clickedGroupChilds.size()-alResultList.size()-1; i++) {
    		alResultList.add(clickedGroupChilds.get(i).get("item_id"));
    		System.out.println("Adding: " + i + " -> " + clickedGroupChilds.get(i).get("item_id"));
    	}
    	
    	String[] strResult = (String[])alResultList.toArray(new String[alResultList.size()]);
    	return strResult;
    }
    
	public void switchTabInActivity(int indexTabToSwitch) {
		TabController thController = (TabController) this.getParent();
		
		thController.switchTab(indexTabToSwitch);
	}
	
	private boolean checkIfIsUpdating() {
		this.m_daoDatabase.open();
		String strStatus = this.m_daoDatabase.getUtilitiesValues("DbIsUpdating");
		this.m_daoDatabase.close();
		
		return strStatus.equals("true") ? true : false;
	}
}
