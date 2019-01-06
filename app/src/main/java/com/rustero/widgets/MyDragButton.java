package com.rustero.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;


public class MyDragButton extends ImageButton{


	public interface OnPressListener {
		public void onPress(MyDragButton aSender);
	}


	private static final String LOG_TAG = "MyDragButton";
	private OnPressListener mListener;
	private float mDragX, mDragY;
	private float mMaxNewX, mMaxNewY;
	private float mDownX, mDownY;
	private long mDownMils;
	private View mAnchor;


	public MyDragButton(Context context) {
		super(context);
		setOnTouchListener(new MyTouchListener());
	}

	public MyDragButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOnTouchListener(new MyTouchListener());
	}

	public MyDragButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setOnTouchListener(new MyTouchListener());
	}



	public void setAnchor(View aAnchor) {
		mAnchor = aAnchor;
	}



	public void setOnPressListener(OnPressListener aListener) {
		mListener = aListener;
	}



	private class MyTouchListener implements View.OnTouchListener {

		public boolean onTouch(View view, MotionEvent event) {
			switch (event.getActionMasked()) {
				case MotionEvent.ACTION_DOWN:
					mDownMils = System.currentTimeMillis();
					mDownX = event.getRawX();
					mDownY = event.getRawY();
					mDragX = view.getX() - event.getRawX();
					mDragY = view.getY() - event.getRawY();

					if (mMaxNewX < 1.0f)
						mMaxNewX = mAnchor.getWidth() - view.getWidth() - 4;
					if (mMaxNewY < 1.0f)
						mMaxNewY = mAnchor.getHeight() - view.getHeight() - 4;

					//App.log( "Action Down " + event.getRawX() + "," + event.getRawY() + "  " + mDragX + "," + mDragY);
					break;

				case MotionEvent.ACTION_MOVE:
					if (System.currentTimeMillis() - mDownMils < 222) return true;  //ignore earlies
					float newX = event.getRawX() + mDragX;
					float newY = event.getRawY() + mDragY;

					if (newX < 0)
						newX = 0;
					if (mMaxNewX > 0)
						if (newX > mMaxNewX)
							newX = mMaxNewX;

					if (newY < 0)
						newY = 0;
					if (mMaxNewY > 0)
						if (newY > mMaxNewY)
							newY = mMaxNewY;

					view.setX(newX);
					view.setY(newY);
					//App.log( "Action Move " + newX + "," + newY);
					break;

				case MotionEvent.ACTION_UP:
					//App.log( "Action Up 1 " + event.getRawX() + "," + event.getRawY() + "  " + mDragX + "," + mDragY);
					float dx = Math.abs(mDownX - event.getRawX());
					float dy = Math.abs(mDownY - event.getRawY());
					long dragMils = System.currentTimeMillis() - mDownMils;
					//App.log( "Action Up 2 " + dragMils + "  " + dx + "," + dy);
					if ( (dx<22  && dy<22) && (dragMils < 222) ) {
						if (null != mListener)
							mListener.onPress(MyDragButton.this);
					}
					break;

				default:
					return false;
			}
			return true;
		}
	};



}
