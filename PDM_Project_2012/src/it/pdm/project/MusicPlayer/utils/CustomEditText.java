package it.pdm.project.MusicPlayer.utils;

import it.pdm.project.MusicPlayer.R;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

/*
 * CustomEditText estende EditText aggiungendo la possibilitˆ
 * di inserire un clear button (drawable) che cancelli il testo scritto
 */
public class CustomEditText extends EditText
{
	private Drawable m_dRight;
	private Rect m_rBounds;

	public CustomEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	public CustomEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public CustomEditText(Context context) {
		super(context);
	}

	@Override
	public void setCompoundDrawables(Drawable left, Drawable top, Drawable right, Drawable bottom)
	{
		if(right != null){
			m_dRight = right;
		}
		super.setCompoundDrawables(left, top, right, bottom);
	}
	
	@Override
	public void setCompoundDrawablesWithIntrinsicBounds(Drawable left, Drawable top, Drawable right, Drawable bottom)
	{
		if(right != null){
			m_dRight = right;
		}
		super.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
	}

	//Intercettiamo il touch sul nostro clear button. Non essendo un button ma un drawable
	//	andiamo a controllare le coordinate dell'evento touch e le confrontiamo con la posizione
	//	e la dimensione del nostro drawable, quindi svuotiamo (clear) il testo
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if(event.getAction() == MotionEvent.ACTION_UP && m_dRight!=null)
		{
			m_rBounds = m_dRight.getBounds();
			final int x = (int)event.getX();
			final int y = (int)event.getY();
			if(x >= (this.getRight() - m_rBounds.width()) && x <= (this.getRight() - this.getPaddingRight())
					&& y >= this.getPaddingTop() && y <= (this.getHeight() - this.getPaddingBottom()))
			{
				this.setText("");
				/* nascondo il clear button, lo rimostrer˜ dall'Activity */
				this.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
				event.setAction(MotionEvent.ACTION_CANCEL);//use this to prevent the keyboard from coming up
			}
		}
		return super.onTouchEvent(event);
	}

	@Override
	protected void finalize() throws Throwable
	{
		m_dRight = null;
		m_rBounds = null;
		super.finalize();
	}
}