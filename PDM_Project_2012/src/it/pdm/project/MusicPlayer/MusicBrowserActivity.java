package it.pdm.project.MusicPlayer;

import it.pdm.project.MusicPlayer.R.drawable;

import it.pdm.project.MusicPlayer.objects.MusicPlayerDAO;
import it.pdm.project.MusicPlayer.utils.CustomEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import android.app.AlertDialog;
import android.app.ExpandableListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Toast;

/**
 * Activity che gestisce la libreria musicale dell'utente.
 */

public class MusicBrowserActivity extends ExpandableListActivity implements OnClickListener, OnKeyListener {
	/**
	 * Button
	 * **/
	private Button m_btnFilterAll, m_btnFilterAlbum, m_btnFilterArtists, m_btnFilterRadio;
	/**
	 *EditText per le ricerca
	 * **/
	private CustomEditText m_txtSearchBar;
	/**
	 *ExpandableListView per i risulati della ricerca
	 * **/
	private ExpandableListView m_expListView;
	/**
	 *Adapter per la lista
	 * **/
	private SimpleExpandableListAdapter m_expListAdapter;
	/**
	 *DAO per la gestione del database
	 * **/
	private MusicPlayerDAO m_daoDatabase;
	/**
	 *ArrayList per gli elementi espandibili
	 * **/
	private ArrayList<HashMap<String, String>> m_alRootElements;
	/**
	 *ArrayList per i sottoelementi di una root
	 * **/
	private ArrayList<ArrayList<HashMap<String, String>>> m_alChildElements;
	/**
	 *Indica la sezione in cui si sta ricercando
	 * **/
	private String m_strSection;
	
	/**Thread per il controllo dell'aggiornamento del database delle canzoni lette nell'sdcard**/
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
	
	/**Handler associato al thread sopra, il quale nel momento in cui Ë terminata la fase di aggiornamento elimina la popup di aggiornamento**/
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
	public void onBackPressed() {
	    //do nothing
		//Disabilitiamo il pulsante back (lasciamo attivo solo l'home button)
	}
	
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
    
    /**
	 * Inizializza le variabili membro.
	 * **/
    private void initMemberVars() {
    	this.m_expListView = this.getExpandableListView();
    	
    	this.m_alRootElements = new ArrayList<HashMap<String, String>>();
    	this.m_alChildElements = new ArrayList<ArrayList<HashMap<String, String>>>();
    	
    	this.m_txtSearchBar = (CustomEditText)findViewById(R.id.search_input);
    	
    	this.m_btnFilterAll = (Button)findViewById(R.id.btnAll);
    	this.m_btnFilterAlbum = (Button)findViewById(R.id.btnAlbum);
    	this.m_btnFilterArtists = (Button)findViewById(R.id.btnArtist);
    	this.m_btnFilterRadio = (Button)findViewById(R.id.btnWebRadio);
    	
    	this.m_btnFilterAll.setOnClickListener(this);
    	this.m_btnFilterAlbum.setOnClickListener(this);
    	this.m_btnFilterArtists.setOnClickListener(this);
    	this.m_btnFilterRadio.setOnClickListener(this);
    	
    	this.m_txtSearchBar.setOnKeyListener(this);
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
    		this.applyFilter("radios");
    	
    }
    
	@Override
	public boolean onKey(View source, int keyCode, KeyEvent event) {
		if (source.getId() == this.m_txtSearchBar.getId() && event.getAction() == KeyEvent.ACTION_UP){
			this.applyFilter(this.m_strSection);

			/* mostro/nascondo il clear button */
			if(!this.m_txtSearchBar.getText().toString().equals(""))
				this.m_txtSearchBar.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.delete_icon), null);
			else
				this.m_txtSearchBar.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
		}
		
		return false;
	}
    
	/**
	 * Richiede i dati dal database e ricostruisce la lista visualizzata all'utente in base al filtro passato. 
	 * @param strFilter Filtro identificante la "sezione" visualizzata.
	 */
    private void applyFilter(String strFilter) {
    	this.m_daoDatabase.open();
    	
    	this.m_strSection = strFilter;
    	
    	this.m_alRootElements.clear();
    	this.m_alChildElements.clear();
    	
    	this.createListFromFilter(strFilter);
    	this.m_expListAdapter.notifyDataSetChanged();
    	
		if (this.m_expListAdapter.getGroupCount() > 0)
		{
			if (strFilter.equalsIgnoreCase("all_tracks") || strFilter.equalsIgnoreCase("radios"))
				m_expListView.expandGroup(0);
			else
				m_expListView.collapseGroup(0);
		}
    	
    	this.m_daoDatabase.close();
    }
    
    /**
     * Richiama le procedure per popolare la lista in base al filtro.
     * @param strFilter Filtro identificante la sezione visualizzata.
     */
    private void createListFromFilter(String strFilter) {
    	if (strFilter.equals("all_tracks"))
    		this.allTracksCase();
    	else if (strFilter.equals("artists"))
    		this.artistsOrAlbumCase("artist");
    	else if (strFilter.equals("albums"))
    		this.artistsOrAlbumCase("album");
    	else if (strFilter.equals("radios"))
    		this.radiosCase();
    }
    
    /**
     * Costruisce la lista popolandola con le webradio presenti sul database.
     */
    private void radiosCase() {
    	this.registerForContextMenu(m_expListView);
    	
    	Cursor cursor;
    	
		if (this.m_txtSearchBar.getText().length() > 0)
			cursor = this.m_daoDatabase.getAllStreams("name", "%" + this.m_txtSearchBar.getText() + "%");
		else
			cursor = this.m_daoDatabase.getAllStreams();
    	
    	HashMap<String, String> hmRootItem = new HashMap<String, String>();
    	hmRootItem.put("key", "Tutte le stazioni radio");
    	
    	this.m_alRootElements.add(hmRootItem);
    	
    	ArrayList<HashMap<String, String>> alChildsElements = new ArrayList<HashMap<String, String>>();
    	HashMap<String, String> hmNewRadioCreationItem = new HashMap<String, String>();
    	hmNewRadioCreationItem.put("item_title", "Aggiungi una Web Radio..");
    	
    	alChildsElements.add(hmNewRadioCreationItem);
    	
    	while (cursor.moveToNext()) {
    		HashMap<String, String> hmNewRadioItem = new HashMap<String, String>();
    		hmNewRadioItem.put("id", cursor.getString(cursor.getColumnIndex("_id")));
    		hmNewRadioItem.put("item_title", cursor.getString(cursor.getColumnIndex("name")));
    		hmNewRadioItem.put("url", cursor.getString(cursor.getColumnIndex("url")));
    		
    		alChildsElements.add(hmNewRadioItem);
    	}
    	
    	this.m_alChildElements.add(alChildsElements);
    }
	
    /**
     * Costruisce la lista popolandola con gli mp3 memorizzati nel database. La visualizzazione varia in base al filtro applicato (album o artisti).
     * @param strKey Filtro identificante la sezione visualizzata.
     */
	private void artistsOrAlbumCase(String strKey) {
		this.unregisterForContextMenu(m_expListView);
		
		Cursor cursor;
		
		//Se Ë stata selezionata la scelta per gli album fa una query per questi
		if (this.m_txtSearchBar.getText().length() > 0 && strKey.equals("album"))
			cursor = this.m_daoDatabase.getAllTracks("album", "%" + this.m_txtSearchBar.getText() + "%");
		//Se Ë stata selezionata la scelta per l'artista fa una query per questo
		else if(this.m_txtSearchBar.getText().length() > 0 && strKey.equals("artist"))
			cursor = this.m_daoDatabase.getAllTracks("artist", "%" + this.m_txtSearchBar.getText() + "%");
		
		else
			//Se non Ë stata fatta nessuna scelta in particolare richiede tutte le tracce
			cursor = this.m_daoDatabase.getAllTracks();
		//Ottenuta la risposta "riempie" gli array di cartelle e sottocartelle
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
	
	/**
	 * Popola la lista senza applicare alcun filtro.
	 */
	private void allTracksCase() {
		this.unregisterForContextMenu(m_expListView);
		
		//Creazione dell'unica root (tutti i brani)
		HashMap<String, String> hmAllTracksNode = new HashMap<String, String>();
		hmAllTracksNode.put("key", "Tutti i brani");
		
		this.m_alRootElements.add(hmAllTracksNode);
		
		//Creazione dei childs
		Cursor cursor;
		
		if (this.m_txtSearchBar.getText().length() > 0) 
			cursor = this.m_daoDatabase.getAllTracks("title", "%" + this.m_txtSearchBar.getText() + "%");
		else
			cursor = this.m_daoDatabase.getAllTracks();
		
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
	
	/**
	 * Richiamata nel momento in cui viene intercettato un evento touch su di un sottoelemento della lista.
	 * @param parent Lista espandibile sulla quale è stato intercettato l'evento.
	 * @param groupPosition Posizione dell'elemento "padre" che contiene l'elemento cliccato.
	 * @param childPosition Posizione dell'elemento cliccato.
	 * @param id Id dell'elemento cliccato.
	 */
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		String[] playlistContent = this.getPlaylistFromClick(groupPosition, childPosition);
		
		if (this.m_strSection.equalsIgnoreCase("radios")) {
			HashMap<String, String> hmSelectedRadio = this.m_alChildElements.get(groupPosition).get(childPosition); 
			//Se Ë stato richiesto l'inserimento di una web radio visualizzo la dialog che lo permette
			if (hmSelectedRadio.get("item_title").equalsIgnoreCase("Aggiungi una Web Radio..")) {
				AlertDialog.Builder builder = new AlertDialog.Builder(MusicBrowserActivity.this);
				LinearLayout linearLayout = new LinearLayout(this);
				final EditText txtURL = new EditText(this);
				final EditText txtLabel = new EditText(this);
				
				linearLayout.setOrientation(1);
				txtURL.setHint("URL della radio");
				txtLabel.setHint("Etichetta");
				
				linearLayout.addView(txtLabel);
				linearLayout.addView(txtURL);
				
				builder.setTitle("Aggiungi")
					   .setMessage("Inserisci l'URL ed il nome della Web Radio da aggiungere")
				       .setCancelable(false)
				       .setView(linearLayout)
				       .setPositiveButton("Aggiungi", new DialogInterface.OnClickListener() {
		    	   			public void onClick(DialogInterface dialog, int id) {
								m_daoDatabase.open();
								m_daoDatabase.insertStream(txtLabel.getText().toString(), txtURL.getText().toString());
								m_daoDatabase.close();
								
								applyFilter("radios");
								m_expListAdapter.notifyDataSetChanged();
								
								Toast.makeText(getApplicationContext(), "Elemento aggiunto con successo", Toast.LENGTH_SHORT).show();
		    	   			}
				       	})
				       	.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
				       		public void onClick(DialogInterface dialog, int id) {
				       			System.out.println("NO");
				                dialog.cancel();
				           }
				       });
				
				AlertDialog alertDialog = builder.create();
				alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
				alertDialog.show();
			} else {
				//Altrimenti permette l'ascolto della web radio sulla tab Player
				Intent newIntent = new Intent("it.pdm.project.MusicPlayer.playerevents");
				newIntent.putExtra("ACTION", "PLAY_STREAM");
				newIntent.putExtra("STREAM_NAME", hmSelectedRadio.get("item_title"));
				newIntent.putExtra("STREAM_URL", hmSelectedRadio.get("url"));
				this.sendBroadcast(newIntent);
				this.switchTabInActivity(0);
			}
		}
		//Altrimenti esegue la playlist sulla tab Player
		else if (playlistContent.length > 0) {
			Intent newIntent = new Intent("it.pdm.project.MusicPlayer.playerevents");
			newIntent.putExtra("ACTION", "PLAY_PLAYLIST");
			newIntent.putExtra("PLAYLIST", playlistContent);
			this.sendBroadcast(newIntent);
			this.switchTabInActivity(0);
		}
		
        return true;
    }
    
    /**
     * Restituisce la playlist in base all'elemento cliccato.
     * @param group Posizione del "padre" contenente l'elemento cliccato.
     * @param clickedChild Posizione dell'elemento cliccato.
     * @return Array di stringhe contenenti gli id degli MP3 costituenti la nuova playlist.
     */
    private String[] getPlaylistFromClick(int group, int clickedChild) {
    	ArrayList<HashMap<String, String>> clickedGroupChilds = this.m_alChildElements.get(group);
    	ArrayList<String> alResultList = new ArrayList<String>();
    	
    	for (int i = clickedChild; i < clickedGroupChilds.size(); i++)
    		alResultList.add(clickedGroupChilds.get(i).get("item_id"));
    	
    	String[] strResult = (String[])alResultList.toArray(new String[alResultList.size()]);
    	return strResult;
    }
    
    /**
     * Cambio di tab posta in primo piano.
     * @param indexTabToSwitch Numero identificativo della tab da porre in primo piano.
     */
    public void switchTabInActivity(int indexTabToSwitch) {
		TabController thController = (TabController) this.getParent();
		
		thController.switchTab(indexTabToSwitch);
	}
	
    /**
     * Utilizzato per verificare se il database è in aggiornamento o meno.
     * @return true se il database è in aggiornamento, false al contrario.
     */
	private boolean checkIfIsUpdating() {
		this.m_daoDatabase.open();
		String strStatus = this.m_daoDatabase.getUtilitiesValues("DbIsUpdating");
		this.m_daoDatabase.close();
		
		return strStatus.equals("true") ? true : false;
	}
	
	/** OnLongClick METHODS, utilizzato per cancellare una web radio se avviene su di essa **/
	@Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo){
		final ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
		
		if (info.id > 0) {
			menu.setHeaderIcon(drawable.ic_delete);
			menu.setHeaderTitle(this.m_alChildElements.get(0).get((int)(info.id)).get("item_title"));
			//Visualizza la dialog per la cancellazione della web radio
			menu.add("Cancella").setOnMenuItemClickListener(new OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem arg0) {
					AlertDialog.Builder builder = new AlertDialog.Builder(MusicBrowserActivity.this);
					builder.setTitle("Eliminazione")
						   .setMessage("Sei sicuro di voler eliminare la stazione radio " + m_alChildElements.get(0).get((int)(info.id)).get("item_title") + "?")
					       .setCancelable(false)
					       .setPositiveButton("Conferma", new DialogInterface.OnClickListener() {
			    	   			public void onClick(DialogInterface dialog, int id) {
			    	   				//Aggiorna il database dopo aver eliminatola webradio
									m_daoDatabase.open();
									m_daoDatabase.deleteStreamById(Integer.parseInt(m_alChildElements.get(0).get((int)(info.id)).get("id")));
									m_daoDatabase.close();
									
									applyFilter("radios");
									m_expListAdapter.notifyDataSetChanged();
									
									Toast.makeText(getApplicationContext(), "Elemento eliminato con successo", Toast.LENGTH_SHORT).show();
			    	   			}
					       	})
					       	//Annulla l'operazione
					       	.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
					       		public void onClick(DialogInterface dialog, int id) {
					       			System.out.println("NO");
					                dialog.cancel();
					           }
					       });
					
					AlertDialog alertDialog = builder.create();
					alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
					alertDialog.show();
					
					return true;
				}
			});
		}
    }
}
