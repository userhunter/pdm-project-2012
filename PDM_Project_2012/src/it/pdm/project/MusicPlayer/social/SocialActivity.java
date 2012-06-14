package it.pdm.project.MusicPlayer.social;

import java.util.ArrayList;

import it.pdm.project.MusicPlayer.R;
import android.app.ListActivity;
import android.database.MatrixCursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ListView;

public class SocialActivity extends ListActivity {
    /** Called when the activity is first created. */
	private Drawable m_drwIcon;
	private ListView m_listView;
	private SocialItemAdapter m_lstAdapter;
	private ArrayList<SocialItem> m_strSource;
	private MatrixCursor m_mtxCursor;
			
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_player_social);
        
        this.m_strSource = new ArrayList<SocialItem>();
        this.m_listView = (ListView)findViewById(android.R.id.list);
        this.m_lstAdapter = new SocialItemAdapter(this, R.layout.music_player_social_row, this.m_strSource, this.getResources());
        
        this.setListAdapter(m_lstAdapter);
        
        Thread child = new Thread(new Runnable() {
        	int i = 0;
        	
			@Override
			public void run() {
				while (i < 30) {
					updaterHandler.sendEmptyMessage(0);
					
					i++;
				
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
        });
        
        child.start();
    }
    
	private Handler updaterHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int randomNumber = (int)Math.random()*100;
			
			SocialItem newSocialItem = new SocialItem("http://wecare.acmos.net/files/avatars/65/070519_124183_bokito-avatar1.jpg", "Rovine", "Clementino", "I.E.N.A", "Andrea Vitale", "14/06 @11:39", 5);
			m_strSource.add(newSocialItem);
			
			m_lstAdapter.notifyDataSetChanged();
		}
	};
}
