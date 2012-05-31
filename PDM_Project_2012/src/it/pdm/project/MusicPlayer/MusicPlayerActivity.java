package it.pdm.project.MusicPlayer;

import it.pdm.project.MusicPlayer.services.MusicPlayerService;
import it.pdm.project.MusicPlayer.services.MusicPlayerService.LocalBinder;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.Shader.TileMode;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MusicPlayerActivity extends Activity implements OnClickListener {
	//Servizio per la gestione del mediaplayer
	private MusicPlayerService m_mpService;
	  
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.music_player_layout);
	    
	    //BindService sarà responsabile del linking tra questa activity e il servizio. True se il bind è avvenuto con successo.
	    if (bindService(new Intent(this, MusicPlayerService.class), mConnection, Context.BIND_AUTO_CREATE)) {
	    	//Abilito questa activity per ricevere notifiche dal servizio MusicPlayerService
	    	registerReceiver(broadcastReceiver, new IntentFilter(MusicPlayerService.BROADCAST_ACTION));
	    } else {
	    	Log.d("BINDSERVICE", "ERROR DURING BINDING");
	    }

	    /*
        btnPlay.setVisibility(View.GONE);
		btnPause.setVisibility(View.VISIBLE);
		
		private final String imageInSD = "/sdcard/er.PNG";
		Bitmap bitmap = BitmapFactory.decodeFile(imageInSD);
        */
        
        ImageView imgCover = (ImageView)findViewById(R.id.cover);
        Bitmap originalImage = BitmapFactory.decodeResource(getResources(), R.drawable.sample_cover);
        Bitmap reflectedImage = createReflectedImage(getBaseContext(), originalImage);
        //imgCover.setImageBitmap(reflectedImage);
        
        /* ora modifichiamo la prospettiva del bitmap */
        float curScale = 1F;
        float curRotate = 0F;
        float curSkewX = 0F;
        float curSkewY = -0.063F;
        
        Matrix matrix = new Matrix();
        //matrix.postScale(curScale, curScale);
        //matrix.postRotate(curRotate);
        matrix.postSkew(curSkewX, curSkewY);
        int width = reflectedImage.getWidth();
        int height = reflectedImage.getHeight();
        
        Bitmap skewedBitmap = Bitmap.createBitmap(reflectedImage, 0, 0, width, height, matrix, true);
        imgCover.setImageBitmap(skewedBitmap);
        
        
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
	  
	//Oggetto responsabile della gestione delle notifiche inviate dal Service.
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
	    public void onReceive(Context context, Intent intent) {
			if (intent.getStringExtra("ACTION").equals("PLAY_SONG")) {
				//Setting param  
			}
	    }
	};
	
	/**
	 * This code is courtesy of Neil Davies at http://www.inter-fuser.com
	 * @param context the current context
	 * @param originalImage The original Bitmap image used to create the reflection
	 * @return the bitmap with a reflection
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

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
}
