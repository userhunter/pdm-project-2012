package it.pdm.project.MusicPlayer;

import it.pdm.project.MusicPlayer.objects.MusicPlayerDAO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import android.app.ExpandableListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;

public class MusicBrowserActivity extends ExpandableListActivity {
	private MusicPlayerDAO m_daoDatabase;
	private ArrayList<HashMap<String, String>> m_alRootElements;
	private ArrayList<ArrayList<HashMap<String, String>>> m_alChildElements;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.music_browser_layout);
    	
    	this.m_daoDatabase = new MusicPlayerDAO(this.getApplicationContext());
    	this.m_alRootElements = new ArrayList<HashMap<String, String>>();
    	this.m_alChildElements = new ArrayList<ArrayList<HashMap<String, String>>>();
    	

		this.m_daoDatabase.open();
		this.createListFromFilter("all_tracks");
		//this.createListFromFilter("artists");
		//this.createListFromFilter("albums");
    	this.m_daoDatabase.close();
    	
    	SimpleExpandableListAdapter expListAdapter = new SimpleExpandableListAdapter(
    		this,
    		this.m_alRootElements,			 //ArrayList contenente le root
            R.layout.music_browser_root,     //XML relativo al layout delle root
            new String[] { "key" },  		 //Chiave delle hashtable relative alle root
            new int[] { R.id.row_name },     //Nome della text view a cui associare il valore della root
            this.m_alChildElements,			 //ArrayList contenente i child per le root
            R.layout.music_browser_child,    //XML relativo al layout dei child
            new String[] { "subkey" },       //Chiave delle hashtable relative ai childs
            new int[] { R.id.grp_child }     //Nome della text view a cui associare il valore della root
    	);
    	
    	this.setListAdapter(expListAdapter);
    	expListAdapter.notifyDataSetChanged();
    }
    
    private void createListFromFilter(String strFilter) {
    	if (strFilter.equals("all_tracks"))
    		this.allTracksCase();
    	else if (strFilter.equals("artists"))
    		this.artistsOrAlbumCase("artist");
    	else if (strFilter.equals("albums"))
    		this.artistsOrAlbumCase("album");
    }
	
	private void artistsOrAlbumCase(String strKey) {
		Cursor cursor = this.m_daoDatabase.getAllTracks();
		while (cursor.moveToNext()) {
			HashMap<String, String> hmNewArtist = new HashMap<String, String>();
			hmNewArtist.put("key", cursor.getString(cursor.getColumnIndex(strKey)));
			
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
				hmNewTrack.put("subkey", tracksCursor.getString(tracksCursor.getColumnIndex("title")));
				
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
			hmNewTrack.put("subkey", cursor.getString(cursor.getColumnIndex("title")));
			
			alAllTracksChilds.add(hmNewTrack);
		}
		
		if (alAllTracksChilds.size() == 0)
			this.m_alRootElements.remove(0);
		
		this.m_alChildElements.add(alAllTracksChilds);
	}
	
    /* This function is called on each child click */
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        System.out.println("Inside onChildClick at groupPosition = " + groupPosition +" Child clicked at position " + childPosition);
        return true;
    }
         
    /* This function is called on expansion of the group */
    public void onGroupExpand(int groupPosition) {
        try {
             System.out.println("Group exapanding Listener => groupPosition = " + groupPosition);
        } catch(Exception e) {
            Log.d("BROWSER ACTIVITY", "ERROR");
            e.printStackTrace();
        }
    }
}
