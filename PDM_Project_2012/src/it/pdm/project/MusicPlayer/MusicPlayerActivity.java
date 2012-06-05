package it.pdm.project.MusicPlayer;

import org.jaudiotagger.tag.images.Artwork;

import it.pdm.project.MusicPlayer.objects.MP3Item;
import it.pdm.project.MusicPlayer.services.MusicPlayerService;
import it.pdm.project.MusicPlayer.services.MusicPlayerService.LocalBinder;
import it.pdm.project.MusicPlayer.utils.Utilities;

import android.app.Activity;
import android.content.*;
import android.graphics.*;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.Shader.TileMode;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MusicPlayerActivity extends Activity implements OnClickListener {
	//Servizio per la gestione del mediaplayer
	private Utilities m_utUtils;
	private MusicPlayerService m_mpService;
	private ImageButton m_btnPlayButton, m_btnPauseButton, m_btnBackwardButton, m_btnForwardButton;
	private TextView m_tvSongTitle, m_tvSongAlbum, m_tvSongYear, m_tvSongArtist, m_tvSongTotalDuration, m_tvSongActualPosition;
	private SeekBar m_pbPositionBar;
	private ImageView m_ivCover;
	public static boolean m_bProgressBarTouching = false;
	  
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.music_player_layout);
	 
	    initViewMemberVars();
	    
	    //BindService sarà responsabile del linking tra questa activity e il servizio. True se il bind è avvenuto con successo.
	    if (this.getApplicationContext().bindService(new Intent(this, MusicPlayerService.class), mConnection, Context.BIND_AUTO_CREATE))
	    	//Abilito questa activity per ricevere notifiche dal servizio MusicPlayerService
	    	registerReceiver(broadcastReceiver, new IntentFilter(MusicPlayerService.BROADCAST_ACTION));
	    else
	    	Log.d("BINDSERVICE", "ERROR DURING BINDING");

	    
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		//con UnregisterReceiver, richiedo di non voler più ricevere notifiche da parte del Service
		//con UnbindService, elimino il link tra questa activity e il servizio.
		unregisterReceiver(broadcastReceiver);
		unbindService(mConnection);
	}
	  
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		//Callback richiamata nel momento in cui il bind tra questa activity e il service è avvenuto con successo.
	    public void onServiceConnected(ComponentName className, IBinder service) {
	        LocalBinder binder = (LocalBinder) service;
	        //Valorizzo m_mpService con il servizio a cui l'activity di è appena linkata in modo da poter richiamare metodi pubblici
	        m_mpService = binder.getService();
		}

	    @Override
	    //Callabck richiamata nel momento in cui il bind tra questa activity ed il service termina.
	    public void onServiceDisconnected(ComponentName arg0) {
	    	Log.d("SERVICES", "UNBOUNDED");
	    }
	};
	
	//Questo handler aggiorna i timer d'esecuzione del brano
	private Handler positionHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			int lCurrentDuration = msg.getData().getInt("CURRENT_DURATION");
			int lTotalDuration = msg.getData().getInt("TOTAL_DURATION");
			
			String strRemainingTime = m_utUtils.milliSecondsToTimer(lTotalDuration);
			String strActualTime = m_utUtils.milliSecondsToTimer(lCurrentDuration);
			
			m_tvSongTotalDuration.setText(strRemainingTime);
			m_tvSongActualPosition.setText(strActualTime);
			
			int lCurrentPercentage = m_utUtils.getProgressPercentage(lCurrentDuration, lTotalDuration);
			if(!m_bProgressBarTouching)
				m_pbPositionBar.setProgress(lCurrentPercentage);
		}
	};
	
	//Questo listener gestisce l'avanzamento "manuale" effettuato sulla barra di progresso
	private OnSeekBarChangeListener positionListener = new OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			/* non fare niente, può causare loop e traumi vari */
		}
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			m_bProgressBarTouching = true;
		}
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			int progress = seekBar.getProgress();
			int iNewPosition = Utilities.progressToTimer(progress, m_mpService.getCurrentPlayingTotalDuration());
			m_mpService.setCurrentPlayingPosition(iNewPosition);
			m_bProgressBarTouching = false;
		}
	};
	
	@Override
	public void onClick(View sourceClick) {
		if (sourceClick.getId() == this.m_btnPlayButton.getId()) {
			this.m_btnPauseButton.setVisibility(View.VISIBLE);
			this.m_btnPlayButton.setVisibility(View.GONE);
			this.m_mpService.playSong();
		} else if (sourceClick.getId() == this.m_btnPauseButton.getId()) {
			this.m_btnPauseButton.setVisibility(View.GONE);
			this.m_btnPlayButton.setVisibility(View.VISIBLE);
			this.m_mpService.pausePlaying();
		}
	}
	  
	//Oggetto responsabile della gestione delle notifiche inviate dal Service.
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@SuppressWarnings("static-access")
		@Override
	    public void onReceive(Context context, Intent intent) {
			if (intent.getStringExtra("ACTION").equals("PLAY_SONG")) {
				MP3Item currentPlaying = m_mpService.getItemFromFileName(intent.getStringExtra("CURRENT_FILE_PLAYING"));
				
				if (currentPlaying != null) {
					m_tvSongAlbum.setText(currentPlaying.getLocalID3Field(currentPlaying.ALBUM));
					m_tvSongArtist.setText(currentPlaying.getLocalID3Field(currentPlaying.ARTIST));
					m_tvSongTitle.setText(currentPlaying.getLocalID3Field(currentPlaying.TITLE));
					m_tvSongYear.setText(currentPlaying.getLocalID3Field(currentPlaying.YEAR));
				}
				
				updateCoverImage(m_mpService.getCurrentPlayingItem());
				
				Thread updateProgressBar = new Thread (new Runnable() {
					boolean isFinished = false;
					
					@Override
					public void run() {
						while (!isFinished) {
							Bundle data = new Bundle();
							Message msg = new Message();
							data.putInt("CURRENT_DURATION", m_mpService.getCurrentPlayingPosition());
							data.putInt("TOTAL_DURATION", m_mpService.getCurrentPlayingTotalDuration());
							
							msg.setData(data);
							
							positionHandler.sendMessage(msg);
							
							if (m_mpService.getCurrentPlayingPosition() == m_mpService.getCurrentPlayingTotalDuration())
								isFinished = true;
							else {
								try {
									Thread.sleep(500);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}
					}
				});
				
				updateProgressBar.start();
			}
	    }
	};
	
	private void initViewMemberVars() {
		this.m_utUtils = new Utilities();
		
		this.m_tvSongAlbum = (TextView)findViewById(R.id.labelAlbumTitle);
		this.m_tvSongArtist = (TextView)findViewById(R.id.labelArtists);
		this.m_tvSongTitle = (TextView)findViewById(R.id.labelSongTitle);
		this.m_tvSongYear = (TextView)findViewById(R.id.labelYear);
		this.m_tvSongActualPosition = (TextView)findViewById(R.id.songCurrentDurationLabel);
		this.m_tvSongTotalDuration = (TextView)findViewById(R.id.songTotalDurationLabel);
		
	    this.m_btnPlayButton = (ImageButton)findViewById(R.id.btnPlay);
	    this.m_btnPauseButton = (ImageButton)findViewById(R.id.btnPause);
	    this.m_btnBackwardButton = (ImageButton)findViewById(R.id.btnPrevious);
	    this.m_btnForwardButton = (ImageButton)findViewById(R.id.btnNext);
	    
	    this.m_ivCover = (ImageView)findViewById(R.id.cover);
	    
	    this.m_btnPlayButton.setOnClickListener(this);
	    this.m_btnPauseButton.setOnClickListener(this);
	    this.m_btnForwardButton.setOnClickListener(this);
	    this.m_btnBackwardButton.setOnClickListener(this);
	    
	    this.m_pbPositionBar = (SeekBar)findViewById(R.id.songProgressBar);
	    this.m_pbPositionBar.setOnSeekBarChangeListener(positionListener);
	}
	
	//Aggiorna la cover dell'album visualizzata
	public void updateCoverImage(MP3Item mp3){
		Artwork artCover = mp3.getCover();
		Bitmap originalCover = null;
		
		if(artCover != null) {
			originalCover = BitmapFactory.decodeByteArray(artCover.getBinaryData(), 0, artCover.getBinaryData().length);
		}
		else {
			/* imposto la cover di default se non presente */
			originalCover = BitmapFactory.decodeResource(getResources(), R.drawable.sample_cover);
		}
		
		Bitmap reflectedCover = createReflectedImage(getBaseContext(), originalCover);
        
        /* ora modifichiamo la prospettiva del bitmap */
        float curScale = 1F;
        float curRotate = 0F;
        float curSkewX = 0F;
        float curSkewY = -0.063F;
        
        Matrix matrix = new Matrix();
        //matrix.postScale(curScale, curScale);
        //matrix.postRotate(curRotate);
        matrix.postSkew(curSkewX, curSkewY);
        int width = reflectedCover.getWidth();
        int height = reflectedCover.getHeight();
        
        Bitmap skewedCover = Bitmap.createBitmap(reflectedCover, 0, 0, width, height, matrix, true);
        m_ivCover.setImageBitmap(skewedCover);
	}
	
	/**
	 * REFLECTED IMAGE METHOD
	 */
	public static Bitmap createReflectedImage(Context context, Bitmap originalImage) {
		//The gap we want between the reflection and the original image
		final int reflectionGap = 5;

		int width = originalImage.getWidth();
		int height = originalImage.getHeight();


		//This will not scale but will flip on the Y axis
		Matrix matrix = new Matrix();
		matrix.preScale(1, -1);

		//Create a Bitmap with the flip matrix applied to it.
		//We only want the bottom half of the image
		Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0, height/2, width, height/2, matrix, false);


		//Create a new bitmap with same width but taller to fit reflection
		Bitmap bitmapWithReflection = Bitmap.createBitmap(width, (height + height/2), Config.ARGB_8888);

		//Create a new Canvas with the bitmap that's big enough for
		//the image plus gap plus reflection
		Canvas canvas = new Canvas(bitmapWithReflection);
		//Draw in the original image
		canvas.drawBitmap(originalImage, 0, 0, null);
		//Draw in the gap
		Paint defaultPaint = new Paint();
		canvas.drawRect(0, height, width, height + reflectionGap, defaultPaint);
		//Draw in the reflection
		canvas.drawBitmap(reflectionImage,0, height + reflectionGap, null);

		//Create a shader that is a linear gradient that covers the reflection
		Paint paint = new Paint();
		LinearGradient shader = new LinearGradient(0, originalImage.getHeight(), 0,
				bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff, 0x00ffffff,
				TileMode.CLAMP);
		//Set the paint to use this shader (linear gradient)
		paint.setShader(shader);
		//Set the Transfer mode to be porter duff and destination in
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		//Draw a rectangle using the paint with our linear gradient
		canvas.drawRect(0, height, width, bitmapWithReflection.getHeight() + reflectionGap, paint);

		return bitmapWithReflection;
	}
}
