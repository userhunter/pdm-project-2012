package it.pdm.project.MusicPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import android.app.ExpandableListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;

public class MusicBrowserActivity extends ExpandableListActivity {
	private ArrayList<HashMap<String, String>> m_alRootElements;
	private ArrayList<ArrayList<HashMap<String, String>>> m_alChildElements;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.music_browser_layout);
    	
    	this.m_alRootElements = new ArrayList<HashMap<String, String>>();
    	this.m_alChildElements = new ArrayList<ArrayList<HashMap<String, String>>>();
    	
    	this.createRootsList();
    	this.createChildsList();
    	
    	SimpleExpandableListAdapter expListAdapter = new SimpleExpandableListAdapter(
    		this,
    		this.m_alRootElements,			 //ArrayList contenente le root
            R.layout.music_browser_root,     //XML relativo al layout delle root
            new String[] { "key" },  		 //Chiave delle hashtable relative alle root
            new int[] { R.id.row_name },     //Nome della text view a cui associare il valore della root
            this.m_alChildElements,			 //ArrayList contenente i child per le root
            R.layout.music_browser_child,    //XML relativo al layout dei child
            new String[] { "subkey" },         //Chiave delle hashtable relative ai childs
            new int[] { R.id.grp_child }      //Nome della text view a cui associare il valore della root
    	);
    	
    	this.setListAdapter(expListAdapter);
    }
   
	private void createRootsList() {
		//Richiedo tutti gli album(artisti dal db e per ognuno di essi creo l'hashmap e l'aggiungo all'array di root elements
		HashMap<String, String> hmClementino = new HashMap<String, String>();
		hmClementino.put("key", "Clementino");
		
		HashMap<String, String> hmAdele = new HashMap<String, String>();
		hmAdele.put("key", "Adele");
		
	    this.m_alRootElements.add(hmAdele);
	    this.m_alRootElements.add(hmClementino);
	}
	
	private void createChildsList() {
		for (int rootCounter = 0; rootCounter < this.m_alRootElements.size(); rootCounter++) {
			Set<String> keys = this.m_alRootElements.get(rootCounter).keySet();
			Iterator<String> iterator = keys.iterator();
			
			String element = "";
			
			while (iterator.hasNext()) {
				String key = iterator.next();
				element = this.m_alRootElements.get(rootCounter).get(key);
			}
			
			Random generator = new Random();
			int randomNumber = generator.nextInt(10);
			
			ArrayList<HashMap<String, String>> childs = new ArrayList<HashMap<String, String>>();
			for (int i = 0; i <= randomNumber; i++) {	
				HashMap<String, String> childElement = new HashMap<String, String>();
				childElement.put("subkey", "sub " + i + " per " + element);
				
				childs.add(childElement);
			}
			
			this.m_alChildElements.add(childs);
		}
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
